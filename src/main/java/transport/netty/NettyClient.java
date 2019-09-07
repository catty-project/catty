package transport.netty;

import codec.Codec;
import codec.susu.SusuCodec;
import exception.TransportException;
import rpc.Request;
import rpc.Response;
import core.URL;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rpc.DefaultResponseFuture;
import rpc.ResponseFuture;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class NettyClient {

  private io.netty.channel.Channel clientChannel;

  private URL url;

  private Codec codec = new SusuCodec();

  private Map<Long, ResponseFuture> currentTask = new ConcurrentHashMap<>();

  public Response invoke(Request request) {

    byte[] msg = codec.encode(request);
    ResponseFuture response = new DefaultResponseFuture();
    currentTask.put(request.getRequestId(), response);
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

  public void open() {

    Bootstrap bootstrap = new Bootstrap();
    NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
    bootstrap.option(ChannelOption.TCP_NODELAY, true);
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.group(nioEventLoopGroup)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
                   @Override
                   protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline pipeline = ch.pipeline();
                     pipeline.addLast("decoder", new NettyDecoder(codec));
                     pipeline.addLast("encoder", new NettyEncoder());
                     pipeline.addLast("handler", new NettyChannelHandler(
                         message -> {
                           Response response = (Response) message;
                           ResponseFuture future = currentTask.remove(response.getRequestId());
                           future.onSuccess(response);
                           return null;
                         }
                     ));
                   }
                 }
        );

    new Thread(() -> {
      try {
        ChannelFuture future = bootstrap.connect("127.0.0.1", 20880).sync();
        clientChannel = future.channel();
        clientChannel.closeFuture().sync();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();


  }

}
