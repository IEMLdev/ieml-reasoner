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
      assert(w.equals(Word.factory(obj)));
      this.words.add(w);

      uslTr.putIfAbsent(obj.getString("ieml"), new ArrayList<String>());
      uslTr.get(obj.getString("ieml")).add(obj.getJSONObject("translations").getJSONArray("fr").toString());
      trUsl.putIfAbsent(obj.getJSONObject("translations").getJSONArray("fr").toString(), new ArrayList<String>());
      trUsl.get(obj.getJSONObject("translations").getJSONArray("fr").toString()).add(obj.getString("ieml"));
    }
  }

  public HashMap<String, LinkedList<String>> searchPolysemy(String lang) throws MissingTranslationException {
    HashMap<String, LinkedList<String>> m = new HashMap<String, LinkedList<String>>();
    HashMap<String, LinkedList<String>> multiple = new HashMap<String, LinkedList<String>>();

    for (Word w: this.words) {
      for (String singleTranslation: this.dict.getFromUSL(w.getUsl()).get(lang)) {
        m.putIfAbsent(singleTranslation, new LinkedList<String>());
        LinkedList<String> l = m.get(singleTranslation);
        l.add(dict.getFromUSL(w.getUsl()).get(lang).toString());
        if (l.size() == 2)
          multiple.put(singleTranslation, l);
      }
    }
    return multiple;
  }

  public LinkedList<String> computeDBProportions() throws MissingTranslationException{
    LinkedList<String> validProportions = new LinkedList<String>();

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          for (int l = i+1; l < words.size(); l++) {
            if (!((i == j && k == l) || (i == k && j == l) || (i == j && j == k && k == l))) {
              DefaultProportion<Word> p = new DefaultProportion<Word>(words.get(i), words.get(j), words.get(k), words.get(l));
              if (p.isValid()) {
                validProportions.add(this.dict.getFromUSL(words.get(i).getUsl()).get("fr") + " : "
                    + this.dict.getFromUSL(words.get(j).getUsl()).get("fr") + " :: "
                    + this.dict.getFromUSL(words.get(k).getUsl()).get("fr") + " : "
                    + this.dict.getFromUSL(words.get(l).getUsl()).get("fr"));
              }
            }
          }
        }
      }
    }
    return validProportions;
  }

  public LinkedList<String> computeEquations() throws IncompatibleSolutionException {
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
            productiveEquations.add(wi.mixedTranslation("fr", 0, dict) + " : "
                + wj.mixedTranslation("fr", 0, dict) + " :: "
                + wk.mixedTranslation("fr", 0, dict) + " : "
                + Word.reFactory(s.getContent()).mixedTranslation("fr", 0, dict).prettyPrint(2));
          }
        }
      }
    }
    return productiveEquations;
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

    System.out.println("The following french words are used in multiple IEML translations:");
    for (Entry<String, LinkedList<String>> mapping: r.searchPolysemy("fr").entrySet()) {
      System.out.println(mapping.getKey() + ": " + mapping.getValue());
    }
    System.out.println();


    System.out.println("Searching for proportions in word database:");
    for (String proportion: r.computeDBProportions())
      System.out.println(proportion);
    System.out.println();


    System.out.println("Solving equations in word database:");
    for (String solvedEquation: r.computeEquations())
      System.out.println(solvedEquation);
    System.out.println();
  }
}
