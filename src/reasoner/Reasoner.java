package reasoner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultEquation;
import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Solution;
import io.github.vletard.analogy.tuple.Tuple;
import io.github.vletard.analogy.tuple.TupleEquation;
import parser.IEMLTuple;
import parser.IEMLUnit;
import parser.IncompatibleSolutionException;
import parser.JSONStructureException;
import parser.MissingTranslationException;
import parser.Morpheme;
import parser.ParseException;
import parser.Polymorpheme;
import parser.StyleException;
import parser.Word;
import parser.Writable;
import util.Pair;

public class Reasoner {
  public static final String DEFAULT_BASENAME = "/tmp/ieml-reasoner";

  private final Dictionary dict;
  private final ArrayList<Morpheme> morphemes;
  private final ArrayList<Polymorpheme> polymorphemes;
//  private final ArrayList<Lexeme> lexemes;
//  private final ArrayList<Word> words;
  String verifyingStats, solvingStats;

  public Reasoner(Dictionary dict, ArrayList<String> usls) {
    this.dict = dict;
    this.morphemes = new ArrayList<Morpheme>();
    this.polymorphemes = new ArrayList<Polymorpheme>();
    
    for (String usl: usls) {
      try {
        Pair<Morpheme, Integer> morphemeParse = Morpheme.parse(usl);
        if (morphemeParse.getSecond() == usl.length())
          morphemes.add(morphemeParse.getFirst());

        Pair<Polymorpheme, Integer> polymorphemeParse = Polymorpheme.parse(usl);
        if (polymorphemeParse.getSecond() == usl.length())
          polymorphemes.add(polymorphemeParse.getFirst());
        
      } catch (ParseException e) {
        System.err.println("Could not parse " + usl);
      }
    }
  }

  //  public HashMap<String, LinkedList<Word>> searchPolysemy(String lang) throws MissingTranslationException {
  //    HashMap<String, LinkedList<Word>> iemlWordsPerNLWord = new HashMap<String, LinkedList<Word>>();
  //    HashMap<String, LinkedList<Word>> multiple = new HashMap<String, LinkedList<Word>>();
  //
  //    for (Word w: this.words) {
  //      for (String singleTranslation: this.dict.getFromUSL(w.getUSL()).get(lang)) {
  //        iemlWordsPerNLWord.putIfAbsent(singleTranslation, new LinkedList<Word>());
  //        LinkedList<Word> iemlWordList = iemlWordsPerNLWord.get(singleTranslation);
  //        iemlWordList.add(w);
  //        if (iemlWordList.size() == 2)
  //          multiple.put(singleTranslation, iemlWordList);
  //      }
  //    }
  //    return multiple;
  //  }

  private static boolean verifySingleProportion(IEMLUnit ieml1, IEMLUnit ieml2, IEMLUnit ieml3, IEMLUnit ieml4) {
    DefaultProportion<IEMLUnit> p = new DefaultProportion<IEMLUnit>(ieml1, ieml2, ieml3, ieml4);
    return p.isValid();
  }

  /**
   * Constitutes an analogical proportion using the provided words in JSON format, and tests for its validity.
   * @param word1 first word of the proportion (position A)
   * @param word2 second word of the proportion (position B)
   * @param word3 third word of the proportion (position C)
   * @param word4 fourth word of the proportion (position D)
   * @return true if the proportion is valid, false of it is not.
   * @throws JSONStructureException if the JSON structure of one of the provided strings is not as expected.
   * @throws StyleException if an inconsistency was found in the arguments of the JSON structure of one of the provided strings.
   */
  public static boolean verifySingleWordProportion(String word1, String word2, String word3, String word4) throws JSONStructureException, StyleException {
    Word w1 = Word.factory(new JSONObject(word1));
    Word w2 = Word.factory(new JSONObject(word2));
    Word w3 = Word.factory(new JSONObject(word3));
    Word w4 = Word.factory(new JSONObject(word4));
    return verifySingleProportion(w1, w2, w3, w4);
  }

