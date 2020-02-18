package parser;

public class IEMLNumberAttribute extends Number implements IEMLUnit {
  private static final long serialVersionUID = 7421298869020559873L;
  private final Number i;
  
  public IEMLNumberAttribute(Number value) {
    this.i = value;
  }
  
  @Override
  public String toString() {
    return this.i.toString();
  }

  @Override
  public double doubleValue() {
    return this.i.doubleValue();
  }

  @Override
  public float floatValue() {
    return this.i.floatValue();
  }

  @Override
  public int intValue() {
    return this.i.intValue();
  }

  @Override
  public long longValue() {
    return this.i.longValue();
  }
}
