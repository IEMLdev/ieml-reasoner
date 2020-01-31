package parser;

import java.util.HashMap;

import org.json.JSONObject;

public class Quality extends SyntagmaticFunction {

  private static HashMap<String, Object> extractJSON(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("IndependantQualitySyntagmaticFunction"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);

    m.put("actor", new Lexeme(obj.getJSONObject("actor")));
    return m;
  };

  public Quality(JSONObject obj) {
    super(extractJSON(obj));
  }

}
