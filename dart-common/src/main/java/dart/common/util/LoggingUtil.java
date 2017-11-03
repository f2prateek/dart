/*
 * Copyright 2013 Jake Wharton
 * Copyright 2014 Prateek Srivastava (@f2prateek)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dart.common.util;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/** Utility class for logging related methods. */
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
