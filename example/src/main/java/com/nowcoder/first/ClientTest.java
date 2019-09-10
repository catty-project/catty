package com.nowcoder.first;


import com.nowcoder.ReferConfig;

/**
 * @author zrj CreateDate: 2019/9/4
 */
public class ClientTest {

  public static void main(String[] args) throws Exception {
    ReferConfig<IService> reference = new ReferConfig<>();
    reference.setInterfaceClass(IService.class);
    reference.addAddress("127.0.0.1", 25880);
    IService service = reference.getRefer();
//    Thread.sleep(1000);
    String result = service.say("zrj");
    System.out.println(result);
  }

}
