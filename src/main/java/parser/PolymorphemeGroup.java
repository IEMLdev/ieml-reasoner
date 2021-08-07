package parser;

import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.tuple.Tuple;
import util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolymorphemeGroup extends IEMLTuple {
  private static final long serialVersionUID = 2248479360540166171L;
  private static final Pattern OPEN_PATTERN = Pattern.compile("(\\s*m(\\d+)\\().*");
  private static final Pattern CLOSE_PATTERN = Pattern.compile("(\\)).*");
  
  private final int multiplicity;
  private final MorphemeSet morphemes;

  private PolymorphemeGroup(Map<?, ? extends IEMLUnit> m, MorphemeSet morphemes, int multiplicity) {
    super(m);
    this.multiplicity = multiplicity;
    this.morphemes = morphemes;
  }
  
  public static Pair<PolymorphemeGroup, Integer> parse(String input) throws ParseException {
    int offset = 0;
    Matcher matcher = OPEN_PATTERN.matcher(input);
    if (!matcher.matches())
      throw new ParseException(PolymorphemeGroup.class, offset, input);
    
    offset += matcher.group(1).length();
    int multiplicity = Integer.parseInt(matcher.group(2));
    
    Pair<MorphemeSet, Integer> result = MorphemeSet.parse(input.substring(offset));
    offset += result.second;
    matcher = CLOSE_PATTERN.matcher(input.substring(offset));
    if (!matcher.matches())
      throw new ParseException(PolymorphemeGroup.class, offset, input);
    offset += matcher.group(1).length();

    MorphemeSet morphemes = result.first;
    HashMap<Object, IEMLUnit> map = new HashMap<>();
    map.put("multiplicity", new IEMLNumberAttribute(multiplicity));
    map.put("morphemes", morphemes);
    
    return new Pair<>(new PolymorphemeGroup(map, morphemes, multiplicity), offset);
  }

  public static PolymorphemeGroup reBuild(Tuple<?> t) throws IncompatibleSolutionException {
    try {
      final IEMLNumberAttribute multiplicity = (IEMLNumberAttribute) t.get("multiplicity");
      final MorphemeSet morphemes = MorphemeSet.reFactory((ImmutableSet<Morpheme>) t.get("morphemes"));

      HashMap<Object, IEMLUnit> map = new HashMap<>();
      map.put("multiplicity", multiplicity);
      map.put("morphemes", morphemes);
      
      return new PolymorphemeGroup(map, morphemes, multiplicity.intValue());
    } catch (ClassCastException e) {
      throw new IncompatibleSolutionException(e);
    }
  }

  public MorphemeSet getMorphemes() {
    return this.morphemes;
  }

  public int getMultiplicity() {
    return this.multiplicity;
  }
}
