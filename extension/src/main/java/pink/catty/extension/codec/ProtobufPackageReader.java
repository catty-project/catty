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
package pink.catty.extension.codec;

import io.netty.buffer.ByteBuf;
import java.util.List;
import pink.catty.core.extension.spi.BrokenDataPackageException;
import pink.catty.core.extension.spi.PackageReader;

public class ProtobufPackageReader implements PackageReader<ByteBuf, byte[]> {

  @Override
  public void readPackage(ByteBuf in, List<byte[]> out) throws BrokenDataPackageException {
    in.markReaderIndex();
    int preIndex = in.readerIndex();
    int length = readRawVarint32(in);
    if (preIndex == in.readerIndex()) {
      return;
    }
    if (length < 0) {
      throw new BrokenDataPackageException("negative length: " + length);
    }
    if (in.readableBytes() < length) {
      in.resetReaderIndex();
    } else {
      byte[] data = new byte[length];
      ByteBuf byteBuf = in.readRetainedSlice(length);
      byteBuf.readBytes(data);
      byteBuf.release();
      out.add(data);
    }
  }

  private static int readRawVarint32(ByteBuf buffer) throws BrokenDataPackageException {
    if (!buffer.isReadable()) {
      return 0;
    }
    buffer.markReaderIndex();
    byte tmp = buffer.readByte();
    if (tmp >= 0) {
      return tmp;
    } else {
      int result = tmp & 127;
      if (!buffer.isReadable()) {
        buffer.resetReaderIndex();
        return 0;
      }
      if ((tmp = buffer.readByte()) >= 0) {
        result |= tmp << 7;
      } else {
        result |= (tmp & 127) << 7;
        if (!buffer.isReadable()) {
          buffer.resetReaderIndex();
          return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
          result |= tmp << 14;
        } else {
          result |= (tmp & 127) << 14;
          if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
          }
          if ((tmp = buffer.readByte()) >= 0) {
            result |= tmp << 21;
          } else {
            result |= (tmp & 127) << 21;
            if (!buffer.isReadable()) {
              buffer.resetReaderIndex();
              return 0;
            }
            result |= (tmp = buffer.readByte()) << 28;
            if (tmp < 0) {
              throw new BrokenDataPackageException("malformed varint.");
            }
          }
        }
      }
      return result;
    }
  }
}
