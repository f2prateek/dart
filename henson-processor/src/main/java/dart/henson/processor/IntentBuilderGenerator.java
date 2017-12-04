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

import android.content.Intent;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import dart.common.BaseGenerator;
import dart.common.BindingTarget;
import dart.common.ExtraInjection;
import dart.common.FieldBinding;
import dart.henson.ActivityClassFinder;
import dart.henson.AllRequiredSetState;
import dart.henson.Bundler;
import dart.henson.RequiredStateSequence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.squareup.javapoet.ClassName.get;
import static dart.common.util.BindingTargetUtil.BUNDLE_BUILDER_SUFFIX;
import static dart.common.util.BindingTargetUtil.INITIAL_STATE_METHOD;

public class IntentBuilderGenerator extends BaseGenerator {

  static final String REQUIRED_SEQUENCE_CLASS = "RequiredSequence";
  static final String RESOLVED_OPTIONAL_SEQUENCE_CLASS = "ResolvedAllSet";
  private static final String OPTIONAL_SEQUENCE_CLASS = "AllSet";
  private static final String OPTIONAL_SEQUENCE_GENERIC = "ALL_SET";
  private static final String OPTIONAL_SEQUENCE_SUBCLASS_GENERIC = "SELF";
  private static final String REQUIRED_SEQUENCE_INTERMEDIARY_CLASS_PREFIX = "AfterSetting";
  private static final String NAVIGATION_MODEL_SUFFIX = "NavigationModel";

  private final BindingTarget target;

  public IntentBuilderGenerator(BindingTarget target) {
    this.target = target;
  }

  @Override
  public String brewJava() {
    TypeSpec.Builder intentBuilderTypeBuilder =
        TypeSpec.classBuilder(builderClassName()).addModifiers(Modifier.PUBLIC);

    emitInitialStateGetterForHenson(intentBuilderTypeBuilder);
    emitInitialStateGetterForSubBuilders(intentBuilderTypeBuilder);
    emitExtraDSLStateMachine(intentBuilderTypeBuilder);
    emitResolvedOptionalSequence(intentBuilderTypeBuilder);

    //build
    JavaFile javaFile =
        JavaFile.builder(target.classPackage, intentBuilderTypeBuilder.build())
            .addFileComment("Generated code from Henson. Do not modify!")
            .addStaticImport(ActivityClassFinder.class, "getClassDynamically")
            .build();
    return javaFile.toString();
  }

  @Override
  public String getFqcn() {
    return target.classPackage + "." + builderClassName();
  }

  private String builderClassName() {
    return target.className + BUNDLE_BUILDER_SUFFIX;
  }

  private void emitInitialStateGetterForHenson(TypeSpec.Builder intentBuilderTypeBuilder) {
    MethodSpec.Builder initialStateGetterForHensonBuilder =
        MethodSpec.methodBuilder(INITIAL_STATE_METHOD)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC)
            .addParameter(get("android.content", "Context"), "context")
            .returns(getInitialStateType(getInitialStateGeneric(true)));

    final String targetFqcn = target.getFQN();
    initialStateGetterForHensonBuilder.addStatement(
        "final $T intent = new $T(context, getClassDynamically($S))",
        Intent.class, Intent.class,
        targetFqcn.substring(0, targetFqcn.indexOf(NAVIGATION_MODEL_SUFFIX))
    );
    initialStateGetterForHensonBuilder.addStatement(
        "final $T bundler = $T.create()",
        Bundler.class, Bundler.class
    );

    if (!target.hasRequiredFields && target.closestRequiredAncestorPackage == null) {
      initialStateGetterForHensonBuilder.addStatement(
          "return new $L(bundler, intent)",
          RESOLVED_OPTIONAL_SEQUENCE_CLASS
      );
      intentBuilderTypeBuilder.addMethod(initialStateGetterForHensonBuilder.build());
      return;
    }

    initialStateGetterForHensonBuilder.addStatement(
        "final $L resolvedAllSet = new $L(bundler, intent)",
        RESOLVED_OPTIONAL_SEQUENCE_CLASS, RESOLVED_OPTIONAL_SEQUENCE_CLASS
    );

    if (target.hasRequiredFields) {
      initialStateGetterForHensonBuilder.addStatement(
          "return new $L<>(bundler, resolvedAllSet)",
          REQUIRED_SEQUENCE_CLASS
      );
      intentBuilderTypeBuilder.addMethod(initialStateGetterForHensonBuilder.build());
      return;
    }

