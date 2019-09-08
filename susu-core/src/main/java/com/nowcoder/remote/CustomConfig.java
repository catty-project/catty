package com.nowcoder.remote;

/**
 * 方便扩展自定义的Config
 *
 * @author zrj CreateDate: 2019/9/8
 */
public interface CustomConfig {

  void addCustomConfig(String key, String value);

  String removeCustomConfig(String key);

}
