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
package pink.catty.benchmark.common;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.INTERNAL_SERVER_ERROR;

import pink.catty.benchmark.service.PojoService;
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

@ChannelHandler.Sharable
public class PojoWrkHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufWrkGateway.class);
  private PojoService service;

  public PojoWrkHandler(PojoService service) {
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

    // get
    try {
      String response = service.service(content);
      if (MD5Utils.md5(content).equals(response)) {
        okResponse(ctx);
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
