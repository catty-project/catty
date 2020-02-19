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
package pink.catty.core.extension.spi;

public class ProtobufPackageReader implements PackageReader {

  @Override
  public CompletePackage readPackage(byte[] dataPackage)
      throws PartialDataException, BrokenDataPackageException {
    if (dataPackage == null || dataPackage.length == 0) {
      throw new BrokenDataPackageException("Data package is empty.");
    }
    int length = 0;

    int pos = 0;
    int bufferLen = dataPackage.length;
    byte tmp = dataPackage[pos++];
    if (tmp >= 0) {
      length = tmp;
    } else {
      if (pos < bufferLen) {
        int result = tmp & 127;
        if ((tmp = dataPackage[pos++]) >= 0) {
          result |= tmp << 7;
          length = result;
        } else {
          if (pos < bufferLen) {
            result |= (tmp & 127) << 7;
            if ((tmp = dataPackage[pos++]) >= 0) {
              result |= tmp << 14;
              length = result;
            } else {
              if (pos < bufferLen) {
                result |= (tmp & 127) << 14;
                if ((tmp = dataPackage[pos++]) >= 0) {
                  result |= tmp << 21;
                  length = result;
                } else {
                  if (pos < bufferLen) {
                    result |= (tmp & 127) << 21;
                    result |= (tmp = dataPackage[pos++]) << 28;
                    if (tmp < 0) {
                      throw new BrokenDataPackageException("malformed varint.");
                    }
                    length = result;
                  }
                }
              }
            }
          }
        }
      }
    }

    if (length < 0) {
      throw new BrokenDataPackageException("Negative length: " + length);
    }
    if (dataPackage.length < length) {
      throw new PartialDataException();
    } else {
      byte[] completePackage = new byte[length];
      byte[] rest = new byte[dataPackage.length - length - pos];
      System.arraycopy(dataPackage, pos, completePackage, 0, completePackage.length);
      System.arraycopy(dataPackage, length + pos, rest, 0, rest.length);
      return new CompletePackage(completePackage, rest);
    }
  }
}
