package reasoner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import parser.JSONParser;
import util.Tuple;

public class Reasoner{
  public static void main(String[] args) throws FileNotFoundException {
    Scanner scanner = new Scanner(new File("resources/words_sample.json"));
    scanner.useDelimiter("\\A");
    JSONArray arr = new JSONArray(scanner.next());
    scanner.close();
    
    for (int i = 0; i < arr.length(); i++) {
      JSONObject obj = arr.getJSONObject(i);
      Tuple<Object> t = JSONParser.parseJSON(obj);
      System.out.println(i + ":\n" + t.prettyPrint(0));
    }
  }
}
