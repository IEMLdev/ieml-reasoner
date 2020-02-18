package parser;

public class IEMLBooleanAttribute implements IEMLUnit {
  private final Boolean bool;
  
  public IEMLBooleanAttribute(boolean value) {
    this.bool = value;
  }
  
  public boolean booleanValue() {
    return this.bool.booleanValue();
  }
  
  @Override
  public String toString() {
    return this.bool.toString();
  }
}
