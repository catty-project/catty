package io.catty.transport.netty;

import io.catty.core.CattyException;
import io.catty.core.DefaultResponse;
import io.catty.core.GlobalConstants;
import io.catty.core.Invocation;
import io.catty.core.Request;
import io.catty.core.Response;
import io.catty.core.TransportException;
import io.catty.core.config.InnerClientConfig;
import io.catty.core.extension.spi.Codec;
import io.catty.core.extension.spi.Codec.DataTypeEnum;
import io.catty.transport.AbstractClient;
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
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class NettyClient extends AbstractClient {

  private Channel clientChannel;
  private NioEventLoopGroup nioEventLoopGroup;

  public NettyClient(InnerClientConfig clientConfig, Codec codec) {
    super(clientConfig, codec);
    nioEventLoopGroup = new NioEventLoopGroup(GlobalConstants.THREAD_NUMBER + 1);
  }

  @Override
  protected void doOpen() {
    Bootstrap bootstrap = new Bootstrap();
    int connectTimeoutMillis = getConfig().getTimeout() > 0 ? getConfig().getTimeout()
        : GlobalConstants.DEFAULT_CLIENT_TIMEOUT;
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
            pipeline.addLast("decoder", new ProtobufVarint32FrameDecoder());
            pipeline.addLast("encoder", new ProtobufVarint32LengthFieldPrepender());
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
      throw new TransportException("NettyClient: connect().sync() interrupted", i);
    }

    clientChannel = future.channel();
  }

  @Override
  protected void doClose() {
    if (!isAvailable()) {
      return;
    }
    if (clientChannel != null) {
      clientChannel.close();
    }
    if (nioEventLoopGroup != null) {
      nioEventLoopGroup.shutdownGracefully();
    }
  }

  @Override
  public Response invoke(Request request, Invocation invocation) {
    // fixme: need thread-safe.
    if (!isAvailable()) {
      init();
    }
    Response response = new DefaultResponse(request.getRequestId());
    addCurrentTask(request.getRequestId(), response);
    try {
      byte[] msg = getCodec().encode(request, DataTypeEnum.REQUEST);
      ByteBuf byteBuf = clientChannel.alloc().heapBuffer();
      byteBuf.writeBytes(msg);
      if (clientChannel.isActive()) {
        clientChannel.writeAndFlush(byteBuf).sync();
      } else {
        throw new CattyException("ClientChannel closed");
      }
      return response;
    } catch (Exception e) {
      response.setValue(e);
      return response;
    }
  }
}
