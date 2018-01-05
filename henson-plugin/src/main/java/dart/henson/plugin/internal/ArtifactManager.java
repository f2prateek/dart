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

package dart.henson.plugin.internal;

import com.android.build.gradle.api.BaseVariant;

import org.gradle.api.logging.Logger;

/**
 * We create one artifact per variant. It will include all the classes of the navigation source tree
 * (code + generated code).
 */
public class ArtifactManager {

  public static final String NAVIGATION_ARTIFACT_PREFIX = "NavigationArtifact";

  private Logger logger;

  public ArtifactManager(Logger logger) {
    this.logger = logger;
  }

  public String getNavigationArtifactName(BaseVariant variant) {
    return variant.getName() + NAVIGATION_ARTIFACT_PREFIX;
  }
}
