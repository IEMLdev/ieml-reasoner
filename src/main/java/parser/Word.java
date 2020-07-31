package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.vletard.analogy.RebuildException;
import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Word extends Writable {
  private static final long serialVersionUID = 8351186437917149613L;
  public static final IEMLStringAttribute TYPE_NAME = new IEMLStringAttribute("word");
  public static final String WORD_START = "[";
  public static final String WORD_END = "]";
  private static final Pattern WORD_START_PATTERN = Pattern.compile("(" + Pattern.quote(WORD_START) + ").*");
  private static final Pattern WORD_END_PATTERN = Pattern.compile("(" + Pattern.quote(WORD_END) + ").*");

  private static final Map<Object, SubtypeRebuilder<?, ?>> BUILDER_MAP;
  static {
    Map<Object, SubtypeRebuilder<?, ? extends IEMLUnit>> map = new HashMap<Object, SubtypeRebuilder<?, ? extends IEMLUnit>>();
    map.put("syntagmatic_function", SyntagmaticFunction.BUILDER);
    BUILDER_MAP = Collections.unmodifiableMap(map);
  }
  public static final WritableBuilder<Word> BUILDER = new WritableBuilder<Word>(BUILDER_MAP) {

    @Override
    public Word rebuild(Tuple<IEMLUnit> object) throws RebuildException {
      try {
        return reBuild(object);
      } catch (IncompatibleSolutionException | StyleException e) {
        throw new RebuildException(e);
      }
    }

    @Override
    public Word parse(String usl) throws ParseException {
      Pair<Word, Integer> parse;
      try {
        parse = Word.parse(usl);
      } catch (StyleException e) {
        throw new RuntimeException (e);
      }
      if (parse.getSecond() != usl.length())
        throw new ParseException(Word.class, parse.getSecond(), usl);
      return parse.getFirst();
    }
  };

  private final IEMLObjectAttribute role;
  public final SyntagmaticFunction syntagmaticFunction;

  /**
   * Private Word constructor available to the public factory method.
   * Role and SyntagmaticFunction are stored in both the Tuple and in fields to keep track of their actual type.
   * @param tupleInit Map to provide to the Tuple constructor.
   * @param r typed link to this word's role.
   * @param sf typed link to this word's syntagmatic function.
   */
  private Word(HashMap<String, IEMLUnit> tupleInit, IEMLObjectAttribute r, SyntagmaticFunction sf) {
    super(tupleInit);
    this.role = r;
    this.syntagmaticFunction = sf;
  }

  public static Pair<Word, Integer> parse(String input) throws ParseException, StyleException {
    WordRole wordRole = new WordRole();
    int offset = 0;
    Matcher matcher = WORD_START_PATTERN.matcher(input);
    if (!matcher.matches())
      throw new ParseException(Word.class, offset, input);
    offset = matcher.group(1).length();

    SyntagmaticFunction sf;
    try {
      Pair<Process, Integer> processParse = Process.parse(wordRole, input.substring(offset));
      sf = processParse.getFirst();
      offset += processParse.getSecond();
    } catch (ParseException e) {
      try {
        Pair<Actant, Integer> actantParse = Actant.parse(wordRole, input.substring(offset));
        sf = actantParse.getFirst();
        offset += actantParse.getSecond();
      } catch (ParseException e2) {
        try {
          Pair<Quality, Integer> qualityParse = Quality.parse(wordRole, input.substring(offset));
          sf = qualityParse.getFirst();
          offset += qualityParse.getSecond();
        } catch (ParseException e3) {
          throw new ParseException(Word.class, offset, input);
        }
      }
    }

    matcher = WORD_END_PATTERN.matcher(input.substring(offset));
    if (!matcher.matches())
      throw new ParseException(Word.class, 0, input);
    offset += matcher.group(1).length();

    IEMLObjectAttribute role = wordRole.toIEMLUnit();
    
    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", TYPE_NAME);
    m.put("role", role);
    m.put("syntagmatic_function", sf);

    return new Pair<Word, Integer>(new Word(m, role, sf), offset);
  }

  public static Word reBuild(Tuple<IEMLUnit> t) throws IncompatibleSolutionException, StyleException {
    try {
      IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      if (!type.contentEquals(TYPE_NAME))
        throw new IncompatibleSolutionException("Incompatible type name.");
      
      IEMLObjectAttribute roleList = (IEMLObjectAttribute) t.get("role");
      SyntagmaticFunction sf = SyntagmaticFunction.reBuild((Tuple<?>) t.get("syntagmatic_function"));
      for (List<Morpheme> rolePath: (List<List<Morpheme>>) roleList.get())
        if (!sf.checkStyle(rolePath))
          throw new IncompatibleSolutionException(new StyleException("Role points to a non existent node."));

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("role", roleList);
      m.put("syntagmatic_function", sf);
      return new Word(m, roleList, sf);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  @Override
  public String getUSL() {
    String usl = "";
    usl += WORD_START;
    usl += this.syntagmaticFunction.generateUSL((List<List<Morpheme>>) this.role.get());
    return usl + WORD_END;
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    if (depth <= 0) {
      try {
        m.put("translations", dictionary.get(this).get(lang));
        return new Tuple<Object>(m);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    m.put("role", this.role);
    m.put("syntagmatic_function", this.syntagmaticFunction.mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }
}
