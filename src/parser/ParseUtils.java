package parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ParseUtils {
  private static final Pattern BLANK_PATTERN = Pattern.compile("(\\s*).*");
  
  public static int consumeBlanks(String input) {
    Matcher m = BLANK_PATTERN.matcher(input);
    if (m.matches())
      return m.group(1).length();
    else
      return 0;
  }
}
