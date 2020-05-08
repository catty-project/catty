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
import pink.catty.core.extension.spi.Codec;
import pink.catty.core.extension.spi.Codec.DataTypeEnum;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.endpoint.AbstractClient;
import pink.catty.core.invoker.frame.DefaultResponse;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.meta.ClientMeta;
import pink.catty.core.service.MethodModel;

public class NettyClient extends AbstractClient {

  private Channel clientChannel;
  private NioEventLoopGroup nioEventLoopGroup;

  public NettyClient(ClientMeta clientMeta, Codec codec) {
    super(clientMeta, codec);
    nioEventLoopGroup = new NioEventLoopGroup(Constants.THREAD_NUMBER + 1);
  }

  @Override
  protected void doOpen() {
    Bootstrap bootstrap = new Bootstrap();
    int connectTimeoutMillis = getMeta().getTimeout() > 0 ? getMeta().getTimeout()
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
          .connect(getMeta().getRemoteIp(), getMeta().getRemotePort())
          .sync();
      clientChannel = future.channel();
    } catch (InterruptedException i) {
      close();
      throw new EndpointInvalidException("NettyClient: connect().sync() interrupted", i);
    }
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
    if (!clientChannel.isActive()) {
      throw new EndpointInvalidException("ClientChannel closed");
    }
    MethodModel methodModel = invocation.getInvokedMethod();
    try {
      Response response = new DefaultResponse(request.getRequestId());

      /*
       * if the invoking method is not need return from provider, than should not listen this
       * response, or will cause OOM.
       */
      if (methodModel.isNeedReturn()) {
        addCurrentTask(request.getRequestId(), response);
      }
      byte[] msg = getCodec().encode(request, DataTypeEnum.REQUEST);
      ByteBuf byteBuf = clientChannel.alloc().heapBuffer();
      byteBuf.writeBytes(msg);
      clientChannel.writeAndFlush(byteBuf).addListener(future -> {
        if (!future.isSuccess()) {
          if (future.cause() != null) {
            logger.error("Client send request failed. ", future.cause());
          } else {
            logger.error("Client send request failed without cause. ");
          }
        }
      });
      return response;
    } catch (Exception e) {
      logger.error("ClientChannel invoke error", e);
      throw new EndpointInvalidException("ClientChannel invoke error", e);
    }
  }
}
