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

package com.f2prateek.dart;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import com.f2prateek.dart.internal.InjectExtraProcessor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extra injection utilities. Use this class to simplify getting extras.
 * <p>
 * Injecting extras from your activity is as easy as:
 * <pre><code>
 * public class ExampleActivity extends Activity {
 *   {@literal @}InjectExtra("key") String extra;
 *
 *   {@literal @}Override protected void onCreate(Bundle savedInstanceState) {
 *     super.onCreate(savedInstanceState);
 *     Dart.inject(this);
 *   }
 * }
 * </code></pre>
 * You can inject an {@link #inject(Activity) activity directly}, {@link
 * #inject(android.app.Fragment) fragment directly}, or inject an
 * {@link #inject(Object, Bundle) bundle into another object}.
 * <p>
 * Be default, extras are required to be present in the bundle for field injections.
 * If an extra is optional add the {@link Optional @Optional} annotation.
 * <pre><code>
 * {@literal @}Optional {@literal @}InjectExtra("key") String extra;
 * </code></pre>
 */
public class Dart {
  static final Map<Class<?>, Method> INJECTORS = new LinkedHashMap<Class<?>, Method>();
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
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@link
   * android.app.Activity}.
   * The intent that called this activity will be used as the source of the extras bundle.
   *
   * @param target Target activity for field injection.
   * @throws com.f2prateek.dart.Dart.UnableToInjectException if injection could not be
   * performed.
   * @see android.content.Intent#getExtras()
   */
  public static void inject(Activity target) {
    inject(target, target, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@link
   * android.app.Fragment}.
   * The arguments that this fragment was called with will be used as the source of the extras
   * bundle.
   *
   * @param target Target fragment for field injection.
   * @throws com.f2prateek.dart.Dart.UnableToInjectException if injection could not be
   * performed.
   * @see android.app.Fragment#getArguments()
   */
  public static void inject(Fragment target) {
    inject(target, target, Finder.FRAGMENT);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@code
   * target} using the {@code source} {@link android.app.Activity}.
   *
   * @param target Target class for field injection.
   * @param source Activity on which IDs will be looked up.
   * @throws com.f2prateek.dart.Dart.UnableToInjectException if injection could not be
   * performed.
   * @see android.content.Intent#getExtras()
   */
  public static void inject(Object target, Activity source) {
    inject(target, source, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@code
   * target} using the {@code source} {@link android.os.Bundle} as the source.
   *
   * @param target Target class for field injection.
   * @param source Bundle source on which extras will be looked up.
   * @throws com.f2prateek.dart.Dart.UnableToInjectException if injection could not be
   * performed.
   */
  public static void inject(Object target, Bundle source) {
    inject(target, source, Finder.BUNDLE);
  }

  static void inject(Object target, Object source, Finder finder) {
    Class<?> targetClass = target.getClass();
    try {
      if (debug) Log.d(TAG, "Looking up extra injector for " + targetClass.getName());
      Method inject = findInjectorForClass(targetClass);
      if (inject != null) {
        inject.invoke(null, finder, target, source);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new UnableToInjectException("Unable to inject extras for " + target, e);
    }
  }

  private static Method findInjectorForClass(Class<?> cls) throws NoSuchMethodException {
    Method inject = INJECTORS.get(cls);
    if (inject != null) {
      if (debug) Log.d(TAG, "HIT: Cached in injector map.");
      return inject;
    }
    String clsName = cls.getName();
    if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
      if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
      return NO_OP;
    }
    try {
      Class<?> injector = Class.forName(clsName + InjectExtraProcessor.SUFFIX);
      inject = injector.getMethod("inject", Finder.class, cls, Object.class);
      if (debug) Log.d(TAG, "HIT: Class loaded injection class.");
    } catch (ClassNotFoundException e) {
      if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
      inject = findInjectorForClass(cls.getSuperclass());
    }
    INJECTORS.put(cls, inject);
    return inject;
  }

  /** Simpler version of {@link android.os.Bundle#get(String)} which infers the target type. */
  @SuppressWarnings({ "unchecked", "UnusedDeclaration" }) // Checked by runtime cast. Public API.
  public static <T> T get(Bundle bundle, String key) {
    return (T) bundle.get(key);
  }

  /**
   * A means of finding a view in either an {@link android.app.Activity}, {@link
   * android.app.Fragment} or a {@link android.os.Bundle}. Exposed for use only
   * by generated code.
   */
  public enum Finder {
    ACTIVITY {
      @Override public Object getExtra(Object source, String key) {
        return ((Activity) source).getIntent().getExtras().get(key);
      }
    },
    FRAGMENT {
      @Override public Object getExtra(Object source, String key) {
        return ((Fragment) source).getArguments().get(key);
      }
    },
    BUNDLE {
      @Override public Object getExtra(Object source, String key) {
        return ((Bundle) source).get(key);
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
