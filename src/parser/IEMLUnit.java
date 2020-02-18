package parser;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public interface IEMLUnit {
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary);
}
