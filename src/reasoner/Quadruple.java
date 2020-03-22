package reasoner;

public class Quadruple<T> {
  private final T first, second, third, fourth;
  
  public Quadruple(T first, T second, T third, T fourth) {
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
  }

  public T getFirst() {
    return first;
  }

  public T getSecond() {
    return second;
  }

  public T getThird() {
    return third;
  }

  public T getFourth() {
    return fourth;
  }
  
  
}
