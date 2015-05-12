package com.f2prateek.dart.internal;

import android.content.Context;
import android.content.Intent;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Iterator;
import java.util.Map;

import javax.lang.model.element.Modifier;

final class IntentBuilder {
  private final Map<String, ExtraInjection> injectionMap;
  private final String classPackage;
  private final String className;
  private final String targetClass;

  IntentBuilder(String classPackage, String className, String targetClass,
        Map<String, ExtraInjection> injectionMap) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.injectionMap = injectionMap;
  }

  String getFqcn() {
    return classPackage + "." + className;
  }

  String brewJava() {

    MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(Context.class, "context")
        .addStatement("this.$N = $N", "context", "context")
        .build();

    TypeSpec.Builder classTypeBuilder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addField(Context.class, "context", Modifier.PRIVATE, Modifier.FINAL)
        .addMethod(constructor);

    MethodSpec.Builder buildBuilder = MethodSpec.methodBuilder("build")
        .addModifiers(Modifier.PUBLIC)
        .returns(Intent.class)
        .addStatement("Intent intent = new Intent(context, $N.class)", targetClass);

    for (ExtraInjection injection : injectionMap.values()) {
      // Only doing first element.
      // Not sure how usable is having several elements with the same key
      Iterator<FieldBinding> iter = injection.getFieldBindings().iterator();
      FieldBinding fb = iter.next();

      classTypeBuilder.addField(TypeName.get(fb.getType()), fb.getName(), Modifier.PRIVATE);

      MethodSpec setter = MethodSpec.methodBuilder("with" + capitalize(fb.getName()))
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.get(fb.getType()), fb.getName())
            .returns(ClassName.get(classPackage, className))
            .addStatement("this.$N = $N", fb.getName(), fb.getName())
            .addStatement("return this")
            .build();
      classTypeBuilder.addMethod(setter);

      if (!fb.isParcel()) {
        buildBuilder.addStatement("intent.putExtra($S, $N)", injection.getKey(), fb.getName());
      }
    }

    buildBuilder.addStatement("return intent");
    classTypeBuilder.addMethod(buildBuilder.build());

    JavaFile javaFile = JavaFile.builder(classPackage, classTypeBuilder.build()).
        addFileComment("Generated code from Dart. Do not modify!").
        build();

    return javaFile.toString();
  }

  private String capitalize(String str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

}
