package parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.tuple.SubTupleRebuilder;

public abstract class WritableBuilder<T extends Writable> extends SubTupleRebuilder<IEMLUnit, T> {
  public abstract T parse(String usl) throws ParseException;

  public WritableBuilder() {
    super(SubtypeRebuilder.identity());
  }

  public WritableBuilder(Map<Object, SubtypeRebuilder<?, ?>> subordinates) {
    super(subordinates);
  }

  public WritableBuilder(Map<Object, SubtypeRebuilder<?, ?>> subordinates, SubtypeRebuilder<IEMLUnit, ? extends IEMLUnit> defaultBuilder) {
    super(subordinates, defaultBuilder);
  }

  public static Iterable<Writable> parseAny(String usl) {
    HashSet<Writable> set = new HashSet<Writable>();

    try {
      set.add(Word.BUILDER.parse(usl));
    } catch (ParseException e) {}

    try {
      set.add(Lexeme.BUILDER.parse(usl));
    } catch (ParseException e) {}

    try {
      set.add(Polymorpheme.BUILDER.parse(usl));
    } catch (ParseException e) {}

    try {
      set.add(Morpheme.BUILDER.parse(usl));
    } catch (ParseException e) {}

    if (set.isEmpty())
      System.err.println("Cannot parse USL " + usl);
    
    return Collections.unmodifiableSet(set);
  }

  public static final WritableBuilder<Lexeme> LEXEME_BUILDER_INSTANCE = Lexeme.BUILDER;
  public static final WritableBuilder<Word> WORD_BUILDER_INSTANCE = Word.BUILDER;
}