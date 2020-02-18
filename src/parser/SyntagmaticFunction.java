package parser;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class SyntagmaticFunction extends IEMLTuple {

  private static final long serialVersionUID = 2105644212743558550L;
  
  public SyntagmaticFunction(Map<String, IEMLUnit> m) {
    super(m);
  }

  public static SyntagmaticFunction factory(JSONObject obj) throws JSONStructureException, JSONException, StyleException {
    String type = obj.getString("type");

    switch (type) {
    case "ProcessSyntagmaticFunction":
      return Process.factory(obj);
    case "DependantQualitySyntagmaticFunction":
      return Actant.factory(obj);
    case "IndependantQualitySyntagmaticFunction":
      return Quality.factory(obj);
    default:
      throw new RuntimeException("Unsupported syntagmatic function: " + type);
    }
  }
}
