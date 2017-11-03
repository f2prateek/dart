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

import dart.common.InjectionTarget;
import dart.common.util.CompilerUtil;
import dart.common.util.FileUtil;
import dart.common.util.InjectExtraUtil;
import dart.common.util.InjectionTargetUtil;
import dart.common.util.LoggingUtil;
import dart.common.util.ParcelerUtil;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({
    InjectExtraProcessor.INJECT_EXTRA_ANNOTATION_CLASS_NAME
}) public final class InjectExtraProcessor extends AbstractProcessor {

  static final String INJECT_EXTRA_ANNOTATION_CLASS_NAME = "dart.InjectExtra";

  private LoggingUtil loggingUtil;
  private FileUtil fileUtil;
  private InjectExtraUtil injectExtraUtil;
  private InjectionTargetUtil injectionTargetUtil;

  private boolean usesParcelerOption = true;

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    final CompilerUtil compilerUtil = new CompilerUtil(processingEnv);
    final ParcelerUtil parcelerUtil =
        new ParcelerUtil(compilerUtil, processingEnv, usesParcelerOption);
    loggingUtil = new LoggingUtil(processingEnv);
    fileUtil = new FileUtil(processingEnv);
    injectionTargetUtil = new InjectionTargetUtil(compilerUtil);
    injectExtraUtil =
        new InjectExtraUtil(compilerUtil, parcelerUtil, loggingUtil, injectionTargetUtil,
            processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    injectExtraUtil.setRoundEnvironment(roundEnv);

    Map<TypeElement, InjectionTarget> targetClassMap = findAndParseTargets();
    generateExtraInjectors(targetClassMap);

    //return false here to let henson process the annotations too
    return false;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  /**
   * Flag to force enabling/disabling Parceler.
   * Used for testing.
   *
   * @param enable whether Parceler should be enable
   */
  public void enableParceler(boolean enable) {
    usesParcelerOption = enable;
  }

  private Map<TypeElement, InjectionTarget> findAndParseTargets() {
    Map<TypeElement, InjectionTarget> targetClassMap = new LinkedHashMap<>();

    // Process each @InjectExtra element.
    injectExtraUtil.parseInjectExtraAnnotatedElements(targetClassMap);
    // Create injection target tree and inherit extra injections.
    injectionTargetUtil.createInjectionTargetTree(targetClassMap);

    return targetClassMap;
  }

  private void generateExtraInjectors(Map<TypeElement, InjectionTarget> targetClassMap) {
    for (Map.Entry<TypeElement, InjectionTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      InjectionTarget injectionTarget = entry.getValue();

      //we unfortunately can't test that nothing is generated in a TRUTH based test
      try {
        fileUtil.writeFile(new ExtraInjectorGenerator(injectionTarget), typeElement);
      } catch (IOException e) {
        loggingUtil.error(typeElement, "Unable to write extra injector for type %s: %s",
            typeElement, e.getMessage());
      }
    }
  }
}
