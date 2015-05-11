package com.f2prateek.dart.internal;

import java.util.Iterator;
import java.util.Map;

final class IntentBuilder {
  private final Map<String, ExtraInjection> injectionMap;
  private final String classPackage;
  private final String className;
  private final String targetClass;

  IntentBuilder(String classPackage, String className, String targetClass, Map<String, ExtraInjection> injectionMap) {
    this.classPackage = classPackage;
    this.className = className;
    this.targetClass = targetClass;
    this.injectionMap = injectionMap;
  }

  String getFqcn() {
    return classPackage + "." + className;
  }

  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Dart. Do not modify!\n");
    builder.append("package ").append(classPackage).append(";\n\n");
    builder.append("import android.content.Context;\n");
    builder.append("import android.content.Intent;\n\n");

    builder.append("public class ").append(className).append(" {\n");
    builder.append("  private final Context context;\n\n");

    createConstructor(builder);

    for (ExtraInjection injection : injectionMap.values()) {
      createSetter(injection, builder);
    }

    createBuildMethod(builder);

    builder.append("}\n");
    return builder.toString();
  }

  private void createSetter(ExtraInjection injection, StringBuilder builder) {
    // TODO: Only doing first element.
    // Not sure how usable is having several elements with the same key
    Iterator<FieldBinding> iter = injection.getFieldBindings().iterator();
    FieldBinding fb = iter.next();

    builder.append("  private ").append(fb.getType()).append(" ").append(fb.getName()).append(";\n");
    builder.append("  public ").append(className).append(" with").append(capitalize(fb.getName())).
            append("(").append(fb.getType()).append(" ").append(fb.getName()).append(") {\n");
    builder.append("    this.").append(fb.getName()).append(" = ").append(fb.getName()).append(";\n");
    builder.append("    return this;\n");
    builder.append("  }\n\n");
  }

  private String capitalize(String str) {
    return str.substring(0,1).toUpperCase() + str.substring(1);
  }

  private void createConstructor(StringBuilder builder) {
    builder.append("  public ").append(className).append("(Context context) {\n");
    builder.append("    this.context = context;\n");
    builder.append("  }\n\n");
  }

  private void createBuildMethod(StringBuilder builder) {
    builder.append("  public Intent build() {\n");
    builder.append("    Intent intent = new Intent(context, ").append(targetClass).append(".class);\n");

    for (ExtraInjection injection : injectionMap.values()) {
        // FIXME: Ugly. Iterating again over the same list
        Iterator<FieldBinding> iter = injection.getFieldBindings().iterator();
        FieldBinding fb = iter.next();
        if (!fb.isParcel()) {
            builder.append("    intent.putExtra(\"").append(injection.getKey()).append("\", ").
                    append(fb.getName()).append(");\n");
        }
    }

    builder.append("    return intent;\n");
    builder.append("  }\n\n");
  }
}