    final String parentIntentBuilderClass = target.parentClass + BUNDLE_BUILDER_SUFFIX;
    initialStateGetterForHensonBuilder.addStatement(
        "return $T.getInitialState(bundler, resolvedAllSet)",
        get(target.parentPackage, parentIntentBuilderClass)
    );
    intentBuilderTypeBuilder.addMethod(initialStateGetterForHensonBuilder.build());
  }

  private void emitInitialStateGetterForSubBuilders(TypeSpec.Builder intentBuilderTypeBuilder) {
    final TypeName initialStateGeneric = getInitialStateGeneric(false);
    MethodSpec.Builder initialStateGetterForSubBuilder =
        MethodSpec.methodBuilder(INITIAL_STATE_METHOD)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC)
            .addTypeVariable((TypeVariableName) initialStateGeneric)
            .addParameter(Bundler.class, "bundler")
            .addParameter(initialStateGeneric, "allSetState")
            .returns(getInitialStateType(initialStateGeneric));

    if (target.hasRequiredFields) {
      initialStateGetterForSubBuilder.addStatement(
          "return new $L<>(bundler, allSetState)",
          REQUIRED_SEQUENCE_CLASS
      );
      intentBuilderTypeBuilder.addMethod(initialStateGetterForSubBuilder.build());
      return;
    }

    if (target.parentPackage != null) {
      final String parentIntentBuilderClass = target.parentClass + BUNDLE_BUILDER_SUFFIX;
      initialStateGetterForSubBuilder.addStatement(
          "return $T.getInitialState(bundler, allSetState)",
          get(target.parentPackage, parentIntentBuilderClass)
      );
      intentBuilderTypeBuilder.addMethod(initialStateGetterForSubBuilder.build());
      return;
    }

    initialStateGetterForSubBuilder.addStatement("return allSetState");
    intentBuilderTypeBuilder.addMethod(initialStateGetterForSubBuilder.build());
  }

  private void emitExtraDSLStateMachine(TypeSpec.Builder intentBuilderTypeBuilder) {
    //separate required extras from optional extras and sort both sublists.
    List<ExtraInjection> requiredInjections = new ArrayList<>();
    List<ExtraInjection> optionalInjections = new ArrayList<>();
    for (ExtraInjection extraInjection : target.bindingMap.values()) {
      if (!extraInjection.getRequiredBindings().isEmpty()) {
        requiredInjections.add(extraInjection);
      } else {
        optionalInjections.add(extraInjection);
      }
    }

    final ExtraInjectionComparator extraInjectionComparator = new ExtraInjectionComparator();
    Collections.sort(requiredInjections, extraInjectionComparator);
    Collections.sort(optionalInjections, extraInjectionComparator);

    emitRequiredSequence(intentBuilderTypeBuilder, requiredInjections);
    emitOptionalSequence(intentBuilderTypeBuilder, optionalInjections);
  }

  private void emitRequiredSequence(TypeSpec.Builder intentBuilderTypeBuilder,
      List<ExtraInjection> requiredInjections) {
    if (!target.hasRequiredFields) {
      return;
    }
    final TypeName generic = getInitialStateGeneric(false);

    TypeSpec.Builder requiredSequenceBuilder =
        TypeSpec.classBuilder(REQUIRED_SEQUENCE_CLASS)
            .superclass(ParameterizedTypeName.get(get(RequiredStateSequence.class), generic))
            .addTypeVariable((TypeVariableName) generic)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC);

    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Bundler.class, "bundler")
            .addParameter(generic, "allRequiredSetState")
            .addStatement("super(bundler, allRequiredSetState)");

    requiredSequenceBuilder.addMethod(constructorBuilder.build());

    TypeSpec.Builder builderStateClass = requiredSequenceBuilder;
    for (int i = 0; i < requiredInjections.size(); i++) {
      final boolean isLast = i == requiredInjections.size() - 1;
      final ExtraInjection binding = requiredInjections.get(i);
      final String nextClass = emitRequiredSetter(builderStateClass, binding, generic, isLast);
      builderStateClass = rotateBuilderState(requiredSequenceBuilder, builderStateClass, nextClass);
    }

    intentBuilderTypeBuilder.addType(requiredSequenceBuilder.build());
  }

  private void emitOptionalSequence(TypeSpec.Builder intentBuilderTypeBuilder,
      List<ExtraInjection> optionalInjections) {
    // find type
    final ClassName optionalSequence =
        get(target.classPackage, builderClassName(), OPTIONAL_SEQUENCE_CLASS);
    final ParameterizedTypeName parameterizedOptionalSequence =
        ParameterizedTypeName.get(optionalSequence,
            TypeVariableName.get(OPTIONAL_SEQUENCE_SUBCLASS_GENERIC));
    final TypeVariableName typeVariable =
        TypeVariableName.get(OPTIONAL_SEQUENCE_SUBCLASS_GENERIC, parameterizedOptionalSequence);

    // find superclass
    final TypeName superClass;
    if (target.parentPackage != null) {
      final ClassName parentOptionalSequence =
          get(target.parentPackage, target.parentClass + BUNDLE_BUILDER_SUFFIX,
              OPTIONAL_SEQUENCE_CLASS);
      superClass = ParameterizedTypeName.get(parentOptionalSequence, typeVariable);
    } else {
      superClass = get(AllRequiredSetState.class);
    }

    TypeSpec.Builder optionalSequenceBuilder =
        TypeSpec.classBuilder(OPTIONAL_SEQUENCE_CLASS)
            .superclass(superClass)
            .addTypeVariable(typeVariable)
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC);

    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Bundler.class, "bundler")
            .addParameter(get("android.content", "Intent"), "intent")
            .addStatement("super(bundler, intent)");

    optionalSequenceBuilder.addMethod(constructorBuilder.build());

    for (int i = 0; i < optionalInjections.size(); i++) {
      emitOptionalSetter(optionalSequenceBuilder, optionalInjections.get(i), typeVariable);
    }

    intentBuilderTypeBuilder.addType(optionalSequenceBuilder.build());
  }

  private void emitResolvedOptionalSequence(TypeSpec.Builder intentBuilderTypeBuilder) {
    // find superclass
    final ClassName optionalSequence =
        get(target.classPackage, builderClassName(), OPTIONAL_SEQUENCE_CLASS);
    final ClassName resolvedOptional =
        get(target.classPackage, builderClassName(), RESOLVED_OPTIONAL_SEQUENCE_CLASS);

    TypeSpec.Builder resolvedOptionalSequenceBuilder =
        TypeSpec.classBuilder(RESOLVED_OPTIONAL_SEQUENCE_CLASS)
            .superclass(ParameterizedTypeName.get(optionalSequence, resolvedOptional))
            .addModifiers(Modifier.PUBLIC)
            .addModifiers(Modifier.STATIC);

    MethodSpec.Builder constructorBuilder =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Bundler.class, "bundler")
            .addParameter(get("android.content", "Intent"), "intent")
            .addStatement("super(bundler, intent)");

    resolvedOptionalSequenceBuilder.addMethod(constructorBuilder.build());
    intentBuilderTypeBuilder.addType(resolvedOptionalSequenceBuilder.build());
  }

  /**
   * @param builder the intent builder in which to emit.
   * @param binding the binding to emit.
   * @param generic generic value.
   * @param isLast whether or not the binding is the last mandatory one.
   * @return the name of the next state class to create
   */
  private String emitRequiredSetter(TypeSpec.Builder builder, ExtraInjection binding,
      TypeName generic, boolean isLast) {
    final Collection<FieldBinding> fieldBindings = binding.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return null;
    }

    // only used for non-last iterations, but needed for the algorithm
    String nextIntermediary =
        REQUIRED_SEQUENCE_INTERMEDIARY_CLASS_PREFIX + capitalize(binding.getKey());
    ;
    // find next intermediary state
    final TypeName nextState;
    if (isLast) {
      if (target.closestRequiredAncestorPackage == null) {
        nextState = generic;
      } else {
        final String closestRequiredAncestorIntentBuilderClass =
            target.closestRequiredAncestorClass + BUNDLE_BUILDER_SUFFIX;
        final ClassName requiredSequence =
            get(target.closestRequiredAncestorPackage, closestRequiredAncestorIntentBuilderClass,
                REQUIRED_SEQUENCE_CLASS);
        nextState = ParameterizedTypeName.get(requiredSequence, generic);
      }
    } else {
      nextState =
          get(target.classPackage, builderClassName(), REQUIRED_SEQUENCE_CLASS, nextIntermediary);
    }

    final FieldBinding firstFieldBinding = fieldBindings.iterator().next();
    final TypeMirror extraType = firstFieldBinding.getType();
    final String castToParcelableIfNecessary = doCreateParcelableCastIfExtraIsParcelable(extraType);
    final String value = extractValue(firstFieldBinding);

    MethodSpec.Builder setterBuilder =
        MethodSpec.methodBuilder(binding.getKey())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.get(extraType), firstFieldBinding.getName())
            .returns(nextState)
            .addStatement(
                "bundler.put($S," + castToParcelableIfNecessary + " $L)",
                binding.getKey(),
                value
            );

    // find return statement
    if (isLast) {
      if (target.parentPackage != null) {
        final String parentIntentBuilderClass = target.parentClass + BUNDLE_BUILDER_SUFFIX;
        setterBuilder.addStatement(
            "return $T.getInitialState(bundler, allSetState)",
            get(target.parentPackage, parentIntentBuilderClass)
        );
      } else {
        setterBuilder.addStatement("return allRequiredSetState");
      }
    } else {
      setterBuilder.addStatement("return new $T()", nextState);
    }

    builder.addMethod(setterBuilder.build());
    return nextIntermediary;
  }

  /**
   * @param builder the intent builder in which to emit.
   * @param binding the binding to emit.
   * @param generic generic value.
   */
  private void emitOptionalSetter(TypeSpec.Builder builder, ExtraInjection binding,
      TypeName generic) {
    Collection<FieldBinding> fieldBindings = binding.getFieldBindings();
    if (fieldBindings.isEmpty()) {
      return;
    }

    FieldBinding firstFieldBinding = fieldBindings.iterator().next();
    TypeMirror extraType = firstFieldBinding.getType();
    String castToParcelableIfNecessary = doCreateParcelableCastIfExtraIsParcelable(extraType);
    final String value = extractValue(firstFieldBinding);

    MethodSpec.Builder setterBuilder =
        MethodSpec.methodBuilder(binding.getKey())
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.get(extraType), firstFieldBinding.getName())
            .returns(generic)
            .addStatement(
                "bundler.put($S," + castToParcelableIfNecessary + " $L)",
                binding.getKey(),
                value
            )
            .addStatement(
                "return ($T) this",
                generic
            );

    builder.addMethod(setterBuilder.build());
  }

  private TypeSpec.Builder rotateBuilderState(TypeSpec.Builder builder,
      TypeSpec.Builder builderStateClass, String nextStateClassName) {
    if (builderStateClass != builder) {
      builder.addType(builderStateClass.build());
    }
    //prepare next state class
    builderStateClass = TypeSpec.classBuilder(nextStateClassName).addModifiers(Modifier.PUBLIC);
    return builderStateClass;
  }

  private TypeName getInitialStateType(TypeName generic) {
    if (target.hasRequiredFields) {
      final ClassName requiredSequence =
          get(target.classPackage, builderClassName(), REQUIRED_SEQUENCE_CLASS);
      return ParameterizedTypeName.get(requiredSequence, generic);
    }
    if (target.closestRequiredAncestorPackage != null) {
      final String closestRequiredAncestorIntentBuilderClass =
          target.closestRequiredAncestorClass + BUNDLE_BUILDER_SUFFIX;
      final ClassName requiredSequence =
          get(target.closestRequiredAncestorPackage, closestRequiredAncestorIntentBuilderClass,
              REQUIRED_SEQUENCE_CLASS);
      return ParameterizedTypeName.get(requiredSequence, generic);
    }
    return generic;
  }

  private TypeName getInitialStateGeneric(boolean resolved) {
    if (resolved) {
      return get(target.classPackage, builderClassName(), RESOLVED_OPTIONAL_SEQUENCE_CLASS);
    }
    final ClassName optionalSequence =
        get(target.classPackage, builderClassName(), OPTIONAL_SEQUENCE_CLASS);
    return TypeVariableName.get(OPTIONAL_SEQUENCE_GENERIC, optionalSequence);
  }

  /**
   * This method returns either an empty String or {@code "(Parcelable)"} if the extra type is
   * Parcelable. We need this explicit conversion in cases where the extra type is both Parcelable
   * and Serializable. In that case we will prefer Parcelable. Not that the extra type has to
   * directly implement Parcelable, not via a super class.
   *
   * @param extraType the type that might be parcelable.
   * @return either an empty String or {@code "(Parcelable)"} if the extra type is Parcelable
   */
  private String doCreateParcelableCastIfExtraIsParcelable(TypeMirror extraType) {
    String castToParcelableIfNecessary = "";
    if (extraType instanceof DeclaredType) {
      boolean isParcelable = false;
      final TypeElement typeElement = (TypeElement) ((DeclaredType) extraType).asElement();
      for (TypeMirror interfaceType : typeElement.getInterfaces()) {
        if ("android.os.Parcelable".equals(interfaceType.toString())) {
          isParcelable = true;
        }
      }

      if (isParcelable) {
        castToParcelableIfNecessary = "(android.os.Parcelable)";
      }
    }
    return castToParcelableIfNecessary;
  }

  private String extractValue(FieldBinding firstFieldBinding) {
    final String value;
    if (firstFieldBinding.isParcel()) {
      value = "org.parceler.Parcels.wrap(" + firstFieldBinding.getName() + ')';
    } else {
      value = firstFieldBinding.getName();
    }
    return value;
  }

  private String capitalize(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Key can't be null.");
    }

    if (key.length() == 0) {
      return "";
    }

    char firstUpper = Character.toUpperCase(key.charAt(0));
    if (key.length() == 1) {
      return Character.toString(firstUpper);
    }

    String rest = key.substring(1);
    return firstUpper + rest;
  }

  private static class ExtraInjectionComparator implements Comparator<ExtraInjection> {
    @Override
    public int compare(ExtraInjection o1, ExtraInjection o2) {
      return o1.getKey().compareTo(o2.getKey());
    }
  }
}
