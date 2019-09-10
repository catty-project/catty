package com.nowcoder.core;

import com.nowcoder.common.constants.SusuConstants;
import com.nowcoder.config.AllConfig.CLIENT_CONFIG;
import com.nowcoder.config.AllConfig.SERVER_CONFIG;
import com.nowcoder.config.AllConfig.URL_CONFIG;
import com.nowcoder.config.RemoteConfig;
import com.nowcoder.exception.SusuException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语言透明的服务描述，配置总线。
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
  private int port;

  /**
   * 任务路径，等同于interfaceName
   */
  private String path;

  /**
   * 通用设置
   */
  private Map<String, String> parameters;


  /* constructor */
  public URL() {
  }

  public URL(String protocol, String host, int port, String path) {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    parameters = new ConcurrentHashMap<>();
    for(URL_CONFIG config0 : URL_CONFIG.values()) {
      // 没有默认值的设置不加载。
      if(config0.getDefaultValue() == null) {
        continue;
      }
      parameters.putIfAbsent(config0.getKey(), config0.getDefaultValue());
    }
  }

  public URL(String protocol, String host, int port, String path,
      Map<String, String> parameters) {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    this.parameters = parameters;
  }
  /* ------ */

  /* static method */

  /**
   * copy from motan, xixi
   */
  public static URL parse(String url) {
    if (url == null || url.length() == 0) {
      throw new SusuException("URL: url is null");
    }
    String protocol = null;
    String host = null;
    int port = 0;
    String path = null;
    Map<String, String> parameters = new HashMap<String, String>();;
    int i = url.indexOf("?"); // seperator between body and parameters
    if (i >= 0) {
      String[] parts = url.substring(i + 1).split("\\&");

      for (String part : parts) {
        part = part.trim();
        if (part.length() > 0) {
          int j = part.indexOf('=');
          if (j >= 0) {
            parameters.put(part.substring(0, j), part.substring(j + 1));
          } else {
            parameters.put(part, part);
          }
        }
      }
      url = url.substring(0, i);
    }
    i = url.indexOf(SusuConstants.PROTOCOL_SEP);
    if (i >= 0) {
      if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
      protocol = url.substring(0, i);
      url = url.substring(i + SusuConstants.PROTOCOL_SEP.length());
    } else {
      i = url.indexOf(":/");
      if (i >= 0) {
        if (i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
        protocol = url.substring(0, i);
        url = url.substring(i + 1);
      }
    }

    i = url.indexOf("/");
    if (i >= 0) {
      path = url.substring(i + 1);
      url = url.substring(0, i);
    }

    i = url.indexOf(":");
    if (i >= 0 && i < url.length() - 1) {
      port = Integer.parseInt(url.substring(i + 1));
      url = url.substring(0, i);
    }
    if (url.length() > 0) host = url;
    return new URL(protocol, host, port, path, parameters);
  }

  public static String buildHostPortStr(String host, int defaultPort) {
    if (defaultPort <= 0) {
      return host;
    }

    int idx = host.indexOf(":");
    if (idx < 0) {
      return host + ":" + defaultPort;
    }

    int port = Integer.parseInt(host.substring(idx + 1));
    if (port <= 0) {
      return host.substring(0, idx + 1) + defaultPort;
    }
    return host;
  }

  public String getIpPortString() {
    return buildHostPortStr(host, port);
  }

  public String getUri() {
    return protocol + SusuConstants.PROTOCOL_SEP + host + ":" + port +
        SusuConstants.PATH_SEP + path;
  }

  public String getString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getUri()).append("?");

    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      builder.append(name).append("=").append(value).append("&");
    }

    return builder.toString();
  }


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

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }


  /* --- */

  public boolean getBooleanConfig(URL_CONFIG key) {
    String value = parameters.get(key.getKey());
    return Boolean.valueOf(value);
  }

  public short getShortConfig(URL_CONFIG key) {
    String value = parameters.get(key.getKey());
    return Short.valueOf(value);
  }

  public int getIntConfig(URL_CONFIG key) {
    String value = parameters.get(key.getKey());
    return Integer.valueOf(value);
  }

  public long getLongConfig(URL_CONFIG key) {
    String value = parameters.get(key.getKey());
    return Long.valueOf(value);
  }

  public String getStringConfig(URL_CONFIG key) {
    return parameters.get(key.getKey());
  }

  public Double getDoubleConfig(URL_CONFIG key) {
    String value = parameters.get(key.getKey());
    return Double.valueOf(value);
  }

  public void setConfig(String key, String value) {
    parameters.put(key, value);
  }

  public void setConfig(URL_CONFIG key, String value) {
    parameters.put(key.getKey(), value);
  }

  /* convert to other config */

  public RemoteConfig toRemoteConfig() {
    Map<String, String> config = parameters;
    boolean isServer = getBooleanConfig(URL_CONFIG.IS_SERVER);
    RemoteConfig remoteConfig = new RemoteConfig(isServer);
    if (isServer) {
      for(SERVER_CONFIG config0 : SERVER_CONFIG.values()) {
        String value = config.get(config0.getKey());
        if(value != null) {
          remoteConfig.setConfig(config0, value);
        }
      }
    } else {
      for(CLIENT_CONFIG config0 : CLIENT_CONFIG.values()) {
        String value = config.get(config0.getKey());
        if(value != null) {
          remoteConfig.setConfig(config0, value);
        }
      }
      remoteConfig.setConfig(CLIENT_CONFIG.REMOTE_IP, host);
      remoteConfig.setConfig(CLIENT_CONFIG.REMOTE_PORT, String.valueOf(port));
    }
    return remoteConfig;
  }

}
