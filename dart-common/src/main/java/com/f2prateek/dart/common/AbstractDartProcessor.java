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

package com.f2prateek.dart.common;

import com.f2prateek.dart.HensonNavigable;
import com.f2prateek.dart.InjectExtra;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.PACKAGE;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Base class of the annotation processors of Dart.
 * It collects all information about extra injections to be performed
 * when scanning the {@link com.f2prateek.dart.InjectExtra} annotations of given source files.
 * The collected information is stored in a collection of {@code InjectionTarget}.
 * All annotations processors in Dart support the option {@code #OPTION_DART_DEBUG}
 * that will log information about annotation processor and generated code.
 *
 * @see #findAndParseTargets(RoundEnvironment)
 */
public abstract class AbstractDartProcessor extends AbstractProcessor {
  public static final String OPTION_DART_DEBUG = "dart.debug";

  private Elements elementUtils;
  private Types typeUtils;
  protected Filer filer;
  protected boolean isDebugEnabled;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    elementUtils = env.getElementUtils();
    typeUtils = env.getTypeUtils();
    filer = env.getFiler();

    final Map<String, String> options = env.getOptions();
    isDebugEnabled |= options.containsKey(OPTION_DART_DEBUG) && Boolean.parseBoolean(
        options.get(OPTION_DART_DEBUG));
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportTypes = new LinkedHashSet<String>();
    supportTypes.add(InjectExtra.class.getCanonicalName());
    return supportTypes;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public Set<String> getSupportedOptions() {
    Set<String> supportedOptions = new LinkedHashSet<String>();
    supportedOptions.add(OPTION_DART_DEBUG);
    return supportedOptions;
  }

  @SuppressWarnings("This method is only used when debugging/creating tests.")
  public void setIsDebugEnabled(boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
  }

  protected abstract Map<TypeElement, InjectionTarget> findAndParseTargets(RoundEnvironment env);

  protected void parseInjectExtraAnnotatedElements(RoundEnvironment env,
      Map<TypeElement, InjectionTarget> targetClassMap, Set<TypeMirror> erasedTargetTypes) {
    for (Element element : env.getElementsAnnotatedWith(InjectExtra.class)) {
      try {
        parseInjectExtra(element, targetClassMap, erasedTargetTypes);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate extra injector when parsing @InjectExtra.\n\n%s",
            stackTrace.toString());
      }
    }
  }

  protected void parseHensonNavigableAnnotatedElements(RoundEnvironment env,
      Map<TypeElement, InjectionTarget> targetClassMap, Set<TypeMirror> erasedTargetTypes) {
    List<TypeElement> modelTypeElements = new ArrayList<>();
    for (Element element : env.getElementsAnnotatedWith(HensonNavigable.class)) {
      try {
        parseHenson((TypeElement) element, targetClassMap, erasedTargetTypes, modelTypeElements);
      } catch (Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));

        error(element, "Unable to generate extra injector when parsing @HensonNavigable.\n\n%s",
            stackTrace.toString());
      }
    }
    for (TypeElement modelTypeElement : modelTypeElements) {
      targetClassMap.remove(modelTypeElement);
    }
  }

  private boolean isValidUsageOfHenson(Class<? extends Annotation> annotationClass,
      Element element) {
    boolean valid = true;

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
      error(element, "@%s class %s must not be private or static.", annotationClass.getSimpleName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify containing type.
    if (element.getEnclosingElement() == null
        || element.getEnclosingElement().getKind() != PACKAGE) {
      error(element, "@%s class %s must be a top level class", annotationClass.getSimpleName(),
          element.getSimpleName());
      valid = false;
    }

    //verify there are no @InjectExtra annotated fields
    for (Element enclosedElement : element.getEnclosedElements()) {
      if (enclosedElement.getAnnotation(InjectExtra.class) != null) {
        error(element, "@%s class %s must not contain any @InjectExtra annotation",
            annotationClass.getSimpleName(), element.getSimpleName());
        valid = false;
      }
    }

    return valid;
  }

  private boolean isValidUsageOfInjectExtra(Class<? extends Annotation> annotationClass,
      Element element) {
    boolean valid = true;
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify modifiers.
    Set<Modifier> modifiers = element.getModifiers();
    if (modifiers.contains(PRIVATE) || modifiers.contains(STATIC)) {
      error(element, "@%s fields must not be private or static. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify containing type.
    if (enclosingElement.getKind() != CLASS) {
      error(enclosingElement, "@%s fields may only be contained in classes. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    // Verify containing class visibility is not private.
    if (enclosingElement.getModifiers().contains(PRIVATE)) {
      error(enclosingElement, "@%s fields may not be contained in private classes. (%s.%s)",
          annotationClass.getSimpleName(), enclosingElement.getQualifiedName(),
          element.getSimpleName());
      valid = false;
    }

    return valid;
  }

  private void parseHenson(TypeElement element, Map<TypeElement, InjectionTarget> targetClassMap,
      Set<TypeMirror> erasedTargetTypes, List<TypeElement> modelInjectTargets) {

    // Verify common generated code restrictions.
    if (!isValidUsageOfHenson(HensonNavigable.class, element)) {
      return;
    }

    // Assemble information on the injection point.
    InjectionTarget hensonNavigableTarget = getOrCreateTargetClass(targetClassMap, element);
    //get the model class of Henson annotation
    AnnotationMirror hensonAnnotationMirror = getAnnotationMirror(element, HensonNavigable.class);
    TypeMirror modelTypeMirror = getHensonModelMirror(hensonAnnotationMirror);
    if (modelTypeMirror != null) {
      TypeElement modelElement = (TypeElement) typeUtils.asElement(modelTypeMirror);
      if (!"Void".equals(modelElement.getSimpleName())) {
        if (isDebugEnabled) {
          System.out.println(String.format("HensonNavigable class %s uses model class %s\n",
              element.getSimpleName(), modelElement.getSimpleName()));
        }
        //we simply copy all extra injections from the model and add them to the target
        InjectionTarget modelInjectionTarget = getOrCreateTargetClass(targetClassMap, modelElement);
        modelInjectTargets.add(modelElement);
        hensonNavigableTarget.injectionMap.putAll(modelInjectionTarget.injectionMap);
      }
    }

    // Add the type-erased version to the valid injection targets set.
    TypeMirror erasedTargetType = typeUtils.erasure(element.asType());
    erasedTargetTypes.add(erasedTargetType);
  }

  private void parseInjectExtra(Element element, Map<TypeElement, InjectionTarget> targetClassMap,
      Set<TypeMirror> erasedTargetTypes) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidUsageOfInjectExtra(InjectExtra.class, element)) {
      return;
    }

    // Assemble information on the injection point.
    String name = element.getSimpleName().toString();
    String key = element.getAnnotation(InjectExtra.class).value();
    TypeMirror type = element.asType();
    boolean required = isRequiredInjection(element);
    boolean parcel =
        hasAnnotationWithFqcn(typeUtils.asElement(element.asType()), "org.parceler.Parcel");

    InjectionTarget injectionTarget = getOrCreateTargetClass(targetClassMap, enclosingElement);
    injectionTarget.addField(isNullOrEmpty(key) ? name : key, name, type, required, parcel);

    // Add the type-erased version to the valid injection targets set.
    TypeMirror erasedTargetType = typeUtils.erasure(enclosingElement.asType());
    erasedTargetTypes.add(erasedTargetType);
  }

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (not fully qualified).
   */
  private static boolean hasAnnotationWithName(Element element, String simpleName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      final Element annnotationElement = mirror.getAnnotationType().asElement();
      String annotationName = annnotationElement.getSimpleName().toString();
      if (simpleName.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
    String clazzName = clazz.getName();
    for (AnnotationMirror m : typeElement.getAnnotationMirrors()) {
      if (m.getAnnotationType().toString().equals(clazzName)) {
        return m;
      }
    }
    return null;
  }

  private static TypeMirror getHensonModelMirror(AnnotationMirror annotationMirror) {
    return getAnnotationValue(annotationMirror, "model");
  }

  private static TypeMirror getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror
        .getElementValues().entrySet()) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        return (TypeMirror) entry.getValue().getValue();
      }
    }
    return null;
  }

  /**
   * Returns {@code true} if an injection is deemed to be not required. This happens if it is
   * annotated with any annotation named {@code Optional} or {@code Nullable}.
   */
  private static boolean isRequiredInjection(Element element) {
    return !hasAnnotationWithName(element, "Nullable") && !hasAnnotationWithName(element,
        "Optional");
  }

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (must be a fully qualified class name).
   */
  private static boolean hasAnnotationWithFqcn(Element element, String annotationClassNameName) {
    if (element != null) {
      for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
        if (annotationMirror.getAnnotationType()
            .asElement()
            .toString()
            .equals(annotationClassNameName)) {
          return true;
        }
      }
    }
    return false;
  }

  private InjectionTarget getOrCreateTargetClass(Map<TypeElement, InjectionTarget> targetClassMap,
      TypeElement typeElement) {
    InjectionTarget injectionTarget = targetClassMap.get(typeElement);
    if (injectionTarget == null) {
      final String targetType = typeElement.getQualifiedName().toString();
      final String classPackage = getPackageName(typeElement);
      final String className = getClassName(typeElement, classPackage);
      final boolean isAbstractType = typeElement.getModifiers().contains(Modifier.ABSTRACT);

      injectionTarget = new InjectionTarget(classPackage, className, targetType, isAbstractType);
      targetClassMap.put(typeElement, injectionTarget);
    }
    return injectionTarget;
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  /** Finds the parent injector type in the supplied set, if any. */
  protected String findParentFqcn(TypeElement typeElement, Set<TypeMirror> parents) {
    TypeMirror type;
    while (true) {
      type = typeElement.getSuperclass();
      if (type.getKind() == TypeKind.NONE) {
        return null;
      }
      typeElement = (TypeElement) ((DeclaredType) type).asElement();
      if (containsTypeMirror(parents, type)) {
        String packageName = getPackageName(typeElement);
        return packageName + "." + getClassName(typeElement, packageName);
      }
    }
  }

  /**
   * Returns true if the string is null or 0-length.
   *
   * @param str the string to be examined
   * @return true if str is null or zero length
   */
  private static boolean isNullOrEmpty(String str) {
    return str == null || str.trim().length() == 0;
  }

  private boolean containsTypeMirror(Collection<TypeMirror> mirrors, TypeMirror query) {
    // Ensure we are checking against a type-erased version for normalization purposes.
    query = typeUtils.erasure(query);

    for (TypeMirror mirror : mirrors) {
      if (typeUtils.isSameType(mirror, query)) {
        return true;
      }
    }
    return false;
  }

  protected void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }
}
