package com.nowcoder.netty;

import com.nowcoder.api.remote.DefaultResponseFuture;
import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.api.remote.ResponseFuture;
import com.nowcoder.api.transport.AbstractClient;
import com.nowcoder.api.transport.Handler;
import com.nowcoder.codec.Codec;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.exception.TransportException;
import com.nowcoder.netty.netty_codec.NettyDecoder;
import com.nowcoder.netty.netty_codec.NettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class NettyClient extends AbstractClient {

  private io.netty.channel.Channel clientChannel;
  private NioEventLoopGroup nioEventLoopGroup;


  private ExecutorService executor;

  public NettyClient(Codec codec, URL serverUrl) {
    super(codec, serverUrl);
    executor = Executors.newSingleThreadExecutor();
    nioEventLoopGroup = new NioEventLoopGroup();
  }

  @Override
  public Response invoke(Request request) {

    byte[] msg = encode(request);
    ResponseFuture response = new DefaultResponseFuture();
    addCurrentTask(request.getRequestId(), response);
    clientChannel.writeAndFlush(msg);
    try {
      return (Response) response.getValue();
    } catch (Exception e) {
      Response response1 = new Response();
      response1.setRequestId(request.getRequestId());
      response1.setException(new TransportException("NettyClient: response.getValue interrupted!"));
      return response1;
    }
  }

  @Override
  public void open() {

    Bootstrap bootstrap = new Bootstrap();

    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.group(nioEventLoopGroup)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
                   @Override
                   protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline pipeline = ch.pipeline();
                     pipeline.addLast("decoder", new NettyDecoder());
                     pipeline.addLast("encoder", new NettyEncoder());
                     pipeline.addLast("handler", new ClientChannelHandler(
                         message -> {
                           Response response = (Response) message;
                           ResponseFuture future = getResponseFuture(response.getRequestId());
                           future.onSuccess(response);
                           return null;
                         }
                     ));
                   }
                 }
        );

    ChannelFuture future;
    try {
      future = bootstrap.connect(getUrl().getHost(), getUrl().getPort()).sync();
    } catch (InterruptedException i) {
      close();
      // todo : log or retry
      throw new TransportException("NettyClient: connect().sync() interrupted", i);
    }

    // 标志当前的Channel已经打开
    super.open();
    // 保存当前的netty channel。
    clientChannel = future.channel();

    // 新起一个线程去监听close事件
    executor.submit(() -> {
      try {
        clientChannel.closeFuture().sync();
      } catch (Exception e) {
        // todo : log
      } finally {
        close();
      }
    });
  }

  /**
   * 先关掉clientChannel，再关掉executor
   */
  @Override
  public void close() {
    if(isClosed()) {
      return;
    }
    if (clientChannel != null) {
      clientChannel.close();
    }
    if(nioEventLoopGroup != null) {
      nioEventLoopGroup.shutdownGracefully();
    }
    executor.shutdown();
    super.close();
  }

  private class ClientChannelHandler extends ChannelDuplexHandler {

    private Handler handler;

    public ClientChannelHandler(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      Object object = decode((byte[]) msg);
      if (!(object instanceof Request) && !(object instanceof Response)) {
        throw new SusuException("NettyChannelHandler: unsupported message type when encode: " + object.getClass());
      }
        processResponse(ctx, (Response) object);
    }

    private void processResponse(ChannelHandlerContext ctx, Response msg) {
      handler.handle(msg);
    }
  }
}
