package reasoner;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import parser.JSONStructureException;
import parser.MissingTranslationException;
import parser.ParseException;
import parser.TranslationSet;
import parser.Writable;
import parser.WritableBuilder;

public class Dictionary {

  private final ArrayList<TranslationSet> translations;
  private final HashMap<String, Integer> uslDict;
  private final HashMap<Writable, Integer> writableDict;

  public Dictionary(ArrayList<JSONObject> translations) throws JSONStructureException {
    this.translations = new ArrayList<TranslationSet>();
    this.uslDict = new HashMap<String, Integer>();
    this.writableDict = new HashMap<Writable, Integer>();

    for (JSONObject obj: translations) {
      TranslationSet t = new TranslationSet(obj);
      
      if (this.uslDict.containsKey(t.getUsl())) {
        if (this.translations.get(this.uslDict.get(t.getUsl())).equals(t))
          System.err.println("Warning: duplicate entry in input JSON file: " + t.getUsl());
        else
          throw new JSONStructureException("The following USL is mapped with two distinct translation sets: " + t.getUsl());
      }
      else {
        this.uslDict.put(t.getUsl(), this.translations.size());
        try {
          this.writableDict.put(WritableBuilder.parseAny(t.getUsl()), this.translations.size());
        } catch (ParseException e) {
          e.printStackTrace();
        }
      }

      this.translations.add(t);
    }
  }

  public TranslationSet get(String usl) throws MissingTranslationException {
    if (!this.uslDict.containsKey(usl))
      throw new MissingTranslationException();
    else
      return this.translations.get(this.uslDict.get(usl));
  }
  
  public TranslationSet get(Writable object) throws MissingTranslationException {
    if (!this.writableDict.containsKey(object))
      throw new MissingTranslationException();
    else
      return this.translations.get(this.writableDict.get(object));
  }

  @Override
  public String toString() {
    String str = "";
    for (TranslationSet t: this.translations)
      str += t + "\n";
    return str;
  }
}
