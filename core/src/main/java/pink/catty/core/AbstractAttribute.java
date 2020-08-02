package pink.catty.core;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAttribute implements Attribute {

  private Map<String, Object> attributes;

  public AbstractAttribute() {
    this(new HashMap<>());
  }

  public AbstractAttribute(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public void addAttribute(String key, Object value) {
    attributes.put(key, value);
  }

  @Override
  public Object removeAttribute(String key) {
    return attributes.remove(key);
  }

  @Override
  public boolean hasAttribute(String key) {
    return attributes.containsKey(key);
  }

  @Override
  public Object getAttribute(String key) {
    return attributes.get(key);
  }
}
