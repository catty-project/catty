package io.catty;


/**
 * The most important interface which represent an rpc invocation.
 * @param <T>
 */
public interface Invoker<T> {

  Class<T> getInterface();

  Response invoke(Request request);

}
