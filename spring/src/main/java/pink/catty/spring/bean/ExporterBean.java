/*
 * Copyright 2020 The Catty Project
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
package pink.catty.spring.bean;

import java.util.List;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import pink.catty.config.Exporter;

public class ExporterBean extends Exporter implements InitializingBean, DisposableBean {

  private List<ServiceBean> services;

  public void setServices(List<ServiceBean> services) {
    this.services = services;
  }

  @Override
  public void destroy() throws Exception {
    unexport();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    for(ServiceBean serviceBean : services) {
      registerService(serviceBean.getInterfaceClass(), serviceBean.getRef());
    }
    export();
  }
}
