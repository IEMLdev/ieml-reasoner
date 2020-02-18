package reasoner;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import parser.JSONStructureException;
import parser.MissingTranslationException;
import parser.TranslationSet;

public class Dictionary {

  private final ArrayList<TranslationSet> translations;
  private final HashMap<String, Integer> uslDict;
//  private final HashMap<String, HashMap<String, Integer>> nlDict;

  public Dictionary(JSONArray arr) throws JSONStructureException {
    this.translations = new ArrayList<TranslationSet>();
    this.uslDict = new HashMap<String, Integer>();
//    this.nlDict = new HashMap<String, HashMap<String,Integer>>();

    for (int i = 0; i < arr.length(); i++) {
      TranslationSet t = new TranslationSet(arr.getJSONObject(i));
      if (this.uslDict.containsKey(t.getUsl())) {
        if (this.translations.get(this.uslDict.get(t.getUsl())).equals(t))
          System.err.println("Warning: duplicate entry in input JSON file: " + t.getUsl());
        else
          throw new JSONStructureException("The following USL is mapped with two distinct translation sets: " + t.getUsl());
      }
      else 
        this.uslDict.put(t.getUsl(), this.translations.size());

//      for (Entry<String, List<String>> e: t) {
//        String language = e.getKey();
//        this.nlDict.putIfAbsent(language, new HashMap<String, Integer>());
//        for (String word: e.getValue()) {
//          if (this.nlDict.get(language).containsKey(word)) {
//            if (!this.translations.get(this.nlDict.get(language).get(word)).equals(t))
//              throw new InvalidJSONStructureException("The following " + language + " word is mapped with two distinct translation sets: " + word); 
//          }
//          this.nlDict.get(language).put(word, this.translations.size());
//        }
//      }

      this.translations.add(t);

    }
  }

  public TranslationSet getFromUSL(String usl) throws MissingTranslationException {
    if (!this.uslDict.containsKey(usl))
      throw new MissingTranslationException();
    else
      return this.translations.get(this.uslDict.get(usl));
  }

//  public TranslationSet getFromNL(String language, String word) throws MissingTranslationException {
//    if (!this.nlDict.containsKey(language) || !this.nlDict.get(language).containsKey(word))
//      throw new MissingTranslationException();
//    else
//      return this.translations.get(this.nlDict.get(language).get(word));
//  }
  
  @Override
  public String toString() {
    String str = "";
    for (TranslationSet t: this.translations)
      str += t + "\n";
    return str;
  }
}
