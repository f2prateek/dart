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

package dart;

import static dart.common.util.NavigationModelBindingTargetUtil.NAVIGATION_MODEL_BINDER_SUFFIX;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extra binding utilities. Use this class to simplify getting extras.
 *
 * <p>Injecting extras from your activity is as easy as:
 *
 * <pre><code>
 * public class ExampleActivity extends Activity {
 *   {@literal @}BindExtra("key") String extra;
 *
 *   {@literal @}Override protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     Dart.bind(this);
 *   }
 * }
 * </code></pre>
 *
 * You can bind an {@link #bind(Activity) activity directly}, {@link #bind(android.app.Fragment)
 * fragment directly}, or bind an {@link #bindNavigationModel(Object, Bundle) bundle into another
 * object}.
 *
 * <p>Be default, extras are required to be present in the bundle for field bindings. If an extra is
 * optional add the {@code Nullable @Nullable} annotation.
 *
 * <pre><code>
 * {@literal @}Nullable {@literal @}BindExtra("key") String extra;
 * </code></pre>
 *
 * <p>If you need to provide a default value for an extra, simply set an initial value while
 * declaring the field, combined with the {@code Nullable @Nullable} annotation.
 *
 * <pre><code>
 * {@literal @}Nullable {@literal @}BindExtra("key") String extra = "default_value";
 * </code></pre>
 */
public class Dart {
  public static final String EXTRA_BINDER_SUFFIX = "__ExtraBinder";

  static final Map<Class<?>, Method> EXTRA_BINDERS = new LinkedHashMap<>();
  static final Map<Class<?>, Method> NAVIGATION_MODEL_BINDERS = new LinkedHashMap<>();
  static final Method NO_OP = null;
  private static final String TAG = "Dart";
  private static boolean debug = false;

  private Dart() {
    // No instances.
  }

  /** Control whether debug logging is enabled. */
  public static void setDebug(boolean debug) {
    Dart.debug = debug;
  }

