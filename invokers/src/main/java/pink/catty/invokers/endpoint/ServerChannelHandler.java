package pink.catty.invokers.endpoint;

import pink.catty.core.CattyException;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.Invocation.InvokerLinkTypeEnum;
import pink.catty.core.invoker.Request;
import pink.catty.core.invoker.Response;
import pink.catty.core.extension.spi.Codec.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public class ServerChannelHandler extends ChannelDuplexHandler {

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
    Response response = nettyServer.invoke(request, new Invocation(InvokerLinkTypeEnum.PROVIDER));
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
    try {
      byte[] msg = nettyServer.getCodec().encode(response, DataTypeEnum.RESPONSE);
      ByteBuf byteBuf = ctx.channel().alloc().heapBuffer();
      byteBuf.writeBytes(msg);
      if (ctx.channel().isActive()) {
        return ctx.channel().writeAndFlush(byteBuf).sync();
      }
    } catch (Exception e) {
      // fixme: fix this
      e.printStackTrace();
    }

    return null;
  }

}
