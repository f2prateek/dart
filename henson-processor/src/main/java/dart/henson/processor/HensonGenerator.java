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

package dart.henson.processor;

import static com.squareup.javapoet.ClassName.get;
import static dart.common.util.BindingTargetUtil.BUNDLE_BUILDER_SUFFIX;
import static dart.henson.processor.IntentBuilderGenerator.INITIAL_STATE_CLASS;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import dart.common.BaseGenerator;
import dart.common.BindingTarget;
import java.util.Collection;
import java.util.Iterator;
import javax.lang.model.element.Modifier;

public class HensonGenerator extends BaseGenerator {

  private static final String HENSON_NAVIGATOR_CLASS_NAME = "Henson";
  private static final String WITH_CONTEXT_SET_STATE_CLASS_NAME = "WithContextSetState";

  private String packageName;
  private Collection<BindingTarget> targets;

  public HensonGenerator(String packageName, Collection<BindingTarget> targets) {
    if (packageName != null) {
      this.packageName = packageName;
    } else {
      this.packageName = findCommonPackage(targets);
    }

    this.targets = targets;
  }

  @Override
  public String brewJava() {
    TypeSpec.Builder hensonNavigatorTypeBuilder =
        TypeSpec.classBuilder(HENSON_NAVIGATOR_CLASS_NAME).addModifiers(Modifier.PUBLIC);

    emitConstructor(hensonNavigatorTypeBuilder);
    emitWith(hensonNavigatorTypeBuilder);
    emitNavigationMethods(hensonNavigatorTypeBuilder);

    //build
    JavaFile javaFile =
        JavaFile.builder(packageName, hensonNavigatorTypeBuilder.build())
            .addFileComment("Generated code from Henson. Do not modify!")
            .build();
    return javaFile.toString();
  }

  @Override
  public String getFqcn() {
    return packageName + "." + HENSON_NAVIGATOR_CLASS_NAME;
  }

  private void emitConstructor(TypeSpec.Builder intentBuilderTypeBuilder) {
    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
    intentBuilderTypeBuilder.addMethod(constructorBuilder.build());
  }

  private void emitWith(TypeSpec.Builder builder) {
    TypeName withContextSetStateClassName =
        get(packageName, HENSON_NAVIGATOR_CLASS_NAME, WITH_CONTEXT_SET_STATE_CLASS_NAME);
    MethodSpec.Builder gotoMethodBuilder =
        MethodSpec.methodBuilder("with")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(get("android.content", "Context"), "context")
            .returns(withContextSetStateClassName)
            .addStatement("return new $L(context)", withContextSetStateClassName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  private void emitNavigationMethods(TypeSpec.Builder hensonNavigatorTypeBuilder) {
    TypeSpec.Builder withContextSetStateBuilder =
        TypeSpec.classBuilder(WITH_CONTEXT_SET_STATE_CLASS_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
    withContextSetStateBuilder.addField(
        FieldSpec.builder(get("android.content", "Context"), "context", Modifier.PRIVATE).build());
    withContextSetStateBuilder.addMethod(
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(get("android.content", "Context"), "context")
            .addStatement("this.context = context")
            .build());
    for (BindingTarget target : targets) {
      emitNavigationMethod(withContextSetStateBuilder, target);
    }
    hensonNavigatorTypeBuilder.addType(withContextSetStateBuilder.build());
  }

  private void emitNavigationMethod(TypeSpec.Builder builder, BindingTarget target) {
    String intentBuilderClass = target.className + BUNDLE_BUILDER_SUFFIX;
    TypeName intentBuilderTypeName = get(target.classPackage, intentBuilderClass);
    TypeName initialStateTypeName =
        get(target.classPackage, intentBuilderClass, INITIAL_STATE_CLASS);
    MethodSpec.Builder gotoMethodBuilder =
        MethodSpec.methodBuilder("goto" + target.className)
            .addModifiers(Modifier.PUBLIC)
            .returns(initialStateTypeName)
            .addStatement("return $L.getInitialState(context)", intentBuilderTypeName);
    builder.addMethod(gotoMethodBuilder.build());
  }

  /**
   * Finds the common package of all classes that are {@link BindingTarget}. Example 1 : {@code
   * foo.ActivityA} and {@code foo.ActivityB} --> package foo. Example 2 : {@code foo.ActivityA} and
   * {@code foo.bar.ActivityB} --> package foo. Example 3 : {@code foo.ActivityA} and {@code
   * bar.ActivityB} --> empty package. In example 3, you would be better to use the annotation
   * processor option {@link HensonProcessor#OPTION_HENSON_PACKAGE}.
   *
   * @param targets the collection of all {@link BindingTarget}.
   * @return the name of the common package. Can be empty, but not null.
   * @see HensonProcessor
   */
  private String findCommonPackage(Collection<BindingTarget> targets) {
    if (targets.isEmpty()) {
      return "";
    }
    final Iterator<BindingTarget> iterator = targets.iterator();
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
      commonPackageName =
          (lastPackageSeparatorPos < 0)
              ? ""
              : commonPackageName.substring(0, lastPackageSeparatorPos);
    }
    return "";
  }
}
