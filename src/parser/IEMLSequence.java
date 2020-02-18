package parser;

import io.github.vletard.analogy.sequence.Sequence;

public abstract class IEMLSequence<T extends IEMLUnit> extends Sequence<T> implements IEMLUnit {

  private static final long serialVersionUID = 8029531455350234589L;

  public IEMLSequence(Iterable<T> s) {
    super(s);
  }
  
//  @Override
//  public String mixedTranslation(String lang, int depth, Dictionary dictionary) {
//    if (depth <= 0)
//      try {
//        return dictionary.getFromUSL(this.getUsl()).get(lang).toString();
//      } catch (MissingTranslationException e) {
//        // If no translation can be found, the mixedTranslation goes on to next level of the tree.
//      }
//    String str = "[";
//    boolean first = true;
//    for (T item: this) {
//      if (first)
//        first = false;
//      else
//        str += ", ";
//      str += item.mixedTranslation(lang, depth-1, dictionary);
//    }
//    return str + "]";
//  };
}
