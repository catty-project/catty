package io.catty.core.utils;

public abstract class MetaInfoUtils {

  // todo: complete. add test.
  public static boolean compareVersion(String version, String target) {
    String[] versions = version.split("\\.");
    String[] targets = target.split("\\.");
    int i = 0, j = 0;
    for(; i < versions.length && j < targets.length; i++, j++) {
      if(Integer.valueOf(targets[j]) > Integer.valueOf(versions[i])) {

      }
    }
    return true;
  }

}
