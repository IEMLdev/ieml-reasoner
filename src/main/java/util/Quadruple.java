package util;

import java.util.ArrayList;

public class Quadruple<T> {
  private final ArrayList<T> items;
  
  public Quadruple(T first, T second, T third, T fourth) {
    this.items = new ArrayList<>();
    this.items.add(first);
    this.items.add(second);
    this.items.add(third);
    this.items.add(fourth);
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

  public T getFourth() {
    return this.items.get(3);
  }
  
  @Override
  public String toString() {
    return this.items.toString();
  }
}
