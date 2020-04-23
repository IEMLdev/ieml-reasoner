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
}
