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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pink.catty.core.CattyException;
import pink.catty.core.extension.spi.Codec.DataTypeEnum;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.endpoint.Void;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;
import pink.catty.core.support.worker.HashableExecutor;

public class ServerChannelHandler extends ChannelDuplexHandler {

  private static Logger logger = LoggerFactory.getLogger(ServerChannelHandler.class);

  private NettyServer nettyServer;

  public ServerChannelHandler(NettyServer nettyServer) {
    this.nettyServer = nettyServer;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    byte[] data = (byte[]) msg;
    Object object = nettyServer.getCodec().decode(data, DataTypeEnum.REQUEST);
    if (!(object instanceof Request)) {
      throw new CattyException(
          "ServerChannelHandler: unsupported message type when decode: " + object.getClass());
    }
    if (nettyServer.getExecutor() != null) {
      if (nettyServer.getMeta().isNeedOrder()) {
        ((HashableExecutor) nettyServer.getExecutor())
            .submit(hashCode(), () -> processRequest(ctx, (Request) object));
      } else {
        nettyServer.getExecutor().submit(() -> processRequest(ctx, (Request) object));
      }
    } else {
      processRequest(ctx, (Request) object);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error("Uncaught exception.", cause);
  }

  private void processRequest(ChannelHandlerContext ctx, Request request) {
    Response response = nettyServer.invoke(request, new Invocation());
    response.whenComplete((value, throwable) -> {
      if (value == null || value instanceof Void) {
        return;
      }
      if (throwable == null) {
        response.setValue(value);
      } else {
        // todo: this should never happen.
      }
      sendResponse(ctx, response);
    });
  }

  private ChannelFuture sendResponse(ChannelHandlerContext ctx, Response response) {
    byte[] msg = nettyServer.getCodec().encode(response, DataTypeEnum.RESPONSE);
    ByteBuf byteBuf = ctx.channel().alloc().heapBuffer();
    byteBuf.writeBytes(msg);
    if (ctx.channel().isActive()) {
      return ctx.channel().writeAndFlush(byteBuf);
    } else {
      return null;
    }
  }

}
