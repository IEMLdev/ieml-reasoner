package reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.vletard.analogy.DefaultProportion;
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
      words.add(w);
      System.out.println(i + " -> " + w.mixedTranslation("fr", 1, dict).prettyPrint(2));
      System.out.println(i + " -> " + w.mixedTranslation("fr", 0, dict).prettyPrint(2));
      
      uslTr.putIfAbsent(obj.getString("ieml"), new ArrayList<String>());
      uslTr.get(obj.getString("ieml")).add(obj.getJSONObject("translations").getJSONArray("fr").toString());
      trUsl.putIfAbsent(obj.getJSONObject("translations").getJSONArray("fr").toString(), new ArrayList<String>());
      trUsl.get(obj.getJSONObject("translations").getJSONArray("fr").toString()).add(obj.getString("ieml"));
    }
    
    System.exit(0);
    
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
//                System.out.println(words.get(i));
//                System.out.println(arr.getJSONObject(i).getString("ieml"));
//                System.out.println(words.get(j));
//                System.out.println(arr.getJSONObject(j).getString("ieml"));
//                System.out.println(words.get(k));
//                System.out.println(arr.getJSONObject(k).getString("ieml"));
//                System.out.println(words.get(l));
//                System.out.println(arr.getJSONObject(l).getString("ieml"));
                System.out.println(dict.getFromUSL(words.get(i).getUsl()).get("fr") + " : "
                                 + dict.getFromUSL(words.get(j).getUsl()).get("fr") + " :: "
                                 + dict.getFromUSL(words.get(k).getUsl()).get("fr") + " : "
                                 + dict.getFromUSL(words.get(l).getUsl()).get("fr"));
              }
            }
          }
          //assert(!(i == j || i == k));
          //DefaultEquation e = new DefaultEquation(words.get(i), words.get(j), words.get(k));
          //try {
          //  SolutionBag<Object> m = e.getBestSolutions();
          //  System.out.println(i + "" + translations.get(i) + " : "
          //      + j + "" + translations.get(j) + " :: "
          //      + k + "" + translations.get(k) + " : "
          //      + m.iterator().next());
          //} catch (NoSolutionException exception) {}
        }
      }
    }
  }
}
