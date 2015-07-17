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

package com.f2prateek.dart.henson.processor;

import com.f2prateek.dart.InjectExtra;
import com.f2prateek.dart.common.AbstractDartProcessor;
import com.f2prateek.dart.common.InjectionTarget;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * Henson annotation processor.
 * It will process all annotations : {@link InjectExtra} and
 * invoke {@link IntentBuilderGenerator} and {@link HensonNavigatorGenerator}.
 * It supports the annotation processor option {@code #OPTION_HENSON_PACKAGE}
 * that lets you determine in which package the generated {@Code Henson} navigator class
 * will be generated.
 * If this option is not present, then the annotation processor tries to find a common
 * package between all classes that contain the {@link InjectExtra} annotation.
 * @see HensonNavigatorGenerator#findCommonPackage(java.util.Collection)
 */
public final class HensonExtraProcessor extends AbstractDartProcessor {

  public static final String OPTION_HENSON_PACKAGE = "dart.henson.package";
  private String hensonPackage;

  @Override public Set<String> getSupportedOptions() {
    Set<String> supportedOptions = new LinkedHashSet<String>();
    supportedOptions.addAll(super.getSupportedOptions());
    supportedOptions.add(OPTION_HENSON_PACKAGE);
    return supportedOptions;
  }

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    hensonPackage = env.getOptions().get(OPTION_HENSON_PACKAGE);
  }

  @Override public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    Map<TypeElement, InjectionTarget> targetClassMap = findAndParseTargets(env);

    for (Map.Entry<TypeElement, InjectionTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      InjectionTarget injectionTarget = entry.getValue();

      // Now write the IntentBuilder
      Writer writer = null;

      // Generate the IntentBuilder
      try {
        IntentBuilderGenerator intentBuilderGenerator = new IntentBuilderGenerator(injectionTarget);
        JavaFileObject jfo = filer.createSourceFile(intentBuilderGenerator.getFqcn(), typeElement);
        writer = jfo.openWriter();
        if (isDebugEnabled) {
          System.out.println(
              "IntentBuilder generated:\n" + intentBuilderGenerator.brewJava() + "---");
        }
        writer.write(intentBuilderGenerator.brewJava());
      } catch (IOException e) {
        error(typeElement, "Unable to write intent builder for type %s: %s", typeElement,
            e.getMessage());
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            error(typeElement, "Unable to close intent builder source file for type %s: %s",
                typeElement, e.getMessage());
          }
        }
      }
    }

    // Generate the Henson navigator
    Writer writer = null;

    if (!targetClassMap.values().isEmpty()) {
      Element[] allTypes = targetClassMap.keySet().toArray(new Element[targetClassMap.size()]);
      try {
        HensonNavigatorGenerator hensonNavigatorGenerator =
            new HensonNavigatorGenerator(hensonPackage, targetClassMap.values());
        JavaFileObject jfo = filer.createSourceFile(hensonNavigatorGenerator.getFqcn(), allTypes);
        writer = jfo.openWriter();
        if (isDebugEnabled) {
          System.out.println(
              "Henson navigator generated:\n" + hensonNavigatorGenerator.brewJava() + "---");
        }
        writer.write(hensonNavigatorGenerator.brewJava());
      } catch (IOException e) {
        e.printStackTrace();
        for (Element element : allTypes) {
          error(element, "Unable to write henson navigator for types %s: %s", element,
              e.getMessage());
        }
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            e.printStackTrace();
            for (Element element : allTypes) {
              error(element, "Unable to close intent builder source file for type %s: %s", element,
                  e.getMessage());
            }
          }
        }
      }
    }

    //return false here to let dart process the annotations too
    return false;
  }
}
