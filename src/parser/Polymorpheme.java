package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Polymorpheme extends Writable {

  private static final long serialVersionUID = -3319789347573674986L;
  public static final String typeName = "polymorpheme";
  
  private static final Map<Object, SubtypeRebuilder<?, ?>> BUILDER_MAP;
  static {
    Map<Object, SubtypeRebuilder<?, ? extends IEMLUnit>> map = new HashMap<Object, SubtypeRebuilder<?, ? extends IEMLUnit>>();
    map.put("constant", MorphemeSet.BUILDER);
    map.put("groups", new SubtypeRebuilder<ImmutableSet<PolymorphemeGroup>, IEMLSet<PolymorphemeGroup>>() {
      @Override
      public IEMLSet<PolymorphemeGroup> rebuild(ImmutableSet<PolymorphemeGroup> object) {
        return new IEMLSet<PolymorphemeGroup>(object.asSet());
      }
    });
    BUILDER_MAP = Collections.unmodifiableMap(map);
  }
  
  public static final WritableBuilder<Polymorpheme> BUILDER = new WritableBuilder<Polymorpheme>(BUILDER_MAP) {

    @Override
    public Polymorpheme parse(String usl) throws ParseException {
      Pair<Polymorpheme, Integer> parse = Polymorpheme.parse(usl);
      if (parse.getSecond() != usl.length())
        throw new ParseException(Polymorpheme.class, parse.getSecond(), usl);
      return parse.getFirst();
    }

    @Override
    public Polymorpheme rebuild(Tuple<IEMLUnit> object) {
      try {
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };

  private final MorphemeSet constant;
  private final IEMLSet<PolymorphemeGroup> groups;

  private Polymorpheme(HashMap<Object, IEMLUnit> m, MorphemeSet constant, IEMLSet<PolymorphemeGroup> groups) {
    super(m);
    this.constant = constant;
    this.groups = groups;
  }

  @Override
  public String getUSL() {
    String usl = "";
    for (Morpheme morpheme: this.constant) {
      if (!usl.contentEquals(""))
        usl += " ";
      usl += morpheme.getUSL();
    }
    
    for (PolymorphemeGroup group: this.groups) {
      if (!usl.contentEquals(""))
        usl += " ";
      usl += "m" + group.getMultiplicity() + "(";
      
      String groupUSL = "";
      for (Morpheme morpheme: group.getMorphemes()) {
        if (!groupUSL.contentEquals(""))
          groupUSL += " ";
        groupUSL += morpheme.getUSL();
      }
      usl += groupUSL + ")";
    }
    return usl;
  }

//  private static HashSet<Morpheme> extractMorphemeSet(JSONArray arr){
//    HashSet<Morpheme> s = new HashSet<Morpheme>();
//    for (int i = 0; i < arr.length(); i++) {
//      boolean absent = s.add(Morpheme.factory(arr.getJSONObject(i)));
//      assert(absent);
//    }
//    return s;
//  }

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

  public static Polymorpheme reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLStringAttribute type = (IEMLStringAttribute) t.get("type");
      final MorphemeSet constant = MorphemeSet.reFactory((ImmutableSet<Morpheme>) t.get("constant"));
      final IEMLSet<PolymorphemeGroup> groups;

      {
        HashSet<PolymorphemeGroup> group_set = new HashSet<PolymorphemeGroup>();
        for (Tuple<IEMLUnit> group_tuple: (ImmutableSet<Tuple<IEMLUnit>>) t.get("groups"))
          group_set.add(PolymorphemeGroup.reBuild(group_tuple));
        groups = new IEMLSet<PolymorphemeGroup>(group_set);
      }
      
      HashMap<Object, IEMLUnit> map = new HashMap<Object, IEMLUnit>();
      map.put("type", type);
      map.put("constant", constant);
      map.put("groups", groups);
      
      return new Polymorpheme(map, constant, groups);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static Polymorpheme factory(JSONObject obj) {
    throw new UnsupportedOperationException();
//    if (!checkStyle(obj))
//      throw new StyleException();
//
//    String type_str = "polymorpheme";  // the type of a Polymorpheme is always polymorpheme
//
//    final String usl = obj.getString("ieml");
//    final IEMLStringAttribute type = new IEMLStringAttribute(type_str);
//    final HashSet<Morpheme> constant;
//    if (obj.getString("type").contentEquals("morpheme") && obj.isNull("constant")) {
//      // style exception
//      constant = new HashSet<Morpheme>();
//      constant.add(Morpheme.factory(obj));
//    }
//    else
//      constant = extractMorphemeSet(obj.getJSONArray("constant"));
//    final ArrayList<HashSet<Morpheme>> groups = new ArrayList<HashSet<Morpheme>>();
//
//    HashMap<Object, IEMLUnit> m = new HashMap<Object, IEMLUnit>();
//    m.put("type", type);
//    m.put("constant", new IEMLSet<Morpheme>(constant));
//
//    final JSONArray groups_arr = obj.getJSONArray("groups");
//    for (int i = 0; i < groups_arr.length(); i++) {
//      HashSet<Morpheme> set = extractMorphemeSet(groups_arr.getJSONArray(i));
//      groups.add(set);
//      m.put(i, new IEMLSet<Morpheme>(set));
//    }
//
//    return new Polymorpheme(m, constant, groups, usl);
  }

  public static Pair<Polymorpheme, Integer> parse(String input) throws ParseException {
    int offset = 0;
    MorphemeSet constant;
    try {
      Pair<MorphemeSet, Integer> result = MorphemeSet.parse(input);
      constant = result.getFirst();
      offset += result.getSecond();
    } catch (ParseException e) {
      constant = new MorphemeSet();
    }

    final HashSet<PolymorphemeGroup> g = new HashSet<PolymorphemeGroup>();
    try {
      while (true) {
        Pair<PolymorphemeGroup, Integer> result = PolymorphemeGroup.parse(input.substring(offset));
        g.add(result.getFirst());
        offset += result.getSecond();
      }
    } catch (ParseException e ) {};

    if (offset == 0)
      throw new ParseException(Polymorpheme.class, offset, input);

    IEMLSet<PolymorphemeGroup> groups = new IEMLSet<PolymorphemeGroup>(g);
    HashMap<Object, IEMLUnit> map = new HashMap<Object, IEMLUnit>();
    map.put("type", new IEMLStringAttribute(typeName));
    map.put("constant", constant);
    map.put("groups", groups);

    return new Pair<Polymorpheme, Integer>(new Polymorpheme(map, constant, groups), offset);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<Object, Object> map = new HashMap<Object, Object>();
    if (depth <= 0) {
      try {
        map.put("translations", dictionary.get(this).get(lang));
        return new Tuple<Object>(map);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary, the mixed translation continues deeper
      }
    }
    HashSet<Object> constant = new HashSet<Object>();
    for (Morpheme m: this.constant)
      constant.add(m.mixedTranslation(lang, depth-1, dictionary));

    ArrayList<HashSet<Object>> groups = new ArrayList<HashSet<Object>>();
    for (PolymorphemeGroup g: this.groups) {
      HashSet<Object> group = new HashSet<Object>();
      for (Morpheme m: g.getMorphemes())
        group.add(m.mixedTranslation(lang, depth-1, dictionary));
      groups.add(group);
    }

    map.put("constant", new ImmutableSet<Object>(constant));
    for (int i = 0; i < groups.size(); i++)
      map.put(i, new ImmutableSet<Object>(groups.get(i)));
    return new Tuple<Object>(map);
  }
}
