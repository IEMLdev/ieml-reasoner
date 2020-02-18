package parser;

import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.tuple.Tuple;
import reasoner.Dictionary;

public class FlexionSet extends IEMLTuple {

  private static final long serialVersionUID = 6890558580471932997L;

  private final String usl;
  private final IEMLSet<Morpheme> morphemes;

  private FlexionSet(HashMap<Object, IEMLUnit> m, IEMLSet<Morpheme> morphemes, String usl) throws StyleException {
    super(m);
    this.usl = usl;
    this.morphemes = morphemes;
  }
  
  private static boolean checkStyle(JSONObject obj) { // TODO add verification for the subset of morphemes contained
    String type = obj.getString("type");
    if (type.contentEquals("polymorpheme"))
      return true;
    else if (type.contentEquals("morpheme")) {  // irregular value, should be uniformized
      int total = obj.getJSONArray("constant").length();
      for (int i = 0; i < obj.getJSONArray("groups").length(); i++)
        total += obj.getJSONArray("groups").getJSONArray(i).length();
      
      return total == 1;
    }
    else
      return false;
  }

  public static FlexionSet factory(JSONObject obj) throws StyleException{
    if (!checkStyle(obj))
      throw new StyleException();
    
    HashSet<Morpheme> s = new HashSet<Morpheme>();

    final String usl = obj.getString("ieml");
    final JSONArray constant = obj.getJSONArray("constant");
    for (int i = 0; i < constant.length(); i++) {
      Morpheme flexion = Morpheme.factory(constant.getJSONObject(i)); // les flexions ne peuvent appartenir qu'à une liste de sous-ensembles de morphèmes donnés
      // si deux flexions ou plus du même sous-ensemble sont présentes, elles sont dans un groupe plutôt que
      // dans les constantes et le polymorphème devient un paradigme
      if (!s.add(flexion))
        throw new StyleException("Duplicate flexion in actor.pm_flexion.constant array. Duplicate is: " + flexion);
    }
    final JSONArray groups = obj.getJSONArray("groups");
    for (int i = 0; i < groups.length(); i++) {
      JSONArray group = groups.getJSONArray(i);
      for (int j = 0; j < group.length(); j++) {
        Morpheme paradigmaticFlexion = Morpheme.factory(group.getJSONObject(j));
        if (! s.add(paradigmaticFlexion))
          throw new StyleException("Duplicate flexion in actor.pm_flexion.groups[" + i + "] array. Duplicate is: " + paradigmaticFlexion);
      }
    }

    final IEMLSet<Morpheme> morphemes = new IEMLSet<Morpheme>(s);
    final HashMap<Object, IEMLUnit> m = new HashMap<Object, IEMLUnit>();
    m.put("morphemes", morphemes);
    return new FlexionSet(m, morphemes, usl);
  }

  @Override
  public Tuple<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    if (depth <= 0) {
      try {
        HashMap<Object, Object> translations = new HashMap<Object, Object>();
        int key = 1;
        for (String tr: dictionary.getFromUSL(this.usl).get(lang))
          translations.put(key++, tr);
        return new Tuple<Object>(translations);
      } catch (MissingTranslationException e) {
        // in case no translation exist for this word in the dictionary the mixed translation continues deeper
      }
    }
    HashSet<Object> s = new HashSet<Object>();
    for (Morpheme m: this.morphemes)
      s.add(m.mixedTranslation(lang, depth-1, dictionary));
    HashMap<Object, Object> m = new HashMap<Object, Object>();
    m.put("morphemes", s);
    return new Tuple<Object>(m);
  }
}
