/*
 * Copyright 2019 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.invokers.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import pink.catty.core.Constants;
import pink.catty.core.EndpointInvalidException;
import pink.catty.core.config.InnerClientConfig;
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.Codec.DataTypeEnum;
import pink.catty.core.invoker.AbstractClient;
import pink.catty.core.invoker.DefaultResponse;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;

public class NettyClient extends AbstractClient {

  private Channel clientChannel;
  private NioEventLoopGroup nioEventLoopGroup;

  public NettyClient(InnerClientConfig clientConfig, Codec codec) {
    super(clientConfig, codec);
    nioEventLoopGroup = new NioEventLoopGroup(Constants.THREAD_NUMBER + 1);
  }

  @Override
  protected void doOpen() {
    Bootstrap bootstrap = new Bootstrap();
    int connectTimeoutMillis = getConfig().getTimeout() > 0 ? getConfig().getTimeout()
        : Constants.DEFAULT_CLIENT_TIMEOUT;
    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    bootstrap.group(nioEventLoopGroup)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new NettyDecoder(getCodec()));
            pipeline.addLast("handler", new ClientChannelHandler(NettyClient.this));
          }
        });
    ChannelFuture future;
    try {
      future = bootstrap
          .connect(getConfig().getServerIp(), getConfig().getServerPort())
          .sync();
    } catch (InterruptedException i) {
      destroy();
      throw new EndpointInvalidException("NettyClient: connect().sync() interrupted", i);
    }

    clientChannel = future.channel();
  }

  @Override
  protected void doClose() {
    if (clientChannel != null) {
      clientChannel.close();
    }
    if (nioEventLoopGroup != null) {
      nioEventLoopGroup.shutdownGracefully();
    }
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    try {
      tryInit();
      Response response = new DefaultResponse(request.getRequestId());
      addCurrentTask(request.getRequestId(), response);
      byte[] msg = getCodec().encode(request, DataTypeEnum.REQUEST);
      ByteBuf byteBuf = clientChannel.alloc().heapBuffer();
      byteBuf.writeBytes(msg);
      if (clientChannel.isActive()) {
        clientChannel.writeAndFlush(byteBuf).syncUninterruptibly();
      } else {
        throw new EndpointInvalidException("ClientChannel closed");
      }
      return response;
    } catch (EndpointInvalidException e) {
      // todo: need invoke destroy() method
      status = DISCONNECTED;
      throw e;
    } catch (Exception e) {
      // todo: need invoke destroy() method
      status = DISCONNECTED;
      throw new EndpointInvalidException("ClientChannel invalid", e);
    }
  }

  private void tryInit() {
    if (!isAvailable()) {
      synchronized (this) {
        if (!isAvailable()) {
          init();
        }
      }
    }
  }
}
