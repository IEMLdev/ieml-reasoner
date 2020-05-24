package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Lexeme extends Writable {

  private static final long serialVersionUID = -2259223808535886545L;
  public static final String typeName = "lexeme";
  public static final String FLEXION_OPEN = "(";
  public static final String FLEXION_CLOSE = ")";
  public static final String CONTENT_OPEN = "(";
  public static final String CONTENT_CLOSE = ")";
  private static final Pattern FLEXION_OPEN_PATTERN = Pattern.compile("(" + Pattern.quote(FLEXION_OPEN) + ").*");
  private static final Pattern FLEXION_CLOSE_PATTERN = Pattern.compile("(" + Pattern.quote(FLEXION_CLOSE) + ").*");
  private static final Pattern CONTENT_OPEN_PATTERN = Pattern.compile("(" + Pattern.quote(CONTENT_OPEN) + ").*");
  private static final Pattern CONTENT_CLOSE_PATTERN = Pattern.compile("(" + Pattern.quote(CONTENT_CLOSE) + ").*");
  
  private static final Map<Object, SubtypeRebuilder<?, ?>> BUILDER_MAP;
  static {
    Map<Object, SubtypeRebuilder<?, ? extends IEMLUnit>> map = new HashMap<Object, SubtypeRebuilder<?, ? extends IEMLUnit>>();
    map.put("content", Polymorpheme.BUILDER);
    map.put("flexions", FlexionSet.BUILDER);
    BUILDER_MAP = Collections.unmodifiableMap(map);
  }
  public static final WritableBuilder<Lexeme> BUILDER = new WritableBuilder<Lexeme>(BUILDER_MAP) {
    
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
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException("Unexpected exception.", e);
      }
    }
  };

  private final String usl;
  private final Polymorpheme pm_content;
  private final FlexionSet pm_flexion;

  private Lexeme(HashMap<String, IEMLUnit> m, Polymorpheme content, FlexionSet flexions, String usl) {
    super(m);
    this.usl = usl;
    this.pm_content = content;
    this.pm_flexion = flexions;
  }

  public static Pair<Lexeme, Integer> parse(String input) throws ParseException {
    int offset = 0;
    try {
      Matcher matcher = FLEXION_OPEN_PATTERN.matcher(input);
      if (!matcher.matches())
        throw new ParseException(Lexeme.class, 0);

      offset += matcher.group(1).length();
      Pair<FlexionSet, Integer> flexionParse = FlexionSet.parse(input.substring(offset));
      offset += flexionParse.getSecond();
      matcher = FLEXION_CLOSE_PATTERN.matcher(input.substring(offset));
      if (!matcher.matches())
        throw new ParseException(Lexeme.class, 0);
      offset += matcher.group(1).length();
      matcher = CONTENT_OPEN_PATTERN.matcher(input.substring(offset));
      if (!matcher.matches())
        throw new ParseException(Lexeme.class, 0);
      offset += matcher.group(1).length();

      Pair<Polymorpheme, Integer> contentParse = Polymorpheme.parse(input.substring(offset));
      offset += contentParse.getSecond();

      matcher = CONTENT_CLOSE_PATTERN.matcher(input.substring(offset));
      if (!matcher.matches())
        throw new ParseException(Lexeme.class, 0);
      offset += matcher.group(1).length();
      
      HashMap<String, IEMLUnit> map = new HashMap<String, IEMLUnit>();
      map.put("type", new IEMLStringAttribute(typeName));
      map.put("content", contentParse.getFirst());
      map.put("flexions", flexionParse.getFirst());
      
      return new Pair<Lexeme, Integer>(new Lexeme(map, contentParse.getFirst(), flexionParse.getFirst(), input.substring(0, offset)), offset);
    } catch (ParseException e) {
      throw new ParseException(Lexeme.class, e.getOffset() + offset);
    }
  }

  public static Lexeme reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      assert(type.getValue().contentEquals(typeName));

      final Polymorpheme content = Polymorpheme.reBuild((Tuple<?>) t.get("content"));
      final FlexionSet flexion = FlexionSet.reBuild((Tuple<?>) t.get("flexions"));

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("content", content);
      m.put("flexions", flexion);
      return new Lexeme(m, content, flexion, null);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Lexeme factory(JSONObject obj) throws StyleException, JSONStructureException {
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final String usl = obj.getString("ieml");
    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final Polymorpheme content = Polymorpheme.factory(obj.getJSONObject("pm_content"));
    final FlexionSet flexion = FlexionSet.factory(obj.getJSONObject("pm_flexion"));

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", type);
    m.put("content", content);
    m.put("flexions", flexion);
    return new Lexeme(m, content, flexion, usl);
  }

  @Override
  public String getUSL() {
    String flexionsUSL = this.pm_flexion.getPseudoUSL();
    String contentUSL = this.pm_content.getUSL();
    String usl = FLEXION_OPEN + flexionsUSL + FLEXION_CLOSE;
    if (contentUSL.length() > 0)
      usl += CONTENT_OPEN + contentUSL + CONTENT_CLOSE;
    return usl;
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    if (depth <= 0) {
      try {
        m.put("translations", dictionary.getFromUSL(this.usl).get(lang));
        return new Tuple<Object>(m);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    m.put("pm_content", this.pm_content.mixedTranslation(lang, depth-1, dictionary));
    m.put("pm_flexion", this.pm_flexion.mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }
}
