package com.f2prateek.dart.example;
import android.content.Context;
import com.f2prateek.dart.Module1ActivityNavigationModel__IntentBuilder;
import com.f2prateek.dart.Module1ServiceNavigationModel__IntentBuilder;
import com.f2prateek.dart.example.SampleActivityNavigationModel__IntentBuilder;
public class HensonNavigator {
  public static Module1ActivityNavigationModel__IntentBuilder.RequiredSequence<Module1ActivityNavigationModel__IntentBuilder.ResolvedAllSet> gotoModule1ActivityNavigationModel(Context context) {
    return Module1ActivityNavigationModel__IntentBuilder.getInitialState(context);
  }
  public static Module1ServiceNavigationModel__IntentBuilder.RequiredSequence<Module1ServiceNavigationModel__IntentBuilder.ResolvedAllSet> gotoModule1ServiceNavigationModel(Context context) {
    return Module1ServiceNavigationModel__IntentBuilder.getInitialState(context);
  }
  public static SampleActivityNavigationModel__IntentBuilder.RequiredSequence<SampleActivityNavigationModel__IntentBuilder.ResolvedAllSet> gotoSampleActivityNavigationModel(Context context) {
    return SampleActivityNavigationModel__IntentBuilder.getInitialState(context);
  }
}

