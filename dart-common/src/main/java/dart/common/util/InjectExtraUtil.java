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

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

import dart.BindExtra;
import dart.common.InjectionTarget;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class InjectExtraUtil {

  private final CompilerUtil compilerUtil;
  private final ParcelerUtil parcelerUtil;
  private final LoggingUtil loggingUtil;
  private final InjectionTargetUtil injectionTargetUtil;
  private final Types typeUtils;

  private RoundEnvironment roundEnv;

  public InjectExtraUtil(
      CompilerUtil compilerUtil,
      ParcelerUtil parcelerUtil,
      LoggingUtil loggingUtil,
      InjectionTargetUtil injectionTargetUtil,
      ProcessingEnvironment processingEnv) {
    this.compilerUtil = compilerUtil;
    this.parcelerUtil = parcelerUtil;
    this.loggingUtil = loggingUtil;
    this.injectionTargetUtil = injectionTargetUtil;
    typeUtils = processingEnv.getTypeUtils();
  }

  public void setRoundEnvironment(RoundEnvironment roundEnv) {
    this.roundEnv = roundEnv;
  }

  public void parseInjectExtraAnnotatedElements(Map<TypeElement, InjectionTarget> targetClassMap) {
    for (Element element : roundEnv.getElementsAnnotatedWith(BindExtra.class)) {
      try {
        parseInjectExtra(element, targetClassMap);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        loggingUtil.error(
            element,
            "Unable to generate extra injector when parsing @BindExtra.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  private void parseInjectExtra(Element element, Map<TypeElement, InjectionTarget> targetClassMap) {
    // Verify common generated code restrictions.
    if (!isValidUsageOfInjectExtra(element)) {
      return;
    }

    // Valid annotation value
    final String annotationValue = element.getAnnotation(BindExtra.class).value();
    if (!StringUtil.isNullOrEmpty(annotationValue)
        && !StringUtil.isValidJavaIdentifier(annotationValue)) {
      throw new IllegalArgumentException(
          "Keys have to be valid java variable identifiers. "
              + "https://docs.oracle.com/cd/E19798-01/821-1841/bnbuk/index.html");
    }

    // Assemble information on the injection point.
    final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
    final InjectionTarget injectionTarget =
        injectionTargetUtil.getOrCreateTargetClass(targetClassMap, enclosingElement);

    final String name = element.getSimpleName().toString();
    final String key = StringUtil.isNullOrEmpty(annotationValue) ? name : annotationValue;
    final TypeMirror type = element.asType();
    final boolean required = isRequiredInjection(element);
    final boolean parcel =
        parcelerUtil.isParcelerAvailable() && parcelerUtil.isValidExtraTypeForParceler(type);
    injectionTarget.addField(key, name, type, required, parcel);
  }

  private boolean isValidUsageOfInjectExtra(Element element) {
    final TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
    boolean valid = true;

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
      loggingUtil.error(
          element,
          "@BindExtra fields must not be private or static. (%s.%s)",
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify that the type is primitive, serializable or parcelable.
    TypeMirror typeElement = element.asType();
    if (!isValidExtraType(typeElement)
        && !(parcelerUtil.isParcelerAvailable()
            && parcelerUtil.isValidExtraTypeForParceler(typeElement))) {
      loggingUtil.error(
          element,
          "@BindExtra field must be a primitive or Serializable or "
              + "Parcelable (%s.%s). "
              + "If you use Parceler, all types supported by Parceler are allowed.",
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify containing type.
    if (enclosingElement.getKind() != CLASS) {
      loggingUtil.error(
          enclosingElement,
          "@BindExtra fields may only be contained in classes. (%s.%s)",
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify containing class visibility is not private.
    if (enclosingElement.getModifiers().contains(PRIVATE)) {
      loggingUtil.error(
          enclosingElement,
          "@BindExtra fields may not be contained in private classes. (%s.%s)",
          enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    return valid;
  }

  private boolean isValidExtraType(TypeMirror type) {
    return compilerUtil.isSerializable(type)
        || compilerUtil.isParcelable(type)
        || compilerUtil.isCharSequence(type);
  }

  /**
   * Returns {@code true} if an injection is deemed to be required. Returns false when a field is
   * annotated with any annotation named {@code Optional} or {@code Nullable}.
   */
  private boolean isRequiredInjection(Element element) {
    return !compilerUtil.hasAnnotationWithName(element, "Nullable");
  }
}
