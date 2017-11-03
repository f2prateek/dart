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

package dart.common;

/** Base class of code generators. They generate java code. */
public abstract class BaseGenerator {

  /**
   * Create all Java code
   *
   * @return the javacode as string.
   */
  public abstract String brewJava();

  /** @return the Fully Qualified Class Name of the generated code. */
  public abstract String getFqcn();
}
