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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import pink.catty.config.Reference;

public class ReferenceFactoryBean<T> extends Reference<T> implements FactoryBean<T>, BeanFactoryAware,
    InitializingBean, DisposableBean {



  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

  }

  @Override
  public void destroy() throws Exception {

  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }

  @Override
  public T getObject() throws Exception {
    return null;
  }

  @Override
  public Class<?> getObjectType() {
    return null;
  }
}
