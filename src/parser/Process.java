package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Process extends SyntagmaticFunction {

  private static final long serialVersionUID = -4257752045085044328L;
  public static final String typeName = "ProcessSyntagmaticFunction";

  private final Lexeme actor;
  private final Map<String, Actant> actants;

  private Process(HashMap<String, IEMLUnit> m, Lexeme actor, Map<String, Actant> actants) {
    super(m);
    this.actor = actor;
    this.actants = actants;
  }

  public static Process factory(JSONObject obj) throws StyleException, JSONStructureException {
    String type_str = obj.getString("type");
    assert(type_str.equals(typeName));

    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));
    final HashMap<String, Actant> actants = new HashMap<String, Actant>();

    for (String actant: new String[] {"initiator", "interactant", "recipient", "time", "location", "intention", "manner", "cause"}) {
      if (!obj.isNull(actant))
        actants.put(actant, Actant.factory(obj.getJSONObject(actant)));
    }    

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", new IEMLStringAttribute(type_str));
    m.put("valence", new IEMLNumberAttribute(obj.getInt("valence")));
    m.put("actor", Lexeme.factory(obj.getJSONObject("actor")));
    for (Entry<String, Actant> e: actants.entrySet()) {
      m.put(e.getKey(), e.getValue());
    }
    return new Process(m, actor, Collections.unmodifiableMap(actants));
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) throws MissingTranslationException {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    for (Entry<String, Actant> e: this.actants.entrySet())
      m.put(e.getKey(), e.getValue().mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }

}
