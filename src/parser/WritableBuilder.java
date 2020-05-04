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
        throw new ParseException("Cannot parse a morpheme for the full usl.");
      return parse.getFirst();
    }

    @Override
    public Morpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Morpheme.reFactory(object);
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
        throw new ParseException("Cannot parse a non paradigmatic morpheme for the full usl.");
      return parse.getFirst();
    }

    @Override
    public Morpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Morpheme.reFactory(object);
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
        throw new ParseException("Cannot parse a polymorpheme for the full usl.");
      return parse.getFirst();
    }

    @Override
    public Polymorpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return Polymorpheme.reFactory(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };
}