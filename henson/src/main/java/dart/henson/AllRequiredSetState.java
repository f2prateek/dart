package dart.henson;

import android.content.Intent;

public class AllRequiredSetState extends State {
  private Intent intent;

  public AllRequiredSetState(Bundler bundler, Intent intent) {
    super(bundler);
    this.intent = intent;
  }

  public Intent build() {
    intent.putExtras(bundler.get());
    return intent;
  }
}
