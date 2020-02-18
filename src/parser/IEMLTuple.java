package parser;

import java.util.Map;

import io.github.vletard.analogy.tuple.Tuple;

public abstract class IEMLTuple extends Tuple<IEMLUnit> implements IEMLUnit {
  private static final long serialVersionUID = -5029365269844131711L;

  public IEMLTuple(Map<?, ? extends IEMLUnit> m) {
    super(m);
  }
}
