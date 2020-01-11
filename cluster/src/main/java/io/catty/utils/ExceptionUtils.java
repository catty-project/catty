package io.catty.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

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
    try {
      t.printStackTrace(p);
      return writer.toString();
    } finally {
      p.close();
    }
  }

  public static void main(String[] args) {
    Object o = null;
    try {
      o.getClass();
    } catch (Exception e) {
      IllegalArgumentException exception = new IllegalArgumentException("test" ,e);
      System.out.println(ExceptionUtils.toString(exception));
      System.out.println(ExceptionUtils.toString("test1", e));
    }
  }
}
