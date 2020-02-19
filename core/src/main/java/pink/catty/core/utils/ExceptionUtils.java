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

import pink.catty.core.CattyException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class ExceptionUtils {

  public static String toString(String msg, Throwable t) {
    StringWriter writer = new StringWriter();
    writer.write(msg + "\n");
    PrintWriter p = new PrintWriter(writer);
    try {
      t.printStackTrace(p);
      return writer.toString();
    } finally {
      p.close();
    }
  }

  public static String toString(Throwable t) {
    StringWriter writer = new StringWriter();
    PrintWriter p = new PrintWriter(writer);
    p.print(t.getClass().getName());
    p.println();
    try {
      t.printStackTrace(p);
      return writer.toString();
    } finally {
      p.close();
    }
  }

  public static String[] parseExceptionString(String s) {
    return new String[]{s.substring(0, s.indexOf("\n")), s.substring(s.indexOf("\n") + 1)};
  }

  public static Throwable getInstance(Class<?> exceptionClass, String msg) {
    try {
      Constructor constructor = exceptionClass.getConstructor(String.class);
      return (Throwable) constructor.newInstance(msg);
    } catch (NoSuchMethodException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException e) {
      return new CattyException(e);
    }
  }

  public static void main(String[] args) {
    Object o = null;
    try {
      o.getClass();
    } catch (Exception e) {
      IllegalArgumentException exception = new IllegalArgumentException("test", e);
      String s = ExceptionUtils.toString(exception);
      System.out.println(s);
      System.out.println(ExceptionUtils.toString("test1", e));
      System.out.println(parseExceptionString(s)[0]);
      System.out.println(parseExceptionString(s)[1]);
    }
  }
}
