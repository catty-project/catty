package com.nowcoder.api.transport;

import com.nowcoder.api.remote.Request;
import com.nowcoder.api.remote.Response;

/**
 * 客户端，客户端具有请求的功能
 *
 * @author zrj CreateDate: 2019/9/8
 */
public interface Client extends Channel {

  Response invoke(Request request);

}
