package parser;

public class IEMLNumberAttribute extends Number implements IEMLUnit {
  private static final long serialVersionUID = 7421298869020559873L;
  private final Number n;

  public IEMLNumberAttribute(Number value) {
    this.n = value;
  }

  @Override
  public String toString() {
    return this.n.toString();
  }

  @Override
  public double doubleValue() {
    return this.n.doubleValue();
  }

  @Override
  public float floatValue() {
    return this.n.floatValue();
  }

  @Override
  public int intValue() {
    return this.n.intValue();
  }

  @Override
  public long longValue() {
    return this.n.longValue();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((n == null) ? 0 : n.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IEMLNumberAttribute other = (IEMLNumberAttribute) obj;
    if (n == null) {
      return other.n == null;
    } else return n.equals(other.n);
  }
}