  /**
   * Inject fields annotated with {@link BindExtra} in the specified {@code target} using the {@code
   * source} {@link android.app.Activity}.
   *
   * @param target Target class for field binding.
   * @param source Activity on which IDs will be looked up.
   * @throws Dart.UnableToInjectException if binding could not be performed.
   * @see android.content.Intent#getExtras()
   */
  public static void bindNavigationModel(Object target, Activity source) {
    bindNavigationModel(target, source, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link BindExtra} in the specified {@code target} using the {@code
   * source} {@link android.app.Fragment}.
   *
   * @param target Target class for field binding.
   * @param source Activity on which IDs will be looked up.
   * @throws Dart.UnableToInjectException if binding could not be performed.
   * @see android.content.Intent#getExtras()
   */
  public static void bindNavigationModel(Object target, Fragment source) {
    bindNavigationModel(target, source, Finder.FRAGMENT);
  }

  /**
   * Inject fields annotated with {@link BindExtra} in the specified {@code target} using the {@code
   * source} {@link android.os.Bundle} as the source.
   *
   * @param target Target class for field binding.
   * @param source Bundle source on which extras will be looked up.
   * @throws Dart.UnableToInjectException if binding could not be performed.
   */
  public static void bindNavigationModel(Object target, Bundle source) {
    bindNavigationModel(target, source, Finder.BUNDLE);
  }

  /**
   * Inject fields annotated with {@link BindExtra} in the NavigationModel annotated with {@link
   * DartModel} inside the target {@link android.app.Activity}. The intent that called this activity
   * will be used as the source of the extras bundle.
   *
   * @param target Target activity for field binding.
   * @throws Dart.UnableToInjectException if binding could not be performed.
   * @see android.content.Intent#getExtras()
   */
  public static void bind(Activity target) {
    bind(target, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link BindExtra} in the NavigationModel annotated with {@link
   * DartModel} inside the target {@link android.app.Fragment}. The arguments that this fragment was
   * called with will be used as the source of the extras bundle.
   *
   * @param target Target fragment for field binding.
   * @throws Dart.UnableToInjectException if binding could not be performed.
   * @see android.app.Fragment#getArguments()
   */
  public static void bind(Fragment target) {
    bind(target, Finder.FRAGMENT);
  }

  static void bindNavigationModel(Object target, Object source, Finder finder) {
    Class<?> targetClass = target.getClass();
    try {
      if (debug) Log.d(TAG, "Looking up extra binder for " + targetClass.getName());
      Method bind = findExtraBinderForClass(targetClass);
      if (bind != null) {
        bind.invoke(null, finder, target, source);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new UnableToInjectException("Unable to bind extras for " + target, e);
    }
  }

  private static Method findExtraBinderForClass(Class<?> cls) throws NoSuchMethodException {
    Method bind = EXTRA_BINDERS.get(cls);
    if (bind != null) {
      if (debug) Log.d(TAG, "HIT: Cached in binder map.");
      return bind;
    }
    String clsName = cls.getName();
    if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
      if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
      return NO_OP;
    }
    try {
      Class<?> binder = Class.forName(clsName + EXTRA_BINDER_SUFFIX);
      bind = binder.getMethod("bind", Finder.class, cls, Object.class);
      if (debug) Log.d(TAG, "HIT: Class loaded binding class.");
    } catch (ClassNotFoundException e) {
      if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
      bind = findExtraBinderForClass(cls.getSuperclass());
    }
    EXTRA_BINDERS.put(cls, bind);
    return bind;
  }

  static void bind(Object target, Finder finder) {
    Class<?> targetClass = target.getClass();
    try {
      if (debug) Log.d(TAG, "Looking up NavigationModel binder for " + targetClass.getName());
      Method bind = findNavigationModelBinderForClass(targetClass);
      if (bind != null) {
        bind.invoke(null, finder, target);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new UnableToInjectException("Unable to bind NavigationModel for " + target, e);
    }
  }

  private static Method findNavigationModelBinderForClass(Class<?> cls)
      throws NoSuchMethodException {
    Method bind = NAVIGATION_MODEL_BINDERS.get(cls);
    if (bind != null) {
      if (debug) Log.d(TAG, "HIT: Cached in binder map.");
      return bind;
    }
    String clsName = cls.getName();
    if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
      if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
      return NO_OP;
    }
    try {
      Class<?> binder = Class.forName(clsName + NAVIGATION_MODEL_BINDER_SUFFIX);
      bind = binder.getMethod("bind", Finder.class, cls);
      if (debug) Log.d(TAG, "HIT: Class loaded binding class.");
    } catch (ClassNotFoundException e) {
      if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
      bind = findNavigationModelBinderForClass(cls.getSuperclass());
    }
    NAVIGATION_MODEL_BINDERS.put(cls, bind);
    return bind;
  }

  /** Simpler version of {@link android.os.Bundle#get(String)} which infers the target type. */
  @SuppressWarnings({"unchecked", "UnusedDeclaration"})
  // Checked by runtime cast. Public API.
  public static <T> T get(Bundle bundle, String key) {
    return (T) bundle.get(key);
  }

  /**
   * A means of finding an extra in either an {@link android.app.Activity}, {@link
   * android.app.Fragment} or a {@link android.os.Bundle}. Exposed for use only by generated code.
   * If any of the means to get a bundle are null, this will simply return a null.
   */
  public enum Finder {
    ACTIVITY {
      @Override
      public Object getExtra(Object source, String key) {
        Intent intent = ((Activity) source).getIntent();
        return intent == null ? null : Finder.BUNDLE.getExtra(intent.getExtras(), key);
      }
    },
    FRAGMENT {
      @Override
      public Object getExtra(Object source, String key) {
        Bundle extras = ((Fragment) source).getArguments();
        return Finder.BUNDLE.getExtra(extras, key);
      }
    },
    BUNDLE {
      @Override
      public Object getExtra(Object source, String key) {
        return source == null ? null : ((Bundle) source).get(key);
      }
    };

    public abstract Object getExtra(Object source, String key);
  }

  public static class UnableToInjectException extends RuntimeException {
    UnableToInjectException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
