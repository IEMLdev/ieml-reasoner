package parser;

import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

import util.Sequence;
import util.Tuple;

public class JSONParser {

  public static Tuple<Object> parseJSON(JSONObject obj) {
    String type = obj.getString("type");

    switch (type) {
    case "word":
      return parseWord(obj);
    default:
      throw new RuntimeException("Unsupported node type: " + type);
    }
  }

  private static Tuple<Object> parseWord(JSONObject obj){
    String type = obj.getString("type");
    assert(type.equals("word"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("role", parseWordRole(obj.getJSONArray("role")));  // TODO move the role marker to the actual node(s) of the role
    m.put("syntagmatic_function", parseSyntagmaticFunction(obj.getJSONObject("syntagmatic_function")));

    return new Tuple<Object>(m);
  }

  private static Sequence<String> parseWordRole(JSONArray arr) {
    LinkedList<String> l = new LinkedList<String>();
    for (int i = 0; i < arr.length(); i++)
      l.add(arr.getString(i));
    return new Sequence<String>(l);
  }

  private static Tuple<Object> parseSyntagmaticFunction(JSONObject obj) {
    String type = obj.getString("type");

    switch (type) {
    case "ProcessSyntagmaticFunction":
      return parseProcess(obj);
    case "DependantQualitySyntagmaticFunction":
      return parseActant(obj);
    case "IndependantQualitySyntagmaticFunction":
      return parseQuality(obj);
    default:
      throw new RuntimeException("Unsupported syntagmatic function: " + type);
    }
  }

  private static Tuple<Object> parseProcess(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("ProcessSyntagmaticFunction"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("valence", obj.getInt("valence"));
    // XXX add here information about role ?

    m.put("actor", parseLexeme(obj.getJSONObject("actor")));

    for (String actant: new String[] {"initiator", "interactant", "recipient", "time", "location", "intention", "manner", "cause"}) {
      if (!obj.isNull(actant))
        m.put(actant, parseActant(obj.getJSONObject(actant)));
    }
    return new Tuple<Object>(m);
  }

  private static Tuple<Object> parseActant(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("DependantQualitySyntagmaticFunction"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    // XXX add here information about role ?

    m.put("actor", parseLexeme(obj.getJSONObject("actor")));

    if (!obj.isNull("dependant"))
      m.put("dependant", parseActant(obj.getJSONObject("dependant")));

    if (!obj.isNull("independant"))
      m.put("independant", parseQuality(obj.getJSONObject("independant")));
    return new Tuple<Object>(m);
  }

  private static Tuple<Object> parseQuality(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("IndependantQualitySyntagmaticFunction"));

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    // XXX add here information about role ?

    m.put("actor", parseLexeme(obj.getJSONObject("actor")));
    return new Tuple<Object>(m);
  }

  private static Tuple<Object> parseLexeme(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("lexeme"));

    Tuple<Object> flexions;
    {
      HashMap<Object, Object> m = new HashMap<Object, Object>();
      JSONArray arr = obj.getJSONObject("pm_flexion").getJSONArray("constant");
      for (int i = 0; i < arr.length(); i++) {
        Object flexion = parseMorpheme(arr.getJSONObject(i));
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
        m.put(i, parsePolymorpheme(arr.getJSONArray(i)));
      groups = new Tuple<Object>(m);
    }

    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("type", type);
    m.put("flexions", flexions);
    m.put("constant", parsePolymorpheme(obj.getJSONObject("pm_content").getJSONArray("constant")));
    m.put("groups", groups);
    return new Tuple<Object>(m);
  }

  private static Sequence<Object> parsePolymorpheme(JSONArray arr) {
    LinkedList<Object> l = new LinkedList<Object>();
    for (int i = 0; i < arr.length(); i++)
      l.add(parseMorpheme(arr.getJSONObject(i)));
    return new Sequence<Object>(l);
  }

  private static String parseMorpheme(JSONObject obj) {
    String type = obj.getString("type");
    assert(type.equals("morpheme"));
    
    return obj.getString("ieml");  // TODO replace by deeper structure
  }
}
