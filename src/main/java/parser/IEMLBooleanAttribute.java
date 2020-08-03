package parser;

public class IEMLBooleanAttribute implements IEMLUnit {
  private final Boolean bool;
  
  public IEMLBooleanAttribute(boolean value) {
    this.bool = value;
  }
  
  public boolean booleanValue() {
    return this.bool;
  }
  
  @Override
  public String toString() {
    return this.bool.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((bool == null) ? 0 : bool.hashCode());
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
    IEMLBooleanAttribute other = (IEMLBooleanAttribute) obj;
    if (bool == null) {
      return other.bool == null;
    } else return bool.equals(other.bool);
  }
}
