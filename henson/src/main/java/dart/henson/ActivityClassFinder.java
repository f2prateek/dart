package dart.henson;

public class ActivityClassFinder {
    public static Class getClassDynamically(String className) {
        try {
            return Class.forName(className);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