  /**
   * Computes the list of every valid analogical proportion from the word database.
   * Note that only one proportion is retained for each commutative cluster. 
   * @return the list of word quadruples that contitute valid proportions.
   */
  //  public LinkedList<Quadruple<Integer>> validDBProportions() {
  //    LinkedList<Long> durations = new LinkedList<Long>();
  //    LinkedList<Quadruple<Integer>> validProportions = new LinkedList<Quadruple<Integer>>();
  //
  //    for (int i = 0; i < words.size(); i++) {
  //      for (int j = i+1; j < words.size(); j++) {
  //        for (int k = j+1; k < words.size(); k++) {
  //          for (int l = i+1; l < words.size(); l++) {
  //            if (!((i == j && k == l) || (i == k && j == l) || (i == j && j == k && k == l))) {
  //              long start = System.currentTimeMillis();
  //              if (verifySingleProportion(words.get(i), words.get(j), words.get(k), words.get(l)))
  //                validProportions.add(new Quadruple<Integer>(i, j, k, l));
  //
  //              durations.add(System.currentTimeMillis() - start);
  //            }
  //          }
  //        }
  //      }
  //    }
  //
  //    if (!durations.isEmpty()) {
  //      long sum = 0;
  //      long min = Long.MAX_VALUE;
  //      long max = 0;
  //      for (Long d: durations) {
  //        if (d < min)
  //          min = d;
  //        if (d > max)
  //          max = d;
  //        sum += d;
  //      }
  //      long avg = Double.valueOf(sum / durations.size()).longValue();
  //      this.verifyingStats  = "   number: " + durations.size() + "\n";
  //      this.verifyingStats += "    total: " + formatDuration(sum) + "\n";
  //      this.verifyingStats += "  average: " + formatDuration(avg) + "\n";
  //      this.verifyingStats += "  minimum: " + formatDuration(min) + "\n";
  //      this.verifyingStats += "  maximum: " + formatDuration(max) + "\n";
  //    }
  //    return validProportions;
  //  }

  private static Iterable<Tuple<?>> solveSingleEquation(Tuple<?> ieml1, Tuple<?> ieml2, Tuple<?> ieml3) {
    DefaultEquation<Tuple<?>, ? extends Solution<Tuple<?>>> e = DefaultEquation.factory(ieml1, ieml2, ieml3);
    return new Iterable<Tuple<?>>() {
      @Override
      public Iterator<Tuple<?>> iterator() {
        return new Iterator<Tuple<?>>() {
          Iterator<? extends Solution<Tuple<?>>> it = e.uniqueSolutions().iterator();

          @Override
          public boolean hasNext() {
            return this.it.hasNext();
          }

          @Override
          public Tuple<?> next() {
            if (this.hasNext())
              return this.it.next().getContent();
            else
              throw new NoSuchElementException();
          }
        };
      }
    };
  }

  private Iterable<String> morphemeEquations() {
    return new Iterable<String>() {
      
      @Override
      public Iterator<String> iterator() {
        return new Iterator<String>() {
          private Iterator<Triple<Integer>> indices = computeEquations(Reasoner.this.morphemes).iterator();
          private Iterator<Tuple<?>> solutions = Collections.emptyIterator();
          private Triple<Integer> currentIndices;
          
          @Override
          public boolean hasNext() {
            if (this.solutions.hasNext())
              return true;
            else if (this.indices.hasNext()) {
              Triple<Integer> t = this.indices.next();
              this.currentIndices = t;
              this.solutions = Reasoner.solveSingleEquation(Reasoner.this.morphemes.get(t.getFirst()), Reasoner.this.morphemes.get(t.getSecond()), Reasoner.this.morphemes.get(t.getThird())).iterator();
              assert(this.solutions.hasNext());
              return true;
            }
            else
              return false;
          }

          @Override
          public String next() {
            if (this.hasNext()) {
              Triple<String> str;
              try {
                str = Reasoner.this.displayMorphemeEquation(this.currentIndices, Morpheme.reFactory(this.solutions.next()));
              } catch (IncompatibleSolutionException e) {
                throw new RuntimeException(e);
              }
              return str.getFirst() + "\n" + str.getSecond() + "\n" + str.getThird();
            }
            else throw new NoSuchElementException();
          }
        };
      }
    };
  }

//  private Iterable<String> polymorphemeEquations() {
//    return new Iterable<String>() {
//      
//      @Override
//      public Iterator<String> iterator() {
//        return new Iterator<String>() {
//          private Iterator<Triple<Integer>> indices = computeEquations(Reasoner.this.polymorphemes).iterator();
//          private Iterator<Tuple<?>> solutions = Collections.emptyIterator();
//          private Triple<Integer> currentIndices;
//          
//          @Override
//          public boolean hasNext() {
//            if (this.solutions.hasNext())
//              return true;
//            else if (this.indices.hasNext()) {
//              Triple<Integer> t = this.indices.next();
//              this.currentIndices = t;
//              this.solutions = Reasoner.solveSingleEquation(Reasoner.this.polymorphemes.get(t.getFirst()), Reasoner.this.polymorphemes.get(t.getSecond()), Reasoner.this.polymorphemes.get(t.getThird())).iterator();
//              assert(this.solutions.hasNext());
//              return true;
//            }
//            else
//              return false;
//          }
//
//          @Override
//          public String next() {
//            if (this.hasNext()) {
//              Triple<String> str;
//              try {
//                str = Reasoner.this.displayPolymorphemeEquation(this.currentIndices, Polymorpheme.reFactory(this.solutions.next()));
//              } catch (IncompatibleSolutionException e) {
//                throw new RuntimeException(e);
//              }
//              return str.getFirst() + "\n" + str.getSecond() + "\n" + str.getThird();
//            }
//            else throw new NoSuchElementException();
//          }
//        };
//      }
//    };
//  }

