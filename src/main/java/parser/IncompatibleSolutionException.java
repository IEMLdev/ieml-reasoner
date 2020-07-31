package parser;

public class IncompatibleSolutionException extends Exception {
  private static final long serialVersionUID = -5964176733215177343L;
  
  public IncompatibleSolutionException(Exception cause) {
    super(cause);
  }

  public IncompatibleSolutionException(String msg) {
    super(msg);
  }
}
