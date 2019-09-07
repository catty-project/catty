package rpc;

/**
 * @author zrj CreateDate: 2019/9/5
 */
public interface ResponseFuture extends Future {

  void onSuccess(Response response);

  void onFailure(Response response) ;

}
