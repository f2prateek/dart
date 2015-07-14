package com.f2prateek.dart.henson.processor;

import android.content.Context;
import android.content.Intent;
import com.f2prateek.dart.common.ExtraInjection;
import com.f2prateek.dart.common.FieldBinding;
import com.f2prateek.dart.common.InjectionTarget;
import com.f2prateek.dart.henson.Bundler;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class IntentBuilder {
  public static final String BUNDLE_BUILDER_SUFFIX = "$$IntentBuilder";

  private final InjectionTarget target;

  public IntentBuilder(InjectionTarget target) {
    this.target = target;
  }

  private String builderClassName() {
    return target.className + BUNDLE_BUILDER_SUFFIX;
  }

  String brewJava() {
    TypeSpec.Builder intentBuilderTypeBuilder =
        TypeSpec.classBuilder(builderClassName()).addModifiers(Modifier.PUBLIC);

    //fields
    FieldSpec.Builder intentFieldBuilder =
        FieldSpec.builder(Intent.class, "intent", Modifier.PRIVATE);
    intentBuilderTypeBuilder.addField(intentFieldBuilder.build());
    FieldSpec.Builder bundlerFieldBuilder =
        FieldSpec.builder(Bundler.class, "bundler", Modifier.PRIVATE);
    bundlerFieldBuilder.initializer("Bundler.create()");
    intentBuilderTypeBuilder.addField(bundlerFieldBuilder.build());

    //constructor
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(Context.class, "context")
        .addStatement("intent = new Intent(context, $L)", target.className + ".class");
    intentBuilderTypeBuilder.addMethod(constructorBuilder.build());

    //getters and setters
    emitSetters(intentBuilderTypeBuilder);
    emitGetter(intentBuilderTypeBuilder);

    //build
    JavaFile javaFile = JavaFile.builder(target.classPackage, intentBuilderTypeBuilder.build()).
        addFileComment("Generated code from Dart. Do not modify!").
        build();
    return javaFile.toString();
  }

  private void emitGetter(TypeSpec.Builder builder) {
    MethodSpec.Builder getBuilder = MethodSpec.methodBuilder("get")
        .addModifiers(Modifier.PUBLIC)
        .returns(Intent.class)
        .addStatement("intent.putExtras(bundler.get())")
        .addStatement("return intent");
    builder.addMethod(getBuilder.build());
  }

  private void emitSetters(TypeSpec.Builder builder) {
    // Loop over each extras injection and emit it.
    for (ExtraInjection injection : target.injectionMap.values()) {
      emitSetter(builder, injection);
    }
  }

  private void emitSetter(TypeSpec.Builder builder, ExtraInjection injection) {
    Collection<FieldBinding> fieldBindings = injection.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return;
    }

    // We can have the same extra key bound to multiple fields. We generate builders by key name,
    // so we pick the first field binding and use it's type since they should all be the same
    // anyway.
    FieldBinding firstFieldBinding = fieldBindings.iterator().next();
    TypeMirror extraType = firstFieldBinding.getType();

    final String value;
    if (firstFieldBinding.isParcel()) {
      value = "org.parceler.Parcels.wrap(" + injection.getKey() + ')';
    } else {
      value = injection.getKey();
    }

    MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(injection.getKey())
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.bestGuess(builderClassName()))
        .addParameter(ClassName.bestGuess(getType(extraType)), injection.getKey())
        .addStatement("bundler.put($S,$L)", injection.getKey(), value)
        .addStatement("return this");
    builder.addMethod(setterBuilder.build());
  }

  public String getFqcn() {
    return target.getFqcn() + BUNDLE_BUILDER_SUFFIX;
  }

  static String getType(TypeMirror type) {
    if (type.getKind().isPrimitive()) {
      // Get wrapper for primitive types
      switch (type.getKind()) {
        case BOOLEAN:
          return "java.lang.Boolean";
        case BYTE:
          return "java.lang.Byte";
        case SHORT:
          return "java.lang.Short";
        case INT:
          return "java.lang.Integer";
        case LONG:
          return "java.lang.Long";
        case CHAR:
          return "java.lang.Character";
        case FLOAT:
          return "java.lang.Float";
        case DOUBLE:
          return "java.lang.Double";
        default:
          // Shouldn't happen
          throw new RuntimeException();
      }
    } else {
      return type.toString();
    }
  }
}
