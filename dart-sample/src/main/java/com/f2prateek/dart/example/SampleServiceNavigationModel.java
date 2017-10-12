package com.f2prateek.dart.example;

import com.f2prateek.dart.InjectExtra;
import com.f2prateek.dart.NavigationModel;

@NavigationModel("com.f2prateek.dart.example.SampleService")
public class SampleServiceNavigationModel {

  @InjectExtra String stringExtra;
}
