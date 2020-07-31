package parser;

import java.util.ArrayList;
import java.util.List;

public class WordRole {
  private final ArrayList<List<Morpheme>> rolePath;
  
  public WordRole() {
    this.rolePath = new ArrayList<List<Morpheme>>();
  }
  
  public IEMLObjectAttribute toIEMLUnit() {
    return new IEMLObjectAttribute(this.rolePath);
  }

  void set(List<Morpheme> path) throws StyleException {
    if (this.rolePath.size() > 0) // actually requires finer check
      throw new StyleException("More than one role is specified.");
    else
      this.rolePath.add(path);
  }
}
