package com.nowcoder.config;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public interface AllConfig {


  /**
   * 注册中心的配置参数
   *
   * 注册中心的目录树如下：/susu/[group]/[interface name]/[provider&consumer]
   */
  enum REGISTRY_CONFIG {
    IP_PORT("registry_ip_port", "127.0.0.1:2181"),
    USERNAME("registry_username", null),
    PASSWORD("registry_password", null),

    ROOT("registry_root", "/susu"),

    ;

    private String key;
    private String defaultValue;

    public String getKey() {
      return key;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    REGISTRY_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

  enum CLIENT_CONFIG {
    REMOTE_IP("remote_ip", "127.0.0.1"),
    REMOTE_PORT("remote_port", "25880"),

    ;
    private String key;
    private String defaultValue;

    public String getKey() {
      return key;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    CLIENT_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

  enum SERVER_CONFIG {
    SERVER_PORT("serverPort", "25880"),

    ;
    private String key;
    private String defaultValue;

    public String getKey() {
      return key;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    SERVER_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

  enum URL_CONFIG {
    ROOT("registry_root", "/susu"),
    GROUP("registry_group", "default"),
    IS_SERVER("is_server", "true"),
    VERSION("version", "1.0"),
    TRANSPORT("transport", "netty"),
    LOAD_BALANCE("load_balance", "random"),
    CODEC("codec", "susu"),


    ;
    private String key;
    private String defaultValue;

    public String getKey() {
      return key;
    }

    public String getDefaultValue() {
      return defaultValue;
    }

    URL_CONFIG(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }
  }

}
