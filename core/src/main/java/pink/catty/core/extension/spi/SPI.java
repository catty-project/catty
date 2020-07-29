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
package pink.catty.core.extension.spi;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * SPI(Service Provider Interface).
 *
 * <p>
 * Mark an interface as a SPI interface, users can customizing their own implement and put a SPI
 * file to $CLASS_PATH/META-INF/services/. SPI file should be like this:
 * <p>
 * File's name for example: pink.catty.core.extension.spi.Codec, indicates which SPI this file
 * about. File's content should be like this:
 *
 * <code>
 *   # this is annotation.
 *   # this is another annotation.
 *   catty=pink.catty.extension.codec.CattyCodec
 *   dubbo=com.a.b.DubboCodec
 * </code>
 *
 * <p>
 * Notice, annotation's character "#" must be the first character of line.
 *
 * @see pink.catty.core.extension.ExtensionFactory
 */
@Target({TYPE})
@Retention(RUNTIME)
@Documented
public @interface SPI {

  Scope scope() default Scope.PROTOTYPE;
}
