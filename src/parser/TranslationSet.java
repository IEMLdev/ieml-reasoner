package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class TranslationSet implements Iterable<Map.Entry<String, List<String>>> {

  private final String usl;
  private final HashMap<String, ArrayList<String>> map;
  
  public TranslationSet(JSONObject obj) {
    this.map = new HashMap<String, ArrayList<String>>();
    if (!obj.isNull("usl"))  // this should better be identical in every type of json dump
      this.usl = obj.getString("usl");
    else
      this.usl = obj.getString("ieml");
    
    final JSONObject tr = obj.getJSONObject("translations");
    for (String lang: tr.keySet()) {
      ArrayList<String> trList = new ArrayList<String>();
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
  
  public String getUsl() {
    return this.usl;
  }
  
  @Override
  public String toString() {
    return this.usl + " " + this.map.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    result = prime * result + ((usl == null) ? 0 : usl.hashCode());
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
      if (other.map != null)
        return false;
    } else if (!map.equals(other.map))
      return false;
    if (usl == null) {
      if (other.usl != null)
        return false;
    } else if (!usl.equals(other.usl))
      return false;
    return true;
  }

  @Override
  public Iterator<Entry<String, List<String>>> iterator() {
    return new Iterator<Map.Entry<String,List<String>>>() {

      private final Iterator<Entry<String, ArrayList<String>>> it = TranslationSet.this.map.entrySet().iterator();
      
      @Override
      public boolean hasNext() {
        return this.it.hasNext();
      }

      @Override
      public Entry<String, List<String>> next() {
        return new Entry<String, List<String>>() {

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