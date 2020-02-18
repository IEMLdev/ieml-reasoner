package parser;

public class IEMLStringAttribute implements IEMLUnit {
  private final String str;
  
  public IEMLStringAttribute(String str) {
    this.str = str;
  }
  
  public String getValue() {
    return this.str;
  }
  
  @Override
  public String toString() {
    return this.str.toString();
  }
}
