package transport.netty;

import common.CodecConstants;
import exception.TransportException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 用于处理半包粘包问题。
 *
 * @author zrj CreateDate: 2019/9/4
 */
public class NettyDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    // 数据比协议头小，直接返回
    if (in.readableBytes() <= CodecConstants.HEADER_SIZE) {
      return;
    }

    // 标记初始位置
    in.markReaderIndex();
    short magic = in.readShort();
    if(magic != CodecConstants.MAGIC_HEAD) {
      in.resetReaderIndex();
      throw new TransportException("NettyDecoder: magic number error: " + magic);
    }

    in.skipBytes(2);
    int contentLength = in.readInt();
    if(in.readableBytes() < contentLength + 8/* requestId 8 byte */) {
      in.resetReaderIndex();
      return;
    }

    byte[] data = new byte[in.readableBytes()];
    in.readBytes(data);
    out.add(data);
  }
}
