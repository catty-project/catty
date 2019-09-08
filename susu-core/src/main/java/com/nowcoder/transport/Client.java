package com.nowcoder.transport;

import com.nowcoder.remote.Channel;
import com.nowcoder.remote.Request;
import com.nowcoder.remote.Response;

/**
 * @author zrj CreateDate: 2019/9/8
 */
public interface Client extends Channel {

  Response request(Request request);

}
