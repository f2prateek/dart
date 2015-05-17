package com.f2prateek.dart.internal;

import java.util.Collection;
import javax.lang.model.type.TypeMirror;

public class BundlerBuilder {
  public static final String BUNDLE_BUILDER_SUFFIX = "_Bundler";

  private final InjectionTarget target;

  public BundlerBuilder(InjectionTarget target) {
    this.target = target;
  }

  private String builderClassName() {
    return target.className + BUNDLE_BUILDER_SUFFIX;
  }

  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Dart. Do not modify!\n");
    builder.append("package ").append(target.classPackage).append(";\n\n");
    builder.append("import android.os.Bundle;\n");
    builder.append("import com.f2prateek.dart.Bundler;\n\n");
    builder.append("public class ").append(builderClassName()).append(" {\n");
    builder.append("  private final Bundler bundler = Bundler.create();\n\n");
    emitSetters(builder);
    emitGetter(builder);
    builder.append("}\n");
    return builder.toString();
  }

  private void emitGetter(StringBuilder builder) {
    builder.append("  public Bundle get() {\n");
    builder.append("    return bundler.get();\n");
    builder.append("  }\n");
  }

  private void emitSetters(StringBuilder builder) {
    // Loop over each extras injection and emit it.
    for (ExtraInjection injection : target.injectionMap.values()) {
      emitSetter(builder, injection);
    }
  }

  private void emitSetter(StringBuilder builder, ExtraInjection injection) {
    Collection<FieldBinding> fieldBindings = injection.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return;
    }

    // We can have the same extra key bound to multiple fields. We generate builders by key name,
    // so we pick the first field binding and use it's type since they should all be the same
    // anyway.
    FieldBinding firstFieldBinding = fieldBindings.iterator().next();
    TypeMirror extraType = firstFieldBinding.getType();
    builder.append("  public ")
        .append(builderClassName())
        .append(' ')
        .append(injection.getKey())
        .append('(')
        .append(getType(extraType))
        .append(' ')
        .append(injection.getKey())
        .append(") {\n")
        .append("    bundler.put(\"").append(injection.getKey()).append("\", ");
    if (firstFieldBinding.isParcel()) {
      builder.append("org.parceler.Parcels.wrap(").append(injection.getKey()).append(')');
    } else {
      builder.append(injection.getKey());
    }
    builder.append(");\n")
        .append("    return this;\n")
        .append("  }\n");
  }

  public String getFqcn() {
    return target.getFqcn() + BUNDLE_BUILDER_SUFFIX;
  }

  static void emitCast(StringBuilder builder, TypeMirror fieldType) {
    builder.append('(').append(getType(fieldType)).append(") ");
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
