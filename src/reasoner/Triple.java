package reasoner;

public class Triple<T> {
  private final T first, second, third;
  
  public Triple(T first, T second, T third) {
    this.first = first;
    this.second = second;
    this.third = third;
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
}
