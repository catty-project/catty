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
package pink.catty.core.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicate that an interface is an RPC-service. @RpcService contains some useful config.
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
@Inherited
public @interface RpcService {

  /**
   * Specify the service's name, if equals "", default name ({@link Class#getName()}) will be used
   * as this service's name.
   */
  String name() default "";

  /**
   * The version of this service.
   * todo : more detail about version rule.
   */
  String version() default "1.0.0";

  /**
   * The group of this service.
   * If group is empty, every group would match.
   */
  String group() default "";

  /**
   * Each method in this service will has this timeout limit.
   * Each method could be set it's own timeout.
   * @see RpcMethod
   */
  int timeout() default -1;

}
