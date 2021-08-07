package reasoner;

import org.json.JSONObject;
import parser.*;

import java.util.*;

public class Dictionary {

  private final List<TranslationSet> translations;
  private final Map<Writable, Integer> writableDict;

  public Dictionary(ArrayList<JSONObject> jsonTranslations) throws JSONStructureException {
    ArrayList<TranslationSet> translations = new ArrayList<>();
    HashMap<Writable, Integer> writableDict = new HashMap<>();

    for (JSONObject obj: jsonTranslations) {
      TranslationSet t = new TranslationSet(obj);
      String usl =  obj.getString("usl");
      
      for (Writable w: WritableBuilder.parseAny(usl)) {
        if (writableDict.containsKey(w)) {
          if (translations.get(writableDict.get(w)).equals(t))
            System.err.println("Warning: duplicate entry in input JSON file: " + usl);
          else
            throw new JSONStructureException("The following USL is mapped with two distinct translation sets: " + usl);
        }
        else {
          writableDict.put(w, translations.size());
          translations.add(t);
        }
      }
    }

    this.translations = Collections.unmodifiableList(translations);
    this.writableDict = Collections.unmodifiableMap(writableDict);
  }

  public TranslationSet get(Writable w) throws MissingTranslationException {
    if (!this.writableDict.containsKey(w))
      throw new MissingTranslationException();
    return this.translations.get(this.writableDict.get(w));
  }

  @Override
  public String toString() {
    String str = "";
    for (TranslationSet t: this.translations)
      str += t + "\n";
    return str;
  }
}
