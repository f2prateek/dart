package com.f2prateek.dart.common.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for String related methods.
 */
public class StringUtil {

  // @formatter:off
  //You must go to Preferences->Code Style->General->Formatter Control
  // and check Enable formatter markers in comments for this to work.
  private static final Set<String> JAVA_KEYWORDS = new HashSet<>(
      Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
          "class", "const", "continue", "enum", "default", "do", "double", "else", "extends",
          "while", "false", "final", "finally", "float", "for", "goto", "if", "implements",
          "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package",
          "private", "protected", "public", "return", "short", "static", "strictfp", "super",
          "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void",
          "volatile"));
  // @formatter:on

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  public static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  /**
   * Returns true if the string is a valid Java identifier.
   * See <a href="https://docs.oracle.com/cd/E19798-01/821-1841/bnbuk/index.html">Identifiers</a>
   *
   * @param str the string to be examined
   * @return true if str is a valid Java identifier
   */
  public static boolean isValidJavaIdentifier(String str) {
    if (isNullOrEmpty(str)) {
      return false;
    }
    if (JAVA_KEYWORDS.contains(str)) {
      return false;
    }
    if (!Character.isJavaIdentifierStart(str.charAt(0))) {
      return false;
    }
    for (int i = 1; i < str.length(); i++) {
      if (!Character.isJavaIdentifierPart(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if the string is a valid Java full qualified class name.
   *
   * @param str the string to be examined
   * @return true if str is a valid Java Fqcn
   */
  public static boolean isValidFqcn(String str) {
    if (isNullOrEmpty(str)) {
      return false;
    }
    final String[] parts = str.split("\\.");
    if (parts.length < 2) {
      return false;
    }
    for (String part : parts) {
      if (!isValidJavaIdentifier(part)) {
        return false;
      }
    }
    return true;
  }

  private StringUtil() {
  }
}
