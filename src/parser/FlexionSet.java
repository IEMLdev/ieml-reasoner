package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONObject;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.SubTupleRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class FlexionSet extends IEMLTuple {
  private static final long serialVersionUID = 6890558580471932997L;
  
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
  
  public static final SubTupleRebuilder<IEMLUnit, FlexionSet> BUILDER = new SubTupleRebuilder<IEMLUnit, FlexionSet>(BUILDER_MAP) {

    @Override
    public FlexionSet rebuild(Tuple<IEMLUnit> object) {
      try {
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };

  private final String usl;
  private final MorphemeSet constant;
  private final IEMLSet<PolymorphemeGroup> groups;

  private FlexionSet(HashMap<Object, IEMLUnit> m, MorphemeSet constant, IEMLSet<PolymorphemeGroup> groups, String usl) { // TODO add verification for the subset of morphemes contained
    super(m);
    this.usl = usl;
    this.constant = constant;
    this.groups = groups;
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

  public static Pair<FlexionSet, Integer> parse(String input) throws ParseException {
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
      throw new ParseException(FlexionSet.class, offset);

    IEMLSet<PolymorphemeGroup> groups = new IEMLSet<PolymorphemeGroup>(g);
    HashMap<Object, IEMLUnit> map = new HashMap<Object, IEMLUnit>();
    map.put("constant", constant);
    map.put("groups", groups);

    return new Pair<FlexionSet, Integer>(new FlexionSet(map, constant, groups, input.substring(0, offset)), offset);
  }

  public static FlexionSet reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final MorphemeSet constant = MorphemeSet.reFactory((ImmutableSet<Morpheme>) t.get("constant"));
      final IEMLSet<PolymorphemeGroup> groups;

      {
        HashSet<PolymorphemeGroup> group_set = new HashSet<PolymorphemeGroup>();
        for (Tuple<IEMLUnit> group_tuple: (ImmutableSet<Tuple<IEMLUnit>>) t.get("groups"))
          group_set.add(PolymorphemeGroup.reBuild(group_tuple));
        groups = new IEMLSet<PolymorphemeGroup>(group_set);
      }
      
      HashMap<Object, IEMLUnit> map = new HashMap<Object, IEMLUnit>();
      map.put("constant", constant);
      map.put("groups", groups);

      String usl = "";
      for (Morpheme morpheme: constant) {
        if (!usl.contentEquals(""))
          usl += " ";
        usl += morpheme.getUSL();
      }
      
      for (PolymorphemeGroup group: groups) {
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
      
      return new FlexionSet(map, constant, groups, usl);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public String getPseudoUSL() {
    return this.usl;
  }

  public static FlexionSet factory(JSONObject obj) throws StyleException, JSONStructureException {
    throw new UnsupportedOperationException();
//    if (!checkStyle(obj))
//      throw new StyleException();
//
//    final String usl = obj.getString("ieml");
//    HashSet<Morpheme> s = new HashSet<Morpheme>();
//
//    if (obj.getString("type").contentEquals("morpheme") && obj.isNull("constant"))
//      s.add(Morpheme.factory(obj));  // style exception
//    else {
//      final JSONArray constant = obj.getJSONArray("constant");
//      for (int i = 0; i < constant.length(); i++) {
//        Morpheme flexion = Morpheme.factory(constant.getJSONObject(i));
//        if (!s.add(flexion))
//          throw new StyleException("Duplicate flexion in actor.pm_flexion.constant array. Duplicate is: " + flexion);
//      }
//      final JSONArray groups = obj.getJSONArray("groups");
//      for (int i = 0; i < groups.length(); i++) {
//        JSONArray group = groups.getJSONArray(i);
//        for (int j = 0; j < group.length(); j++) {
//          Morpheme paradigmaticFlexion = Morpheme.factory(group.getJSONObject(j));
//          if (! s.add(paradigmaticFlexion))
//            throw new StyleException("Duplicate flexion in actor.pm_flexion.groups[" + i + "] array. Duplicate is: " + paradigmaticFlexion);
//        }
//      }
//    }
//
//    final IEMLSet<Morpheme> morphemes = new IEMLSet<Morpheme>(s);
//    final HashMap<Object, IEMLUnit> m = new HashMap<Object, IEMLUnit>();
//    m.put("morphemes", morphemes);
//    return new FlexionSet(m, morphemes, usl);
  }

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
