package ieml;

import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Solution;
import io.github.vletard.analogy.SubtypeRebuilder;
import io.github.vletard.analogy.sequence.Sequence;
import io.github.vletard.analogy.sequence.SequenceEquation;
import io.github.vletard.analogy.sequence.SequenceProportion;
import io.github.vletard.analogy.set.ImmutableSet;
import io.github.vletard.analogy.set.SimpleSetEquation;
import io.github.vletard.analogy.tuple.SubTupleRebuilder;
import io.github.vletard.analogy.tuple.Tuple;
import io.github.vletard.analogy.tuple.TupleEquation;
import io.github.vletard.analogy.util.CharacterSequence;
import io.github.vletard.analogy.util.InvalidParameterException;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Test extends TestCase {

    final CharacterSequence A = new CharacterSequence("baa");
    final CharacterSequence B = new CharacterSequence("aba");
    final CharacterSequence C = new CharacterSequence("aab");
    final CharacterSequence D = new CharacterSequence("aab");
    final SubtypeRebuilder<Sequence<Character>, CharacterSequence> charSeqRebuilder =
            CharacterSequence::new;

    public void test1() {

        assertTrue(new SequenceProportion<>(A, B, C, D).isValid());
    }

    public void test2() throws InvalidParameterException {
        SequenceEquation<Character, CharacterSequence> e = new SequenceEquation<>(A, B, C, charSeqRebuilder);

        {
            String equation = A + " : " + B + " :: " + C + " : ";
            boolean degreePrintedOut = false;
            for (Solution<CharacterSequence> s : e.nBestDegreeSolutions(1)) {
                if (!degreePrintedOut) {
                    System.out.println("Degree " + s.getDegree() + ":");
                    degreePrintedOut = true;
                }
                System.out.println(equation + s.getContent());
                System.out.println(s.getStraightRelation());
                System.out.println(s.getCrossedRelation());
            }
            System.out.println();
        }

        HashMap<String, Sequence<Character>> regularMap, freeMap;
        regularMap = new HashMap<>();
        freeMap = new HashMap<>();
        regularMap.put("regular", new CharacterSequence("AKCKE"));
        freeMap.put("free", new CharacterSequence("AKCKE"));
        Tuple<Sequence<Character>> tA = new Tuple<>(regularMap, freeMap);
        regularMap = new HashMap<>();
        freeMap = new HashMap<>();
        regularMap.put("regular", new CharacterSequence("BKCKF"));
        freeMap.put("free", new CharacterSequence("BKCKF"));
        Tuple<Sequence<Character>> tB = new Tuple<>(regularMap, freeMap);
        regularMap = new HashMap<>();
        freeMap = new HashMap<>();
        regularMap.put("regular", new CharacterSequence("AKDKE"));
        freeMap.put("free", new CharacterSequence("AKDKE"));
        Tuple<Sequence<Character>> tC = new Tuple<>(regularMap, freeMap);
        regularMap = new HashMap<>();
        freeMap = new HashMap<>();
        regularMap.put("regular", new CharacterSequence("BKDKF"));
        freeMap.put("free", new CharacterSequence("BKDKF"));
        Tuple<Sequence<Character>> tD = new Tuple<>(regularMap, freeMap);
        assertTrue(new DefaultProportion<Object>(tA, tB, tC, tD).isValid());

        {
            String equation = tA + " : " + tB + " :: " + tC + " : ";
            SubTupleRebuilder<Sequence<Character>, Tuple<Sequence<Character>>> tupleRebuilder = new SubTupleRebuilder<>(charSeqRebuilder) {

                @Override
                public Tuple<Sequence<Character>> rebuild(Tuple<Sequence<Character>> object) {
                    return object;
                }
            };
            for (Solution<Tuple<Sequence<Character>>> s : new TupleEquation<>(tA, tB, tC, tupleRebuilder).uniqueSolutions()) {
//      System.out.println(s.getDegree());
                System.out.println(equation + s.getContent());
                System.out.println(s.getStraightRelation());
                System.out.println(s.getCrossedRelation());
            }
            System.out.println();
        }

        ImmutableSet<Integer> sA = new ImmutableSet<>(new HashSet<>(Arrays.asList(1, 2)));
        ImmutableSet<Integer> sB = new ImmutableSet<>(new HashSet<>(Arrays.asList(3, 1, 5)));
        ImmutableSet<Integer> sC = new ImmutableSet<>(new HashSet<>(Arrays.asList(2, 0, 4)));
        ImmutableSet<Integer> sD = new ImmutableSet<>(new HashSet<>(Arrays.asList(4, 0, 3, 5)));

        assertTrue(new DefaultProportion<Object>(sA, sB, sC, sD).isValid());

        {
            String equation = sA + " : " + sB + " :: " + sC + " : ";
            for (Solution<ImmutableSet<Integer>> s : new SimpleSetEquation<>(sA, sB, sC))
                System.out.println(equation + s.getContent());
        }
    }
}
