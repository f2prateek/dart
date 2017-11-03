package dart.henson.processor;

import javax.annotation.processing.Processor;
import java.util.Arrays;

public class ProcessorTestUtilities {
  static Iterable<? extends Processor> hensonProcessors() {
    return Arrays.asList(new HensonProcessor());
  }

  static Iterable<? extends Processor> hensonProcessorWithoutParceler() {
    HensonProcessor hensonProcessor = new HensonProcessor();
    hensonProcessor.enableParceler(false);
    return Arrays.asList(hensonProcessor);
  }
}
