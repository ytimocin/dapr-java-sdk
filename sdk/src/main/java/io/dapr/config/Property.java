/*
 * Copyright 2021 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.config;

import java.util.logging.Logger;

/**
 * A configuration property in the Dapr's SDK.
 */
public abstract class Property<T> {

  private static final Logger LOGGER = Logger.getLogger(Property.class.getName());

  /**
   * Property's name as a Java Property.
   */
  private final String name;

  /**
   * Property's name as a environment variable.
   */
  private final String envName;

  /**
   * Default value.
   */
  private final T defaultValue;

  /**
   * Instantiates a new configuration property.
   *
   * @param name Java property name.
   * @param envName Environment variable name.
   * @param defaultValue Default value.
   */
  Property(String name, String envName, T defaultValue) {
    this.name = name;
    this.envName = envName;
    this.defaultValue = defaultValue;
  }

  /**
   * Gets the Java property's name.
   * @return Name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Gets the environment variable's name.
   * @return Name.
   */
  public String getEnvName() {
    return this.envName;
  }

  /**
   * Gets the value defined by system property first, then env variable or sticks to default.
   *
   * @return Value from system property (1st) or env variable (2nd) or default (last).
   */
  public T get() {
    return this.get(null);
  }

  /**
   * Gets the value defined by system property first, then env variable or sticks to default.
   * @param override overrides the property value
   * @return Value from system property (1st) or env variable (2nd) or default (last).
   */
  public T get(String override) {
    if ((override != null) && !override.isEmpty()) {
      try {
        return this.parse(override);
      } catch (IllegalArgumentException e) {
        LOGGER.warning(String.format("Invalid override value in property: %s", this.name));
        // OK, we tried. Falling back to system environment variable.
      }
    }

    String propValue = System.getProperty(this.name);
    if (propValue != null && !propValue.trim().isEmpty()) {
      try {
        return this.parse(propValue);
      } catch (IllegalArgumentException e) {
        LOGGER.warning(String.format("Invalid value in property: %s", this.name));
        // OK, we tried. Falling back to system environment variable.
      }
    }

    String envValue = System.getenv(this.envName);
    if (envValue != null && !envValue.trim().isEmpty()) {
      try {
        return this.parse(envValue);
      } catch (IllegalArgumentException e) {
        LOGGER.warning(String.format("Invalid value in environment variable: %s", this.envName));
        // OK, we tried. Falling back to default.
      }
    }

    return this.defaultValue;
  }

  /**
   * Parses the value to the specific type.
   * @param value String value to be parsed.
   * @return Value in the specific type.
   */
  protected abstract T parse(String value);
}
