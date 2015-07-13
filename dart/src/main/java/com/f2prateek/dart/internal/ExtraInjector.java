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
package com.f2prateek.dart.internal;

import java.util.Collection;
import java.util.List;
import javax.lang.model.type.TypeMirror;

public class ExtraInjector {
  public static final String INJECTOR_SUFFIX = "$$ExtraInjector";
  private final InjectionTarget target;

  public ExtraInjector(InjectionTarget target) {
    this.target = target;
  }

  String brewJava() {
    StringBuilder builder = new StringBuilder();
    builder.append("// Generated code from Dart. Do not modify!\n");
    builder.append("package ").append(target.classPackage).append(";\n\n");
    builder.append("import com.f2prateek.dart.Dart.Finder;\n\n");
    builder.append("public class ").append(target.className + INJECTOR_SUFFIX).append(" {\n");
    emitInject(builder);
    builder.append("}\n");
    return builder.toString();
  }

  private void emitInject(StringBuilder builder) {
    builder.append("  public static void inject(Finder finder, final ")
        .append(target.targetClass)
        .append(" target, Object source) {\n");

    // Emit a call to the superclass injector, if any.
    if (target.parentTarget != null) {
      builder.append("    ")
          .append(target.parentTarget + INJECTOR_SUFFIX)
          .append(".inject(finder, target, source);\n\n");
    }

    // Local variable in which all extras will be temporarily stored.
    builder.append("    Object object;\n");

    // Loop over each extras injection and emit it.
    for (ExtraInjection injection : target.injectionMap.values()) {
      emitExtraInjection(builder, injection);
    }

    builder.append("  }\n");
  }

  private void emitExtraInjection(StringBuilder builder, ExtraInjection injection) {
    builder.append("    object = finder.getExtra(source, \"")
        .append(injection.getKey())
        .append("\");\n");

    List<Binding> requiredBindings = injection.getRequiredBindings();
    if (!requiredBindings.isEmpty()) {
      builder.append("    if (object == null) {\n")
          .append("      throw new IllegalStateException(\"Required extra with key '")
          .append(injection.getKey())
          .append("' for ");
      emitHumanDescription(builder, requiredBindings);
      builder.append(" was not found. If this extra is optional add '@Nullable' annotation.\");\n")
          .append("    }\n");
      emitFieldBindings(builder, injection);
    } else {
      // an optional extra, wrap it in a check to keep original value, if any
      builder.append("    if (object != null) {\n");
      builder.append("  ");
      emitFieldBindings(builder, injection);
      builder.append("    }\n");
    }
  }

  private void emitFieldBindings(StringBuilder builder, ExtraInjection injection) {
    Collection<FieldBinding> fieldBindings = injection.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return;
    }

    for (FieldBinding fieldBinding : fieldBindings) {
      builder.append("    target.").append(fieldBinding.getName()).append(" = ");

      if (fieldBinding.isParcel()) {
        builder.append("org.parceler.Parcels.unwrap((android.os.Parcelable) object);\n");
      } else {
        emitCast(builder, fieldBinding.getType());
        builder.append("object;\n");
      }
    }
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

  static void emitHumanDescription(StringBuilder builder, List<Binding> bindings) {
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
  }

  public String getFqcn() {
    return target.getFqcn() + INJECTOR_SUFFIX;
  }
}
