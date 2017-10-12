package com.f2prateek.dart.henson.processor;

import com.f2prateek.dart.common.InjectionTarget;
import com.f2prateek.dart.common.util.CompilerUtil;
import com.f2prateek.dart.common.util.FileUtil;
import com.f2prateek.dart.common.util.InjectExtraUtil;
import com.f2prateek.dart.common.util.InjectionTargetUtil;
import com.f2prateek.dart.common.util.LoggingUtil;
import com.f2prateek.dart.common.util.NavigationModelUtil;
import com.f2prateek.dart.common.util.ParcelerUtil;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({
    HensonProcessor.NAVIGATION_MODEL_ANNOTATION_CLASS_NAME,
    HensonProcessor.INJECT_EXTRA_ANNOTATION_CLASS_NAME
}) @SupportedOptions({
    HensonProcessor.OPTION_HENSON_PACKAGE
}) public class HensonProcessor extends AbstractProcessor {

  static final String NAVIGATION_MODEL_ANNOTATION_CLASS_NAME = "com.f2prateek.dart.NavigationModel";
  static final String INJECT_EXTRA_ANNOTATION_CLASS_NAME = "com.f2prateek.dart.InjectExtra";

  static final String OPTION_HENSON_PACKAGE = "dart.henson.package";

  private LoggingUtil loggingUtil;
  private FileUtil fileUtil;
  private InjectExtraUtil injectExtraUtil;
  private NavigationModelUtil navigationModelUtil;
  private InjectionTargetUtil injectionTargetUtil;

  private String hensonPackage;
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
    navigationModelUtil = new NavigationModelUtil(loggingUtil, injectionTargetUtil, processingEnv);

    parseAnnotationProcessorOptions(processingEnv);
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    injectExtraUtil.setRoundEnvironment(roundEnv);
    navigationModelUtil.setRoundEnvironment(roundEnv);

    Map<TypeElement, InjectionTarget> targetClassMap = findAndParseTargets();
    generateIntentBuilders(targetClassMap);
    generateHensonNavigator(targetClassMap);

    //return false here to let dart process the annotations too
    return false;
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

  private void parseAnnotationProcessorOptions(ProcessingEnvironment processingEnv) {
    hensonPackage = processingEnv.getOptions().get(OPTION_HENSON_PACKAGE);
  }

  private Map<TypeElement, InjectionTarget> findAndParseTargets() {
    Map<TypeElement, InjectionTarget> targetClassMap = new LinkedHashMap<>();

    // Process each @NavigationModel element.
    navigationModelUtil.parseNavigationModelAnnotatedElements(targetClassMap);
    // Process each @InjectExtra element.
    injectExtraUtil.parseInjectExtraAnnotatedElements(targetClassMap);
    // Create injection target tree and inherit extra injections.
    injectionTargetUtil.createInjectionTargetTree(targetClassMap);
    injectionTargetUtil.inheritExtraInjections(targetClassMap);
    // Use only Navigation Models
    injectionTargetUtil.filterNavigationModels(targetClassMap);

    return targetClassMap;
  }

  private void generateIntentBuilders(Map<TypeElement, InjectionTarget> targetClassMap) {
    for (Map.Entry<TypeElement, InjectionTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      InjectionTarget injectionTarget = entry.getValue();

      //we unfortunately can't test that nothing is generated in a TRUTH based test
      try {
        fileUtil.writeFile(new IntentBuilderGenerator(injectionTarget), typeElement);
      } catch (IOException e) {
        loggingUtil.error(typeElement, "Unable to write intent builder for type %s: %s",
            typeElement, e.getMessage());
      }
    }
  }

  private void generateHensonNavigator(Map<TypeElement, InjectionTarget> targetClassMap) {
    if (!targetClassMap.values().isEmpty()) {
      Element[] allTypes = targetClassMap.keySet().toArray(new Element[targetClassMap.size()]);
      try {
        fileUtil.writeFile(new HensonNavigatorGenerator(hensonPackage, targetClassMap.values()),
            allTypes);
      } catch (IOException e) {
        for (Element element : allTypes) {
          loggingUtil.error(element, "Unable to write henson navigator for types %s: %s", element,
              e.getMessage());
        }
      }
    }
  }
}
