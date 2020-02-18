package parser;

import java.util.HashMap;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Quality extends SyntagmaticFunction {

  private static final long serialVersionUID = -135803884486184079L;
  public static final String typeName = "IndependantQualitySyntagmaticFunction";

  private final Lexeme actor;

  private Quality(HashMap<String, IEMLUnit> m, Lexeme actor) {
    super(m);
    this.actor = actor;
  }

  public static Quality factory(JSONObject obj) throws StyleException {
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", type);
    m.put("actor", actor);
    return new Quality(m, actor);
  };

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) throws MissingTranslationException {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    return new Tuple<Object>(m);
  }

}
