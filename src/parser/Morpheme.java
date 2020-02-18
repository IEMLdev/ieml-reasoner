package parser;

import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONObject;

import io.github.vletard.analogy.set.ImmutableSet;
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

  public ImmutableSet<String> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashSet<String> translations = new HashSet<String>();
    try {
      for (String tr: dictionary.getFromUSL(this.usl).get(lang)) {
        translations.add(tr);
      }
    } catch (MissingTranslationException e) {
      translations.add(this.usl); // in case no translation exist for this word in the dictionary, just output the usl
    }
    return new ImmutableSet<String>(translations);
  }
}
