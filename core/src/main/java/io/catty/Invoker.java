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

  Response invoke(Request request, Runtime runtime);

}
