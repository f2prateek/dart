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

import dart.common.BindingTarget;
import dart.common.util.BindExtraUtil;
import dart.common.util.BindingTargetUtil;
import dart.common.util.CompilerUtil;
import dart.common.util.DartModelUtil;
import dart.common.util.FileUtil;
import dart.common.util.LoggingUtil;
import dart.common.util.ParcelerUtil;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({
  HensonProcessor.NAVIGATION_MODEL_ANNOTATION_CLASS_NAME,
  HensonProcessor.EXTRA_ANNOTATION_CLASS_NAME
})
@SupportedOptions({HensonProcessor.OPTION_HENSON_PACKAGE})
public class HensonProcessor extends AbstractProcessor {

  static final String NAVIGATION_MODEL_ANNOTATION_CLASS_NAME = "dart.DartModel";
  static final String EXTRA_ANNOTATION_CLASS_NAME = "dart.BindExtra";
  static final String OPTION_HENSON_PACKAGE = "dart.henson.package";

  private LoggingUtil loggingUtil;
  private BindExtraUtil bindExtraUtil;
  private FileUtil fileUtil;
  private DartModelUtil dartModelUtil;
  private BindingTargetUtil bindingTargetUtil;

  private String hensonPackage;
  private boolean usesParceler = true;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    final CompilerUtil compilerUtil = new CompilerUtil(processingEnv);
    final ParcelerUtil parcelerUtil = new ParcelerUtil(compilerUtil, processingEnv, usesParceler);
    loggingUtil = new LoggingUtil(processingEnv);
    fileUtil = new FileUtil(processingEnv);
    bindingTargetUtil = new BindingTargetUtil(compilerUtil, processingEnv, loggingUtil);
    dartModelUtil = new DartModelUtil(loggingUtil, bindingTargetUtil, compilerUtil);
    bindExtraUtil =
        new BindExtraUtil(
            compilerUtil, parcelerUtil, loggingUtil, bindingTargetUtil, dartModelUtil);

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

    Map<TypeElement, BindingTarget> targetClassMap = findAndParseTargets();
    generateIntentBuilders(targetClassMap);
    generateHenson(targetClassMap);

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

  private Map<TypeElement, BindingTarget> findAndParseTargets() {
    Map<TypeElement, BindingTarget> targetClassMap = new LinkedHashMap<>();

    dartModelUtil.parseDartModelAnnotatedElements(targetClassMap);
    bindExtraUtil.parseBindExtraAnnotatedElements(targetClassMap);
    bindingTargetUtil.createBindingTargetTrees(targetClassMap);
    bindingTargetUtil.addClosestRequiredAncestorForTargets(targetClassMap);

    return targetClassMap;
  }

  private void generateIntentBuilders(Map<TypeElement, BindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, BindingTarget> entry : targetClassMap.entrySet()) {
      if (entry.getValue().topLevel) {
        generateIntentBuildersForTree(targetClassMap, entry.getKey());
      }
    }
  }

  private void generateIntentBuildersForTree(
      Map<TypeElement, BindingTarget> targetClassMap, TypeElement typeElement) {
    //we unfortunately can't test that nothing is generated in a TRUTH based test
    final BindingTarget bindingTarget = targetClassMap.get(typeElement);
    try {
      fileUtil.writeFile(new IntentBuilderGenerator(bindingTarget), typeElement);
    } catch (IOException e) {
      loggingUtil.error(
          typeElement,
          "Unable to write intent builder for type %s: %s",
          typeElement,
          e.getMessage());
    }

    for (TypeElement child : bindingTarget.childClasses) {
      generateIntentBuildersForTree(targetClassMap, child);
    }
  }

  private void generateHenson(Map<TypeElement, BindingTarget> targetClassMap) {
    if (!targetClassMap.values().isEmpty()) {
      Element[] allTypes = targetClassMap.keySet().toArray(new Element[targetClassMap.size()]);
      try {
        fileUtil.writeFile(new HensonGenerator(hensonPackage, targetClassMap.values()), allTypes);
      } catch (IOException e) {
        for (Element element : allTypes) {
          loggingUtil.error(
              element,
              "Unable to write henson navigator for types %s: %s",
              element,
              e.getMessage());
        }
      }
    }
  }
}
