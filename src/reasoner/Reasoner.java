package reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import analogy.DefaultEquation;
import analogy.DefaultProportion;
import analogy.NoSolutionException;
import analogy.SolutionBag;
import analogy.SolutionMap;
import analogy.sequence.EquationReadingHead;
import parser.JSONParser;
import util.Tuple;

public class Reasoner{
  public static final String WORDS_SAMPLE_FILENAME = "resources/words_sample.json";


  public static void main(String[] args) {
    Scanner scanner = null;
    try {
      scanner = new Scanner(new File(WORDS_SAMPLE_FILENAME));
    } catch (FileNotFoundException e) {
      System.err.println("Cannot open IEML JSON exports. Please generate them first.");
      System.exit(1);
    }
    scanner.useDelimiter("\\A");
    JSONArray arr = new JSONArray(scanner.next());
    scanner.close();

    HashMap<String, ArrayList<String>> uslTr = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<String>> trUsl = new HashMap<String, ArrayList<String>>();
    
    ArrayList<Tuple<Object>> words = new ArrayList<Tuple<Object>>();
    ArrayList<String> translations = new ArrayList<String>();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject obj = arr.getJSONObject(i);
      Tuple<Object> t = JSONParser.parseJSON(obj);
      words.add(t);
      translations.add(obj.getJSONObject("translations").getJSONArray("fr").toString());
//      System.out.println(i + ":\n" + t.prettyPrint(0) + "\n" + obj.getJSONObject("translations").getJSONArray("fr"));

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
//                System.out.println(words.get(i));
//                System.out.println(arr.getJSONObject(i).getString("ieml"));
//                System.out.println(words.get(j));
//                System.out.println(arr.getJSONObject(j).getString("ieml"));
//                System.out.println(words.get(k));
//                System.out.println(arr.getJSONObject(k).getString("ieml"));
//                System.out.println(words.get(l));
//                System.out.println(arr.getJSONObject(l).getString("ieml"));
                System.out.println(translations.get(i) + " : "
                                 + translations.get(j) + " :: "
                                 + translations.get(k) + " : "
                                 + translations.get(l));
              }
            }
          }
        }
      }
    }
  }
}
