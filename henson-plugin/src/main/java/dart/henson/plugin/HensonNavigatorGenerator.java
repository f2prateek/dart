package dart.henson.plugin;

import java.util.Set;

import static java.lang.String.format;

public class HensonNavigatorGenerator {

    public String generateHensonNavigatorClass(Set<String> targetActivities, String packageName) {
        String packageStatement = "package " + packageName + ";\n";

        StringBuilder importStatement = new StringBuilder("import android.content.Context;\n");
        targetActivities.stream().forEach(targetActivity -> {
            importStatement.append(format("import %s__IntentBuilder;\n", targetActivity));
        });

        String classStartStatement = "public class HensonNavigator {\n";
        StringBuilder methodStatement = new StringBuilder();
        targetActivities.stream().forEach(targetActivity -> {
                    String targetActivitySimpleName = targetActivity.substring(1 + targetActivity.lastIndexOf('.'), targetActivity.length());
                    String targetActivityCapitalizedName = StringUtil.capitalize(targetActivitySimpleName);
                    methodStatement.append(format("  public static %s__IntentBuilder goto%s(Context context) {\n",
                            targetActivityCapitalizedName, targetActivityCapitalizedName));
                    methodStatement.append(format("    return new %s__IntentBuilder(context);\n", targetActivityCapitalizedName));
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
