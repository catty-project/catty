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
package pink.catty.core.utils;

import java.util.UUID;
import pink.catty.core.Constants;
import pink.catty.core.invoker.Invocation;
import pink.catty.core.invoker.frame.DefaultRequest;
import pink.catty.core.invoker.frame.Request;
import pink.catty.core.meta.MetaInfo;
import pink.catty.core.service.HeartBeatService;
import pink.catty.core.service.MethodModel;
import pink.catty.core.service.ServiceModel;

public abstract class HeartBeatUtils {

  private static final ServiceModel<HeartBeatService> HEART_BEAT_SERVICE_MODEL;
  private static final MethodModel METHOD_MODEL;

  static {
    HEART_BEAT_SERVICE_MODEL = ServiceModel.parse(HeartBeatService.class);
    METHOD_MODEL = HEART_BEAT_SERVICE_MODEL.getMethodMetaByName(Constants.HEARTBEAT_METHOD_NAME);
  }

  public static Invocation buildHeartBeatInvocation(Object target, MetaInfo metaInfo) {
    Invocation invocation = new Invocation();
    invocation.setMetaInfo(metaInfo);
    invocation.setTarget(target);
    invocation.setServiceModel(HEART_BEAT_SERVICE_MODEL);
    invocation.setInvokedMethod(METHOD_MODEL);
    return invocation;
  }

  public static Request buildHeartBeatRequest() {
    String arg = UUID.randomUUID().toString();
    Object[] args = {arg};
    return new DefaultRequest(RequestIdGenerator.next(),
        HEART_BEAT_SERVICE_MODEL.getServiceName(), METHOD_MODEL.getName(), args);
  }
}
