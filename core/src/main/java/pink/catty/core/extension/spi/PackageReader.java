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

import java.util.List;

/**
 * Distinguish integral data package from net transport's byte-streaming.
 *
 * @param <I> Different framework might have different encapsulation of byte-streaming such as
 * ByteBuffer from jdk, ByteBuf from Netty and byte[].
 */
public interface PackageReader<I, O> {

  /**
   * Reading data package received from tcp or other stream completely.
   *
   * @param data data received from tcp.
   * @param out integral data
   * @return return the rest of data.
   * @throws BrokenDataPackageException If reading data package occurs some error.
   */
  void readPackage(I data, List<O> out) throws BrokenDataPackageException;

}
