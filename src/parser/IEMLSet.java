package parser;

import java.util.Set;

import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class IEMLSet<T extends IEMLUnit> extends ImmutableSet<T> implements IEMLUnit { // TODO make abstract

  private static final long serialVersionUID = 4159268675160525986L;

  public IEMLSet(Set<T> s) {
    super(s);
  }
}
