package dart.henson;

public class RequiredStateSequence<ALL_REQUIRED_SET_STATE extends State> extends State {
  protected final ALL_REQUIRED_SET_STATE allRequiredSetState;

  public RequiredStateSequence(Bundler bundler, ALL_REQUIRED_SET_STATE allRequiredSetState) {
    super(bundler);
    this.allRequiredSetState = allRequiredSetState;
  }
}
