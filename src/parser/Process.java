package parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Process extends SyntagmaticFunction {

  private static final long serialVersionUID = -4257752045085044328L;
  public static final String typeName = "ProcessSyntagmaticFunction";
  public static final String typeRoleName = "process";
  public static final List<String> typeRolePerValence = Arrays.asList(new String[] {"E:S:.", "E:T:.", "E:B:."});
  public static final List<String> actantNames = Arrays.asList(new String[] {"initiator", "interactant", "recipient", "time", "location", "intention", "manner", "cause"});
  public static final List<String> actantRoles = Arrays.asList(new String[] {"E:.n.-", "E:.d.-", "E:.k.-", "E:.t.-", "E:.l.-", "E:.m.-", "E:.f.-", "E:.s.-"});

  private final IEMLNumberAttribute valence;
  private final Lexeme actor;
  private final Map<String, Actant> actants;

  private Process(HashMap<String, IEMLUnit> m, Lexeme actor, Map<String, Actant> actants, IEMLStringAttribute type, IEMLNumberAttribute valence) {
    super(m, type);
    this.actor = actor;
    this.actants = actants;
    this.valence = valence;
  }

  public static Process processRefactory(Tuple<?> t, IEMLStringAttribute type) throws IncompatibleSolutionException {
    try {
      assert(type.getValue().equals(typeName));

      final Lexeme actor = Lexeme.reFactory((Tuple<?>) t.get("actor"));
      final IEMLNumberAttribute valence = (IEMLNumberAttribute) t.get("valence");
      final HashMap<String, Actant> actants = new HashMap<String, Actant>();

      for (String actant: actantNames) {
        Tuple<?> reloaded = (Tuple<?>) t.get(actant);
        if (reloaded != null)
          actants.put(actant, Actant.actantRefactory(reloaded, (IEMLStringAttribute) reloaded.get("type")));
      }    

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("valence", valence);
      m.put("actor", actor);
      for (Entry<String, Actant> e: actants.entrySet())
        m.put(e.getKey(), e.getValue());
      return new Process(m, actor, Collections.unmodifiableMap(actants), type, valence);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Process factory(JSONObject obj) throws StyleException, JSONStructureException {
    String type_str = obj.getString("type");
    assert(type_str.equals(typeName));

    final IEMLNumberAttribute valence = new IEMLNumberAttribute(obj.getInt("valence"));
    final Lexeme actor = Lexeme.factory(obj.getJSONObject("actor"));
    final HashMap<String, Actant> actants = new HashMap<String, Actant>();

    for (String actant: actantNames) {
      if (!obj.isNull(actant))
        actants.put(actant, Actant.factory(obj.getJSONObject(actant)));
    }

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("valence", valence);
    m.put("actor", Lexeme.factory(obj.getJSONObject("actor")));
    for (Entry<String, Actant> e: actants.entrySet()) {
      m.put(e.getKey(), e.getValue());
    }
    return new Process(m, actor, Collections.unmodifiableMap(actants), new IEMLStringAttribute(type_str), valence);
  }

  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    for (Entry<String, Actant> e: this.actants.entrySet())
      m.put(e.getKey(), e.getValue().mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }

  @Override
  protected String buildPseudoUSL(List<IEMLStringAttribute> wordRole, String rolePrefix) throws StyleException {
    boolean markedRole = false;
    String usl = this.actor.getUSL();

    for (int i = 0; i < actantNames.size(); i++) {
      String a = actantNames.get(i);
      String newRolePrefix = actantRoles.get(i);
      if (this.actants.containsKey(a)) {
        usl += " > ";
        if (wordRole.size() > 0 && wordRole.get(0).getValue().contentEquals(a)) {
          markedRole = true;
          if (wordRole.size() == 1)
            usl += "! ";
          usl += newRolePrefix + " " + this.actants.get(a).buildPseudoUSL(wordRole.subList(1, wordRole.size()), newRolePrefix);
        }
        else
          usl += newRolePrefix + " " + this.actants.get(a).buildPseudoUSL(Collections.emptyList(), newRolePrefix);
      }
    }
    if (wordRole.size() > 0 && !markedRole)
      throw new StyleException("The word role specifies a non existing node.");

    return usl;
  }

  @Override
  public String getPseudoUSL(List<IEMLStringAttribute> wordRole) throws StyleException {
    if (wordRole.size() < 1 || (!wordRole.get(0).contentEquals(typeRoleName) && !actantNames.contains(wordRole.get(0).getValue())))
      throw new StyleException("The word role specifies a non existing node.");
    final String typeRole = typeRolePerValence.get(this.valence.intValue()-1);
    
    if (wordRole.get(0).contentEquals(typeRoleName))
      return "! " + typeRole + " " + this.buildPseudoUSL(wordRole.subList(1, wordRole.size()), typeRole);
    else
      return typeRole + " " + this.buildPseudoUSL(wordRole, typeRole);
  }
}
