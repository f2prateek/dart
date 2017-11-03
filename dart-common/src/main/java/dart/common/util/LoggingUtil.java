package dart.common.util;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Utility class for logging related methods.
 */
public class LoggingUtil {

  private final Messager messager;

  public LoggingUtil(ProcessingEnvironment processingEnv) {
    messager = processingEnv.getMessager();
  }

  public void error(Element element, String message, Object... args) {
    messager.printMessage(ERROR, String.format(message, args), element);
  }

  public void warning(Element element, String message, Object... args) {
    messager.printMessage(WARNING, String.format(message, args), element);
  }
}
