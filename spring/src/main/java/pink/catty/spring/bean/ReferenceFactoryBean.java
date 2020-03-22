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
