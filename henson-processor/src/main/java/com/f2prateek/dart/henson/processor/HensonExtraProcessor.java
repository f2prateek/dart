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

import com.f2prateek.dart.common.AbstractDartProcessor;
import com.f2prateek.dart.common.InjectionTarget;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public final class HensonExtraProcessor extends AbstractDartProcessor {

  @Override public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    Map<TypeElement, InjectionTarget> targetClassMap = findAndParseTargets(env);

    for (Map.Entry<TypeElement, InjectionTarget> entry : targetClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      InjectionTarget injectionTarget = entry.getValue();

      // Now write the IntentBuilder
      Writer writer = null;

      // Generate the IntentBuilder
      try {
        IntentBuilder intentBuilder = new IntentBuilder(injectionTarget);
        JavaFileObject jfo = filer.createSourceFile(intentBuilder.getFqcn(), typeElement);
        writer = jfo.openWriter();
        //TODO this should be turned on by a processor option
        //to debug : un-comment this line
        //System.out.println(intentBuilder.brewJava());
        writer.write(intentBuilder.brewJava());
      } catch (IOException e) {
        error(typeElement, "Unable to write intent builder for type %s: %s", typeElement,
              e.getMessage());
      } finally {
        if (writer != null) {
          try {
            writer.close();
          } catch (IOException e) {
            error(typeElement, "Unable to close intent builder source file for type %s: %s",
                  typeElement,
                  e.getMessage());
          }
        }
      }
    }

    //return false here to let dart process the annotations too
    return false;
  }
}
