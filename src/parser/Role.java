package parser;

import java.util.LinkedList;

import org.json.JSONArray;

public class Role extends IEMLSequence<IEMLStringAttribute> {
  
  private static final long serialVersionUID = 72985125223312666L;

  private static LinkedList<IEMLStringAttribute> extractJSON(JSONArray arr){
    LinkedList<IEMLStringAttribute> l = new LinkedList<IEMLStringAttribute>();
    for (int i = 0; i < arr.length(); i++)
      l.add(new IEMLStringAttribute(arr.getString(i)));
    return l;
  }

  public Role(JSONArray arr) {
    super(extractJSON(arr));
  }

}
