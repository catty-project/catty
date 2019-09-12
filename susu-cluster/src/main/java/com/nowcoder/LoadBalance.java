package com.nowcoder;

import java.util.List;

/**
 * @author zrj CreateDate: 2019/9/10
 */
public interface LoadBalance {

  Invoker select(List<Invoker> invokers);

}
