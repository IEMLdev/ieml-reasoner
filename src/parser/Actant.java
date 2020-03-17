package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Actant extends SyntagmaticFunction {

  private static final long serialVersionUID = 6496934876365452689L;
  public static final String typeName = "DependantQualitySyntagmaticFunction";
  public static final String typeRole = "E:A:.";
  public static final String typeRoleName = "dependant";

  private final Lexeme actor;
  private final Actant dependant;
  private final Quality independant;

  private Actant(HashMap<String, IEMLUnit> m, Lexeme actor, Actant dependant, Quality independant, IEMLStringAttribute type) {
    super(m, type);
    this.actor = actor;
    this.dependant = dependant;
    this.independant = independant;
  }

  public static Actant actantRefactory(Tuple<?> t, IEMLStringAttribute type) throws IncompatibleSolutionException {
    try {
      assert(type.getValue().contentEquals(typeName));

      final Lexeme actor = Lexeme.reFactory((Tuple<?>) t.get("actor"));

      final Actant dependant;
      if (t.get("dependant") == null)
        dependant = null;
      else {
        Actant depActant = (Actant) t.get("dependant");
        dependant = Actant.actantRefactory(depActant, (IEMLStringAttribute) depActant.get("type"));
      }

      final Quality independant;
      if (t.get("independant") == null)
        independant = null;
      else {
        Quality indepActant = (Quality) t.get("independant");
        independant = Quality.qualityRefactory(indepActant, (IEMLStringAttribute) indepActant.get("type"));
      }

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("actor", actor);
      m.put("dependant", dependant);
      m.put("independant", independant);
      return new Actant(m, actor, dependant, independant, type);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Actant factory(JSONObject obj) throws JSONStructureException, StyleException {
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));

    final Actant dependant;
    if (obj.isNull("dependant"))
      dependant = null;
    else {
      JSONObject depObj = obj.getJSONObject("dependant");
      dependant = Actant.factory(depObj);
    }

    final Quality independant;
    if (obj.isNull("independant"))
      independant = null;
    else {
      JSONObject indepObj = obj.getJSONObject("independant");
      independant = Quality.factory(indepObj);
    }

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("actor", actor);
    m.put("dependant", dependant);
    m.put("independant", independant);
    return new Actant(m, actor, dependant, independant, new IEMLStringAttribute(type_str));
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    if (this.dependant != null)
      m.put("dependant", this.dependant.mixedTranslation(lang, depth-1, dictionary));
    if (this.independant != null)
      m.put("independant", this.independant.mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }

  @Override
  protected String buildPseudoUSL(List<IEMLStringAttribute> wordRole, String rolePrefix) throws StyleException {
    boolean markedRole = false;
    String usl = this.actor.getUSL();

    if (this.dependant != null) {
      String newRolePrefix = rolePrefix + " " + Actant.typeRole;
      usl += " > ";
      if (wordRole.size() > 0 && wordRole.get(0).getValue().contentEquals(Actant.typeRoleName)) {
        markedRole = true;
        if (wordRole.size() == 1)
          usl += "! ";
        usl += newRolePrefix + " " + this.dependant.buildPseudoUSL(wordRole.subList(1, wordRole.size()), newRolePrefix);
      }
      else
        usl += newRolePrefix + " " + this.dependant.buildPseudoUSL(Collections.emptyList(), newRolePrefix);
    }
    if (this.independant != null) {
      String newRolePrefix = rolePrefix + " " + Quality.typeRole;
      usl += " > ";
      if (wordRole.size() > 0 && wordRole.get(0).getValue().contentEquals(Quality.typeRoleName)) {
        markedRole = true;
        if (wordRole.size() == 1)
          usl += "! ";
        usl += newRolePrefix + " " + this.independant.buildPseudoUSL(wordRole.subList(1, wordRole.size()), newRolePrefix);
      }
      else
        usl += newRolePrefix + " " + this.independant.buildPseudoUSL(Collections.emptyList(), newRolePrefix);
    }
    if (wordRole.size() > 0 && !markedRole)
      throw new StyleException("The word role specifies a non existing node.");

    return usl;
  }

  @Override
  public String getPseudoUSL(List<IEMLStringAttribute> wordRole) throws StyleException {
    if (wordRole.size() < 1 || !wordRole.get(0).contentEquals(typeRoleName))
      throw new StyleException("The word role specifies a non existing node.");
    String usl = "";
    if (wordRole.size() == 1)
      usl += "! ";
    return usl + typeRole + " " + this.buildPseudoUSL(wordRole.subList(1, wordRole.size()), typeRole);
  }
}
