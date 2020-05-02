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
package pink.catty.core.invoker;

import pink.catty.core.meta.EndpointMeta;

public abstract class AbstractLinkedInvoker implements Invoker, LinkedInvoker {

  protected Invoker next;

  public AbstractLinkedInvoker() {
  }

  public AbstractLinkedInvoker(Invoker next) {
    this.next = next;
  }

  @Override
  public EndpointMeta getMeta() {
    return next.getMeta();
  }

  @Override
  public void setNext(Invoker next) {
    this.next = next;
  }

  @Override
  public Invoker getNext() {
    return next;
  }
}
