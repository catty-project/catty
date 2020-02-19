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
package pink.catty.benchmark.service;

import pink.catty.benchmark.generated.BenchmarkProtocol;
import pink.catty.core.utils.MD5Utils;
import pink.catty.benchmark.generated.BenchmarkProtocol.Request;
import pink.catty.benchmark.generated.BenchmarkProtocol.Response;

public class ProtobufServiceImpl implements ProtobufService {

  @Override
  public Response service(
      Request request) {
    return BenchmarkProtocol.Response.newBuilder()
        .setValue(MD5Utils.md5(request.getValue()))
        .build();
  }
}
