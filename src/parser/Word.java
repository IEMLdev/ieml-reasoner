package parser;

import java.util.HashMap;
import org.json.JSONObject;

import io.github.vletard.analogy.sequence.Sequence;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Word extends IEMLTuple {

  private static final long serialVersionUID = 8351186437917149613L;
  public static final String typeName = "word";

  private final String usl;
  private final Role role;
  public final SyntagmaticFunction syntagmaticFunction;

  /**
   * Private Word constructor available to the public factory method.
   * Role and SyntagmaticFunction are stored in both the Tuple and in fields to keep track of their actual type.
   * @param tupleInit Map to provide to the Tuple constructor.
   * @param r typed link to this word's role.
   * @param sf typed link to this word's syntagmatic function.
   * @param usl USL expression extracted for this word.
   */
  private Word(HashMap<String, IEMLUnit> tupleInit, Role r, SyntagmaticFunction sf, String usl) {
    super(tupleInit);
    this.role = r;
    this.syntagmaticFunction = sf;
    this.usl = usl;
  }

  public static Word reFactory(Tuple<?> t) throws IncompatibleSolutionException, StyleException {
    try {
      IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      Role role = Role.reFactory((Sequence<?>) t.get("role"));
      SyntagmaticFunction sf = SyntagmaticFunction.reFactory((Tuple<?>) t.get("syntagmatic_function"));
      
      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("role", role);
      m.put("syntagmatic_function", sf);
      return new Word(m, role, sf, "[" + sf.getPseudoUSL(role.toList()) + "]");
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Word factory(JSONObject obj) throws JSONStructureException, StyleException {
    String type_str = obj.getString("type");
    if (!type_str.contentEquals(typeName))
      throw new JSONStructureException();

    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final Role role = new Role(obj.getJSONArray("role"));
    final SyntagmaticFunction sf = SyntagmaticFunction.factory(obj.getJSONObject("syntagmatic_function"));
    final String usl = obj.getString("ieml");

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", type);
    m.put("role", role);
    m.put("syntagmatic_function", sf);
    return new Word(m, role, sf, usl);
  }

  public String getUsl() {
    return this.usl;
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    if (depth <= 0) {
      try {
        m.put("translations", dictionary.getFromUSL(this.usl).get(lang));
        return new Tuple<Object>(m);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    m.put("role", this.role);
    m.put("syntagmatic_function", this.syntagmaticFunction.mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }
}
