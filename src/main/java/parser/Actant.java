package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Actant extends SyntagmaticFunction {
  private static final long serialVersionUID = 6496934876365452689L;
  public static final IEMLStringAttribute TYPE_NAME = new IEMLStringAttribute("DependantQualitySyntagmaticFunction");
  private static final String TYPE_ROLE = "E:A:.";
  public static final String TYPE_ROLE_NAME = "dependant";

  private final Lexeme actor;
  private final Actant dependant;
  private final Quality independant;

  Actant(HashMap<String, IEMLUnit> m, Lexeme actor, Actant dependant, Quality independant) {
    super(m, TYPE_NAME);
    this.actor = actor;
    this.dependant = dependant;
    this.independant = independant;
  }

  public static Pair<Actant, Integer> parse(WordRole role, String input) throws ParseException, StyleException {
    return Actant.parse(role, input, 0, Collections.emptyList());
  }

  static Pair<Actant, Integer> parse(WordRole role, String input, int offset, List<Morpheme> pathPrefix) throws ParseException, StyleException {
    Matcher matcher = ROLE_MARKER_PATTERN.matcher(input.substring(offset));
    final boolean isRole;
    if (!matcher.matches())
      isRole = false;
    else {
      isRole = true;
      offset += matcher.group(1).length();
    }
    
    offset += ParseUtils.consumeBlanks(input.substring(offset));

    Pair<List<Morpheme>, Integer> pathParse = SyntagmaticFunction.parsePath(input, offset);
    LinkedList<Morpheme> typePath = new LinkedList<Morpheme>(pathParse.getFirst());
    offset = pathParse.getSecond();
    
    offset += ParseUtils.consumeBlanks(input.substring(offset));

    if (typePath.size() - pathPrefix.size() != 1 || !typePath.subList(0, pathPrefix.size()).equals(pathPrefix))
      throw new ParseException(SyntagmaticFunction.class, 0, input);

    if (!typePath.getLast().getUSL().contentEquals(TYPE_ROLE))
      throw new ParseException(Actant.class, offset - typePath.getLast().getUSL().length(), input);

    if (isRole)
      role.set(typePath);
    
    Pair<Lexeme, Integer> lexemeParse = Lexeme.parse(input.substring(offset));
    Lexeme actor = lexemeParse.getFirst();
    offset += lexemeParse.getSecond();

    Actant dependant = null;
    Quality independant = null;

    try {
      while (true) {
        int tmp_offset = offset + ParseUtils.consumeBlanks(input.substring(offset));
        matcher = SYNTAGMATIC_FUNCTION_SEPARATOR_PATTERN.matcher(input.substring(tmp_offset));
        if (!matcher.matches())
          break;
        tmp_offset += matcher.group(1).length();

        tmp_offset += ParseUtils.consumeBlanks(input.substring(tmp_offset));
        
        try {
          Pair<Actant, Integer> actantParse = Actant.parse(role, input, tmp_offset, typePath);
          if (dependant != null)
            throw new ParseException(Actant.class, tmp_offset, input);

          dependant = actantParse.getFirst();
          offset = actantParse.getSecond();
        } catch (ParseException e) {
          Pair<Quality, Integer> qualityParse = Quality.parse(role, input, tmp_offset, typePath);
          if (independant != null)
            throw new ParseException(Actant.class, tmp_offset, input);

          independant = qualityParse.getFirst();
          offset = qualityParse.getSecond();
        }
      }
    } catch (ParseException e) {}

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("actor", actor);
    m.put(Actant.TYPE_ROLE_NAME, dependant);
    m.put(Quality.TYPE_ROLE_NAME, independant);

    return new Pair<Actant, Integer>(new Actant(m, actor, dependant, independant), offset);
  }

  public static Actant actantRebuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      if (!((IEMLStringAttribute) t.get("type")).contentEquals(TYPE_NAME))
        throw new IncompatibleSolutionException("Incorrect stored type.");

      final Lexeme actor = Lexeme.reBuild((Tuple<?>) t.get("actor"));

      final Actant dependant;
      if (t.get(Actant.TYPE_ROLE_NAME) == null)
        dependant = null;
      else {
        Actant depActant = (Actant) t.get(Actant.TYPE_ROLE_NAME);
        dependant = Actant.actantRebuild(depActant);
      }

      final Quality independant;
      if (t.get(Quality.TYPE_ROLE_NAME) == null)
        independant = null;
      else {
        Quality indepActant = (Quality) t.get(Quality.TYPE_ROLE_NAME);
        independant = Quality.qualityRebuild(indepActant);
      }

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("actor", actor);
      m.put(Actant.TYPE_ROLE_NAME, dependant);
      m.put(Quality.TYPE_ROLE_NAME, independant);
      return new Actant(m, actor, dependant, independant);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

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
  boolean checkStyle(List<Morpheme> rolePath) {
    if (!rolePath.get(0).getUSL().contentEquals(TYPE_ROLE))
      return false;
    else if (rolePath.size() == 1)
      return true;
    else {
      boolean result = false;
      List<Morpheme> subList = rolePath.subList(1, rolePath.size());
      if (this.dependant != null)
        result = result || this.dependant.checkStyle(subList);
      if (this.independant != null)
        result = result || this.independant.checkStyle(subList);
      return result;
    }
  }

  @Override
  String generateUSL(List<List<Morpheme>> roleList, String pathPrefix) {
    List<List<Morpheme>> nextRoleList = new ArrayList<List<Morpheme>>();
    String usl = "";
    
    for (List<Morpheme> role: roleList) {
      assert(role.size() > 0);
      if (role.get(0).getUSL().contentEquals(TYPE_ROLE)) {
        if (role.size() == 1)
          usl += ROLE_MARKER + " ";
        else
          nextRoleList.add(role.subList(1, role.size()));
      }
    }
    
    String nextPathPrefix = "";
    if (pathPrefix.length() > 0)
      nextPathPrefix += pathPrefix + " ";
    nextPathPrefix += TYPE_ROLE;
    usl += nextPathPrefix + " " + this.actor.getUSL();

    if (this.dependant != null) {
      usl += " " + SYNTAGMATIC_FUNCTION_SEPARATOR + " ";
      usl += this.dependant.generateUSL(nextRoleList, nextPathPrefix);
    }
    
    if (this.independant != null) {
      usl += " " + SYNTAGMATIC_FUNCTION_SEPARATOR + " ";
      usl += this.independant.generateUSL(nextRoleList, nextPathPrefix);
    }
    return usl;
  }

  public Lexeme getActor() {
    return this.actor;
  }

  public Actant getDependant() {
    return this.dependant;
  }
  
  public Quality getIndependant() {
    return this.independant;
  }
}
