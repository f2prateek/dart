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

import dart.common.NavigationModelBindingTarget;
import dart.common.util.CompilerUtil;
import dart.common.util.FileUtil;
import dart.common.util.LoggingUtil;
import dart.common.util.NavigationModelBindingTargetUtil;
import dart.common.util.NavigationModelFieldUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({
  NavigationModelBinderProcessor.NAVIGATION_MODEL_ANNOTATION_CLASS_NAME,
})
public final class NavigationModelBinderProcessor extends AbstractProcessor {

  static final String NAVIGATION_MODEL_ANNOTATION_CLASS_NAME = "dart.DartModel";

  private LoggingUtil loggingUtil;
  private FileUtil fileUtil;
  private NavigationModelBindingTargetUtil navigationModelBindingTargetUtil;
  private NavigationModelFieldUtil navigationModelFieldUtil;
  private Map<String, TypeElement> allRoundsGeneratedToTypeElement = new HashMap<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    final CompilerUtil compilerUtil = new CompilerUtil(processingEnv);
    loggingUtil = new LoggingUtil(processingEnv);
    fileUtil = new FileUtil(processingEnv);
    navigationModelBindingTargetUtil =
        new NavigationModelBindingTargetUtil(compilerUtil, processingEnv);
    navigationModelFieldUtil =
        new NavigationModelFieldUtil(loggingUtil, navigationModelBindingTargetUtil);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    navigationModelFieldUtil.setRoundEnvironment(roundEnv);

    Map<TypeElement, NavigationModelBindingTarget> targetClassMap = findAndParseTargets();
    generateNavigationModelBinder(targetClassMap);

    //return false here to let henson process the annotations too
    return false;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  private Map<TypeElement, NavigationModelBindingTarget> findAndParseTargets() {
    Map<TypeElement, NavigationModelBindingTarget> targetClassMap = new LinkedHashMap<>();

    navigationModelFieldUtil.parseDartModelAnnotatedFields(targetClassMap);
    navigationModelBindingTargetUtil.createBindingTargetTrees(targetClassMap);

    return targetClassMap;
  }

  private void generateNavigationModelBinder(
      Map<TypeElement, NavigationModelBindingTarget> targetClassMap) {
    for (Map.Entry<TypeElement, NavigationModelBindingTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      NavigationModelBindingTarget navigationModelBindingTarget = entry.getValue();

      //we unfortunately can't test that nothing is generated in a TRUTH based test
      try {
        NavigationModelBinderGenerator generator =
            new NavigationModelBinderGenerator(navigationModelBindingTarget);
        fileUtil.writeFile(generator, typeElement);
        allRoundsGeneratedToTypeElement.put(generator.getFqcn(), typeElement);
      } catch (IOException e) {
        loggingUtil.error(
            typeElement,
            "Unable to write extra binder for type %s: %s",
            typeElement,
            e.getMessage());
      }
    }
  }

  /*visible for testing*/
  TypeElement getOriginatingElement(String generatedQualifiedName) {
    return allRoundsGeneratedToTypeElement.get(generatedQualifiedName);
  }
}
