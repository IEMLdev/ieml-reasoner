package parser;

public class IEMLObjectAttribute implements IEMLUnit {
  private final Object object;
  
  public IEMLObjectAttribute(Object object) {
    this.object = object;
  }

  public Object get() {
    return this.object;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((object == null) ? 0 : object.hashCode());
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
    IEMLObjectAttribute other = (IEMLObjectAttribute) obj;
    if (object == null) {
      if (other.object != null)
        return false;
    } else if (!object.equals(other.object))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return this.object.toString();
  }
}
