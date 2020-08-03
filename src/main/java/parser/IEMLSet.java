package parser;

import io.github.vletard.analogy.set.ImmutableSet;

import java.util.Set;

public class IEMLSet<T extends IEMLUnit> extends ImmutableSet<T> implements IEMLUnit { // TODO make abstract
  private static final long serialVersionUID = 4159268675160525986L;

  public IEMLSet(Set<T> s) {
    super(s);
  }
}
