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

import com.f2prateek.dart.InjectExtra;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.ElementKind.CLASS;
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

  // @formatter:off
  //You must go to Preferences->Code Style->General->Formatter Control
  // and check Enable formatter markers in comments for this to work.
  private static final Set<String> JAVA_KEYWORDS = new HashSet<>(Arrays.asList(
      "abstract",  "assert",       "boolean",    "break",      "byte",      "case",
      "catch",     "char",         "class",      "const",     "continue",   "enum",
      "default",   "do",           "double",     "else",      "extends",    "while",
      "false",     "final",        "finally",    "float",     "for",
      "goto",      "if",           "implements", "import",    "instanceof",
      "int",       "interface",    "long",       "native",    "new",
      "null",      "package",      "private",    "protected", "public",
      "return",    "short",        "static",     "strictfp",  "super",
      "switch",    "synchronized", "this",       "throw",     "throws",
      "transient", "true",         "try",        "void",      "volatile"
  ));
  // @formatter:on

  private List<Element> singleCollections;
  private List<Element> doubleCollections;
  private Elements elementUtils;
  protected Types typeUtils;
  protected Filer filer;
  protected boolean isDebugEnabled;
  protected boolean usesParcelerOption = true;

  protected abstract Map<TypeElement, InjectionTarget> findAndParseTargets(RoundEnvironment env);

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    elementUtils = env.getElementUtils();
    typeUtils = env.getTypeUtils();
    filer = env.getFiler();

    //note for maintenance : here we use class names directly
    //as a general rule of thumb, we should not use classes in an annotation processor
    //as those classes are different from classes seen at runtime.
    //Using class created a bug in apps built by gradle as SparseArray was not found during
    //annotation processing time.
    singleCollections = getTypeElements(new String[] {
        "java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Set",
        "java.util.HashSet", "java.util.SortedSet", "java.util.TreeSet", "java.util.LinkedHashSet",
        "android.util.SparseArray"
    });

    doubleCollections = getTypeElements(new String[] {
        "java.util.Map", "java.util.HashMap", "java.util.LinkedHashMap", "java.util.SortedMap",
        "java.util.TreeMap"
    });

    final Map<String, String> options = env.getOptions();
    isDebugEnabled |= options.containsKey(OPTION_DART_DEBUG) && Boolean.parseBoolean(
        options.get(OPTION_DART_DEBUG));
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportTypes = new LinkedHashSet<>();
    supportTypes.add(InjectExtra.class.getCanonicalName());
    return supportTypes;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public Set<String> getSupportedOptions() {
    Set<String> supportedOptions = new LinkedHashSet<>();
    supportedOptions.add(OPTION_DART_DEBUG);
    return supportedOptions;
  }

  @SuppressWarnings("This method is only used when debugging/creating tests.")
  public void setIsDebugEnabled(boolean isDebugEnabled) {
    this.isDebugEnabled = isDebugEnabled;
  }

  public void setUsesParcelerOption(boolean usesParcelerOption) {
    this.usesParcelerOption = usesParcelerOption;
  }

  protected InjectionTarget getOrCreateTargetClass(Map<TypeElement, InjectionTarget> targetClassMap,
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

  protected void error(Element element, String message, Object... args) {
    processingEnv.getMessager().printMessage(ERROR, String.format(message, args), element);
  }

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

  private void parseInjectExtra(Element element, Map<TypeElement, InjectionTarget> targetClassMap,
      Set<TypeMirror> erasedTargetTypes) {
    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

    // Verify common generated code restrictions.
    if (!isValidUsageOfInjectExtra(InjectExtra.class, element)) {
      return;
    }

    String annotationValue = element.getAnnotation(InjectExtra.class).value();
    if (!isNullOrEmpty(annotationValue) && !isValidJavaIdentifier(annotationValue)) {
      throw new IllegalArgumentException("Keys have to be valid java variable identifiers. "
          + "https://docs.oracle.com/cd/E19798-01/821-1841/bnbuk/index.html");
    }

    // Assemble information on the injection point.
    String name = element.getSimpleName().toString();
    String key = isNullOrEmpty(annotationValue) ? name : annotationValue;
    TypeMirror type = element.asType();
    boolean required = isRequiredInjection(element);
    boolean parcel = isParcelerAvailable() && isValidExtraTypeForParceler(type);

    InjectionTarget injectionTarget = getOrCreateTargetClass(targetClassMap, enclosingElement);
    injectionTarget.addField(key, name, type, required, parcel);

    // Add the type-erased version to the valid injection targets set.
    TypeMirror erasedTargetType = typeUtils.erasure(enclosingElement.asType());
    erasedTargetTypes.add(erasedTargetType);
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

    //Verify that the type is primitive or serializable or parcelable
    TypeMirror typeElement = element.asType();
    if (!isValidExtraType(typeElement) && !(isParcelerAvailable() && isValidExtraTypeForParceler(
        typeElement))) {
      error(element, "@%s field must be a primitive or Serializable or Parcelable (%s.%s). "
              + "If you use Parceler, all types supported by Parceler are allowed.",
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

  private boolean isValidExtraType(TypeMirror type) {
    return isSerializable(type) || isParcelable(type) || isCharSequence(type);
  }

  private boolean isValidExtraTypeForParceler(TypeMirror type) {
    return isValidForParceler(type, false);
  }

  private boolean isValidForParceler(TypeMirror type, boolean subCollection) {
    if (subCollection && (isSerializable(type) || isParcelable(type))) {
      return true;
    }
    if (isAnnotatedWithParcel(type)) {
      return true;
    }
    if (type instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) type;
      if (existsWithin(type, singleCollections)) {
        return isValidForParceler(declaredType.getTypeArguments().get(0), true);
      }
      if (existsWithin(type, doubleCollections)) {
        return isValidForParceler(declaredType.getTypeArguments().get(0), true)
            && isValidForParceler(declaredType.getTypeArguments().get(1), true);
      }
    }
    return false;
  }

  private boolean isSerializable(TypeMirror type) {
    TypeMirror serializableTypeMirror =
        elementUtils.getTypeElement("java.io.Serializable").asType();
    return typeUtils.isAssignable(type, serializableTypeMirror);
  }

  private boolean isParcelable(TypeMirror type) {
    TypeMirror parcelableTypeMirror = elementUtils.getTypeElement("android.os.Parcelable").asType();
    return typeUtils.isAssignable(type, parcelableTypeMirror);
  }

  private boolean isCharSequence(TypeMirror type) {
    TypeMirror charSequenceTypeMirror =
        elementUtils.getTypeElement("java.lang.CharSequence").asType();
    return typeUtils.isAssignable(type, charSequenceTypeMirror);
  }

  private boolean isParcelerAvailable() {
    return usesParcelerOption && elementUtils.getTypeElement("org.parceler.Parcel") != null;
  }

  private boolean isAnnotatedWithParcel(TypeMirror type) {
    return hasAnnotationWithFqcn(typeUtils.asElement(type), "org.parceler.Parcel");
  }

  private boolean existsWithin(TypeMirror type, List<Element> supportedTypes) {
    for (Element supportedType : supportedTypes) {
      if (typeUtils.erasure(type).equals(typeUtils.erasure(supportedType.asType()))) {
        return true;
      }
    }
    return false;
  }

  private List<Element> getTypeElements(String[] classNames) {
    List<Element> elements = new ArrayList<>();
    for (String className : classNames) {
      elements.add(elementUtils.getTypeElement(className));
    }
    return elements;
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

  /**
   * Returns {@code true} if an injection is deemed to be required. Returns false when a field is
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

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
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

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  /**
   * Returns true if the string is a valid Java identifier.
   * See <a href="https://docs.oracle.com/cd/E19798-01/821-1841/bnbuk/index.html">Identifiers</a>
   *
   * @param str the string to be examined
   * @return true if str is a valid Java identifier
   */
  static boolean isValidJavaIdentifier(String str) {
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
}
