package dart.henson.plugin;

public class HensonPluginExtension {
    private String navigatorPackageName;
    private boolean navigatorOnly;

    public String getNavigatorPackageName() {
        return navigatorPackageName;
    }

    public void setNavigatorPackageName(String navigatorPackageName) {
        this.navigatorPackageName = navigatorPackageName;
    }

    public boolean isNavigatorOnly() {
        return navigatorOnly;
    }

    public void setNavigatorOnly(boolean navigatorOnly) {
        this.navigatorOnly = navigatorOnly;
    }
}
