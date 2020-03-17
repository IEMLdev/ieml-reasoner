package parser;

import java.util.HashMap;
import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Morpheme extends IEMLTuple implements Comparable<Morpheme> {

  private static final long serialVersionUID = 8740154662446704759L;
  public static final String typeName = "morpheme";

  private final IEMLStringAttribute usl;
  private final IEMLStringAttribute indexString;

  private Morpheme(HashMap<String, IEMLUnit> m, IEMLStringAttribute usl, IEMLStringAttribute indexString) {
    super(m);
    this.usl = usl;
    this.indexString = indexString;
  }

  public static Morpheme reFactory(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      assert(type.getValue().contentEquals(typeName));

      final IEMLStringAttribute usl = (IEMLStringAttribute) t.get("content");
      final IEMLStringAttribute index = (IEMLStringAttribute) t.get("index");
      
      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("type", type);
      m.put("content", usl);
      m.put("index", index);
      return new Morpheme(m, usl, index);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Morpheme factory(JSONObject obj){
    String type_str = obj.getString("type");
    assert(type_str.contentEquals(typeName));

    final IEMLStringAttribute usl = new IEMLStringAttribute(obj.getString("ieml"));
    final IEMLStringAttribute index = new IEMLStringAttribute(obj.getString("index"));
    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("type", new IEMLStringAttribute(type_str));
    m.put("content", usl);
    m.put("index", index);
    return new Morpheme(m, usl, index);
  }

  public String getUSL() {
    return this.usl.getValue();
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<Object, Object> m = new HashMap<Object, Object>();
    try {
      m.put("translations", dictionary.getFromUSL(this.usl.getValue()).get(lang));
    } catch (MissingTranslationException e) {
      m.put("usl", this.usl);  // in case no translation exist for this morpheme in the dictionary, just output the usl
      
    }
    return new Tuple<Object>(m);
  }

  /**
   * Defines a dictionary order on morphemes using their USL.
   */
  @Override
  public int compareTo(Morpheme m) {
    return this.indexString.getValue().compareTo(m.indexString.getValue());
  }
}
