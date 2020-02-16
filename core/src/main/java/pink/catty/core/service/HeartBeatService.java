package pink.catty.core.service;

import pink.catty.core.GlobalConstants;

@RpcService(name = GlobalConstants.HEARTBEAT_SERVICE_NAME)
public interface HeartBeatService {

  @RpcMethod(name = GlobalConstants.HEARTBEAT_METHOD_NAME)
  String heartBeat(String uuid);
}
