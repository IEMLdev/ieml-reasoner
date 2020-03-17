package reasoner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.Solution;
import io.github.vletard.analogy.tuple.Tuple;
import io.github.vletard.analogy.tuple.TupleEquation;
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

  public Reasoner(InputStream wordStream, InputStream dictStream) throws JSONStructureException, StyleException {
    Scanner scanner = null;
    JSONArray jsonTranslations = null;
    JSONArray jsonWordList = null;
    scanner = new Scanner(dictStream);
    scanner.useDelimiter("\\A");
    jsonTranslations = new JSONArray(scanner.next());
    scanner.close();

    scanner = new Scanner(wordStream);
    scanner.useDelimiter("\\A");
    jsonWordList = new JSONArray(scanner.next());
    scanner.close();

    this.dict = new Dictionary(jsonTranslations);

    HashMap<String, ArrayList<String>> uslTr = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> trUsl = new HashMap<String, ArrayList<String>>();

    this.words = new ArrayList<Word>();
    for (int i = 0; i < jsonWordList.length(); i++) {
      JSONObject obj = jsonWordList.getJSONObject(i);
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

  public LinkedList<String> computeDBProportions() throws MissingTranslationException{
    LinkedList<Long> durations = new LinkedList<Long>();
    LinkedList<String> validProportions = new LinkedList<String>();

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          for (int l = i+1; l < words.size(); l++) {
            if (!((i == j && k == l) || (i == k && j == l) || (i == j && j == k && k == l))) {
              long start = System.currentTimeMillis();
              DefaultProportion<Word> p = new DefaultProportion<Word>(words.get(i), words.get(j), words.get(k), words.get(l));
              if (p.isValid()) {
                validProportions.add(i + "" + this.dict.getFromUSL(words.get(i).getUsl()).get("fr") + " : "
                    + j + "" + this.dict.getFromUSL(words.get(j).getUsl()).get("fr") + " :: "
                    + k + "" + this.dict.getFromUSL(words.get(k).getUsl()).get("fr") + " : "
                    + l + "" + this.dict.getFromUSL(words.get(l).getUsl()).get("fr"));
              }
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

  public LinkedList<String> computeEquations() throws IncompatibleSolutionException, MissingTranslationException, StyleException {
    LinkedList<Long> durations = new LinkedList<Long>();
    LinkedList<String> productiveEquations = new LinkedList<String>();

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          assert(!(i == j || i == k));
          Word wi = words.get(i);
          Word wj = words.get(j);
          Word wk = words.get(k);
          TupleEquation<IEMLUnit> e = new TupleEquation<IEMLUnit>(wi, wj, wk);
          for (Solution<Tuple<IEMLUnit>> s: e.uniqueSolutions()) {
            long start = System.currentTimeMillis();
            productiveEquations.add(this.dict.getFromUSL(wi.getUsl()).get("fr") + "\n" + wi.getUsl() + "\n\t : \n"
                + this.dict.getFromUSL(wj.getUsl()).get("fr") + "\n" + wj.getUsl() + "\n\t :: \n"
                + this.dict.getFromUSL(wk.getUsl()).get("fr") + "\n" + wk.getUsl() + "\n\t : \n"
                + Word.reFactory(s.getContent()).getUsl());
            durations.add(System.currentTimeMillis() - start);
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
      r = new Reasoner(wordStream, dictStream);
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
    for (String solvedEquation: r.computeEquations())
      System.out.println(solvedEquation + "\n\n");
    System.out.println();


    System.out.println("Searching for proportions in word database:");
    for (String proportion: r.computeDBProportions())
      System.out.println(proportion);
    System.out.println();

    System.err.println("Equation solving stats:\n" + r.getSolvingStats());
    System.err.println("Proportion verification stats:\n" + r.getVerifyingStats());
  }

  public static String formatDuration(long millisInterval) {
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
