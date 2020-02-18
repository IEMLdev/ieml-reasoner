package parser;

import java.util.Map;

import io.github.vletard.analogy.tuple.Tuple;

public abstract class IEMLTuple extends Tuple<IEMLUnit> implements IEMLUnit {
  private static final long serialVersionUID = -5029365269844131711L;

  public IEMLTuple(Map<?, ? extends IEMLUnit> m) {
    super(m);
  }

//  @Override
//  public String mixedTranslation(String lang, int depth, Dictionary dictionary) {
//    if (depth <= 0)
//      try {
//        return dictionary.getFromUSL(this.getUsl()).get(lang).toString();
//      } catch (MissingTranslationException e) {
//        // If no translation can be found, the mixedTranslation goes on to next level of the tree.
//      } catch (NoUSLException e) {
//        // This case is similar to the previous catch, for a different reason.
//      }
//    String str = "{";
//    boolean first = true;
//    for (Object k: this.keySet()) {
//      if (first)
//        first = false;
//      else
//        str += ", ";
//      str += k + "=" + this.get(k).mixedTranslation(lang, depth-1, dictionary);
//    }
//    return str + "}";
//  };
}
