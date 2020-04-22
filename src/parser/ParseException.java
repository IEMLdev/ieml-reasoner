package parser;

public class ParseException extends Exception {
  private static final long serialVersionUID = -2059831230073491075L;
  
  public ParseException(String msg) {
    super(msg);
  }

  public ParseException(String msg, Exception throwable) {
    super(msg, throwable);
  }
}
