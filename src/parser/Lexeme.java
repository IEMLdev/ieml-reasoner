package parser;

import java.util.HashMap;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Lexeme extends IEMLTuple {

  private static final long serialVersionUID = -2259223808535886545L;
  public static final String typeName = "lexeme";

  private final String usl;
  private final Polymorpheme pm_content;
  private final FlexionSet pm_flexion;

  private Lexeme(HashMap<String, IEMLUnit> m, Polymorpheme content, FlexionSet flexions, String usl) {
    super(m);
    this.usl = usl;
    this.pm_content = content;
    this.pm_flexion = flexions;
  }
  
  public static Lexeme reFactory(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      assert(type.getValue().contentEquals(typeName));

      final Polymorpheme content = Polymorpheme.reFactory((Tuple<?>) t.get("content"));
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

  public String getUSL() {
    String flexionsUSL = this.pm_flexion.getPseudoUSL();
    String contentUSL = this.pm_content.getUSL();
    if (contentUSL.length() == 0)
      return "(" + flexionsUSL + ")";
    else
      return "(" + flexionsUSL + ")(" + contentUSL + ")";
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
