package com.nowcoder.api.transport;

import com.nowcoder.codec.Codec;
import com.nowcoder.config.RemoteConfig;
import com.nowcoder.api.remote.ResponseFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端，保存了当前已经发送的所有请求，并保存了返回的Future
 *
 * @author zrj CreateDate: 2019/9/8
 */
abstract public class AbstractClient extends AbstractEndpoint implements Client {

  private Map<Long, ResponseFuture> currentTask = new ConcurrentHashMap<>();

  public AbstractClient(Codec codec, RemoteConfig remoteConfig) {
    super(codec, remoteConfig);
  }

  protected ResponseFuture getResponseFuture(long requestId) {
    return currentTask.remove(requestId);
  }

  protected void addCurrentTask(long requestId, ResponseFuture responseFuture) {
    currentTask.putIfAbsent(requestId, responseFuture);
  }

}
