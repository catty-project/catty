package pink.catty.core.service;

public class HeartBeatSerivceImpl implements HeartBeatService {

  @Override
  public String heartBeat(String uuid) {
    return uuid;
  }
}
