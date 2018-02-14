package test;

import com.f2prateek.dart.Dart;

public class TestPreCompiled$$ExtraInjector {

    public static void inject(Dart.Finder finder, TestPreCompiled target, Object source) {
        Object object;
        object = finder.getExtra(source, "key");
        if (object == null) {
            throw new IllegalStateException("Required extra with key 'key' for field 'extra'" +
            "was not found.If this extra is optional add '@Nullable' annotation." );
        }
        target.extra = (String) object;
    }
}
