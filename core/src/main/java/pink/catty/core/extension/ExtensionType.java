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
package pink.catty.core.extension;

public final class ExtensionType {

  public interface SerializationType {
    String PROTOBUF = "protobuf";
    String FASTJSON = "fastjson";
    String PROTOBUF_FASTJSON = "protobuf_fastjson";
    String HESSIAN2 = "hessian2";
  }

  public interface LoadBalanceType {
    String RANDOM = "random";
    String WEIGHTED_RANDOM = "weighted_random";
  }

  public interface CodecType {
    String CATTY = "catty";
  }

  public interface ProtocolType {
    String CATTY = "catty";
  }

  public interface RegistryType {
    String ZOOKEEPER = "zookeeper";
  }

  public interface EndpointFactoryType {
    String NETTY = "netty";
  }

  public interface ClusterType {
    String FAIL_FAST = "fail-fast";
    String FAIL_OVER = "fail-over";
    String FAIL_SAFE = "fail-safe";
    String FAIL_BACK = "fail-back";
  }

}
