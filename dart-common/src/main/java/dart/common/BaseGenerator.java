package dart.common;

/**
 * Base class of code generators.
 * They generate java code.
 */
public abstract class BaseGenerator {

  /**
   * Create all Java code
   * @return the javacode as string.
   */
  public abstract String brewJava();

  /**
   * @return the Fully Qualified Class Name of the generated code.
   */
  public abstract String getFqcn();
}
