package parser;

import org.json.JSONObject;

import util.CharacterSequence;

public class Morpheme extends CharacterSequence {
  
  private static String extractJSON(JSONObject obj){
    String type = obj.getString("type");
    assert(type.equals("morpheme"));
    
    return obj.getString("ieml");  // TODO replace by deeper structure
  }

  public Morpheme(JSONObject obj) {
    super(extractJSON(obj));
  }
}