  /**
   * Constitutes an analogical equation using the provided words in JSON format, and returns an {@link Iterable}
   * object over its unique solutions (position D).
   * @param word1 first word of the equation (position A)
   * @param word2 second word of the equation (position B)
   * @param word3 third word of the equation (position C)
   * @return true if the proportion is valid, false of it is not.
   * @throws JSONStructureException if the JSON structure of one of the provided strings is not as expected.
   * @throws StyleException if an inconsistency was found in the arguments of the JSON structure of one of the provided strings.
   */
  public static Iterable<Word> solveSingleWordEquation(String word1, String word2, String word3) throws JSONStructureException, StyleException {
    Word w1 = Word.factory(new JSONObject(word1));
    Word w2 = Word.factory(new JSONObject(word2));
    Word w3 = Word.factory(new JSONObject(word3));
    return new Iterable<Word>() {

      @Override
      public Iterator<Word> iterator() {
        return new Iterator<Word>() {

          private Iterator<Tuple<?>> it = solveSingleEquation(w1, w2, w3).iterator();
          @Override
          public boolean hasNext() {
            return this.it.hasNext();
          }

          @Override
          public Word next() {
            if (this.hasNext())
              try {
                return Word.reBuild(this.it.next());
              } catch (IncompatibleSolutionException | StyleException e) {
                throw new RuntimeException(e);
              }
            else
              throw new NoSuchElementException();
          }
        };
      }
    };
  }

  /**
   * Computes and returns the list of every productive analogical equation from the word database.
   * Note that only one equation is retained for each commutative cluster.
   * @return the list of the word triples that constitute productive equations.
   */
  public static <T extends Tuple<?>> Iterable<Triple<Integer>> computeEquations(List<T> list) {
    return new Iterable<Triple<Integer>>() {

      @Override
      public Iterator<Triple<Integer>> iterator() {
        return new Iterator<Triple<Integer>>() {
          private int i = 0, j = 1, k = 2;
          private Triple<Integer> nextProductiveEquation = null;

          @Override
          public boolean hasNext() {
            if (this.nextProductiveEquation == null && this.i < list.size() && this.j < list.size() && this.k < list.size())
              this.nextProductiveEquation = this.computeNext();
            return this.nextProductiveEquation != null;
          }

          @Override
          public Triple<Integer> next() {
            if (this.hasNext()) {
              Triple<Integer> t = this.nextProductiveEquation;
              this.nextProductiveEquation = null;
              return t;
            }
            else
              throw new NoSuchElementException();
          }

          private Triple<Integer> computeNext() {
            this.k ++;
            while (this.i < list.size()) {
              if (this.j >= list.size())
                this.j = this.i + 1;
              while (this.j < list.size()) {
                if (this.k >= list.size())
                  this.k = this.j + 1;
                while (this.k < list.size()) {
                  assert(!(this.i == this.j || this.i == this.k));
                  T wi = list.get(this.i);
                  T wj = list.get(this.j);
                  T wk = list.get(this.k);
                  if (solveSingleEquation(wi, wj, wk).iterator().hasNext())
                    return new Triple<Integer>(i, j, k);
                  this.k ++;
                }
                this.j ++;
              }
              this.i ++;
            }
            return null;
          }
        };
      }
    };
    //      LinkedList<Triple<Integer>> productiveEquations = new LinkedList<Triple<Integer>>();
    //
    //      for (int i = 0; i < list.size(); i++) {
    //        System.out.println();
    //        for (int j = i+1; j < list.size(); j++) {
    //          for (int k = j+1; k < list.size(); k++) {
    //            assert(!(i == j || i == k));
    //            T wi = list.get(i);
    //            T wj = list.get(j);
    //            T wk = list.get(k);
    //            if (solveSingleEquation(wi, wj, wk).iterator().hasNext()) {
    //              productiveEquations.add(new Triple<Integer>(i, j, k));
    //              System.out.println(i + "/" + list.size() + " " + productiveEquations.size());
    //            }
    //          }
    //        }
    //      }
    //
    //      return productiveEquations;
  }

