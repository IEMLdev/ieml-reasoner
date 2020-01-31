package parser;

import java.util.HashMap;

import org.json.JSONObject;

import util.Tuple;

public class Word extends Tuple<Object> {
  
  private static HashMap<String, Object> extractJSON(JSONObject obj) throws InvalidJSONStructureException {
    String type = obj.getString("type");
    if (!type.equals("word"))
      throw new InvalidJSONStructureException();
    
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("role", new Role(obj.getJSONArray("role")));  // TODO move the role marker to the actual node(s) of the role
    m.put("syntagmatic_function", SyntagmaticFunction.newSyntagmaticFunction(obj.getJSONObject("syntagmatic_function")));
    return m;
  }
  
  public Word(JSONObject obj) throws InvalidJSONStructureException {
    super(extractJSON(obj));
  }

}
