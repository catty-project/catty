package core;

import java.util.Map;

/**
 * 一次远程调用，
 *
 * @author zrj CreateDate: 2019/9/4
 */
public class URL {

  /**
   * 协议
   */
  private String protocol;

  /**
   * host地址
   */
  private String host;

  /**
   * 服务端口
   */
  private String post;

  /**
   * 任务路径，等同于interfaceName
   */
  private String path;

  /**
   * 通用设置
   */
  private Map<String, String> setting;


  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPost() {
    return post;
  }

  public void setPost(String post) {
    this.post = post;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, String> getSetting() {
    return setting;
  }

  public void setSetting(Map<String, String> setting) {
    this.setting = setting;
  }
}
