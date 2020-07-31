package parser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.set.ImmutableSet;
import util.Pair;

public class MorphemeSet extends IEMLSet<Morpheme> {
  private static final long serialVersionUID = -7540455236524511294L;
  
  private final SortedSet<Morpheme> sorted;

  public static final SubtypeRebuilder<ImmutableSet<Morpheme>, MorphemeSet> BUILDER = new SubtypeRebuilder<ImmutableSet<Morpheme>, MorphemeSet>() {
    @Override
    public MorphemeSet rebuild(ImmutableSet<Morpheme> object) {
      return new MorphemeSet(object.asSet());
    }
  };
  
  public MorphemeSet() {
    super(Collections.emptySet());
    this.sorted = Collections.emptySortedSet();
  }
  
  public MorphemeSet(Set<Morpheme> s) {
    super(s);
    this.sorted = Collections.unmodifiableSortedSet(new TreeSet<Morpheme>(s));
  }

  public static Pair<MorphemeSet, Integer> parse(String input) throws ParseException {
    int offset = 0;
    HashSet<Morpheme> morphemes = new HashSet<Morpheme>();
    try {
      while (true) {
        Pair<Morpheme, Integer> result = Morpheme.parse(input.substring(offset));
        if (result.getFirst().isParadigm())
          throw new ParseException(MorphemeSet.class, offset, input);
        offset += result.getSecond();
        offset += ParseUtils.consumeBlanks(input.substring(offset));
        morphemes.add(result.getFirst());
      }
    } catch (ParseException e) {
      if (offset == 0)
        throw e;
      else
        return new Pair<MorphemeSet, Integer>(new MorphemeSet(morphemes), offset);
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
