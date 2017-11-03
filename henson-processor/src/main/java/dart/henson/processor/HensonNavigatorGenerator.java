package dart.henson.processor;

import dart.common.BaseGenerator;
import dart.common.InjectionTarget;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.util.Collection;
import java.util.Iterator;

public class HensonNavigatorGenerator extends BaseGenerator {

  private static final String HENSON_NAVIGATOR_CLASS_NAME = "Henson";
  private static final String WITH_CONTEXT_SET_STATE_CLASS_NAME = "WithContextSetState";

  private String packageName;
  private Collection<InjectionTarget> targets;

  public HensonNavigatorGenerator(String packageName, Collection<InjectionTarget> targets) {
    if (packageName != null) {
      this.packageName = packageName;
    } else {
      this.packageName = findCommonPackage(targets);
    }

    this.targets = targets;
  }

  @Override public String brewJava() {
    TypeSpec.Builder hensonNavigatorTypeBuilder =
        TypeSpec.classBuilder(HENSON_NAVIGATOR_CLASS_NAME).addModifiers(Modifier.PUBLIC);

    emitConstructor(hensonNavigatorTypeBuilder);
    emitWith(hensonNavigatorTypeBuilder);
    emitNavigationMethods(hensonNavigatorTypeBuilder);

    //build
    JavaFile javaFile = JavaFile.builder(packageName, hensonNavigatorTypeBuilder.build())
        .addFileComment("Generated code from Henson. Do not modify!")
        .build();
    return javaFile.toString();
  }

  @Override public String getFqcn() {
    return packageName + "." + HENSON_NAVIGATOR_CLASS_NAME;
  }

  private void emitConstructor(TypeSpec.Builder intentBuilderTypeBuilder) {
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
    intentBuilderTypeBuilder.addMethod(constructorBuilder.build());
  }

  private void emitWith(TypeSpec.Builder builder) {
    TypeName withContextSetStateClassName =
        ClassName.get(packageName, HENSON_NAVIGATOR_CLASS_NAME, WITH_CONTEXT_SET_STATE_CLASS_NAME);
    MethodSpec.Builder gotoMethodBuilder = MethodSpec.methodBuilder("with")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(ClassName.get("android.content", "Context"), "context")
        .returns(withContextSetStateClassName)
        .addStatement("return new $L(context)", withContextSetStateClassName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  private void emitNavigationMethods(TypeSpec.Builder hensonNavigatorTypeBuilder) {
    TypeSpec.Builder withContextSetStateBuilder =
        TypeSpec.classBuilder(WITH_CONTEXT_SET_STATE_CLASS_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    withContextSetStateBuilder.addField(
        FieldSpec.builder(ClassName.get("android.content", "Context"), "context", Modifier.PRIVATE)
            .build());
    withContextSetStateBuilder.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ClassName.get("android.content", "Context"), "context")
        .addStatement("this.context = context")
        .build());
    for (InjectionTarget target : targets) {
      emitNavigationMethod(withContextSetStateBuilder, target);
    }
    hensonNavigatorTypeBuilder.addType(withContextSetStateBuilder.build());
  }

  private void emitNavigationMethod(TypeSpec.Builder builder, InjectionTarget target) {
    TypeName intentBuilderClassName = ClassName.bestGuess(target.classPackage
        + "."
        + target.targetClassName
        + IntentBuilderGenerator.BUNDLE_BUILDER_SUFFIX);
    String simpleTargetClassName = target.targetClassName;
    MethodSpec.Builder gotoMethodBuilder = MethodSpec.methodBuilder("goto" + simpleTargetClassName)
        .addModifiers(Modifier.PUBLIC)
        .returns(intentBuilderClassName)
        .addStatement("return new $L(context)", intentBuilderClassName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  /**
   * Finds the common package of all classes that are {@link InjectionTarget}.
   * Example 1 : {@code foo.ActivityA} and {@code foo.ActivityB} --> package foo.
   * Example 2 : {@code foo.ActivityA} and {@code foo.bar.ActivityB} --> package foo.
   * Example 3 : {@code foo.ActivityA} and {@code bar.ActivityB} --> empty package.
   * In example 3, you would be better to use the annotation processor option
   * {@link HensonProcessor#OPTION_HENSON_PACKAGE}.
   *
   * @param targets the collection of all {@link InjectionTarget}.
   * @return the name of the common package. Can be empty, but not null.
   * @see HensonProcessor
   */
  private String findCommonPackage(Collection<InjectionTarget> targets) {
    if (targets.isEmpty()) {
      return "";
    }
    final Iterator<InjectionTarget> iterator = targets.iterator();
    String commonPackageName = iterator.next().classPackage;
    while (iterator.hasNext()) {
      commonPackageName = findCommonPackage(commonPackageName, iterator.next().classPackage);
    }
    return commonPackageName;
  }

  private String findCommonPackage(String commonPackageName, String packageName) {
    while (commonPackageName.length() > 0) {
      if (packageName.startsWith(commonPackageName)) {
        return commonPackageName;
      }
      final int lastPackageSeparatorPos = commonPackageName.lastIndexOf(".");
      commonPackageName = (lastPackageSeparatorPos < 0) ? ""
          : commonPackageName.substring(0, lastPackageSeparatorPos);
    }
    return "";
  }
}
