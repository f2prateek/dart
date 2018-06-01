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

package com.f2prateek.dart.module1;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import dart.Dart;

public class Module1Service extends IntentService {

  // We need to instantiate the navigation model for services.
  // The reason for this is that for activities and fragments Dart.bind(Activity/Fragment)
  // knows how to get intent or the arguments that the navigation model will be bound to.
  // In this case, Dart.bind will handle the creation of the navigation model instance.
  // In the case of a service, we cannot access the intent from the service, so we need
  // to use Dart.bindNavigationModel so we can pass the extras and it requires an instance
  // of the navigation model.
  private Module1ServiceNavigationModel navigationModel = new Module1ServiceNavigationModel();

  public Module1Service() {
    super("Module1Service");
  }

  @Override protected void onHandleIntent(Intent intent) {
    Dart.bindNavigationModel(navigationModel, intent.getExtras());
    Log.d("DH3", String.format("Module1Service onHandleIntent called with extra:  %s", navigationModel.stringExtra));
  }
}
