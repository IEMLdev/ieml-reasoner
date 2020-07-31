package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public class Process extends SyntagmaticFunction {
  private static final long serialVersionUID = -4257752045085044328L;
  public static final IEMLStringAttribute TYPE_NAME = new IEMLStringAttribute("ProcessSyntagmaticFunction");
  public static final String TYPE_ROLE_NAME = "process";
  public static final List<String> TYPE_ROLE_PER_VALENCE = Arrays.asList(new String[] {"E:S:.", "E:T:.", "E:B:."});
  public static final List<String> FUNCTION_NAMES = Arrays.asList(new String[] {"initiator", "interactant", "recipient", "time", "location", "intention", "manner", "cause"});
  public static final List<String> FUNCTION_USLS = Arrays.asList(new String[] {"E:.n.-", "E:.d.-", "E:.k.-", "E:.t.-", "E:.l.-", "E:.m.-", "E:.f.-", "E:.s.-"});

  private final IEMLNumberAttribute valence;
  private final Lexeme root; // actor
  private final IEMLTuple syntagmae;

  private Process(Map<String, IEMLUnit> m, Lexeme root, IEMLTuple syntagmae, IEMLNumberAttribute valence) {
    super(m, new IEMLStringAttribute(TYPE_NAME));
    this.root = root;
    this.syntagmae = syntagmae;
    this.valence = valence;
  }

  public static Pair<Process, Integer> parse(WordRole role, String input) throws ParseException, StyleException {
    return parse(role, input, 0, Collections.emptyList());
  }

  static Pair<Process, Integer> parse(WordRole role, String input, int offset, List<Morpheme> pathPrefix) throws ParseException, StyleException {
    Matcher matcher = ROLE_MARKER_PATTERN.matcher(input);
    final boolean isRole;
    if (!matcher.matches())
      isRole = false;
    else {
      isRole = true;
      offset += matcher.group(1).length();
    }
    
    offset += ParseUtils.consumeBlanks(input.substring(offset));

    Pair<List<Morpheme>, Integer> pathParse = parsePath(input, offset);
    LinkedList<Morpheme> typePath = new LinkedList<Morpheme>(pathParse.getFirst());
    offset = pathParse.getSecond();

    offset += ParseUtils.consumeBlanks(input.substring(offset));

    if (typePath.size() - pathPrefix.size() != 1 || !typePath.subList(0, pathPrefix.size()).equals(pathPrefix))
      throw new ParseException(SyntagmaticFunction.class, 0, input);

    IEMLNumberAttribute valence = new IEMLNumberAttribute(TYPE_ROLE_PER_VALENCE.indexOf(typePath.getLast().getUSL()));
    if (valence.intValue() < 0)
      throw new ParseException(Process.class, offset - typePath.getLast().getUSL().length(), input);

    if (isRole)
      role.set(typePath);

    Pair<Lexeme, Integer> lexemeParse = Lexeme.parse(input.substring(offset));
    Lexeme root = lexemeParse.getFirst();
    offset += lexemeParse.getSecond();

    Pair<IEMLTuple, Integer> syntagmaeParse = parseSyntagmae(role, input, offset, pathPrefix);
    IEMLTuple syntagmae = syntagmaeParse.getFirst();
    offset = syntagmaeParse.getSecond();

    HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
    m.put("root", root);
    m.put("syntagmae", syntagmae);
    m.put("valence", valence);

    return new Pair<Process, Integer>(new Process(m, root, syntagmae, new IEMLNumberAttribute(valence)), offset);
  }

  private static Pair<IEMLTuple, Integer> parseSyntagmae(WordRole role, String input, int offset, List<Morpheme> pathPrefix) throws ParseException, StyleException {
    HashMap<String, Actant> m = new HashMap<String, Actant>();

    try {
      while (true) {
        int tmp_offset = offset + ParseUtils.consumeBlanks(input.substring(offset));
        Matcher matcher = SYNTAGMATIC_FUNCTION_SEPARATOR_PATTERN.matcher(input.substring(tmp_offset));
        if (!matcher.matches())
          break;
        tmp_offset += matcher.group(1).length();
        tmp_offset += ParseUtils.consumeBlanks(input.substring(tmp_offset));
        
        matcher = SyntagmaticFunction.ROLE_MARKER_PATTERN.matcher(input.substring(tmp_offset));
        final boolean isRole;
        if (!matcher.matches())
          isRole = false;
        else {
          isRole = true;
          tmp_offset += matcher.group(1).length();
        }
        tmp_offset += ParseUtils.consumeBlanks(input.substring(tmp_offset));

        Pair<List<Morpheme>, Integer> pathParse = SyntagmaticFunction.parsePath(input, tmp_offset);
        LinkedList<Morpheme> typePath = new LinkedList<Morpheme>(pathParse.getFirst());
        tmp_offset = pathParse.getSecond();
        tmp_offset += ParseUtils.consumeBlanks(input.substring(tmp_offset));

        if (typePath.size() - pathPrefix.size() != 1 || !typePath.subList(0, pathPrefix.size()).equals(pathPrefix))
          throw new ParseException(SyntagmaticFunction.class, 0, input);

        int functionIndex = FUNCTION_USLS.indexOf(typePath.getLast().getUSL());
        if (functionIndex < 0)
          throw new ParseException(Process.class, tmp_offset - typePath.getLast().getUSL().length(), input);
        String functionName = FUNCTION_NAMES.get(functionIndex);

        if (isRole)
          role.set(typePath);

        Pair<Lexeme, Integer> lexemeParse = Lexeme.parse(input.substring(tmp_offset));
        Lexeme actor = lexemeParse.getFirst();
        tmp_offset += lexemeParse.getSecond();

        Actant dependant = null;
        Quality independant = null;

        try {
          while (true) {
            int tmp_offset2 = tmp_offset + ParseUtils.consumeBlanks(input.substring(tmp_offset));
            matcher = SYNTAGMATIC_FUNCTION_SEPARATOR_PATTERN.matcher(input.substring(tmp_offset2));
            if (!matcher.matches())
              break;
            tmp_offset2 += matcher.group(1).length();
            tmp_offset2 += ParseUtils.consumeBlanks(input.substring(tmp_offset2));
            
            try {
              Pair<Actant, Integer> actantParse = Actant.parse(role, input, tmp_offset2, typePath);
              if (dependant != null)
                throw new ParseException(Actant.class, tmp_offset2, input);

              dependant = actantParse.getFirst();
              tmp_offset = actantParse.getSecond();
            } catch (ParseException e) {
              Pair<Quality, Integer> qualityParse = Quality.parse(role, input, tmp_offset2, typePath);
              if (independant != null)
                throw new ParseException(Actant.class, tmp_offset2, input);

              independant = qualityParse.getFirst();
              tmp_offset = qualityParse.getSecond();
            }
          }
        } catch (ParseException e) {}

        HashMap<String, IEMLUnit> m2 = new HashMap<String, IEMLUnit>();
        m2.put("actor", actor);
        m2.put("dependant", dependant);
        m2.put("independant", independant);

        m.put(functionName, new Actant(m2, actor, dependant, independant));
        offset = tmp_offset;
      }
    } catch (ParseException e) {}

    return new Pair<IEMLTuple, Integer>(new IEMLTuple(m), offset);
  }

  public static Process processRebuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      if (!((IEMLStringAttribute) t.get("type")).contentEquals(TYPE_NAME))
        throw new IncompatibleSolutionException("Incorrect stored type.");

      final Lexeme root = Lexeme.reBuild((Tuple<?>) t.get("root"));
      final IEMLNumberAttribute valence = (IEMLNumberAttribute) t.get("valence");
      final IEMLTuple syntagmae;
      
      {
        Tuple<IEMLUnit> syntagmae_tmp = (Tuple<IEMLUnit>) t.get("syntagmae");
        HashMap<Object, Actant> actants = new HashMap<Object, Actant>();
        for (Object key: syntagmae_tmp.keySet()) {
          if (syntagmae_tmp.get(key) == null)
            continue;
          if (!FUNCTION_NAMES.contains(key))
            throw new IncompatibleSolutionException("Unknown syntagmatic function name " + key);
          actants.put(key, Actant.actantRebuild((Tuple<?>) syntagmae_tmp.get(key)));
        }

        syntagmae = new IEMLTuple(actants);
      }

      HashMap<String, IEMLUnit> m = new HashMap<String, IEMLUnit>();
      m.put("valence", valence);
      m.put("root", root);
      m.put("syntagmae", syntagmae);
      return new Process(m, root, syntagmae, valence);
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    HashMap<String, Object> m = new HashMap<String, Object>();
    m.put("root", this.root.mixedTranslation(lang, depth-1, dictionary));
    for (String functionName: FUNCTION_NAMES)
      if (this.syntagmae.containsKey(functionName))
        m.put(functionName, ((Actant) this.syntagmae.get(functionName)).mixedTranslation(lang, depth-1, dictionary));
    return new Tuple<Object>(m);
  }

  @Override
  boolean checkStyle(List<Morpheme> rolePath) {
    if (rolePath.get(0).getUSL().contentEquals(TYPE_ROLE_PER_VALENCE.get(this.valence.intValue())))
      return rolePath.size() == 1;
    else if (!FUNCTION_USLS.contains(rolePath.get(0).getUSL()))
      return false;
    else {
      String name = FUNCTION_NAMES.get(FUNCTION_USLS.indexOf(rolePath.get(0).getUSL()));
      if (!this.syntagmae.containsKey(name))
        return false;
      else
        return ((Actant) this.syntagmae.get(name)).checkStyle(rolePath);
    }
  }

  @Override
  String generateUSL(List<List<Morpheme>> roleList, String pathPrefix) {
    String usl = "";
    String rootTypeRole = TYPE_ROLE_PER_VALENCE.get(this.valence.intValue());
    for (List<Morpheme> role: roleList) {
      assert(role.size() > 0);
      if (role.get(0).getUSL().contentEquals(rootTypeRole)) {
        assert(role.size() == 1);
        usl += ROLE_MARKER + " ";
      }
    }

    assert(pathPrefix.contentEquals(""));

    usl += rootTypeRole + " ";
    usl += this.root.getUSL();

    for (int i = 0; i < FUNCTION_NAMES.size(); i++) {
      String functionName = FUNCTION_NAMES.get(i);
      
      if (this.syntagmae.containsKey(functionName)) {
        String functionUSL = FUNCTION_USLS.get(i);
        usl += " " + SYNTAGMATIC_FUNCTION_SEPARATOR + " ";
        
        List<List<Morpheme>> nextRoleList = new ArrayList<List<Morpheme>>();
        Actant syntagma = (Actant) this.syntagmae.get(functionName);
        
        for (List<Morpheme> role: roleList) {
          assert(role.size() > 0);
          if (role.get(0).getUSL().contentEquals(functionUSL)) {
            if (role.size() == 1)
              usl += ROLE_MARKER + " ";
            else
              nextRoleList.add(role.subList(1, role.size()));
          }
        }

        usl += functionUSL + " ";
        usl += syntagma.getActor().getUSL();

        if (syntagma.get(Actant.TYPE_ROLE_NAME) != null) {
          usl += " " + SYNTAGMATIC_FUNCTION_SEPARATOR + " ";
          usl += syntagma.getDependant().generateUSL(nextRoleList, functionUSL);
        }

        if (syntagma.getIndependant() != null) {
          usl += " " + SYNTAGMATIC_FUNCTION_SEPARATOR + " ";
          usl += syntagma.getIndependant().generateUSL(nextRoleList, functionUSL);
        }
      }
    }
    return usl;
  }
}
