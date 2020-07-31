package parser;

public class JSONStructureException extends Exception {
  private static final long serialVersionUID = -2384853757616839552L;
  
  public JSONStructureException() {
    super();
  }
  
  public JSONStructureException(String str) {
    super(str);
  }
}
