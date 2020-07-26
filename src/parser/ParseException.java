package parser;

public class ParseException extends Exception {
  private static final long serialVersionUID = -2059831230073491075L;
  private final int offset;
  
  public ParseException(Class<?> clazz, int offset, String input) {
    super(offset + ": cannot parse a valid " + clazz.getName() + " in " + input);
    this.offset = offset;
  }

  public int getOffset() {
    return this.offset;
  }
}
