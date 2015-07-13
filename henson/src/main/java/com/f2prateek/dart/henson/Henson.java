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

package com.f2prateek.dart.henson;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.f2prateek.dart.Nullable;
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
 * #inject(Fragment) fragment directly}, or inject an
 * {@link #inject(Object, Bundle) bundle into another object}.
 * <p>
 * Be default, extras are required to be present in the bundle for field injections.
 * If an extra is optional add the {@link Nullable @Nullable} annotation.
 * <pre><code>
 * {@literal @}Nullable {@literal @}InjectExtra("key") String extra;
 * </code></pre>
 * <p>
 * If you need to provide a default value for an extra, simply set an initial value
 * while declaring the field, combined with the {@link Nullable @Nullable} annotation.
 * <pre><code>
 * {@literal @}Nullable {@literal @}InjectExtra("key") String extra = "default_value";
 * </code></pre>
 */
public class Henson {
  public static final String INJECTOR_SUFFIX = "$$ExtraInjector";

  static final Map<Class<?>, Method> INJECTORS = new LinkedHashMap<Class<?>, Method>();
  static final Method NO_OP = null;
  private static final String TAG = "Dart";
  private static boolean debug = false;

  private Henson() {
    // No instances.
  }

  /** Control whether debug logging is enabled. */
  public static void setDebug(boolean debug) {
    Henson.debug = debug;
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@link
   * Activity}.
   * The intent that called this activity will be used as the source of the extras bundle.
   *
   * @param target Target activity for field injection.
   * @throws com.f2prateek.dart.henson.Henson.UnableToInjectException if injection could not be
   * performed.
   * @see Intent#getExtras()
   */
  public static void inject(Activity target) {
    inject(target, target, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@link
   * Fragment}.
   * The arguments that this fragment was called with will be used as the source of the extras
   * bundle.
   *
   * @param target Target fragment for field injection.
   * @throws com.f2prateek.dart.henson.Henson.UnableToInjectException if injection could not be
   * performed.
   * @see Fragment#getArguments()
   */
  public static void inject(Fragment target) {
    inject(target, target, Finder.FRAGMENT);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@code
   * target} using the {@code source} {@link Activity}.
   *
   * @param target Target class for field injection.
   * @param source Activity on which IDs will be looked up.
   * @throws com.f2prateek.dart.henson.Henson.UnableToInjectException if injection could not be
   * performed.
   * @see Intent#getExtras()
   */
  public static void inject(Object target, Activity source) {
    inject(target, source, Finder.ACTIVITY);
  }

  /**
   * Inject fields annotated with {@link com.f2prateek.dart.InjectExtra} in the specified {@code
   * target} using the {@code source} {@link Bundle} as the source.
   *
   * @param target Target class for field injection.
   * @param source Bundle source on which extras will be looked up.
   * @throws com.f2prateek.dart.henson.Henson.UnableToInjectException if injection could not be
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
      Class<?> injector = Class.forName(clsName + INJECTOR_SUFFIX);
      inject = injector.getMethod("inject", Finder.class, cls, Object.class);
      if (debug) Log.d(TAG, "HIT: Class loaded injection class.");
    } catch (ClassNotFoundException e) {
      if (debug) Log.d(TAG, "Not found. Trying superclass " + cls.getSuperclass().getName());
      inject = findInjectorForClass(cls.getSuperclass());
    }
    INJECTORS.put(cls, inject);
    return inject;
  }

  /** Simpler version of {@link Bundle#get(String)} which infers the target type. */
  @SuppressWarnings({ "unchecked", "UnusedDeclaration" })
  // Checked by runtime cast. Public API.
  public static <T> T get(Bundle bundle, String key) {
    return (T) bundle.get(key);
  }

  /**
   * A means of finding an extra in either an {@link Activity}, {@link
   * Fragment} or a {@link Bundle}. Exposed for use only
   * by generated code.
   * If any of the means to get a bundle are null, this will simply return a null.
   */
  public enum Finder {
    ACTIVITY {
      @Override public Object getExtra(Object source, String key) {
        Intent intent = ((Activity) source).getIntent();
        return intent == null ? null : Finder.BUNDLE.getExtra(intent.getExtras(), key);
      }
    },
    FRAGMENT {
      @Override public Object getExtra(Object source, String key) {
        Bundle extras = ((Fragment) source).getArguments();
        return Finder.BUNDLE.getExtra(extras, key);
      }
    },
    BUNDLE {
      @Override public Object getExtra(Object source, String key) {
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
