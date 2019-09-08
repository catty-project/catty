package com.nowcoder.remote;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public interface FutureListener {

    void operationComplete(Future future) throws Exception;

}
