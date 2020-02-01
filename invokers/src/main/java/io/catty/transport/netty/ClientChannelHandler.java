package io.catty.transport.netty;

import io.catty.core.CattyException;
import io.catty.core.Response;
import io.catty.core.extension.spi.Codec.DataTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ClientChannelHandler extends ChannelDuplexHandler {

  private NettyClient nettyClient;

  public ClientChannelHandler(NettyClient nettyClient) {
    this.nettyClient = nettyClient;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf byteBuf = (ByteBuf) msg;
    byte[] data = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(data);
    byteBuf.release();
    Object object = nettyClient.getCodec().decode(data, DataTypeEnum.RESPONSE);
    if (!(object instanceof Response)) {
      throw new CattyException(
          "NettyChannelHandler: unsupported message type when encode: " + object
              .getClass());
    }
    processResponse((Response) object);
  }

  private void processResponse(Response response) {
    Response future = nettyClient.getResponseFuture(response.getRequestId());
    future.setValue(response.getValue());
  }

}