  /**
   * Computes and returns the list of every productive analogical equation from the word database.
   * Note that only one equation is retained for each commutative cluster.
   * @return the list of the word triples that constitute productive equations.
   */
  //  public LinkedList<Triple<Integer>> computeEquations() {
  //    LinkedList<Long> durations = new LinkedList<Long>();
  //    LinkedList<Triple<Integer>> productiveEquations = new LinkedList<Triple<Integer>>();
  //
  //    for (int i = 0; i < words.size(); i++) {
  //      for (int j = i+1; j < words.size(); j++) {
  //        for (int k = j+1; k < words.size(); k++) {
  //          assert(!(i == j || i == k));
  //          Word wi = words.get(i);
  //          Word wj = words.get(j);
  //          Word wk = words.get(k);
  //          long start = System.currentTimeMillis();
  //          if (solveSingleEquation(wi, wj, wk).iterator().hasNext())
  //            productiveEquations.add(new Triple<Integer>(i, j, k));
  //          durations.add(System.currentTimeMillis() - start);
  //        }
  //      }
  //    }
  //
  //    if (!durations.isEmpty()) {
  //      long sum = 0;
  //      long min = Long.MAX_VALUE;
  //      long max = 0;
  //      for (Long d: durations) {
  //        if (d < min)
  //          min = d;
  //        if (d > max)
  //          max = d;
  //        sum += d;
  //      }
  //      long avg = Double.valueOf(sum / durations.size()).longValue();
  //      this.solvingStats  = "    total: " + formatDuration(sum) + "\n";
  //      this.solvingStats += "   number: " + durations.size() + "\n";
  //      this.solvingStats += "  average: " + formatDuration(avg) + "\n";
  //      this.solvingStats += "  minimum: " + formatDuration(min) + "\n";
  //      this.solvingStats += "  maximum: " + formatDuration(max) + "\n";
  //    }
  //    return productiveEquations;
  //  }

  private Triple<String> displayMorphemeEquation(Triple<Integer> t, Morpheme result) {
    String indices = t.getFirst() + "\t" + t.getSecond() + "\t" + t.getThird();
    String translations = "";
    for (Integer i: t) {
      try {
        translations += this.dict.getFromUSL(this.morphemes.get(i).getUSL()).get("fr") + "\t";
      } catch (MissingTranslationException e) {
        translations += "<no translation>\t";
      }
    }
    try {
      translations += this.dict.getFromUSL(result.getUSL()).get("fr");
    } catch (MissingTranslationException e) {
      translations += "<no translation>"; // TODO try analogies on translations
    }

    String usls = this.morphemes.get(t.getFirst()).getUSL() + "\t";
    usls += this.morphemes.get(t.getSecond()).getUSL() + "\t";
    usls += this.morphemes.get(t.getThird()).getUSL() + "\t";
    usls += result.getUSL();

    return new Triple<String>(indices, translations, usls);
  }

  public String getVerifyingStats() {
    return this.verifyingStats;
  }

  public String getSolvingStats() {
    return this.solvingStats;
  }


