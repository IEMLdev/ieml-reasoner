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
  
  @Override
  public String toString() {
    return this.str.toString();
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    throw new RuntimeException(new MissingTranslationException());
  }
}
