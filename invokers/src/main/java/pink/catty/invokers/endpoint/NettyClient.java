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
import pink.catty.core.CattyException;
import pink.catty.core.GlobalConstants;
import pink.catty.core.TransportException;
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
    tryInit();
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
