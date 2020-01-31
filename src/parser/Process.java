package parser;

import java.util.HashMap;

import org.json.JSONObject;

public class Process extends SyntagmaticFunction {

  private static HashMap<String, Object> extractJSON(JSONObject obj) throws InvalidJSONStructureException {
    String type = obj.getString("type");
    assert(type.equals("ProcessSyntagmaticFunction"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("valence", obj.getInt("valence"));

    m.put("actor", new Lexeme(obj.getJSONObject("actor")));

    for (String actant: new String[] {"initiator", "interactant", "recipient", "time", "location", "intention", "manner", "cause"}) {
      if (!obj.isNull(actant))
        m.put(actant, new Actant(obj.getJSONObject(actant)));
    }
    return m;
  }

  public Process(JSONObject obj) throws InvalidJSONStructureException {
    super(extractJSON(obj));
  }

}
