package parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.vletard.analogy.RebuildException;
import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.tuple.SubTupleRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;
import util.Pair;

public abstract class SyntagmaticFunction extends IEMLTuple {
  // note, it may not be mandatory to separate SyntagmaticFunction from Process Actant and Quality
  public static final String SYNTAGMATIC_FUNCTION_SEPARATOR = ">";
  public static final String ROLE_MARKER = "!";
  static final Pattern SYNTAGMATIC_FUNCTION_SEPARATOR_PATTERN = Pattern.compile("(" + Pattern.quote(SYNTAGMATIC_FUNCTION_SEPARATOR) + ").*");
  static final Pattern ROLE_MARKER_PATTERN = Pattern.compile("(" + Pattern.quote(ROLE_MARKER) + ").*");
  private static final long serialVersionUID = 2105644212743558550L;
  
  private static final Map<Object, SubtypeRebuilder<?, ?>> BUILDER_MAP;
  static {
    BUILDER_MAP = new HashMap<Object, SubtypeRebuilder<?,?>>();
    BUILDER_MAP.put("root", Lexeme.BUILDER);
    BUILDER_MAP.put("actor", Lexeme.BUILDER);
    BUILDER_MAP.put(Actant.TYPE_NAME, SyntagmaticFunction.BUILDER);
    BUILDER_MAP.put(Quality.TYPE_NAME, SyntagmaticFunction.BUILDER);
    BUILDER_MAP.put("syntagmae", new SubTupleRebuilder<IEMLUnit, IEMLTuple>(SyntagmaticFunction.BUILDER) {
      @Override
      public IEMLTuple rebuild(Tuple<IEMLUnit> t) throws RebuildException {
        Map<Object, SyntagmaticFunction> m = new HashMap<Object, SyntagmaticFunction>();
        for (Object key: t.keySet())
          m.put(key, (SyntagmaticFunction) t.get(key));
        return new IEMLTuple(m);
      }
    });
  }
  
  public static final SubTupleRebuilder<IEMLUnit, SyntagmaticFunction> BUILDER = new SubTupleRebuilder<IEMLUnit, SyntagmaticFunction>(BUILDER_MAP) {
    @Override
    public SyntagmaticFunction rebuild(Tuple<IEMLUnit> t) throws RebuildException {
      try {
        return SyntagmaticFunction.reBuild(t);
      } catch (IncompatibleSolutionException e) {
        throw new RuntimeException("Unexpected exception.", e);
      }
    }
  };

  private static Map<String, IEMLUnit> initType(Map<String, IEMLUnit> m, IEMLStringAttribute type) {
    m.put("type", type);
    return m;
  }

  public SyntagmaticFunction(Map<String, IEMLUnit> m, IEMLStringAttribute type) {
    super(SyntagmaticFunction.initType(m, type));
  }

  public static SyntagmaticFunction reBuild(Tuple<?> sf) throws IncompatibleSolutionException {
    try {
      IEMLStringAttribute type = (IEMLStringAttribute) sf.get("type");
      if (type.contentEquals(Process.TYPE_NAME))
        return Process.processRebuild(sf);
      else if (type.contentEquals(Actant.TYPE_NAME))
        return Actant.actantRebuild(sf);
      else if (type.contentEquals(Quality.TYPE_NAME))
        return Quality.qualityRebuild(sf);
      else
        throw new IncompatibleSolutionException(new Exception("Unsupported syntagmatic function: " + type));
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public static SyntagmaticFunction factory(JSONObject obj) throws JSONStructureException, JSONException, StyleException {
    String type = obj.getString("type");

    switch (type) {
    case "ProcessSyntagmaticFunction":
      return Process.factory(obj);
    case "DependantQualitySyntagmaticFunction":
      return Actant.factory(obj);
    case "IndependantQualitySyntagmaticFunction":
      return Quality.factory(obj);
    default:
      throw new RuntimeException("Unsupported syntagmatic function: " + type);
    }
  }

  /**
   * Checks whether the rebuilt SyntagmaticFunction object is compatible with a role located
   * as specified by the given Morpheme list.
   * @param rolePath the path of the word role to be verified.
   * @return true if the role path points to an existing node.
   */
  abstract boolean checkStyle(List<Morpheme> rolePath);

  /**
   * Generates the USL corresponding to this syntagmatic function.
   * @param roleList list of role paths to express.
   * @return the generated USL.
   */
  public String generateUSL(List<List<Morpheme>> roleList) {
    return this.generateUSL(roleList, "");
  }
  abstract String generateUSL(List<List<Morpheme>> roleList, String pathPrefix);
  
  abstract Object mixedTranslation(String lang, int i, Dictionary dictionary);

  static Pair<List<Morpheme>, Integer> parsePath(String input, int offset) throws ParseException {
    try {
      List<Morpheme> path = new ArrayList<Morpheme>();
      Pair<Morpheme, Integer> morphemeParse = Morpheme.parse(input.substring(offset));
      path.add(morphemeParse.getFirst());
      offset += morphemeParse.getSecond();

      try {
        while (true) {
          int tmp_offset = offset + ParseUtils.consumeBlanks(input.substring(offset));
          
          morphemeParse = Morpheme.parse(input.substring(tmp_offset));
          path.add(morphemeParse.getFirst());
          offset = tmp_offset + morphemeParse.getSecond();
        }
      } catch (ParseException e) {}
      return new Pair<List<Morpheme>, Integer>(path, offset);

    } catch (ParseException e) {
      throw new ParseException(SyntagmaticFunction.class, offset, input);
    }
  }
}
