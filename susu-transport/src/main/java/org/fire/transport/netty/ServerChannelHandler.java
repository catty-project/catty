package org.fire.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import org.fire.core.codec.Codec.DataTypeEnum;
import org.fire.core.exception.SusuException;
import org.fire.transport.api.Handler;
import org.fire.transport.api.Request;
import org.fire.transport.api.Response;

public class ServerChannelHandler extends ChannelDuplexHandler {

  private Handler handler;
  private NettyServer nettyServer;

  public ServerChannelHandler(NettyServer nettyServer) {
    this.nettyServer = nettyServer;
    this.handler = nettyServer.getRoutableHandler();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf byteBuf = (ByteBuf) msg;
    byte[] data = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(data);
    byteBuf.release();
    Object object = nettyServer.getCodec().decode(data, DataTypeEnum.REQUEST);
    if (!(object instanceof Request)) {
      throw new SusuException(
          "ServerChannelHandler: unsupported message type when decode: " + object.getClass());
    }
    if (nettyServer.getExecutor() != null) {
      if (nettyServer.getConfig().isNeedOrder()) {
        nettyServer.getExecutor().submit(hashCode(), () -> processRequest(ctx, (Request) object));
      } else {
        nettyServer.getExecutor().submit(() -> processRequest(ctx, (Request) object));
      }
    } else {
      processRequest(ctx, (Request) object);
    }
  }

  private void processRequest(ChannelHandlerContext ctx, Request request) {
    Response response = (Response) handler.handle(request);
    response.setRequestId(request.getRequestId());
    Object value = response.getValue();
    if (value == null || value instanceof Void) {
      return;
    }
    if (value instanceof CompletableFuture) {
      CompletableFuture future = (CompletableFuture) value;
      future.whenComplete((r, t) -> {
        if (t != null) {
          response.setThrowable((Throwable) t);
        } else {
          response.setValue(r);
        }
        sendResponse(ctx, response);
      });
    } else {
      sendResponse(ctx, response);
    }
  }

  private ChannelFuture sendResponse(ChannelHandlerContext ctx, Response response) {
    try {
      byte[] msg = nettyServer.getCodec().encode(response);
      ByteBuf byteBuf = ctx.channel().alloc().heapBuffer();
      byteBuf.writeBytes(msg);
      if (ctx.channel().isActive()) {
        return ctx.channel().writeAndFlush(byteBuf).sync();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

}
