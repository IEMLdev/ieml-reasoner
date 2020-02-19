package reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultProportion;
import io.github.vletard.analogy.tuple.TupleEquation;
import io.github.vletard.analogy.tuple.TupleSolution;
import parser.IEMLUnit;
import parser.JSONStructureException;
import parser.MissingTranslationException;
import parser.StyleException;
import parser.Word;

public class Reasoner{
  public static final String WORDS_SAMPLE_FILENAME = "resources/words_sample.json";
  public static final String DICTIONARY_FILENAME = "resources/dictionary.json";


  public static void main(String[] args) throws JSONStructureException, MissingTranslationException, JSONException, StyleException {
    Scanner scanner = null;
    JSONArray jsonTranslations = null;
    JSONArray jsonWordList = null;
    try {
      scanner = new Scanner(new File(DICTIONARY_FILENAME));
      scanner.useDelimiter("\\A");
      jsonTranslations = new JSONArray(scanner.next());
      scanner.close();
      
      scanner = new Scanner(new File(WORDS_SAMPLE_FILENAME));
      scanner.useDelimiter("\\A");
      jsonWordList = new JSONArray(scanner.next());
      scanner.close();
    } catch (FileNotFoundException e) {
      System.err.println("Cannot open IEML JSON exports. Please generate them first.");
      System.exit(1);
    }

    Dictionary dict = new Dictionary(jsonTranslations);

    HashMap<String, ArrayList<String>> uslTr = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> trUsl = new HashMap<String, ArrayList<String>>();

    ArrayList<Word> words = new ArrayList<Word>();
    for (int i = 0; i < jsonWordList.length(); i++) {
      JSONObject obj = jsonWordList.getJSONObject(i);
      Word w = Word.factory(obj);
      assert(w.equals(Word.factory(obj)));
      words.add(w);
      System.out.println(i + " -> " + w.mixedTranslation("fr", 1, dict).prettyPrint(2));
      System.out.println(i + " -> " + w.mixedTranslation("fr", 0, dict).prettyPrint(2));
      
      uslTr.putIfAbsent(obj.getString("ieml"), new ArrayList<String>());
      uslTr.get(obj.getString("ieml")).add(obj.getJSONObject("translations").getJSONArray("fr").toString());
      trUsl.putIfAbsent(obj.getJSONObject("translations").getJSONArray("fr").toString(), new ArrayList<String>());
      trUsl.get(obj.getJSONObject("translations").getJSONArray("fr").toString()).add(obj.getString("ieml"));
    }
    
    System.out.println("Listing fr synonymous:");
    for (String k: uslTr.keySet()){
      if (uslTr.get(k).size() > 1){
        System.out.println(k + ":");
        for (String v: uslTr.get(k)){
          System.out.println(v);
        }
        System.out.println();
      }
    }
    System.out.println();

    System.out.println("Listing IEML synonymous:");
    for (String k: trUsl.keySet()){
      if (trUsl.get(k).size() > 1){
        System.out.println(k + ":");
        for (String v: trUsl.get(k)){
          System.out.println(v);
        }
        System.out.println();
      }
    }
    System.out.println();
    
    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          for (int l = i+1; l < words.size(); l++) {
            if (!((i == j && k == l) || (i == k && j == l) || (i == j && j == k && k == l))) {
              DefaultProportion<Object> p = new DefaultProportion<Object>(words.get(i), words.get(j), words.get(k), words.get(l));
              if (p.isValid()) {
                System.out.println(dict.getFromUSL(words.get(i).getUsl()).get("fr") + " : "
                                 + dict.getFromUSL(words.get(j).getUsl()).get("fr") + " :: "
                                 + dict.getFromUSL(words.get(k).getUsl()).get("fr") + " : "
                                 + dict.getFromUSL(words.get(l).getUsl()).get("fr"));
              }
            }
          }
        }
      }
    }
    

    for (int i = 0; i < words.size(); i++) {
      for (int j = i+1; j < words.size(); j++) {
        for (int k = j+1; k < words.size(); k++) {
          assert(!(i == j || i == k));
          Word wi = words.get(i);
          Word wj = words.get(j);
          Word wk = words.get(k);
          TupleEquation<IEMLUnit> e = new TupleEquation<IEMLUnit>(wi, wj, wk);
          for (TupleSolution<IEMLUnit> s: e.uniqueSolutions()) {
            System.out.println(wi.mixedTranslation("fr", 0, dict) + " : "
                             + wj.mixedTranslation("fr", 0, dict) + " :: "
                             + wk.mixedTranslation("fr", 0, dict) + " : "
                             + s);
          }
        }
      }
    }
  }
}
