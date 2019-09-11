package com.nowcoder.netty;

import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;
import com.nowcoder.api.transport.AbstractServer;
import com.nowcoder.api.transport.Handler;
import com.nowcoder.codec.Codec;
import com.nowcoder.core.URL;
import com.nowcoder.exception.SusuException;
import com.nowcoder.netty.netty_codec.NettyDecoder;
import com.nowcoder.netty.netty_codec.NettyEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class NettyServer extends AbstractServer {

  private io.netty.channel.Channel serverChannel;
  private NioEventLoopGroup bossGroup;
  private NioEventLoopGroup workerGroup;


  private Handler handler;

  public NettyServer(Codec codec, URL url,
      Handler handler) {
    super(codec, url);
    this.handler = handler;
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
  }

  public void open() {
    ServerBootstrap serverBootstrap = new ServerBootstrap();
    serverBootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("decoder", new NettyDecoder());
            pipeline.addLast("encoder", new NettyEncoder());
            pipeline.addLast("handler", new ServerChannelHandler(handler));
          }
        });
    serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    try {
      ChannelFuture f = serverBootstrap.bind(getUrl().getPort()).sync();
      serverChannel = f.channel();
    } catch (Exception e) {
      // todo: log
    }
  }

  @Override
  public void close() {
    if (serverChannel != null) {
      serverChannel.close();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
      bossGroup = null;
    }
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
      workerGroup = null;
    }
    super.close();
  }

  private class ServerChannelHandler extends ChannelDuplexHandler {

    private Handler handler;

    public ServerChannelHandler(Handler handler) {
      this.handler = handler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      Object object = decode((byte[]) msg);
      if (!(object instanceof Request) && !(object instanceof Response)) {
        throw new SusuException("NettyChannelHandler: unsupported message type when encode: " + object.getClass());
      }
        processRequest(ctx, (Request) object);
    }

    private void processRequest(ChannelHandlerContext ctx, Request msg) {
      Object result = handler.handle(msg);
      Response response = new Response();
      response.setRequestId(msg.getRequestId());

      if(result instanceof Exception) {
        response.setException((Exception) result);
      } else {
        response.setReturnValue(result);
      }
      sendResponse(ctx, response);
    }

    private ChannelFuture sendResponse(ChannelHandlerContext ctx, Response response) {
      byte[] msg = encode(response);
      if (ctx.channel().isActive()) {
        return ctx.channel().writeAndFlush(msg);
      }

      return null;
    }
  }
}
