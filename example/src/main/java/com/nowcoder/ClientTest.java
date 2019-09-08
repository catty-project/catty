package com.nowcoder;

import com.nowcoder.core.Reference;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ClientTest {

  public static void main(String[] args) throws Exception {
    Reference<IService> reference = new Reference<>();
    reference.setInterfaceClass(IService.class);
    IService service = reference.getRefer();
//    Thread.sleep(1000);
    String result = service.say("zrj");
    System.out.println(result);
  }

}
