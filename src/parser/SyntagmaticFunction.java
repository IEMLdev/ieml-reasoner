package parser;

import java.util.Map;

import org.json.JSONObject;

import util.Tuple;

public abstract class SyntagmaticFunction extends Tuple<Object> {

  public SyntagmaticFunction(Map<String, Object> m) {
    super(m);

    // TODO Add here redundant information about role
  }

  public static SyntagmaticFunction newSyntagmaticFunction(JSONObject obj) throws InvalidJSONStructureException {
    String type = obj.getString("type");

    switch (type) {
    case "ProcessSyntagmaticFunction":
      return new Process(obj);
    case "DependantQualitySyntagmaticFunction":
      return new Actant(obj);
    case "IndependantQualitySyntagmaticFunction":
      return new Quality(obj);
    default:
      throw new RuntimeException("Unsupported syntagmatic function: " + type);
    }
  }

}
