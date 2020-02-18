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
}
