package dart.common.util;

import dart.common.BaseGenerator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;

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
