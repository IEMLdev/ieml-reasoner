package parser;

import java.util.HashMap;
import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Morpheme extends IEMLTuple {

  private static final long serialVersionUID = 8740154662446704759L;
  public static final String typeName = "morpheme";

  private final String usl;

  private Morpheme(HashMap<String, IEMLUnit> m, String usl) {
    super(m);
    this.usl = usl;
  }

  public static Morpheme factory(JSONObject obj){
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final String usl = obj.getString("ieml");
    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", new IEMLStringAttribute(type_str));
    m.put("content", new IEMLStringAttribute(usl));
    return new Morpheme(m, usl);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<Object, Object> m = new HashMap<Object, Object>();
    try {
      m.put("translations", dictionary.getFromUSL(this.usl).get(lang));
    } catch (MissingTranslationException e) {
      m.put("usl", this.usl);  // in case no translation exist for this morpheme in the dictionary, just output the usl
      
    }
    return new Tuple<Object>(m);
  }
}