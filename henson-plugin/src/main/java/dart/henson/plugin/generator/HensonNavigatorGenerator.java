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

package dart.henson.plugin.generator;

import static java.lang.String.format;

import java.util.Set;

public class HensonNavigatorGenerator {

  public String generateHensonNavigatorClass(Set<String> targetActivities, String packageName) {
    String packageStatement = "package " + packageName + ";\n";

    StringBuilder importStatement = new StringBuilder("import android.content.Context;\n");
    targetActivities
        .stream()
        .forEach(
            targetActivity -> {
              importStatement.append(format("import %s__IntentBuilder;\n", targetActivity));
            });

    String classStartStatement = "public class HensonNavigator {\n";
    StringBuilder methodStatement = new StringBuilder();
    targetActivities
        .stream()
        .forEach(
            targetActivity -> {
              String targetActivitySimpleName =
                  targetActivity.substring(
                      1 + targetActivity.lastIndexOf('.'), targetActivity.length());
              String targetActivityCapitalizedName =
                  dart.henson.plugin.util.StringUtil.capitalize(targetActivitySimpleName);
              methodStatement.append(
                  format(
                      "  public static %s__IntentBuilder goto%s(Context context) {\n",
                      targetActivityCapitalizedName, targetActivityCapitalizedName));
              methodStatement.append(
                  format(
                      "    return new %s__IntentBuilder(context);\n",
                      targetActivityCapitalizedName));
              methodStatement.append("  }\n");
            });
    String classEndStatement = "}\n";
    return new StringBuilder()
        .append(packageStatement)
        .append(importStatement)
        .append(classStartStatement)
        .append(methodStatement)
        .append(classEndStatement)
        .toString();
  }
}
