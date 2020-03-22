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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CattyBeanDefinitionParser implements BeanDefinitionParser {

  private static final String EMPTY = "";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String CLASS = "class";


  private Class<?> beanClass;

  public CattyBeanDefinitionParser(Class<?> beanClass) {
    this.beanClass = beanClass;
  }

  @Override
  public BeanDefinition parse(Element element, ParserContext parserContext) {
    RootBeanDefinition bd = new RootBeanDefinition();
    bd.setLazyInit(false);
    bd.setBeanClass(beanClass);

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
    bd.getPropertyValues().addPropertyValue("id", id);


    return bd;
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

  private boolean isEmpty(String str) {
    if (str == null || EMPTY.equals(str)) {
      return true;
    }
    return false;
  }
}
