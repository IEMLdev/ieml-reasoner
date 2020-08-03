package parser;

import io.github.vletard.analogy.tuple.Tuple;
import org.json.JSONObject;
import reasoner.Dictionary;
import util.Pair;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Morpheme extends Writable implements Comparable<Morpheme> {

  private static final long serialVersionUID = 8740154662446704759L;
  public static final String TYPE_NAME = "morpheme";

  private static final String LAYER_0_CHARS_str = "EUASBT";
  public static int LAYER_0_CHARS(char c) {
    switch (c) {
      case 'E': return 0;
      case 'U': return 1;
      case 'A': return 2;
      case 'S': return 3;
      case 'B': return 4;
      case 'T': return 5;
      default: return -1;
    }
  }


  private static final Pattern LAYER_0_SINGULAR = Pattern.compile("([" + LAYER_0_CHARS_str + "]).*");
  private static final Pattern LAYER_0_PARADIGM = Pattern.compile("([MOFI]).*");
  //  private static final Pattern LAYER_1 = Pattern.compile("(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|s|t|u|wa|we|wo|wu|x|y).*");
  private static final char[] LAYER_CHAR = {':', '.', '-', '\'', ',', '_', ';'};
  private static final Map<Pattern, String> LAYER_1_MAP;
  static {
    LAYER_1_MAP = Map.ofEntries(Map.entry(Pattern.compile("(wo).*"), "U:U:"), Map.entry(Pattern.compile("(wa).*"), "U:A:"), Map.entry(Pattern.compile("(y).*"), "U:S:"), Map.entry(Pattern.compile("(o).*"), "U:B:"), Map.entry(Pattern.compile("(e).*"), "U:T:"), Map.entry(Pattern.compile("(wu).*"), "A:U:"), Map.entry(Pattern.compile("(we).*"), "A:A:"), Map.entry(Pattern.compile("(u).*"), "A:S:"), Map.entry(Pattern.compile("(a).*"), "A:B:"), Map.entry(Pattern.compile("(i).*"), "A:T:"), Map.entry(Pattern.compile("(j).*"), "S:U:"), Map.entry(Pattern.compile("(g).*"), "S:A:"), Map.entry(Pattern.compile("(s).*"), "S:S:"), Map.entry(Pattern.compile("(b).*"), "S:B:"), Map.entry(Pattern.compile("(t).*"), "S:T:"), Map.entry(Pattern.compile("(h).*"), "B:U:"), Map.entry(Pattern.compile("(c).*"), "B:A:"), Map.entry(Pattern.compile("(k).*"), "B:S:"), Map.entry(Pattern.compile("(m).*"), "B:B:"), Map.entry(Pattern.compile("(n).*"), "B:T:"), Map.entry(Pattern.compile("(p).*"), "T:U:"), Map.entry(Pattern.compile("(x).*"), "T:A:"), Map.entry(Pattern.compile("(d).*"), "T:S:"), Map.entry(Pattern.compile("(f).*"), "T:B:"), Map.entry(Pattern.compile("(l).*"), "T:T:"));
  }

  public static final WritableBuilder<Morpheme> BUILDER = new WritableBuilder<>() {
    @Override
    public Morpheme parse(String usl) throws ParseException {
      Pair<Morpheme, Integer> parse = Morpheme.parse(usl);
      if (parse.second != usl.length())
        throw new ParseException(Morpheme.class, parse.second, usl);
      return parse.first;
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

  public static final WritableBuilder<Morpheme> NON_PARADIGMATIC_BUILDER = new WritableBuilder<>() {
    @Override
    public Morpheme parse(String usl) throws ParseException {
      Pair<Morpheme, Integer> parse = Morpheme.parse(usl);
      if (parse.second != usl.length() || parse.first.isParadigm())
        throw new ParseException(Morpheme.class, parse.second, usl);
      return parse.first;
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

      HashMap<String, IEMLUnit> m = new HashMap<>();
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
    String output = result.first;
    IEMLBooleanAttribute paradigm = new IEMLBooleanAttribute(result.second);
    assert(output != null && !output.isEmpty());

    IEMLStringAttribute usl = new IEMLStringAttribute(output);
    HashMap<String, IEMLUnit> map = new HashMap<>();
    map.put("type", new IEMLStringAttribute(TYPE_NAME));
    map.put("content", usl);
    map.put("index", usl);
    map.put("paradigm", paradigm);
    return new Pair<>(new Morpheme(map, usl, usl, paradigm), usl.getValue().length());
  }

  private static Pair<String, Boolean> parseSingle(String input) throws ParseException {
    for (int layer = 6; layer >= 0; layer--) {
      try {
        return parseSingleRec(input, layer);
      } catch (ParseException e) {}
    }
    throw new ParseException(Morpheme.class, 0, input);
  }

  private static Pair<String, Boolean> parseSingleRec(String input, int layer) throws ParseException {
    if (layer < 0 || layer > 6)
      throw new RuntimeException("Invalid parameter layer: " + layer);

    boolean paradigm = false;
    String expr = null;
    if (layer == 0) {
      Matcher m = LAYER_0_SINGULAR.matcher(input);
      if (!m.matches())
        m = LAYER_0_PARADIGM.matcher(input);
      if (m.matches() && input.length() > m.group(1).length() && input.charAt(m.group(1).length()) == LAYER_CHAR[layer])
        expr = m.group(1);
      else
        throw new ParseException(Morpheme.class, 0, input);
    }
    else if (layer == 1) {
      for (Pattern p: LAYER_1_MAP.keySet()) {
        Matcher m = p.matcher(input);
        if (m.matches()) {
          expr = m.group(1);
          break;
        }
      }
    }

    if (expr == null) { // try to read a compound morpheme if no base morpheme was read already
      Pair<String, Boolean> result = parseSingleRec(input, layer-1);
      expr = result.first;
      paradigm = paradigm || result.second;
      try {
        result = parseSingleRec(input.substring(expr.length()), layer-1);
        expr += result.first;
        paradigm = paradigm || result.second;

        result = parseSingleRec(input.substring(expr.length()), layer-1);
        expr += result.first;
        paradigm = paradigm || result.second;
      } catch (ParseException e) {}
    }

    if (input.length() <= expr.length() || input.charAt(expr.length()) != LAYER_CHAR[layer])
      throw new ParseException(Morpheme.class, expr.length(), input);
    else
      expr += input.charAt(expr.length());  // adding the postfix char for the recognized layer

    while (input.length() > expr.length() && input.charAt(expr.length()) == '+') {  // trying to read an infix paradigmatic list 
      Pair<String, Boolean> result = parseSingleRec(input.substring(expr.length() + 1), layer);
      expr += '+' + result.first;
      paradigm = true;
    }

    return new Pair<>(expr, paradigm);
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
    HashMap<Object, Object> m = new HashMap<>();
    try {
      m.put("translations", dictionary.get(this).get(lang));
    } catch (MissingTranslationException e) {
      m.put("usl", this.usl);  // in case no translation exist for this morpheme in the dictionary, just output the usl
    }
    return new Tuple<>(m);
  }

  private int getLayer() {
    int layer = -1;
    final String v = usl.getValue();
    for (int i = 0; i < LAYER_CHAR.length; i++) {
      if (LAYER_CHAR[i] == v.charAt(v.length()-1)) {
        layer = i;
        break;
      }
    }
    if (layer == -1)
      throw new RuntimeException("Unknown layer.");
    return layer;
  }

  private static int compareMorphemeUSL(String usl1, String usl2) {
    if (usl1.isEmpty())
      return usl2.isEmpty() ? 0 : 1;
    if (usl2.isEmpty())
      return -1;

    for (Entry<Pattern, String> entry: LAYER_1_MAP.entrySet()) {
      final Pattern key = entry.getKey();
      Matcher m1 = key.matcher(usl1);
      final String value = entry.getValue();
      if (m1.matches())
        usl1 = value + usl1.substring(m1.group(1).length());

      Matcher m2 = key.matcher(usl2);
      if (m2.matches())
        usl2 = value + usl2.substring(m2.group(1).length());
    }

    if (usl1.charAt(0) == usl2.charAt(0))
      return compareMorphemeUSL(usl1.substring(1), usl2.substring(1));

    int i1 = LAYER_0_CHARS(usl1.charAt(0));
    int i2 = LAYER_0_CHARS(usl2.charAt(0));
    if (i1 == -1 && i2 == -1)
      return usl1.charAt(0) - usl2.charAt(0);
    else
      return i1 - i2;
  }

  /**
   * Defines a dictionary order on morphemes using their USL.
   */
  @Override
  public int compareTo(Morpheme m) {
    if (this == m) return 0;
    int thisLayer = this.getLayer();
    int mLayer = m.getLayer();
    if (thisLayer < mLayer)
      return -1;
    else if (thisLayer > mLayer)
      return 1;
    return compareMorphemeUSL(this.getUSL(), m.getUSL());
  }

  @Override
  public String toString() {
    return this.usl.toString();
  }
}
