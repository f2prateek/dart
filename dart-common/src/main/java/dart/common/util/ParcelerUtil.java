package dart.common.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * Utility class for Parceler library related methods.
 */
public class ParcelerUtil {

  private final CompilerUtil compilerUtil;
  private final Elements elementUtils;
  private final Types typeUtils;

  private final List<Element> singleCollections;
  private final List<Element> doubleCollections;

  // Flag to force enabling/disabling Parceler.
  // Used for testing.
  private final boolean isEnabled;

  public ParcelerUtil(CompilerUtil compilerUtil, ProcessingEnvironment processingEnv,
      boolean enable) {
    this.compilerUtil = compilerUtil;
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
    isEnabled = enable;

    singleCollections = compilerUtil.getTypeElements(new String[] {
        "java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Set",
        "java.util.HashSet", "java.util.SortedSet", "java.util.TreeSet", "java.util.LinkedHashSet",
        "android.util.SparseArray"
    });

    doubleCollections = compilerUtil.getTypeElements(new String[] {
        "java.util.Map", "java.util.HashMap", "java.util.LinkedHashMap", "java.util.SortedMap",
        "java.util.TreeMap"
    });
  }

  public boolean isParcelerAvailable() {
    return isEnabled && elementUtils.getTypeElement("org.parceler.Parcel") != null;
  }

  public boolean isValidExtraTypeForParceler(TypeMirror type) {
    return isValidForParceler(type, false);
  }

  private boolean isValidForParceler(TypeMirror type, boolean subCollection) {
    if (subCollection && (compilerUtil.isSerializable(type) || compilerUtil.isParcelable(type))) {
      return true;
    }
    if (isAnnotatedWithParcel(type)) {
      return true;
    }
    if (type instanceof DeclaredType) {
      DeclaredType declaredType = (DeclaredType) type;
      if (compilerUtil.existsWithin(type, singleCollections)) {
        return isValidForParceler(declaredType.getTypeArguments().get(0), true);
      }
      if (compilerUtil.existsWithin(type, doubleCollections)) {
        return isValidForParceler(declaredType.getTypeArguments().get(0), true)
            && isValidForParceler(declaredType.getTypeArguments().get(1), true);
      }
    }
    return false;
  }

  private boolean isAnnotatedWithParcel(TypeMirror type) {
    return compilerUtil.hasAnnotationWithFqcn(typeUtils.asElement(type), "org.parceler.Parcel");
  }
}
