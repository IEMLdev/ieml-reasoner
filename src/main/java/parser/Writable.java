package parser;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

import java.util.Map;

public abstract class Writable extends IEMLTuple {

  private static final long serialVersionUID = 1657596529626743462L;

  public Writable(Map<?, ? extends IEMLUnit> m) {
    super(m);
  }

  public abstract String getUSL();

  public abstract Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary);
}
