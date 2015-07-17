package com.f2prateek.dart.henson.processor;

import android.content.Context;
import com.f2prateek.dart.InjectExtra;
import com.f2prateek.dart.common.BaseGenerator;
import com.f2prateek.dart.common.InjectionTarget;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import java.util.HashSet;
import javax.lang.model.element.Modifier;

/**
 * Creates Java code to invoke intent builders
 * without having to know them explicitly.
 * This generator creates the Henson class.
 * The intent builders are created by {@link IntentBuilderGenerator}.
 * @see {@link com.f2prateek.dart.henson.Henson} to use this code at runtime.
 */
public class HensonNavigatorGenerator extends BaseGenerator {
  public static final String HENSON_NAVIGATOR_CLASS_NAME = "Henson";
  public static final String WITH_CONTEXT_SET_STATE_CLASS_NAME = "WithContextSetState";
  private String packageName;
  private Collection<String> targetClassNames;

  public HensonNavigatorGenerator(String packageName, Collection<InjectionTarget> targets) {
    if (packageName != null) {
      this.packageName = packageName;
    } else {
      this.packageName = findCommonPackage(targets);
    }

    this.targetClassNames = getAllClassNames(targets);
  }

  private String hensonNavigatorClassName() {
    return HENSON_NAVIGATOR_CLASS_NAME;
  }

  @Override public String brewJava() {
    TypeSpec.Builder hensonNavigatorTypeBuilder =
        TypeSpec.classBuilder(hensonNavigatorClassName()).addModifiers(Modifier.PUBLIC);

    emitConstructor(hensonNavigatorTypeBuilder);
    emitWith(hensonNavigatorTypeBuilder);
    emitNavigationMethods(hensonNavigatorTypeBuilder);

    //build
    JavaFile javaFile = JavaFile.builder(packageName, hensonNavigatorTypeBuilder.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();
  }

  private void emitConstructor(TypeSpec.Builder intentBuilderTypeBuilder) {
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
    intentBuilderTypeBuilder.addMethod(constructorBuilder.build());
  }

  private void emitNavigationMethods(TypeSpec.Builder hensonNavigatorTypeBuilder) {
    TypeSpec.Builder withContextSetStateBuilder =
        TypeSpec.classBuilder(WITH_CONTEXT_SET_STATE_CLASS_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    withContextSetStateBuilder.addField(
        FieldSpec.builder(Context.class, "context", Modifier.PRIVATE).build());
    withContextSetStateBuilder.addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PRIVATE)
        .addParameter(Context.class, "context")
        .addStatement("this.context = context")
        .build());
    //separate required extras from optional extras and sort both sublists.
    for (String targetClassName : targetClassNames) {
      emitNavigationMethod(withContextSetStateBuilder, targetClassName);
    }
    hensonNavigatorTypeBuilder.addType(withContextSetStateBuilder.build());
  }

  @Override public String getFqcn() {
    return packageName + "." + HENSON_NAVIGATOR_CLASS_NAME;
  }

  private void emitWith(TypeSpec.Builder builder) {
    TypeName withContextSetStateClassName =
        ClassName.get(packageName, HENSON_NAVIGATOR_CLASS_NAME, WITH_CONTEXT_SET_STATE_CLASS_NAME);
    MethodSpec.Builder gotoMethodBuilder = MethodSpec.methodBuilder("with")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(Context.class, "context")
        .returns(withContextSetStateClassName)
        .addStatement("return new $L(context)", withContextSetStateClassName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  private void emitNavigationMethod(TypeSpec.Builder builder, String targetClassName) {
    TypeName intentBuilderClassName =
        ClassName.bestGuess(targetClassName + IntentBuilderGenerator.BUNDLE_BUILDER_SUFFIX);
    String simpleTargetClassName = targetClassName.substring(targetClassName.lastIndexOf('.') + 1);
    MethodSpec.Builder gotoMethodBuilder = MethodSpec.methodBuilder("goto" + simpleTargetClassName)
        .addModifiers(Modifier.PUBLIC)
        .returns(intentBuilderClassName)
        .addStatement("return new $L(context)", intentBuilderClassName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  /**
   * Finds the common package of all classes that contain {@link InjectExtra} annotations.
   * Example 1 : {@code foo.ActivityA} and {@code foo.ActivityB} --> package foo.
   * Example 2 : {@code foo.ActivityA} and {@code foo.bar.ActivityB} --> package foo.
   * Example 3 : {@code foo.ActivityA} and {@code bar.ActivityB} --> default package.
   * In example 3, you would be better to use the annotation processor option
   * {@link HensonExtraProcessor#OPTION_HENSON_PACKAGE}.
   * @see HensonExtraProcessor
   * @param targets the collection of all {@link InjectExtra} annotation bindings.
   * @return the name of the common package. Can be empty, but not null.
   */
  private String findCommonPackage(Collection<InjectionTarget> targets) {
    String commonPackageName = null;
    for (InjectionTarget target : targets) {
      final String packageName = target.getFqcn().substring(0, target.getFqcn().lastIndexOf('.'));
      if (commonPackageName == null) {
        commonPackageName = packageName;
      } else {
        commonPackageName = findCommonPackage(commonPackageName, packageName);
      }
    }

    return commonPackageName;
  }

  private String findCommonPackage(String commonPackageName, String packageName) {
    int indexCommon = 0;
    int maxLength = Math.min(commonPackageName.length(), packageName.length());
    for (; indexCommon < maxLength; indexCommon++) {
      if (commonPackageName.charAt(indexCommon) != packageName.charAt(indexCommon)) {
        break;
      }
    }
    String commonRoot = packageName.substring(0, indexCommon);
    final int lastDotIndex = commonRoot.lastIndexOf('.');
    if (lastDotIndex != -1) {
      commonRoot = commonRoot.substring(lastDotIndex);
    }
    return commonRoot;
  }

  private Collection<String> getAllClassNames(Collection<InjectionTarget> targets) {
    Collection<String> classNames = new HashSet<>();
    for (InjectionTarget injectionTarget : targets) {
      classNames.add(injectionTarget.targetClass);
    }
    return classNames;
  }
}
