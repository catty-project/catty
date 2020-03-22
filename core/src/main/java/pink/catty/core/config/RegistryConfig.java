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
package pink.catty.core.config;

import pink.catty.core.extension.ExtensionType.RegistryType;

public class RegistryConfig {

  private String address;

  private String registryType = RegistryType.ZOOKEEPER;

  public String getRegistryType() {
    return registryType;
  }

  /**
   * {@link RegistryType}
   */
  public void setRegistryType(String registryType) {
    this.registryType = registryType;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }
}
