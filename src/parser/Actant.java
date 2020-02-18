package parser;

import java.util.HashMap;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Actant extends SyntagmaticFunction {

  private static final long serialVersionUID = 6496934876365452689L;
  public static final String typeName = "DependantQualitySyntagmaticFunction";

  private final Lexeme actor;
  private final Actant dependant;
  private final Quality independant;

  private Actant(HashMap<String, IEMLUnit> m, Lexeme actor, Actant dependant, Quality independant) {
    super(m);
    this.actor = actor;
    this.dependant = dependant;
    this.independant = independant;
  }

  public static Actant factory(JSONObject obj) throws JSONStructureException, StyleException {
    String type_str = obj.getString("type");
    if (!type_str.contentEquals(typeName))
      throw new JSONStructureException();

    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));

    final Actant dependant;
    if (obj.isNull("dependant"))
      dependant = null;
    else
      dependant = Actant.factory(obj.getJSONObject("dependant"));

    final Quality independant;
    if (obj.isNull("independant"))
      independant = null;
    else
      independant = Quality.factory(obj.getJSONObject("independant"));

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", type);
    m.put("actor", actor);
    m.put("dependant", dependant);
    m.put("independant", independant);
    return new Actant(m, actor, dependant, independant);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) throws MissingTranslationException {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    if (this.dependant != null)
      m.put("dependant", this.dependant.mixedTranslation(lang, depth-1, dictionary));
    if (this.independant != null)
      m.put("independant", this.independant.mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }
}
