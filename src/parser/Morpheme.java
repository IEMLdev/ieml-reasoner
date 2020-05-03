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
  private static final Pattern LAYER_0 = Pattern.compile("([SBTAUEMOFI]).*");
  private static final Pattern LAYER_1 = Pattern.compile("(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|s|t|u|wa|we|wo|wu|x|y).*");
  private static final char[] LAYER_CHAR = {':', '.', '-', '\'', ',', '_', ';'};
  
  private final IEMLStringAttribute usl;
  private final IEMLStringAttribute indexString;

  private Morpheme(HashMap<String, IEMLUnit> m, IEMLStringAttribute usl, IEMLStringAttribute indexString) {
    super(m);
    this.usl = usl;
    this.indexString = indexString;
  }

  public static Morpheme reFactory(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      assert(type.getValue().contentEquals(TYPE_NAME));

      final IEMLStringAttribute usl = (IEMLStringAttribute) t.get("content");
      final IEMLStringAttribute index = (IEMLStringAttribute) t.get("index");

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("content", usl);
      m.put("index", index);
      return new Morpheme(m, usl, index);
    } catch (ClassCastException e) {
      e.printStackTrace();
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Morpheme factory(JSONObject obj){
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(TYPE_NAME));

    final IEMLStringAttribute usl = new IEMLStringAttribute(obj.getString("ieml"));
    final IEMLStringAttribute index = new IEMLStringAttribute(obj.getString("index"));
    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", new IEMLStringAttribute(type_str));
    m.put("content", usl);
    m.put("index", index);
    return new Morpheme(m, usl, index);
  }

  public static Pair<Morpheme, Integer> parse(String input) throws ParseException {
    String output = parseSingle(input);
    assert(output != null && output.length() > 0);
    
    IEMLStringAttribute usl = new IEMLStringAttribute(output);
    HashMap<String, IEMLUnit> map = new HashMap<String, IEMLUnit>();
    map.put("type", new IEMLStringAttribute(TYPE_NAME));
    map.put("content", usl);
    map.put("index", usl);
    return new Pair<Morpheme, Integer>(new Morpheme(map, usl, usl), usl.getValue().length());
  }

  private static String parseSingle(String input) throws ParseException {
    for (int layer = 6; layer >= 0; layer--) {
      try {
        return parseSingleRec(input, layer);
      } catch (ParseException e) {}
    }
    throw new ParseException("Could not read a valid morpheme.");
  }

  private static String parseSingleRec(String input, int layer) throws ParseException {
    if (layer < 0 || layer > 6)
      throw new RuntimeException("Invalid parameter layer: " + layer);

    String expr = null;
    if (layer == 0) {
      Matcher m = LAYER_0.matcher(input);
      boolean matching = m.matches();
      if (matching && input.length() > m.group(1).length() && input.charAt(m.group(1).length()) == LAYER_CHAR[layer])
        expr = m.group(1);
      else
        throw new ParseException("Could not read a morpheme of layer 0.");
    }
    else if (layer == 1) {
      Matcher m = LAYER_1.matcher(input);
      if (m.matches())
        expr = m.group(1);
    }

    if (expr == null) { // try to read a compound morpheme if no base morpheme was read already
      expr = parseSingleRec(input, layer-1);
      try {
        expr += parseSingleRec(input.substring(expr.length()), layer-1);
        expr += parseSingleRec(input.substring(expr.length()), layer-1);
      } catch (ParseException e) {}
    }

    if (input.length() <= expr.length() || input.charAt(expr.length()) != LAYER_CHAR[layer])
      throw new ParseException("Could not read a morpheme of layer " + layer);
    else
      expr += input.charAt(expr.length());  // adding the postfix char for the recognized layer
    
    while (input.length() > expr.length() && input.charAt(expr.length()) == '+') {  // trying to read an infix paradigmatic list 
      expr += '+';
      expr += parseSingleRec(input.substring(expr.length()), layer);
    }
    
    return expr;
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
}
