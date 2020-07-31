package parser;

import java.util.Map;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public abstract class Writable extends IEMLTuple {

  private static final long serialVersionUID = 1657596529626743462L;

  public Writable(Map<?, ? extends IEMLUnit> m) {
    super(m);
  }

  public abstract String getUSL();

  public abstract Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary);
}
