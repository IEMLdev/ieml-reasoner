package reasoner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultEquation;
import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Solution;
import parser.IEMLTuple;
import parser.IEMLUnit;
import parser.IncompatibleSolutionException;
import parser.JSONStructureException;
import parser.MissingTranslationException;
import parser.StyleException;
import parser.Word;

public class Reasoner{
  private final Dictionary dict;
  private final ArrayList<Word> words;
  String verifyingStats, solvingStats;
  
  public Reasoner(ArrayList<JSONObject> jsonWordList) throws JSONStructureException, StyleException {
    this(jsonWordList, jsonWordList);
  }

  public Reasoner(ArrayList<JSONObject> jsonWordList, ArrayList<JSONObject> fullIEMLTranslations) throws JSONStructureException, StyleException {
    this.dict = new Dictionary(fullIEMLTranslations);

    HashMap<String, ArrayList<String>> uslTr = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> trUsl = new HashMap<String, ArrayList<String>>();

    this.words = new ArrayList<Word>();
    for (JSONObject obj: jsonWordList) {
      Word w = Word.factory(obj);
      assert(w.equals(Word.factory(obj)));  // checking equals implementation
      try {
        String usl = w.getUsl().replaceAll(" +", " ");
        String reBuiltUSL = Word.reFactory(w).getUsl();
        assert(usl.contentEquals(reBuiltUSL));  // checking USL generator
      } catch (IncompatibleSolutionException e) {
        System.err.println(w.getUsl());
        System.err.println(w);
        throw new RuntimeException(e);
      }
      this.words.add(w);

      uslTr.putIfAbsent(obj.getString("ieml"), new ArrayList<String>());
      uslTr.get(obj.getString("ieml")).add(obj.getJSONObject("translations").getJSONArray("fr").toString());
      trUsl.putIfAbsent(obj.getJSONObject("translations").getJSONArray("fr").toString(), new ArrayList<String>());
      trUsl.get(obj.getJSONObject("translations").getJSONArray("fr").toString()).add(obj.getString("ieml"));
    }
  }

  private Word getWord(int i) {
    return this.words.get(i);
  }
  
  private List<String> getWordTranslation(int i, String lang) throws MissingTranslationException {
    return this.dict.getFromUSL(this.words.get(i).getUsl()).get(lang);
  }

