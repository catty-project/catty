package com.nowcoder.config;

/**
 * @author zrj CreateDate: 2019/9/9
 */
public interface AllConfig {

  /**
   *
   *
   */
  enum URL_CONFIG {
    ROOT("registry_root", "/susu"),
    GROUP("registry_group", "default"),
    IS_SERVER("is_server", "true"),
    VERSION("version", "1.0"),
    TRANSPORT("transport", "netty"),
    CODEC("codec", "susu"),
    LOAD_BALANCE("load_balance", "random"),



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
