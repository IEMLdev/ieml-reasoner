package parser;

import java.util.LinkedList;

import org.json.JSONArray;

import util.Sequence;

public class Role extends Sequence<String> {
  
  private static LinkedList<String> extractJSON(JSONArray arr){
    LinkedList<String> l = new LinkedList<String>();
    for (int i = 0; i < arr.length(); i++)
      l.add(arr.getString(i));
    return l;
  }

  public Role(JSONArray arr) {
    super(extractJSON(arr));
  }

}
