package pink.catty.invokers.endpoint;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import pink.catty.core.CattyException;
import pink.catty.core.extension.spi.Codec.DataTypeEnum;
import pink.catty.core.invoker.Response;

public class ClientChannelHandler extends ChannelDuplexHandler {

  private NettyClient nettyClient;

  public ClientChannelHandler(NettyClient nettyClient) {
    this.nettyClient = nettyClient;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    byte[] data = (byte[]) msg;
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
