package parser;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

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

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    throw new RuntimeException(new MissingTranslationException());
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
      if (other.bool != null)
        return false;
    } else if (!bool.equals(other.bool))
      return false;
    return true;
  }
}