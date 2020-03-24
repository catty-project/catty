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
package pink.catty.spring;

import java.util.Arrays;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pink.catty.spring.bean.ClientConfigBean;
import pink.catty.spring.bean.ExporterBean;
import pink.catty.spring.bean.ProtocolConfigBean;
import pink.catty.spring.bean.ReferenceFactoryBean;
import pink.catty.spring.bean.ServerConfigBean;
import pink.catty.spring.bean.ServiceBean;

public class CattyBeanDefinitionParser implements BeanDefinitionParser {

  private static final String EMPTY = "";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String CLASS = "class";
  private static final String CODEC = "codec";
  private static final String LOAD_BALANCE = "load-balance";
  private static final String SERIALIZATION = "serialization";
  private static final String ENDPOINT = "endpoint";
  private static final String CLUSTER = "cluster";
  private static final String TIMEOUT = "timeout";
  private static final String ADDRESSES = "addresses";
  private static final String ADDRESS_SPLIT = ";";
  private static final String SERVER_PORT = "port";
  private static final String WORKER_NUM = "worker-num";
  private static final String PROTOCOL_REF = "protocol";
  private static final String CLIENT_CONFIG_REF = "client-config";
  private static final String SERVER_CONFIG_REF = "server-config";
  private static final String INTERFACE = "interface";
  private static final String IMPLEMENT_REF = "ref";

  private Class<?> beanClass;

  public CattyBeanDefinitionParser(Class<?> beanClass) {
    this.beanClass = beanClass;
  }

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    RootBeanDefinition bd = new RootBeanDefinition();
    bd.setLazyInit(false);
    bd.setBeanClass(beanClass);

    String id = parseIdAndRegister(element, parserContext, beanClass, bd);

    if (ProtocolConfigBean.class == beanClass) {
      String loadBalance = element.getAttribute(LOAD_BALANCE);
      String codec = element.getAttribute(CODEC);
      String serialization = element.getAttribute(SERIALIZATION);
      String endpoint = element.getAttribute(ENDPOINT);
      String cluster = element.getAttribute(CLUSTER);

      if (!isEmpty(loadBalance)) {
        bd.getPropertyValues().addPropertyValue("loadBalanceType", loadBalance);
      }
      if (!isEmpty(loadBalance)) {
        bd.getPropertyValues().addPropertyValue("codecType", codec);
      }
      if (!isEmpty(loadBalance)) {
        bd.getPropertyValues().addPropertyValue("serializationType", serialization);
      }
      if (!isEmpty(loadBalance)) {
        bd.getPropertyValues().addPropertyValue("endpointType", endpoint);
      }
      if (!isEmpty(loadBalance)) {
        bd.getPropertyValues().addPropertyValue("clusterType", cluster);
      }
    }

    if (ClientConfigBean.class == beanClass) {
      String timeout = element.getAttribute(TIMEOUT);
      String addresses = element.getAttribute(ADDRESSES);
      if (!isEmpty(timeout)) {
        bd.getPropertyValues().addPropertyValue("timeout", timeout);
      }
      assertNotEmpty(addresses, "xml client-config's addresses can't be empty" + id);
      String[] address = addresses.split(ADDRESS_SPLIT);
      bd.getPropertyValues().addPropertyValue("addresses", Arrays.asList(address));
    }

    if (ServerConfigBean.class == beanClass) {
      String port = element.getAttribute(SERVER_PORT);
      String workerNum = element.getAttribute(WORKER_NUM);
      assertNotEmpty(port, "xml server-config's port can't be empty" + id);
      bd.getPropertyValues().addPropertyValue("port", port);
      if (!isEmpty(workerNum)) {
        bd.getPropertyValues().addPropertyValue("workerThreadNum", workerNum);
      }
    }

    if (ReferenceFactoryBean.class == beanClass) {
      String interfaceName = element.getAttribute(INTERFACE);
      String protocolConfig = element.getAttribute(PROTOCOL_REF);
      String clientConfig = element.getAttribute(CLIENT_CONFIG_REF);
      assertNotEmpty(interfaceName, "xml reference's interfaceName can't be empty" + id);
      assertNotEmpty(protocolConfig, "xml reference's protocolConfig can't be empty" + id);
      assertNotEmpty(clientConfig, "xml reference's clientConfig can't be empty" + id);
      Class<?> interfaceClass;
      try {
        interfaceClass = Class.forName(interfaceName);
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException("Class not found: " + interfaceName, e);
      }
      bd.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
      bd.getPropertyValues()
          .addPropertyValue("protocolConfig", new RuntimeBeanReference(protocolConfig));
      bd.getPropertyValues()
          .addPropertyValue("clientConfig", new RuntimeBeanReference(clientConfig));
    }

