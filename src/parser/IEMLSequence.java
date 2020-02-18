package parser;

import io.github.vletard.analogy.sequence.Sequence;

public abstract class IEMLSequence<T extends IEMLUnit> extends Sequence<T> implements IEMLUnit {

  private static final long serialVersionUID = 8029531455350234589L;

  public IEMLSequence(Iterable<T> s) {
    super(s);
  }
}
