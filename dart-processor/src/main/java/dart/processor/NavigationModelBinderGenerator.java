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

package dart.processor;

import static com.squareup.javapoet.ClassName.bestGuess;
import static com.squareup.javapoet.ClassName.get;
import static dart.common.util.NavigationModelBindingTargetUtil.NAVIGATION_MODEL_BINDER_SUFFIX;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dart.Dart;
import dart.common.BaseGenerator;
import dart.common.NavigationModelBindingTarget;
import javax.lang.model.element.Modifier;

/**
 * Creates Java code to bind NavigationModel into an activity/services/fragment.
 *
 * <p>{@link Dart} to use this code at runtime.
 */
public class NavigationModelBinderGenerator extends BaseGenerator {

  private final NavigationModelBindingTarget target;

  public NavigationModelBinderGenerator(NavigationModelBindingTarget target) {
    this.target = target;
  }

  @Override
  public String brewJava() {
    TypeSpec.Builder binderTypeSpec =
        TypeSpec.classBuilder(binderClassName()).addModifiers(Modifier.PUBLIC);

    emitBind(binderTypeSpec);
    emitAssign(binderTypeSpec);

    JavaFile javaFile =
        JavaFile.builder(target.classPackage, binderTypeSpec.build())
            .addFileComment("Generated code from Dart. Do not modify!")
            .build();
    return javaFile.toString();
  }

  @Override
  public String getFqcn() {
    return target.classPackage + "." + binderClassName();
  }

  private String binderClassName() {
    return target.className + NAVIGATION_MODEL_BINDER_SUFFIX;
  }

  private void emitBind(TypeSpec.Builder builder) {
    MethodSpec.Builder bindBuilder =
        MethodSpec.methodBuilder("bind")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(get(Dart.Finder.class), "finder")
            .addParameter(bestGuess(target.getFQN()), "target");

    bindBuilder.addStatement(
        "$T.bind(finder, target.$L, target)",
        get(target.navigationModelPackage, target.navigationModelClass + Dart.EXTRA_BINDER_SUFFIX),
        target.navigationModelFieldName);

    if (target.parentPackage != null) {
      // Emit a call to the superclass binder, if any.
      bindBuilder.addStatement(
          "$T.assign(target, target.$L)",
          bestGuess(target.getParentFQN() + NAVIGATION_MODEL_BINDER_SUFFIX),
          target.navigationModelFieldName);
    }

    builder.addMethod(bindBuilder.build());
  }

  private void emitAssign(TypeSpec.Builder builder) {
    MethodSpec.Builder bindBuilder =
        MethodSpec.methodBuilder("assign")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(bestGuess(target.getFQN()), "target")
            .addParameter(
                get(target.navigationModelPackage, target.navigationModelClass), "navigationModel");

    bindBuilder.addStatement("target.$L = navigationModel", target.navigationModelFieldName);

    if (target.parentPackage != null) {
      // Emit a call to the superclass binder, if any.
      bindBuilder.addStatement(
          "$T.assign(target, navigationModel)",
          bestGuess(target.getParentFQN() + NAVIGATION_MODEL_BINDER_SUFFIX));
    }

    builder.addMethod(bindBuilder.build());
  }
}
