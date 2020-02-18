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

  private static HashSet<Morpheme> extractMorphemeSet(JSONArray arr){
    HashSet<Morpheme> s = new HashSet<Morpheme>();
    for (int i = 0; i < arr.length(); i++) {
      boolean absent = s.add(Morpheme.factory(arr.getJSONObject(i)));
      assert(absent);
    }
    return s;
  }

  private static boolean checkStyle(JSONObject obj) {
    String type = obj.getString("type");
    if (type.contentEquals("polymorpheme"))
      return true;
    else if (type.contentEquals("morpheme")) {  // irregular value, should be uniformized
      int total = obj.getJSONArray("constant").length();
      for (int i = 0; i < obj.getJSONArray("groups").length(); i++)
        total += obj.getJSONArray("groups").getJSONArray(i).length();
      
      return total == 1;
    }
    else
      return false;
  }

  public static Polymorpheme factory(JSONObject obj) throws StyleException {
    if (!checkStyle(obj))
      throw new StyleException();
        
    String type_str = obj.getString("type");

    final String usl = obj.getString("ieml");
    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
    final HashSet<Morpheme> constant = extractMorphemeSet(obj.getJSONArray("constant"));
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
    if (depth <= 0) {
      try {
        HashMap<Object, Object> translations = new HashMap<Object, Object>();
        int key = 1;
        for (String tr: dictionary.getFromUSL(this.usl).get(lang))
          translations.put(key++, tr);
        return new Tuple<Object>(translations);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    HashSet<Object> constant = new HashSet<Object>();
    for (Morpheme morpheme: this.constant)
      constant.add(morpheme.mixedTranslation(lang, depth-1, dictionary));

    ArrayList<HashSet<Object>> groups = new ArrayList<HashSet<Object>>();
    for (HashSet<Morpheme> s: this.groups) {
      HashSet<Object> group = new HashSet<Object>();
      for (Morpheme morpheme: s)
        group.add(morpheme.mixedTranslation(lang, depth-1, dictionary));
    }

    HashMap<Object, Object> m = new HashMap<Object, Object>();
    m.put("constant", new ImmutableSet<Object>(constant));
    for (int i = 0; i < groups.size(); i++)
      m.put(i, new ImmutableSet<Object>(groups.get(i)));
    return new Tuple<Object>(m);
  }

}
