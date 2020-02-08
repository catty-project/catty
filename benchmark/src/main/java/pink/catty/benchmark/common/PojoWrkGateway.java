package pink.catty.benchmark.common;

import pink.catty.benchmark.service.PojoService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PojoWrkGateway {

  private Logger logger = LoggerFactory.getLogger(PojoWrkGateway.class);
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  public void start(PojoService service) {
    ServerBootstrap bootstrap = new ServerBootstrap();
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
    ChannelHandler handler = new PojoWrkHandler(service);

    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
            new ChannelInitializer<Channel>() {
              @Override
              protected void initChannel(Channel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpServerCodec());
                pipeline.addLast(new HttpObjectAggregator(0));
                pipeline.addLast(handler);
              }
            })
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true);

    String host = "0.0.0.0";
    int port = 8088;
    try {
      ChannelFuture f = bootstrap.bind(host, port).sync();
      logger.info("Gateway started, host is {}, port is {}.", host, port);
      f.channel().closeFuture().sync();
      logger.info("Gateway proxy closed, host is {} , port is {}.", host, port);
    } catch (InterruptedException e) {
      logger.error("Gateway proxy start failed", e);
    } finally {
      destroy();
    }
  }

  public void destroy() {
    if (workerGroup != null) {
      workerGroup.shutdownGracefully();
    }
    if (bossGroup != null) {
      bossGroup.shutdownGracefully();
    }
  }
}
