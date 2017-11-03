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

package dart.common.util;

import dart.common.BaseGenerator;
import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

public class FileUtil {

  private final Filer filer;

  private boolean isDebugEnabled = false;

  public FileUtil(ProcessingEnvironment processingEnv) {
    filer = processingEnv.getFiler();
  }

  public void writeFile(BaseGenerator generator, Element... originatingElements)
      throws IOException {
    Writer writer = null;
    try {
      JavaFileObject jfo = filer.createSourceFile(generator.getFqcn(), originatingElements);
      writer = jfo.openWriter();

      if (isDebugEnabled) {
        System.out.println("File generated:\n" + generator.brewJava() + "---");
      }

      writer.write(generator.brewJava());
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }
}
