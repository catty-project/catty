/*
 * Copyright 2020 The Catty Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pink.catty.core.support;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @see java.util.HashSet
 */
public class ConcurrentHashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
  static final long serialVersionUID = -5024744406713321676L;

  private transient ConcurrentMap<E,Object> map;

  // Dummy value to associate with an Object in the backing Map
  private static final Object PRESENT = new Object();

  /**
   * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
   * default initial capacity (16) and load factor (0.75).
   */
  public ConcurrentHashSet() {
    map = new ConcurrentHashMap<>();
  }

  /**
   * Constructs a new set containing the elements in the specified
   * collection.  The <tt>HashMap</tt> is created with default load factor
   * (0.75) and an initial capacity sufficient to contain the elements in
   * the specified collection.
   *
   * @param c the collection whose elements are to be placed into this set
   * @throws NullPointerException if the specified collection is null
   */
  public ConcurrentHashSet(Collection<? extends E> c) {
    map = new ConcurrentHashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
    addAll(c);
  }

  /**
   * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
   * the specified initial capacity and the specified load factor.
   *
   * @param      initialCapacity   the initial capacity of the hash map
   * @param      loadFactor        the load factor of the hash map
   * @throws     IllegalArgumentException if the initial capacity is less
   *             than zero, or if the load factor is nonpositive
   */
  public ConcurrentHashSet(int initialCapacity, float loadFactor) {
    map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
  }

  /**
   * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
   * the specified initial capacity and default load factor (0.75).
   *
   * @param      initialCapacity   the initial capacity of the hash table
   * @throws     IllegalArgumentException if the initial capacity is less
   *             than zero
   */
  public ConcurrentHashSet(int initialCapacity) {
    map = new ConcurrentHashMap<>(initialCapacity);
  }

  /**
   * Returns an iterator over the elements in this set.  The elements
   * are returned in no particular order.
   *
   * @return an Iterator over the elements in this set
   * @see ConcurrentModificationException
   */
  public Iterator<E> iterator() {
    return map.keySet().iterator();
  }

  /**
   * Returns the number of elements in this set (its cardinality).
   *
   * @return the number of elements in this set (its cardinality)
   */
  public int size() {
    return map.size();
  }

  /**
   * Returns <tt>true</tt> if this set contains no elements.
   *
   * @return <tt>true</tt> if this set contains no elements
   */
  public boolean isEmpty() {
    return map.isEmpty();
  }

  /**
   * Returns <tt>true</tt> if this set contains the specified element.
   * More formally, returns <tt>true</tt> if and only if this set
   * contains an element <tt>e</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
   *
   * @param o element whose presence in this set is to be tested
   * @return <tt>true</tt> if this set contains the specified element
   */
  public boolean contains(Object o) {
    return map.containsKey(o);
  }

  /**
   * Adds the specified element to this set if it is not already present.
   * More formally, adds the specified element <tt>e</tt> to this set if
   * this set contains no element <tt>e2</tt> such that
   * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
   * If this set already contains the element, the call leaves the set
   * unchanged and returns <tt>false</tt>.
   *
   * @param e element to be added to this set
   * @return <tt>true</tt> if this set did not already contain the specified
   * element
   */
  public boolean add(E e) {
    return map.put(e, PRESENT)==null;
  }

  /**
   * Removes the specified element from this set if it is present.
   * More formally, removes an element <tt>e</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
   * if this set contains such an element.  Returns <tt>true</tt> if
   * this set contained the element (or equivalently, if this set
   * changed as a result of the call).  (This set will not contain the
   * element once the call returns.)
   *
   * @param o object to be removed from this set, if present
   * @return <tt>true</tt> if the set contained the specified element
   */
  public boolean remove(Object o) {
    return map.remove(o)==PRESENT;
  }

  /**
   * Removes all of the elements from this set.
   * The set will be empty after this call returns.
   */
  public void clear() {
    map.clear();
  }
}
