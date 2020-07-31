package parser;

public class IEMLStringAttribute implements IEMLUnit, CharSequence {
  private final String str;
  
  public IEMLStringAttribute(CharSequence str) {
    this.str = str.toString();
  }
  
  public String getValue() {
    return this.str;
  }

  public boolean contentEquals(CharSequence s) {
    return this.str.contentEquals(s);
  }

  @Override
  public char charAt(int i) {
    return this.str.charAt(i);
  }

  @Override
  public int length() {
    return this.str.length();
  }

  @Override
  public IEMLStringAttribute subSequence(int from, int to) {
    return new IEMLStringAttribute(str.subSequence(from, to));
  }
  
  @Override
  public String toString() {
    return this.str.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((str == null) ? 0 : str.hashCode());
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
    IEMLStringAttribute other = (IEMLStringAttribute) obj;
    if (str == null) {
      if (other.str != null)
        return false;
    } else if (!str.contentEquals(other.str))
      return false;
    return true;
  }
}
