package io.catty.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class MD5Utils {

  public static String md5(String input) {
    char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    MessageDigest mdInst;
    try {
      mdInst = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalArgumentException(e);
    }

    mdInst.update(input.getBytes());
    byte[] md = mdInst.digest();
    int j = md.length;
    char[] str = new char[j * 2];
    int k = 0;
    for (int i = 0; i < j; i++) {
      byte byte0 = md[i];
      str[k++] = hexDigits[byte0 >>> 4 & 0xf];
      str[k++] = hexDigits[byte0 & 0xf];
    }
    return new String(str);
  }

}
