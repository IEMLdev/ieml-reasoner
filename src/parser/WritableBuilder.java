package parser;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import util.Pair;

public abstract class WritableBuilder<T extends Writable> extends SubtypeRebuilder<Tuple<IEMLUnit>, T> {

  public abstract T parse(String usl) throws ParseException;
  

  public static final WritableBuilder<Morpheme> MORPHEME_BUILDER_INSTANCE = new WritableBuilder<Morpheme>() {

    @Override
    public Morpheme parse(String usl) throws ParseException {
      Pair<Morpheme, Integer> parse = Morpheme.parse(usl);
      if (parse.getSecond() != usl.length())
        throw new ParseException(Morpheme.class, parse.getSecond());
      return parse.getFirst();
    }

    @Override
    public Morpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Morpheme.reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };
  

  public static final WritableBuilder<Morpheme> NON_PARADIGMATIC_MORPHEME_BUILDER_INSTANCE = new WritableBuilder<Morpheme>() {

    @Override
    public Morpheme parse(String usl) throws ParseException {
      Pair<Morpheme, Integer> parse = Morpheme.parse(usl);
      if (parse.getSecond() != usl.length() || parse.getFirst().isParadigm())
        throw new ParseException(Morpheme.class, parse.getSecond());
      return parse.getFirst();
    }

    @Override
    public Morpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Morpheme.reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };
  
  public static final WritableBuilder<Polymorpheme> POLYMORPHEME_BUILDER_INSTANCE = new WritableBuilder<Polymorpheme>() {

    @Override
    public Polymorpheme parse(String usl) throws ParseException {
      Pair<Polymorpheme, Integer> parse = Polymorpheme.parse(usl);
      if (parse.getSecond() != usl.length())
        throw new ParseException(Polymorpheme.class, parse.getSecond());
      return parse.getFirst();
    }

    @Override
    public Polymorpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Polymorpheme.reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };


  public static final WritableBuilder<Lexeme> LEXEME_BUILDER_INSTANCE = new WritableBuilder<Lexeme>() {

    @Override
    public Lexeme parse(String usl) throws ParseException {
      Pair<Lexeme, Integer> parse = Lexeme.parse(usl);
      if (parse.getSecond() != usl.length())
        throw new ParseException(Lexeme.class, parse.getSecond());
      return parse.getFirst();
    }

    @Override
    public Lexeme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Lexeme.reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException("Unexpected exception.", e);
      }
    }
  };
  
  public static final WritableBuilder<Word> WORD_BUILDER_INSTANCE = new WritableBuilder<Word>() {

    @Override
    public Word parse(String usl) throws ParseException {
      Pair<Word, Integer> parse = Word.parse(usl);
      if (parse.getSecond() != usl.length())
        throw new ParseException(Word.class, parse.getSecond());
      return parse.getFirst();
    }

    @Override
    public Word rebuild(Tuple<IEMLUnit> object) {
      try {
        return Word.reBuild(object);
      } catch (IncompatibleSolutionException | StyleException e) {
        throw new RuntimeException(e);
      }
    }
  };
}