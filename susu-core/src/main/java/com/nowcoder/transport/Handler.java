package com.nowcoder.transport;


/**
 * 传输层获得数据后，交给具体的处理方法
 *
 * @author zrj
 */
public interface Handler {

  Object handle(Object message);

}
