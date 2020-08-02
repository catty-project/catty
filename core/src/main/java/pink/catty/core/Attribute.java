package pink.catty.core;

public interface Attribute {

  void addAttribute(String key, Object value);

  Object getAttribute(String key);

  Object removeAttribute(String key);

  boolean hasAttribute(String key);

}
