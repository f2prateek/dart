package com.f2prateek.dart.common.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Utility class for type and element related methods.
 */
public class CompilerUtil {

  private final Elements elementUtils;
  private final Types typeUtils;

  public CompilerUtil(ProcessingEnvironment processingEnv) {
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  /** Finds the parent injector type in the supplied set, if any. */
  public TypeElement findParent(TypeElement typeElement, Set<TypeElement> parents) {
    TypeMirror type;
    while (true) {
      type = typeElement.getSuperclass();
      if (type.getKind() == TypeKind.NONE) {
        return null;
      }
      typeElement = (TypeElement) ((DeclaredType) type).asElement();
      if (containsTypeMirror(parents, type)) {
        return typeElement;
      }
    }
  }

  public boolean hasAnnotationWithFqcn(Element element, String annotationClassNameName) {
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

  /**
   * Returns {@code true} if the an annotation is found on the given element with the given class
   * name (not fully qualified).
   */
  public boolean hasAnnotationWithName(Element element, String simpleName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      final Element annnotationElement = mirror.getAnnotationType().asElement();
      String annotationName = annnotationElement.getSimpleName().toString();
      if (simpleName.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isSerializable(TypeMirror type) {
    return isAssignable(type, "java.io.Serializable");
  }

  public boolean isParcelable(TypeMirror type) {
    return isAssignable(type, "android.os.Parcelable");
  }

  public boolean isCharSequence(TypeMirror type) {
    return isAssignable(type, "java.lang.CharSequence");
  }

  public List<Element> getTypeElements(String[] classNames) {
    List<Element> elements = new ArrayList<>();
    for (String className : classNames) {
      elements.add(this.elementUtils.getTypeElement(className));
    }
    return elements;
  }

  public String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  public String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }

  public boolean existsWithin(TypeMirror type, List<Element> supportedTypes) {
    for (Element supportedType : supportedTypes) {
      if (typeUtils.erasure(type).equals(typeUtils.erasure(supportedType.asType()))) {
        return true;
      }
    }
    return false;
  }

  private boolean isAssignable(TypeMirror type, String assignableType) {
    TypeMirror charSequenceTypeMirror = elementUtils.getTypeElement(assignableType).asType();
    return typeUtils.isAssignable(type, charSequenceTypeMirror);
  }

  private boolean containsTypeMirror(Collection<TypeElement> typeElements, TypeMirror query) {
    // Ensure we are checking against a type-erased version for normalization purposes.
    query = typeUtils.erasure(query);
    for (TypeElement element : typeElements) {
      final TypeMirror typeMirror = typeUtils.erasure(element.asType());
      if (typeUtils.isSameType(typeMirror, query)) {
        return true;
      }
    }
    return false;
  }
}
