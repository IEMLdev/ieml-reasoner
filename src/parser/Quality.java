package parser;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Quality extends SyntagmaticFunction {

  private static final long serialVersionUID = -135803884486184079L;
  public static final String typeName = "IndependantQualitySyntagmaticFunction";
  public static final String typeRole = "E:U:.";
  public static final String typeRoleName = "independant";

  private final Lexeme actor;

  private Quality(HashMap<String, IEMLUnit> m, Lexeme actor, IEMLStringAttribute type) {
    super(m, type);
    this.actor = actor;
  }

  public static Quality qualityRefactory(Tuple<?> t, IEMLStringAttribute type) throws IncompatibleSolutionException {
    try {
      assert(type.getValue().contentEquals(typeName));

      final Lexeme actor = Lexeme.reBuild((Tuple<?>) t.get("actor"));

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("actor", actor);
      return new Quality(m, actor, type);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Quality factory(JSONObject obj) throws StyleException, JSONStructureException {
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("actor", actor);
    return new Quality(m, actor, new IEMLStringAttribute(type_str));
  };

  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    return new Tuple<Object>(m);
  }

  @Override
  protected String buildPseudoUSL(List<IEMLStringAttribute> wordRole, String rolePrefix) throws StyleException {
    if (wordRole.size() > 0)
      throw new StyleException("The word role specifies a non existing node.");
    return this.actor.getUSL();
  }

  @Override
  public String getPseudoUSL(List<IEMLStringAttribute> wordRole) throws StyleException {
    if (wordRole.size() != 1 || !wordRole.get(0).contentEquals(typeRoleName))
      throw new StyleException("The word role specifies a non existing node.");
    return "! " + typeRole + " " + this.buildPseudoUSL(wordRole.subList(1, wordRole.size()), typeRole);
  }
}
