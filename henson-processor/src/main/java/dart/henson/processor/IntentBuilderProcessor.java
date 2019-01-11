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

import dart.common.ExtraBindingTarget;
import dart.common.util.BindExtraUtil;
import dart.common.util.CompilerUtil;
import dart.common.util.DartModelUtil;
import dart.common.util.ExtraBindingTargetUtil;
import dart.common.util.FileUtil;
import dart.common.util.LoggingUtil;
import dart.common.util.ParcelerUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({
  IntentBuilderProcessor.NAVIGATION_MODEL_ANNOTATION_CLASS_NAME,
  IntentBuilderProcessor.EXTRA_ANNOTATION_CLASS_NAME
})
@SupportedOptions({IntentBuilderProcessor.OPTION_HENSON_PACKAGE})
public class IntentBuilderProcessor extends AbstractProcessor {

  static final String NAVIGATION_MODEL_ANNOTATION_CLASS_NAME = "dart.DartModel";
  static final String EXTRA_ANNOTATION_CLASS_NAME = "dart.BindExtra";
  static final String OPTION_HENSON_PACKAGE = "dart.henson.package";

  private LoggingUtil loggingUtil;
  private BindExtraUtil bindExtraUtil;
  private FileUtil fileUtil;
  private DartModelUtil dartModelUtil;
  private ExtraBindingTargetUtil extraBindingTargetUtil;

  private String hensonPackage;
  private boolean usesParceler = true;
  private Map<String, TypeElement> allRoundsGeneratedToTypeElement = new HashMap<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    final CompilerUtil compilerUtil = new CompilerUtil(processingEnv);
    final ParcelerUtil parcelerUtil = new ParcelerUtil(compilerUtil, processingEnv, usesParceler);
    loggingUtil = new LoggingUtil(processingEnv);
    fileUtil = new FileUtil(processingEnv);
    extraBindingTargetUtil = new ExtraBindingTargetUtil(compilerUtil, processingEnv, loggingUtil);
    dartModelUtil = new DartModelUtil(loggingUtil, extraBindingTargetUtil, compilerUtil);
    bindExtraUtil =
        new BindExtraUtil(
            compilerUtil, parcelerUtil, loggingUtil, extraBindingTargetUtil, dartModelUtil);

    parseAnnotationProcessorOptions(processingEnv);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    dartModelUtil.setRoundEnvironment(roundEnv);
    bindExtraUtil.setRoundEnvironment(roundEnv);

    final Map<TypeElement, ExtraBindingTarget> targetClassMap = findAndParseTargets();
    generateIntentBuilders(targetClassMap);

    //return false here to let dart process the annotations too
    return false;
  }

  /**
   * Flag to force enabling/disabling Parceler. Used for testing.
   *
   * @param enable whether Parceler should be enable
   */
  public void enableParceler(boolean enable) {
    usesParceler = enable;
  }

  private void parseAnnotationProcessorOptions(ProcessingEnvironment processingEnv) {
    hensonPackage = processingEnv.getOptions().get(OPTION_HENSON_PACKAGE);
  }

  private Map<TypeElement, ExtraBindingTarget> findAndParseTargets() {
    Map<TypeElement, ExtraBindingTarget> targetClassMap = new LinkedHashMap<>();

    dartModelUtil.parseDartModelAnnotatedTypes(targetClassMap);
    bindExtraUtil.parseBindExtraAnnotatedElements(targetClassMap);
    extraBindingTargetUtil.createBindingTargetTrees(targetClassMap);
    extraBindingTargetUtil.addClosestRequiredAncestorForTargets(targetClassMap);

    return targetClassMap;
  }

  private void generateIntentBuilders(Map<TypeElement, ExtraBindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, ExtraBindingTarget> entry : targetClassMap.entrySet()) {
      if (entry.getValue().topLevel) {
        generateIntentBuildersForTree(targetClassMap, entry.getKey());
      }
    }
  }

  private void generateIntentBuildersForTree(
      Map<TypeElement, ExtraBindingTarget> targetClassMap, TypeElement typeElement) {
    //we unfortunately can't test that nothing is generated in a TRUTH based test
    final ExtraBindingTarget extraBindingTarget = targetClassMap.get(typeElement);
    try {
      IntentBuilderGenerator generator = new IntentBuilderGenerator(extraBindingTarget);
      fileUtil.writeFile(generator, typeElement);
      allRoundsGeneratedToTypeElement.put(generator.getFqcn(), typeElement);
    } catch (IOException e) {
      loggingUtil.error(
          typeElement,
          "Unable to write intent builder for type %s: %s",
          typeElement,
          e.getMessage());
    }

    for (TypeElement child : extraBindingTarget.childClasses) {
      generateIntentBuildersForTree(targetClassMap, child);
    }
  }

  /*visible for testing*/
  TypeElement getOriginatingElement(String generatedQualifiedName) {
    return allRoundsGeneratedToTypeElement.get(generatedQualifiedName);
  }
}
