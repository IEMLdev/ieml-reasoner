package parser;

import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import util.Pair;

import java.util.*;

public class MorphemeSet extends IEMLSet<Morpheme> {
  private static final long serialVersionUID = -7540455236524511294L;
  
  private final SortedSet<Morpheme> sorted;

  public static final SubtypeRebuilder<ImmutableSet<Morpheme>, MorphemeSet> BUILDER =
        object -> new MorphemeSet(object.asSet());
  
  public MorphemeSet() {
    super(Collections.emptySet());
    this.sorted = Collections.emptySortedSet();
  }
  
  public MorphemeSet(Set<Morpheme> s) {
    super(s);
    this.sorted = Collections.unmodifiableSortedSet(new TreeSet<>(s));
  }

  public static Pair<MorphemeSet, Integer> parse(String input) throws ParseException {
    int offset = 0;
    HashSet<Morpheme> morphemes = new HashSet<>();
    try {
      while (true) {
        Pair<Morpheme, Integer> result = Morpheme.parse(input.substring(offset));
        if (result.first.isParadigm())
          throw new ParseException(MorphemeSet.class, offset, input);
        offset += result.second;
        offset += ParseUtils.consumeBlanks(input.substring(offset));
        morphemes.add(result.first);
      }
    } catch (ParseException e) {
      if (offset == 0)
        throw e;
      else
        return new Pair<>(new MorphemeSet(morphemes), offset);
    }
  }

  public static MorphemeSet reFactory(ImmutableSet<Morpheme> s) {
    return new MorphemeSet(s.asSet());
  }
  
  @Override
  public Iterator<Morpheme> iterator() {
    return this.sorted.iterator();
  }
}
