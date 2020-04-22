package parser;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;

public abstract class SyntagmaticFunction extends IEMLTuple {
  // note, it may not be mandatory to separate SyntagmaticFunction from Process Actant and Quality
  private static final long serialVersionUID = 2105644212743558550L;

  private static Map<String, IEMLUnit> initType(Map<String, IEMLUnit> m, IEMLStringAttribute type){
    m.put("type", type);
    return m;
  }

  protected abstract String buildPseudoUSL(List<IEMLStringAttribute> wordRole, String rolePrefix) throws StyleException;
  public abstract String getPseudoUSL(List<IEMLStringAttribute> wordRole) throws StyleException;
  
  public SyntagmaticFunction(Map<String, IEMLUnit> m, IEMLStringAttribute type) {
    super(SyntagmaticFunction.initType(m, type));
  }

  public static SyntagmaticFunction reBuild(Tuple<?> sf) throws IncompatibleSolutionException {
    try {
      IEMLStringAttribute type = (IEMLStringAttribute) sf.get("type");
      switch (type.getValue()) {
      case "ProcessSyntagmaticFunction":
        return Process.processRefactory(sf, type);
      case "DependantQualitySyntagmaticFunction":
        return Actant.actantRefactory(sf, type);
      case "IndependantQualitySyntagmaticFunction":
        return Quality.qualityRefactory(sf, type);
      default:
        throw new IncompatibleSolutionException(new Exception("Unsupported syntagmatic function: " + type));
      }
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
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
