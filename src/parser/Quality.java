package parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Quality extends SyntagmaticFunction {
  private static final long serialVersionUID = -135803884486184079L;
  public static final IEMLStringAttribute TYPE_NAME = new IEMLStringAttribute("IndependantQualitySyntagmaticFunction");
  public static final String TYPE_ROLE = "E:U:.";
  public static final String TYPE_ROLE_NAME = "independant";

  private final Lexeme actor;

  private Quality(HashMap<String, IEMLUnit> m, Lexeme actor) {
    super(m, TYPE_NAME);
    this.actor = actor;
  }
  
  public static Pair<Quality, Integer> parse(WordRole role, String input) throws ParseException, StyleException {
    return Quality.parse(role, input, 0, Collections.emptyList());
  }

  static Pair<Quality, Integer> parse(WordRole role, String input, int offset, List<Morpheme> pathPrefix) throws ParseException, StyleException {
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

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("actor", actor);

    return new Pair<Quality, Integer>(new Quality(m, actor), offset);
  }

  public static Quality qualityRebuild(Tuple<?> t) throws IncompatibleSolutionException {
    if (!((IEMLStringAttribute) t.get("type")).contentEquals(TYPE_NAME))
      throw new IncompatibleSolutionException("Incorrect stored type.");
    try {
      final Lexeme actor = Lexeme.reBuild((Tuple<?>) t.get("actor"));
      
      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("actor", actor);
      return new Quality(m, actor);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("actor", this.actor.mixedTranslation(lang, depth-1, dictionary));
    m.put("type", this.get("type"));
    return new Tuple<Object>(m);
  }

  @Override
  boolean checkStyle(List<Morpheme> rolePath) {
    return rolePath.size() == 1 && rolePath.get(0).getUSL().contentEquals(TYPE_ROLE);
  }

  @Override
  String generateUSL(List<List<Morpheme>> roleList, String pathPrefix) {
    String usl = "";
    assert(roleList.size() <= 1);
    for (List<Morpheme> role: roleList) {
      assert(role.get(0).getUSL().contentEquals(TYPE_ROLE) && role.size() == 1);
      usl += ROLE_MARKER + " ";
    }
    
    if (pathPrefix.length() > 0)
      usl += pathPrefix + " ";
    usl += TYPE_ROLE + " " + this.actor.getUSL();
    
    return usl;
  }
}
