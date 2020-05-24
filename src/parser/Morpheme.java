package parser;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Morpheme extends Writable implements Comparable<Morpheme> {

  private static final long serialVersionUID = 8740154662446704759L;
  public static final String TYPE_NAME = "morpheme";
  private static final Pattern LAYER_0_SINGULAR = Pattern.compile("([SBTAUE]).*");
  private static final Pattern LAYER_0_PARADIGM = Pattern.compile("([MOFI]).*");
  private static final Pattern LAYER_1 = Pattern.compile("(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|s|t|u|wa|we|wo|wu|x|y).*");
  private static final char[] LAYER_CHAR = {':', '.', '-', '\'', ',', '_', ';'};
  
  public static final WritableBuilder<Morpheme> BUILDER = new WritableBuilder<Morpheme>() {
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
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };
  
  public static final WritableBuilder<Morpheme> NON_PARADIGMATIC_BUILDER = new WritableBuilder<Morpheme>() {
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
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };
  
  private final IEMLStringAttribute usl;
  private final IEMLStringAttribute indexString;
  private final IEMLBooleanAttribute paradigm;

  private Morpheme(HashMap<String, IEMLUnit> m, IEMLStringAttribute usl, IEMLStringAttribute indexString, IEMLBooleanAttribute paradigm) {
    super(m);
    this.usl = usl;
    this.indexString = indexString;
    this.paradigm = paradigm;
  }

  public static Morpheme reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      assert(type.getValue().contentEquals(TYPE_NAME));

      final IEMLStringAttribute usl = (IEMLStringAttribute) t.get("content");
      final IEMLStringAttribute index = (IEMLStringAttribute) t.get("index");
      final IEMLBooleanAttribute paradigm = (IEMLBooleanAttribute) t.get("paradigm");

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("content", usl);
      m.put("index", index);
      m.put("paradigm", paradigm);
      return new Morpheme(m, usl, index, paradigm);
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Morpheme factory(JSONObject obj){
    throw new UnsupportedOperationException();
    //    String type_str = obj.getString("type");
    //    assert(type_str.contentEquals(TYPE_NAME));
    //
    //    final IEMLStringAttribute usl = new IEMLStringAttribute(obj.getString("ieml"));
    //    final IEMLStringAttribute index = new IEMLStringAttribute(obj.getString("index"));
    //    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    //    m.put("type", new IEMLStringAttribute(type_str));
    //    m.put("content", usl);
    //    m.put("index", index);
    //    return new Morpheme(m, usl, index);
  }

  public static Pair<Morpheme, Integer> parse(String input) throws ParseException {
    Pair<String, Boolean> result = parseSingle(input);
    String output = result.getFirst();
    IEMLBooleanAttribute paradigm = new IEMLBooleanAttribute(result.getSecond());
    assert(output != null && output.length() > 0);

    IEMLStringAttribute usl = new IEMLStringAttribute(output);
    HashMap<String, IEMLUnit> map = new HashMap<String, IEMLUnit>();
    map.put("type", new IEMLStringAttribute(TYPE_NAME));
    map.put("content", usl);
    map.put("index", usl);
    map.put("paradigm", paradigm);
    return new Pair<Morpheme, Integer>(new Morpheme(map, usl, usl, paradigm), usl.getValue().length());
  }

  private static Pair<String, Boolean> parseSingle(String input) throws ParseException {
    for (int layer = 6; layer >= 0; layer--) {
      try {
        return parseSingleRec(input, layer);
      } catch (ParseException e) {}
    }
    throw new ParseException(Morpheme.class, 0);
  }

  private static Pair<String, Boolean> parseSingleRec(String input, int layer) throws ParseException {
    if (layer < 0 || layer > 6)
      throw new RuntimeException("Invalid parameter layer: " + layer);

    Boolean paradigm = false;
    String expr = null;
    if (layer == 0) {
      Matcher m = LAYER_0_SINGULAR.matcher(input);
      if (!m.matches())
        m = LAYER_0_PARADIGM.matcher(input);
      if (m.matches() && input.length() > m.group(1).length() && input.charAt(m.group(1).length()) == LAYER_CHAR[layer])
        expr = m.group(1);
      else
        throw new ParseException(Morpheme.class, 0);
    }
    else if (layer == 1) {
      Matcher m = LAYER_1.matcher(input);
      if (m.matches())
        expr = m.group(1);
    }

    if (expr == null) { // try to read a compound morpheme if no base morpheme was read already
      Pair<String, Boolean> result = parseSingleRec(input, layer-1);
      expr = result.getFirst();
      paradigm = paradigm || result.getSecond();
      try {
        result = parseSingleRec(input.substring(expr.length()), layer-1);
        expr += result.getFirst();
        paradigm = paradigm || result.getSecond();

        result = parseSingleRec(input.substring(expr.length()), layer-1);
        expr += result.getFirst();
        paradigm = paradigm || result.getSecond();
      } catch (ParseException e) {}
    }

    if (input.length() <= expr.length() || input.charAt(expr.length()) != LAYER_CHAR[layer])
      throw new ParseException(Morpheme.class, expr.length());
    else
      expr += input.charAt(expr.length());  // adding the postfix char for the recognized layer

    while (input.length() > expr.length() && input.charAt(expr.length()) == '+') {  // trying to read an infix paradigmatic list 
      Pair<String, Boolean> result = parseSingleRec(input.substring(expr.length()), layer);
      expr += '+' + result.getFirst();
      paradigm = true;
    }

    return new Pair<String, Boolean>(expr, paradigm);
  }

  public boolean isParadigm() {
    return this.paradigm.booleanValue();
  }

  @Override
  public String getUSL() {
    return this.usl.getValue();
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<Object, Object> m = new HashMap<Object, Object>();
    try {
      m.put("translations", dictionary.getFromUSL(this.usl.getValue()).get(lang));
    } catch (MissingTranslationException e) {
      m.put("usl", this.usl);  // in case no translation exist for this morpheme in the dictionary, just output the usl

    }
    return new Tuple<Object>(m);
  }

  /**
   * Defines a dictionary order on morphemes using their USL.
   */
  @Override
  public int compareTo(Morpheme m) {
    return this.indexString.getValue().compareTo(m.indexString.getValue());
  }
  
  @Override
  public String toString() {
    return this.usl.toString();
  }
}
