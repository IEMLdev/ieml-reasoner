package parser;

import java.util.HashMap;

import org.json.JSONObject;

public class Actant extends SyntagmaticFunction {

  private static HashMap<String, Object> extractJSON(JSONObject obj) throws InvalidJSONStructureException {
    String type = obj.getString("type");
    if (!type.equals("DependantQualitySyntagmaticFunction"))
      throw new InvalidJSONStructureException();

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);

    m.put("actor", new Lexeme(obj.getJSONObject("actor")));

    if (!obj.isNull("dependant"))
      m.put("dependant", new Actant(obj.getJSONObject("dependant")));

    if (!obj.isNull("independant"))
      m.put("independant", new Quality(obj.getJSONObject("independant")));
    return m;
  }
  
  public Actant(JSONObject obj) throws InvalidJSONStructureException {
    super(extractJSON(obj));
  }

}
