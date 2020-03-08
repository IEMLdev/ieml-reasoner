package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class Polymorpheme extends IEMLTuple {

  private static final long serialVersionUID = -3319789347573674986L;
  public static final String typeName = "polymorpheme";

  private final String usl;
  private final HashSet<Morpheme> constant;
  private final ArrayList<HashSet<Morpheme>> groups;

  private Polymorpheme(HashMap<Object, IEMLUnit> m, HashSet<Morpheme> constant, ArrayList<HashSet<Morpheme>> groups, String usl) {
    super(m);
    this.usl = usl;
    this.constant = constant;
    this.groups = groups;
  }

  private static HashSet<Morpheme> restoreMorphemeSet(ImmutableSet<?> immutableSet) throws IncompatibleSolutionException {
    HashSet<Morpheme> s = new HashSet<Morpheme>();
    for (Object m: (ImmutableSet<?>) immutableSet)
      s.add(Morpheme.reFactory((Tuple<?>) m));
    return s;
  }

  private static HashSet<Morpheme> extractMorphemeSet(JSONArray arr){
    HashSet<Morpheme> s = new HashSet<Morpheme>();
    for (int i = 0; i < arr.length(); i++) {
      boolean absent = s.add(Morpheme.factory(arr.getJSONObject(i)));
      assert(absent);
    }
    return s;
  }

  private static boolean checkStyle(JSONObject obj) throws JSONStructureException {
    String type = obj.getString("type");
    if (type.contentEquals("polymorpheme"))
      return true;
    else if (type.contentEquals("morpheme")) {  // irregular value, should be uniformized
      return (obj.isNull("constant") || obj.getJSONArray("constant").length() == 1) && obj.getJSONArray("groups").length() == 0;
//      int total = obj.getJSONArray("constant").length();
//      for (int i = 0; i < obj.getJSONArray("groups").length(); i++)
//        total += obj.getJSONArray("groups").getJSONArray(i).length();
//      
//      return total == 1;
    }
    else
      throw new JSONStructureException();
  }

  public static Polymorpheme reFactory(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      final HashSet<Morpheme> constant = restoreMorphemeSet((ImmutableSet<?>) t.get("constant"));

      HashMap<Object, IEMLUnit> m = new HashMap<Object, IEMLUnit>();
      m.put("type", type);
      m.put("constant", new IEMLSet<Morpheme>(constant));

      final ArrayList<HashSet<Morpheme>> groups = new ArrayList<HashSet<Morpheme>>();
      for (int i = 0; t.containsKey(i); i++) {
        HashSet<Morpheme> set = restoreMorphemeSet((ImmutableSet<?>) t.get(i));
        groups.add(set);
        m.put(i, new IEMLSet<Morpheme>(set));
      }

      return new Polymorpheme(m, constant, groups, null);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Polymorpheme factory(JSONObject obj) throws StyleException, JSONStructureException {
    if (!checkStyle(obj))
      throw new StyleException();
        
    String type_str = "polymorpheme";  // the type of a Polymorpheme is always polymorpheme

    final String usl = obj.getString("ieml");
    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final HashSet<Morpheme> constant;
    if (obj.getString("type").contentEquals("morpheme") && obj.isNull("constant")) {
      // style exception
      constant = new HashSet<Morpheme>();
      constant.add(Morpheme.factory(obj));
    }
    else
      constant = extractMorphemeSet(obj.getJSONArray("constant"));
    final ArrayList<HashSet<Morpheme>> groups = new ArrayList<HashSet<Morpheme>>();

    HashMap<Object, IEMLUnit> m = new HashMap<Object, IEMLUnit>();
    m.put("type", type);
    m.put("constant", new IEMLSet<Morpheme>(constant));

    final JSONArray groups_arr = obj.getJSONArray("groups");
    for (int i = 0; i < groups_arr.length(); i++) {
      HashSet<Morpheme> set = extractMorphemeSet(groups_arr.getJSONArray(i));
      groups.add(set);
      m.put(i, new IEMLSet<Morpheme>(set));
    }

    return new Polymorpheme(m, constant, groups, usl);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    if (depth <= 0) {
      try {
        map.put("translations", dictionary.getFromUSL(this.usl).get(lang));
        return new Tuple<Object>(map);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    HashSet<Object> constant = new HashSet<Object>();
    for (Morpheme m: this.constant)
      constant.add(m.mixedTranslation(lang, depth-1, dictionary));

    ArrayList<HashSet<Object>> groups = new ArrayList<HashSet<Object>>();
    for (HashSet<Morpheme> s: this.groups) {
      HashSet<Object> group = new HashSet<Object>();
      for (Morpheme m: s)
        group.add(m.mixedTranslation(lang, depth-1, dictionary));
    }

    map.put("constant", new ImmutableSet<Object>(constant));
    for (int i = 0; i < groups.size(); i++)
      map.put(i, new ImmutableSet<Object>(groups.get(i)));
    return new Tuple<Object>(map);
  }

}
