package com.f2prateek.dart.common;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class AbstractDartProcessorTest {
  @Test public void testIsValidJavaIdentifier_empty() {
    String empty = "";
    assertThat(AbstractDartProcessor.isValidJavaIdentifier(empty)).isEqualTo(false);
  }

  @Test public void testIsValidJavaIdentifier_keyword() {
    String keyword = "final";
    assertThat(AbstractDartProcessor.isValidJavaIdentifier(keyword)).isEqualTo(false);
  }

  @Test public void testIsValidJavaIdentifier_wrongStart() {
    String wrongStart = "8var";
    assertThat(AbstractDartProcessor.isValidJavaIdentifier(wrongStart)).isEqualTo(false);
  }

  @Test public void testIsValidJavaIdentifier_wrongPart() {
    String wrongPart = "a.b";
    assertThat(AbstractDartProcessor.isValidJavaIdentifier(wrongPart)).isEqualTo(false);
  }

  @Test public void testIsValidJavaIdentifier_valid() {
    String valid = "a$valid_IDENTIFIER";
    assertThat(AbstractDartProcessor.isValidJavaIdentifier(valid)).isEqualTo(true);
  }
}
