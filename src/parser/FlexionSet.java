package parser;

import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

import io.github.vletard.analogy.set.ImmutableSet;
import reasoner.Dictionary;

public class FlexionSet extends IEMLSet<Morpheme> {

  private static final long serialVersionUID = 6890558580471932997L;
  
  private final String usl;

  private FlexionSet(HashSet<Morpheme> s, String usl) throws StyleException {
    super(s);
    this.usl = usl;
  }
  
  public static FlexionSet factory(JSONObject obj) throws StyleException{
    HashSet<Morpheme> s = new HashSet<Morpheme>();
    assert(obj.getString("type").contentEquals("polymorpheme"));
    
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
    return new FlexionSet(s, usl);
  }

  public ImmutableSet<Object> mixedTranslation(String lang, int depth, Dictionary dictionary) {
    if (depth <= 0) {
      HashSet<Object> translations = new HashSet<Object>();
      try {
        for (String tr: dictionary.getFromUSL(this.usl).get(lang)) {
          translations.add(tr);
        }
      } catch (MissingTranslationException e) {
        translations.add(this.usl); // in case no translation exist for this word in the dictionary, just output the usl
      }
      return new ImmutableSet<Object>(translations);
    }
    else {
      HashSet<Object> s = new HashSet<Object>();
      for (Morpheme m: this)
        s.add(m.mixedTranslation(lang, depth-1, dictionary));
      return new ImmutableSet<Object>(s);
    }
  }
}
