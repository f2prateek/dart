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
package com.f2prateek.dart.processor;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.common.BaseGenerator;
import com.f2prateek.dart.common.Binding;
import com.f2prateek.dart.common.ExtraInjection;
import com.f2prateek.dart.common.FieldBinding;
import com.f2prateek.dart.common.InjectionTarget;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;

/**
 * Creates Java code to inject extras into an activity
 * or a {@link android.os.Bundle}.
 *
 * @see {@link Dart} to use this code at runtime.
 */
public class ExtraInjectorGenerator extends BaseGenerator {

  private final InjectionTarget target;

  public ExtraInjectorGenerator(InjectionTarget target) {
    this.target = target;
  }

  @Override public String brewJava() {
    TypeSpec.Builder injectorTypeSpec =
        TypeSpec.classBuilder(injectorClassName()).addModifiers(Modifier.PUBLIC);
    emitInject(injectorTypeSpec);
    JavaFile javaFile = JavaFile.builder(target.classPackage, injectorTypeSpec.build())
        .addFileComment("Generated code from Dart. Do not modify!")
        .build();
    return javaFile.toString();
  }

  @Override public String getFqcn() {
    return target.classPackage + "." + injectorClassName();
  }

  private String injectorClassName() {
    return target.className + Dart.INJECTOR_SUFFIX;
  }

  private void emitInject(TypeSpec.Builder builder) {
    MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("inject")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(ClassName.get(Dart.Finder.class), "finder")
        .addParameter(ClassName.bestGuess(target.classFqcnCanonical), "target")
        .addParameter(ClassName.get(Object.class), "source");

    if (target.parentClassFqcn != null) {
      // Emit a call to the superclass injector, if any.
      injectBuilder.addStatement("$T.inject(finder, target, source)",
          ClassName.bestGuess(target.parentClassFqcn + Dart.INJECTOR_SUFFIX));
    }

    // Local variable in which all extras will be temporarily stored.
    injectBuilder.addStatement("Object object");

    // Loop over each extras injection and emit it.
    for (ExtraInjection injection : target.injectionMap.values()) {
      emitExtraInjection(injectBuilder, injection);
    }

    builder.addMethod(injectBuilder.build());
  }

  private void emitExtraInjection(MethodSpec.Builder builder, ExtraInjection injection) {
    builder.addStatement("object = finder.getExtra(source, $S)", injection.getKey());

    List<Binding> requiredBindings = injection.getRequiredBindings();
    if (!requiredBindings.isEmpty()) {
      builder.beginControlFlow("if (object == null)")
          .addStatement("throw new IllegalStateException(\"Required extra with key '$L' for $L "
                  + "was not found. If this extra is optional add '@Nullable' annotation.\")",
              injection.getKey(), emitHumanDescription(requiredBindings))
          .endControlFlow();
      emitFieldBindings(builder, injection);
    } else {
      // an optional extra, wrap it in a check to keep original value, if any
      builder.beginControlFlow("if (object != null)");
      emitFieldBindings(builder, injection);
      builder.endControlFlow();
    }
  }

  private void emitFieldBindings(MethodSpec.Builder builder, ExtraInjection injection) {
    Collection<FieldBinding> fieldBindings = injection.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return;
    }

    for (FieldBinding fieldBinding : fieldBindings) {
      builder.addCode("target.$L = ", fieldBinding.getName());

      if (fieldBinding.isParcel()) {
        builder.addCode("org.parceler.Parcels.unwrap((android.os.Parcelable) object);\n");
      } else {
        emitCast(builder, fieldBinding.getType());
        builder.addCode("object;\n");
      }
    }
  }

  private void emitCast(MethodSpec.Builder builder, TypeMirror fieldType) {
    builder.addCode("($T) ", TypeName.get(fieldType));
  }

  //TODO add android annotations dependency to get that annotation plus others.

  /** Visible for testing */
  String emitHumanDescription(List<Binding> bindings) {
    StringBuilder builder = new StringBuilder();
    switch (bindings.size()) {
      case 1:
        builder.append(bindings.get(0).getDescription());
        break;
      case 2:
        builder.append(bindings.get(0).getDescription())
            .append(" and ")
            .append(bindings.get(1).getDescription());
        break;
      default:
        for (int i = 0, count = bindings.size(); i < count; i++) {
          Binding requiredField = bindings.get(i);
          if (i != 0) {
            builder.append(", ");
          }
          if (i == count - 1) {
            builder.append("and ");
          }
          builder.append(requiredField.getDescription());
        }
        break;
    }
    return builder.toString();
  }
}
