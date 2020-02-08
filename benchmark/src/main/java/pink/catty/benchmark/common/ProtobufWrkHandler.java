package pink.catty.benchmark.common;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.INTERNAL_SERVER_ERROR;

import pink.catty.benchmark.generated.BenchmarkProtocol;
import pink.catty.benchmark.service.ProtobufService;
import pink.catty.benchmark.utils.BenchmarkConstans;
import pink.catty.core.utils.MD5Utils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.benchmark.generated.BenchmarkProtocol.Request;

@ChannelHandler.Sharable
public class ProtobufWrkHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufWrkGateway.class);
  private ProtobufService service;

  public ProtobufWrkHandler(ProtobufService service) {
    this.service = service;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
    long start = System.currentTimeMillis();
    String content = BenchmarkConstans._1000_BYTES + UUID.randomUUID();

    // put
    Request request = BenchmarkProtocol.Request.newBuilder()
        .setValue(content)
        .build();

    // get
    try {
      BenchmarkProtocol.Response response = service.service(request);
      if (MD5Utils.md5(content).equals(response.getValue())) {
        okResponse(ctx);
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Request result:success cost:{} ms", System.currentTimeMillis() - start);
        }
      } else {
        badReponse(ctx);
        if (LOGGER.isInfoEnabled()) {
          LOGGER
              .info("Request result:failure cost:{} ms", System.currentTimeMillis() - start);
        }
      }
    } catch (Throwable t) {
      badReponse(ctx);
      if (LOGGER.isInfoEnabled()) {
        LOGGER
            .info("Request result:failure cost:{} ms", System.currentTimeMillis() - start, t);
      }
    }
  }

  private void okResponse(ChannelHandlerContext ctx) {
    FullHttpResponse ok =
        new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled
            .copiedBuffer("OK\n", CharsetUtil.UTF_8));
    ok.headers().add(HttpHeaderNames.CONTENT_LENGTH, 3);
    ctx.writeAndFlush(ok);
  }

  private void badReponse(ChannelHandlerContext ctx) {
    FullHttpResponse error =
        new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
    error.headers().add(HttpHeaderNames.CONTENT_LENGTH, 0);
    ctx.writeAndFlush(error);
  }

}
