package dart.henson;

public abstract class State {
  protected final Bundler bundler;

  public State(Bundler bundler) {
    this.bundler = bundler;
  }
}
