package parser;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.SubTupleRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import org.json.JSONObject;
import reasoner.Dictionary;
import util.Pair;

import java.util.*;

public class FlexionSet extends IEMLTuple {
  private static final long serialVersionUID = 6890558580471932997L;
  
  private static final Map<Object, SubtypeRebuilder<?, ?>> BUILDER_MAP;
  static {
    BUILDER_MAP = Map.of("constant", MorphemeSet.BUILDER, "groups",
        (SubtypeRebuilder<ImmutableSet<PolymorphemeGroup>, IEMLSet<PolymorphemeGroup>>) object -> new IEMLSet<>(object.asSet()));
  }
  
  public static final SubTupleRebuilder<IEMLUnit, FlexionSet> BUILDER = new SubTupleRebuilder<>(BUILDER_MAP) {

    @Override
    public FlexionSet rebuild(Tuple<IEMLUnit> object) {
      try {
        return reBuild(object);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException(e);
      }
    }
  };

  private final MorphemeSet constant;
  private final IEMLSet<PolymorphemeGroup> groups;

  private FlexionSet(HashMap<Object, IEMLUnit> m, MorphemeSet constant, IEMLSet<PolymorphemeGroup> groups) { // TODO add verification for the subset of morphemes contained
    super(m);
    this.constant = constant;
    this.groups = groups;
  }

  private static boolean checkStyle(JSONObject obj) throws JSONStructureException {
    String type = obj.getString("type");
    if (type.contentEquals("polymorpheme"))
      return true;
    else if (type.contentEquals("morpheme")) {  // irregular value, should be uniformized
      return (obj.isNull("constant") || obj.getJSONArray("constant").length() == 1) && obj.getJSONArray("groups").isEmpty();
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
      constant = result.first;
      offset += result.second;
    } catch (ParseException e) {
      constant = new MorphemeSet();
    }

    final HashSet<PolymorphemeGroup> g = new HashSet<>();
    try {
      while (true) {
        Pair<PolymorphemeGroup, Integer> result = PolymorphemeGroup.parse(input.substring(offset));
        g.add(result.first);
        offset += result.second;
      }
    } catch (ParseException e ) {}

    IEMLSet<PolymorphemeGroup> groups = new IEMLSet<>(g);
    HashMap<Object, IEMLUnit> map = new HashMap<>();
    map.put("constant", constant);
    map.put("groups", groups);

    return new Pair<>(new FlexionSet(map, constant, groups), offset);
  }

  public static FlexionSet reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final MorphemeSet constant = MorphemeSet.reFactory((ImmutableSet<Morpheme>) t.get("constant"));
      final IEMLSet<PolymorphemeGroup> groups;

      {
        HashSet<PolymorphemeGroup> group_set = new HashSet<>();
        for (Tuple<IEMLUnit> group_tuple: (ImmutableSet<Tuple<IEMLUnit>>) t.get("groups"))
          group_set.add(PolymorphemeGroup.reBuild(group_tuple));
        groups = new IEMLSet<>(group_set);
      }
      
      HashMap<Object, IEMLUnit> map = new HashMap<>();
      map.put("constant", constant);
      map.put("groups", groups);
      
      return new FlexionSet(map, constant, groups);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public String getPseudoUSL() {
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
    return usl;
  }

  public static FlexionSet factory(JSONObject obj) {
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
    HashMap<Object, Object> map = new HashMap<>();
    
    HashSet<Object> constant = new HashSet<>();
    for (Morpheme m: this.constant)
      constant.add(m.mixedTranslation(lang, depth-1, dictionary));

    ArrayList<HashSet<Object>> groups = new ArrayList<>();
    for (PolymorphemeGroup g: this.groups) {
      HashSet<Object> group = new HashSet<>();
      for (Morpheme m: g.getMorphemes())
        group.add(m.mixedTranslation(lang, depth-1, dictionary));
      groups.add(group);
    }

    map.put("constant", new ImmutableSet<>(constant));
    for (int i = 0; i < groups.size(); i++)
      map.put(i, new ImmutableSet<>(groups.get(i)));
    return new Tuple<>(map);
  }
}
