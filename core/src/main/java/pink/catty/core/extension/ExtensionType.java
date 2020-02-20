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
    String PROTOBUF = "PROTOBUF";
    String FASTJSON = "FASTJSON";
    String PROTOBUF_FASTJSON = "PROTOBUF_FASTJSON";
    String HESSIAN2 = "HESSIAN2";
  }

  public interface LoadBalanceType {
    String RANDOM = "RANDOM";
    String WEIGHTED_RANDOM = "WEIGHTED_RANDOM";
  }

  public interface CodecType {
    String CATTY = "CATTY";
  }

  public interface InvokerBuilderType {
    String DIRECT = "DIRECT";
  }

  public interface RegistryType {
    String ZOOKEEPER = "ZOOKEEPER";
  }

  public interface EndpointFactoryType {
    String NETTY = "NETTY";
  }

}
