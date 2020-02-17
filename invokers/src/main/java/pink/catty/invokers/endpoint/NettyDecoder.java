package pink.catty.invokers.endpoint;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import pink.catty.core.extension.spi.BrokenDataPackageException;
import pink.catty.core.extension.spi.CompletePackage;
import pink.catty.core.extension.spi.PackageReader;
import pink.catty.core.extension.spi.PartialDataException;

public class NettyDecoder extends ByteToMessageDecoder {

  private PackageReader packageReader;
  private byte[] buffer;

  public NettyDecoder(PackageReader packageReader) {
    this.packageReader = packageReader;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    byte[] data;
    if (buffer == null) {
      data = new byte[in.readableBytes()];
      in.readBytes(data);
    } else {
      data = new byte[in.readableBytes() + buffer.length];
      System.arraycopy(buffer, 0, data, 0, buffer.length);
      in.readBytes(data, buffer.length, in.readableBytes());
    }
    buffer = data;
    CompletePackage completePackage;
    while (buffer != null && buffer.length > 0) {
      try {
        completePackage = packageReader.readPackage(buffer);
        out.add(completePackage.getCompletePackage());
        buffer = completePackage.getRestData();
      } catch (BrokenDataPackageException e) {
        throw e;
      } catch (PartialDataException e) {
        break;
      }
    }
  }
}
