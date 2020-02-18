package parser;

public class MissingTranslationException extends Exception {

  private static final long serialVersionUID = 7072025540046750391L;

  public MissingTranslationException() {
    super();
  }
  
  public MissingTranslationException(String string) {
    super(string);
  }
}
