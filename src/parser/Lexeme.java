package parser;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import util.Tuple;

public class Lexeme extends Tuple<Object> {

  private static HashMap<String, Object> extractJSON(JSONObject obj){
    String type = obj.getString("type");
    assert(type.equals("lexeme"));

    Tuple<Object> flexions;
    {
      HashMap<Object, Object> m = new HashMap<Object, Object>();
      JSONArray arr = obj.getJSONObject("pm_flexion").getJSONArray("constant");
      for (int i = 0; i < arr.length(); i++) {
        Object flexion = new Morpheme(arr.getJSONObject(i)); // les flexions ne peuvent appartenir qu'à une liste de sous-ensembles de morphèmes donnés
                                                              // si deux flexions ou plus du même sous-ensemble sont présentes, elles sont dans un groupe plutôt que
                                                              // dans les constantes et le polymorphème devient un paradigme
        if (m.put(flexion, true) != null)
          throw new RuntimeException("Duplicate flexion in actor.pm_flexion.constant array. Duplicate is: " + flexion);
      }
      flexions = new Tuple<Object>(m);
    }

    Tuple<Object> groups; // XXX replace by Sequence<Object> ?
    {
      HashMap<Integer, Object> m = new HashMap<Integer, Object>();
      JSONArray arr = obj.getJSONObject("pm_content").getJSONArray("groups");
      for (int i = 0; i < arr.length(); i++)
        m.put(i, new Polymorpheme(arr.getJSONArray(i)));
      groups = new Tuple<Object>(m);
    }

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("flexions", flexions);
    m.put("constant", new Polymorpheme(obj.getJSONObject("pm_content").getJSONArray("constant")));
    m.put("groups", groups);
    return m;
  }

  public Lexeme(JSONObject obj) {
    super(extractJSON(obj));
  }

}
