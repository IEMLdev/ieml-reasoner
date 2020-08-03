package parser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;

public class TranslationSet implements Iterable<Map.Entry<String, List<String>>> {

  private final HashMap<String, ArrayList<String>> map;
  
  public TranslationSet(JSONObject obj) {
    this.map = new HashMap<>();
    
    final JSONObject tr = obj.getJSONObject("translations");
    for (String lang: tr.keySet()) {
      ArrayList<String> trList = new ArrayList<>();
      JSONArray arr = tr.getJSONArray(lang);
      for (int i = 0; i < arr.length(); i++)
        trList.add(arr.getString(i));
      this.map.put(lang, trList);
    }
  }

  public List<String> get(String lang) throws MissingTranslationException {
    if (!this.map.containsKey(lang))
      throw new MissingTranslationException();
    return Collections.unmodifiableList(this.map.get(lang));
  }
  
  @Override
  public String toString() {
    return this.map.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TranslationSet other = (TranslationSet) obj;
    if (map == null) {
      return other.map == null;
    } else return map.equals(other.map);
  }

  @Override
  public Iterator<Entry<String, List<String>>> iterator() {
    return new Iterator<>() {

      private final Iterator<Entry<String, ArrayList<String>>> it = TranslationSet.this.map.entrySet().iterator();

      @Override
      public boolean hasNext() {
        return this.it.hasNext();
      }

      @Override
      public Entry<String, List<String>> next() {
        return new Entry<>() {

          private final Entry<String, ArrayList<String>> entry = it.next();

          @Override
          public String getKey() {
            return this.entry.getKey();
          }

          @Override
          public List<String> getValue() {
            return Collections.unmodifiableList(this.entry.getValue());
          }

          @Override
          public List<String> setValue(List<String> arg0) {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
}