  public static void main(String[] args) throws JSONStructureException, MissingTranslationException, StyleException, IncompatibleSolutionException, FileNotFoundException, IOException {
    final String WORDS_SAMPLE_FILENAME = "resources/words_sample.json.bz2";
    final String DICTIONARY_FILENAME = "resources/dictionary.json.bz2";

    Reasoner reasoner;
    ArrayList<Morpheme> morphemes = new ArrayList<Morpheme>();
    ArrayList<Polymorpheme> polymorphemes = new ArrayList<Polymorpheme>();
    try {
      InputStream wordStream = new BZip2CompressorInputStream(new FileInputStream(WORDS_SAMPLE_FILENAME));
      InputStream dictStream = new BZip2CompressorInputStream(new FileInputStream(DICTIONARY_FILENAME));

      ArrayList<JSONObject> jsonTranslations = new ArrayList<JSONObject>();
      ArrayList<String> usls = new ArrayList<String>();
      {
        Scanner scanner = new Scanner(dictStream);
        scanner.useDelimiter("\\A");
        JSONArray arr = new JSONArray(scanner.next());
        for (int i = 0; i < arr.length(); i++) {
          JSONObject obj = arr.getJSONObject(i);
          jsonTranslations.add(obj);

          if (!obj.isNull("usl"))  // this should better be identical in every type of json dump
            usls.add(obj.getString("usl"));
          else
            usls.add(obj.getString("ieml"));
        }
        scanner.close();
      }

      reasoner = new Reasoner(new Dictionary(jsonTranslations), usls);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open IEML JSON exports. Please generate them first.", e);
    }

    {
      final String filename = Reasoner.DEFAULT_BASENAME + "-equations-morphemes.txt.bz2";
      PrintStream out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
      System.out.println("Writing morpheme equations to " + filename + "...");
      for (String equationResult: reasoner.morphemeEquations())
        out.println(equationResult);
      out.close();
    }

//    {
//      final String filename = Reasoner.DEFAULT_BASENAME + "-equations-polymorphemes.txt.bz2";
//      PrintStream out = new PrintStream(new BZip2CompressorOutputStream(new FileOutputStream(filename)));
//      System.out.println("Writing polymorpheme equations to " + filename + "...");
//      for (String equationResult: reasoner.polymorphemeEquations())
//        out.println(equationResult);
//      out.close();
//    }

    //    System.out.println("Displaying current database:");
    //    for (Word w: r.words) {
    //      System.out.println(w.mixedTranslation("fr", 0, r.dict).get("translations") + " = " + w.getUSL());
    //    }
    //    System.out.println();
    //
    //    System.out.println("The following french words are used in multiple IEML translations:");
    //    for (Entry<String, LinkedList<Word>> mapping: r.searchPolysemy("fr").entrySet()) {
    //      System.out.println(mapping.getKey() + ":");
    //      for (Word w: mapping.getValue())
    //        System.out.println("\t" + w.getUSL() + " (" + r.dict.getFromUSL(w.getUSL()).get("fr") + ")");
    //    }
    //    System.out.println();
    //
    //
    //    System.out.println("Solving equations in word database:");
    //    for (Triple<Integer> t: r.computeEquations()) {
    //      for (Tuple<?> tuple: solveSingleEquation(r.getWord(t.getFirst()), r.getWord(t.getSecond()), r.getWord(t.getThird()))) {
    //        Word w = Word.reFactory(tuple);
    //        System.out.println(r.getWordTranslation(t.getFirst(), "fr"));
    //        System.out.println(r.getWord(t.getFirst()).getUSL());
    //        System.out.println("\t:");
    //        System.out.println(r.getWordTranslation(t.getSecond(), "fr"));
    //        System.out.println(r.getWord(t.getSecond()).getUSL());
    //        System.out.println("\t::");
    //        System.out.println(r.getWordTranslation(t.getThird(), "fr"));
    //        System.out.println(r.getWord(t.getThird()).getUSL());
    //        System.out.println("\t:");
    //        System.out.println(w.getUSL());
    //        System.out.println("\n");
    //      }
    //    }
    //    System.out.println();
    //
    //
    //    System.out.println("Searching for proportions in word database:");
    //    for (Quadruple<Integer> q: r.validDBProportions()) {
    //      String s = r.getWordTranslation(q.getFirst(), "fr") + " : ";
    //      s += r.getWordTranslation(q.getSecond(), "fr") + " :: ";
    //      s += r.getWordTranslation(q.getThird(), "fr") + " : ";
    //      s += r.getWordTranslation(q.getFourth(), "fr");
    //      System.out.println(s);
    //    }
    //    System.out.println();
    //
    //    System.err.println("Equation solving stats:\n" + r.getSolvingStats());
    //    System.err.println("Proportion verification stats:\n" + r.getVerifyingStats());
  }

  private static String formatDuration(long millisInterval) {
    long abs = Math.abs(millisInterval);
    String str;
    if (millisInterval == 0)
      return "0";
    else {
      str = (abs % 1000) + "ms";
      abs = abs / 1000;
      if (abs > 0) {
        str = (abs % 60) + "s " + str;
        abs = abs / 60;
        if (abs > 0) {
          str = (abs % 60) + "m " + str;
          abs = abs / 60;
          if (abs > 0) {
            str = (abs % 24) + "h " + str;
            abs = abs / 24;
            if (abs > 0) {
              str = (abs % 365.25) + "d " + str;
              abs = (long) Math.floor(abs / 365.25);
              if (abs > 0) {
                str = "y " + str;
              }
            }
          }
        }
      }
    }
    return millisInterval < 0 ? "-" + str : str;
  }
}