    if (ExporterBean.class == beanClass) {
      String protocolConfig = element.getAttribute(PROTOCOL_REF);
      String serverConfig = element.getAttribute(SERVER_CONFIG_REF);
      assertNotEmpty(protocolConfig, "xml ExporterBean's protocolConfig can't be empty" + id);
      assertNotEmpty(serverConfig, "xml ExporterBean's serverConfig can't be empty" + id);
      bd.getPropertyValues()
          .addPropertyValue("protocolConfig", new RuntimeBeanReference(protocolConfig));
      bd.getPropertyValues()
          .addPropertyValue("serverConfig", new RuntimeBeanReference(serverConfig));
      parseServiceRef(bd, element.getChildNodes(), parserContext);
    }

    return bd;
  }

  private void parseServiceRef(RootBeanDefinition root, NodeList nodeList,
      ParserContext parserContext) {
    if (nodeList != null && nodeList.getLength() > 0) {
      ManagedList services = null;
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node instanceof Element && "service".equals(node.getLocalName())) {
          if (services == null) {
            services = new ManagedList();
          }
          Element element = (Element) node;
          RootBeanDefinition service = new RootBeanDefinition();
          service.setLazyInit(false);
          String serviceId = parseIdAndRegister(element, parserContext, beanClass, service);

          String interfaceName = element.getAttribute(INTERFACE);
          String ref = element.getAttribute(IMPLEMENT_REF);
          assertNotEmpty(interfaceName, "xml ServiceBean's interface can't be empty" + serviceId);
          assertNotEmpty(ref, "xml ServiceBean's ref can't be empty" + serviceId);
          Class<?> interfaceClass;
          try {
            interfaceClass = Class.forName(interfaceName);
          } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class not found: " + interfaceName, e);
          }
          service.setBeanClass(ServiceBean.class);
          service.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
          service.getPropertyValues().addPropertyValue("ref", new RuntimeBeanReference(ref));
          BeanDefinitionHolder serviceBeanDefinitionHolder = new BeanDefinitionHolder(service,
              serviceId);
          services.add(serviceBeanDefinitionHolder);
        }
      }
      if (services != null) {
        root.getPropertyValues().addPropertyValue("services", services);
      }
    }
  }

  private static void parseProperties(NodeList nodeList, RootBeanDefinition beanDefinition) {
    if (nodeList != null && nodeList.getLength() > 0) {
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        if (node instanceof Element) {
          if ("property".equals(node.getNodeName()) || "property".equals(node.getLocalName())) {
            String name = ((Element) node).getAttribute("name");
            if (name != null && name.length() > 0) {
              String value = ((Element) node).getAttribute("value");
              String ref = ((Element) node).getAttribute("ref");
              if (value != null && value.length() > 0) {
                beanDefinition.getPropertyValues().addPropertyValue(name, value);
              } else if (ref != null && ref.length() > 0) {
                beanDefinition.getPropertyValues()
                    .addPropertyValue(name, new RuntimeBeanReference(ref));
              } else {
                throw new UnsupportedOperationException("Unsupported <property name=\"" + name
                    + "\"> sub tag, Only supported <property name=\"" + name
                    + "\" ref=\"...\" /> or <property name=\""
                    + name + "\" value=\"...\" />");
              }
            }
          }
        }
      }
    }
  }

  private static String parseIdAndRegister(Element element, ParserContext parserContext,
      Class<?> beanClass, RootBeanDefinition bd) {
    String id = element.getAttribute(ID);
    if ((id == null || id.length() == 0)) {
      String generatedBeanName = element.getAttribute(NAME);
      if (generatedBeanName == null || generatedBeanName.length() == 0) {
        generatedBeanName = element.getAttribute(CLASS);
      }
      if (generatedBeanName == null || generatedBeanName.length() == 0) {
        generatedBeanName = beanClass.getName();
      }
      id = generatedBeanName;
      int counter = 2;
      while (parserContext.getRegistry().containsBeanDefinition(id)) {
        id = generatedBeanName + (counter++);
      }
    }
    if (id != null && id.length() > 0) {
      if (parserContext.getRegistry().containsBeanDefinition(id)) {
        throw new IllegalStateException("Duplicate spring bean id " + id);
      }
      parserContext.getRegistry().registerBeanDefinition(id, bd);
    }
    return id;
  }

  private static boolean isEmpty(String str) {
    if (str == null || EMPTY.equals(str)) {
      return true;
    }
    return false;
  }

  private static void assertNotEmpty(String str, String msg) {
    if (isEmpty(str)) {
      throw new IllegalStateException(msg);
    }
  }
}