  public HashMap<String, LinkedList<Word>> searchPolysemy(String lang) throws MissingTranslationException {
    HashMap<String, LinkedList<Word>> iemlWordsPerNLWord = new HashMap<String, LinkedList<Word>>();
    HashMap<String, LinkedList<Word>> multiple = new HashMap<String, LinkedList<Word>>();

    for (Word w: this.words) {
      for (String singleTranslation: this.dict.getFromUSL(w.getUsl()).get(lang)) {
        iemlWordsPerNLWord.putIfAbsent(singleTranslation, new LinkedList<Word>());
        LinkedList<Word> iemlWordList = iemlWordsPerNLWord.get(singleTranslation);
        iemlWordList.add(w);
        if (iemlWordList.size() == 2)
          multiple.put(singleTranslation, iemlWordList);
      }
    }
    return multiple;
  }

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
  public LinkedList<Quadruple<Integer>> validDBProportions() {
    LinkedList<Long> durations = new LinkedList<Long>();
    LinkedList<Quadruple<Integer>> validProportions = new LinkedList<Quadruple<Integer>>();

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          for (int l = i+1; l < words.size(); l++) {
            if (!((i == j && k == l) || (i == k && j == l) || (i == j && j == k && k == l))) {
              long start = System.currentTimeMillis();
              if (verifySingleProportion(words.get(i), words.get(j), words.get(k), words.get(l)))
                validProportions.add(new Quadruple<Integer>(i, j, k, l));
                
              durations.add(System.currentTimeMillis() - start);
            }
          }
        }
      }
    }

    if (!durations.isEmpty()) {
      long sum = 0;
      long min = Long.MAX_VALUE;
      long max = 0;
      for (Long d: durations) {
        if (d < min)
          min = d;
        if (d > max)
          max = d;
        sum += d;
      }
      long avg = Double.valueOf(sum / durations.size()).longValue();
      this.verifyingStats  = "   number: " + durations.size() + "\n";
      this.verifyingStats += "    total: " + formatDuration(sum) + "\n";
      this.verifyingStats += "  average: " + formatDuration(avg) + "\n";
      this.verifyingStats += "  minimum: " + formatDuration(min) + "\n";
      this.verifyingStats += "  maximum: " + formatDuration(max) + "\n";
    }
    return validProportions;
  }

  private static Iterable<Word> solveSingleEquation(IEMLTuple ieml1, IEMLTuple ieml2, IEMLTuple ieml3) {
    DefaultEquation<IEMLTuple, ? extends Solution<IEMLTuple>> e = DefaultEquation.factory(ieml1, ieml2, ieml3);
    return new Iterable<Word>() {
      @Override
      public Iterator<Word> iterator() {
        return new Iterator<Word>() {
          Iterator<? extends Solution<IEMLTuple>> it = e.uniqueSolutions().iterator();

          @Override
          public boolean hasNext() {
            return this.it.hasNext();
          }

          @Override
          public Word next() {
            if (this.hasNext()) {
              try {
                return Word.reFactory(this.it.next().getContent());
              } catch (IncompatibleSolutionException | StyleException e) {
                throw new RuntimeException(e);
              }
            }
            else
              throw new NoSuchElementException();
          }
        };
      }
    };
  }

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
    return solveSingleEquation(w1, w2, w3);
  }

  /**
   * Computes and returns the list of every productive analogical equation from the word database.
   * Note that only one equation is retained for each commutative cluster.
   * @return the list of the word triples that constitute productive equations.
   */
  public LinkedList<Triple<Integer>> computeEquations() {
    LinkedList<Long> durations = new LinkedList<Long>();
    LinkedList<Triple<Integer>> productiveEquations = new LinkedList<Triple<Integer>>();

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          assert(!(i == j || i == k));
          Word wi = words.get(i);
          Word wj = words.get(j);
          Word wk = words.get(k);
          long start = System.currentTimeMillis();
          if (solveSingleEquation(wi, wj, wk).iterator().hasNext())
            productiveEquations.add(new Triple<Integer>(i, j, k));
          durations.add(System.currentTimeMillis() - start);
        }
      }
    }

    if (!durations.isEmpty()) {
      long sum = 0;
      long min = Long.MAX_VALUE;
      long max = 0;
      for (Long d: durations) {
        if (d < min)
          min = d;
        if (d > max)
          max = d;
        sum += d;
      }
      long avg = Double.valueOf(sum / durations.size()).longValue();
      this.solvingStats  = "    total: " + formatDuration(sum) + "\n";
      this.solvingStats += "   number: " + durations.size() + "\n";
      this.solvingStats += "  average: " + formatDuration(avg) + "\n";
      this.solvingStats += "  minimum: " + formatDuration(min) + "\n";
      this.solvingStats += "  maximum: " + formatDuration(max) + "\n";
    }
    return productiveEquations;
  }

  public String getVerifyingStats() {
    return this.verifyingStats;
  }

  public String getSolvingStats() {
    return this.solvingStats;
  }


  public static void main(String[] args) throws JSONStructureException, MissingTranslationException, StyleException, IncompatibleSolutionException {
    final String WORDS_SAMPLE_FILENAME = "resources/words_sample.json.bz2";
    final String DICTIONARY_FILENAME = "resources/dictionary.json.bz2";

    Reasoner r;
    try {
      InputStream wordStream = new BZip2CompressorInputStream(new FileInputStream(WORDS_SAMPLE_FILENAME));
      InputStream dictStream = new BZip2CompressorInputStream(new FileInputStream(DICTIONARY_FILENAME));

      ArrayList<JSONObject> jsonTranslations = new ArrayList<JSONObject>();
      {
        Scanner scanner = new Scanner(dictStream);
        scanner.useDelimiter("\\A");
        JSONArray arr = new JSONArray(scanner.next());
        for (int i = 0; i < arr.length(); i++)
          jsonTranslations.add(arr.getJSONObject(i));
        scanner.close();
      }

      ArrayList<JSONObject> jsonWordList = new ArrayList<JSONObject>();
      {
        Scanner scanner = new Scanner(wordStream);
        scanner.useDelimiter("\\A");
        JSONArray arr = new JSONArray(scanner.next());
        for (int i = 0; i < arr.length(); i++)
          jsonWordList.add(arr.getJSONObject(i));
        scanner.close();
      }

      r = new Reasoner(jsonWordList, jsonTranslations);
    } catch (IOException e) {
      throw new RuntimeException("Cannot open IEML JSON exports. Please generate them first.", e);
    }

    System.out.println("Displaying current database:");
    for (Word w: r.words) {
      System.out.println(w.mixedTranslation("fr", 0, r.dict).get("translations") + " = " + w.getUsl());
    }
    System.out.println();

    System.out.println("The following french words are used in multiple IEML translations:");
    for (Entry<String, LinkedList<Word>> mapping: r.searchPolysemy("fr").entrySet()) {
      System.out.println(mapping.getKey() + ":");
      for (Word w: mapping.getValue())
        System.out.println("\t" + w.getUsl() + " (" + r.dict.getFromUSL(w.getUsl()).get("fr") + ")");
    }
    System.out.println();


    System.out.println("Solving equations in word database:");
    for (Triple<Integer> t: r.computeEquations()) {
      for (Word w: solveSingleEquation(r.getWord(t.getFirst()), r.getWord(t.getSecond()), r.getWord(t.getThird()))) {
        System.out.println(r.getWordTranslation(t.getFirst(), "fr"));
        System.out.println(r.getWord(t.getFirst()).getUsl());
        System.out.println("\t:");
        System.out.println(r.getWordTranslation(t.getSecond(), "fr"));
        System.out.println(r.getWord(t.getSecond()).getUsl());
        System.out.println("\t::");
        System.out.println(r.getWordTranslation(t.getThird(), "fr"));
        System.out.println(r.getWord(t.getThird()).getUsl());
        System.out.println("\t:");
        System.out.println(w.getUsl());
        System.out.println("\n");
      }
    }
    System.out.println();


    System.out.println("Searching for proportions in word database:");
    for (Quadruple<Integer> q: r.validDBProportions()) {
      String s = r.getWordTranslation(q.getFirst(), "fr") + " : ";
      s += r.getWordTranslation(q.getSecond(), "fr") + " :: ";
      s += r.getWordTranslation(q.getThird(), "fr") + " : ";
      s += r.getWordTranslation(q.getFourth(), "fr");
      System.out.println(s);
    }
    System.out.println();

    System.err.println("Equation solving stats:\n" + r.getSolvingStats());
    System.err.println("Proportion verification stats:\n" + r.getVerifyingStats());
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
