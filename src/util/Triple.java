package util;

import java.util.ArrayList;
import java.util.Iterator;

public class Triple<T> implements Iterable<T> {
  private final ArrayList<T> items;
  
  public Triple(T first, T second, T third) {
    this.items = new ArrayList<T>();
    this.items.add(first);
    this.items.add(second);
    this.items.add(third);
  }

  public T getFirst() {
    return this.items.get(0);
  }

  public T getSecond() {
    return this.items.get(1);
  }

  public T getThird() {
    return this.items.get(2);
  }

  @Override
  public Iterator<T> iterator() {
    return this.items.iterator();
  }
  
  @Override
  public String toString() {
    return this.items.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((items == null) ? 0 : items.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Triple other = (Triple) obj;
    if (items == null) {
      if (other.items != null)
        return false;
    } else if (!items.equals(other.items))
      return false;
    return true;
  }
}
