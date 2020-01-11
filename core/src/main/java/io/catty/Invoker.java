package io.catty;


/**
 * The most important interface which represent an rpc invocation. Every struct in catty which in
 * the invoke-link is an Invoker.
 *
 * --------------------------------------------------------------------
 * | Consumer entry                           Provider entry          |
 * |       |                                       |                  |
 * |     Invoker                                 Invoker              |
 * |       |                                       |                  |
 * |      ... (Invoker Chain)                     ... (Invoker Chain) |
 * |       |                                       |                  |
 * |      Client (also an invoker)  -->   Server (also an invoker)    |
 * --------------------------------------------------------------------
 *
 *
 * A RPC invoke will be wrapped as a Request, and the return of the RPC invoke will be wrapped as a
 * Response.
 *
 * Runtime contains the whole information of the current invocation.
 */
public interface Invoker {

  /**
   *
   * @param request rpc request
   * @param runtime rpc invoke runtime arguments.
   * @return rpc return
   * @throws io.catty.exception.CattyException If inner error occurred. CattyException will be
   * thrown.
   */
  Response invoke(Request request, Runtime runtime);

}
