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
package pink.catty.extension.cluster;

import pink.catty.core.extension.Extension;
import pink.catty.core.extension.ExtensionType.ClusterType;
import pink.catty.core.extension.spi.AbstractCluster;
import pink.catty.core.invoker.Consumer;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.invoker.frame.Response;

@Extension(ClusterType.FAIL_OVER)
public class FailOverCluster extends AbstractCluster {

  private static final String RETRY_TIMES = "$TRY_TIMES$";

  @Override
  public Response onError(Consumer invoker, Consumer failedConsumer, Request request,
      RuntimeException e) {
    int retryLimit = invoker.getMeta().getRetryTimes();

    int alreadyRetry;
    Object alreadyRetryAttr = request.getAttribute(RETRY_TIMES);
    if (alreadyRetryAttr == null) {
      alreadyRetry = 0;
    } else {
      alreadyRetry = (Integer) alreadyRetryAttr;
    }

    if (alreadyRetry >= retryLimit) {
      logger.error("FailOverCluster, reach retry limits after retry: {} times. meta: {}",
          alreadyRetry, failedConsumer.getMeta(), e);
      throw e;
    }

    request.addAttribute(RETRY_TIMES, ++alreadyRetry);

    return invoker.invoke(request);
  }
}
