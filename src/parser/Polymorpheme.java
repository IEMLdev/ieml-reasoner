package parser;

import java.util.LinkedList;

import org.json.JSONArray;

import util.Sequence;

public class Polymorpheme extends Sequence<Object> {

  private static LinkedList<Object> extractJSON(JSONArray arr) {  // c'est un ensemble et non une séquence
    LinkedList<Object> l = new LinkedList<Object>();
    for (int i = 0; i < arr.length(); i++)
      l.add(new Morpheme(arr.getJSONObject(i)));
    return l;
  }
  
  public Polymorpheme(JSONArray arr) {
    super(extractJSON(arr));
  }

}
