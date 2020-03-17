package parser;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class IEMLStringAttribute implements IEMLUnit {
  private final String str;
  
  public IEMLStringAttribute(String str) {
    this.str = str;
  }
  
  public String getValue() {
    return this.str;
  }

  public boolean contentEquals(CharSequence s) {
    return this.str.contentEquals(s);
  }
  
  @Override
  public String toString() {
    return this.str.toString();
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    throw new RuntimeException(new MissingTranslationException());
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
    } else if (!str.equals(other.str))
      return false;
    return true;
  }
}
