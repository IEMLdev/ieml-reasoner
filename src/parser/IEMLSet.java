package parser;

import java.util.Set;

import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class IEMLSet<T extends IEMLUnit> extends ImmutableSet<T> implements IEMLUnit {

  private static final long serialVersionUID = 4159268675160525986L;

  public IEMLSet(Set<T> s) {
    super(s);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    throw new RuntimeException(new MissingTranslationException());
  }
}
