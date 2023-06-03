package org.abelsromero.parallels.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ConfigurationTest {

    @Nested
    class WhenReading {

        @Nested
        class AString {

            @Nested
            class FromEnvironmentVariable {

                @Test
                @Disabled("TODO: find good solution to inject environment variables")
                public void shouldGetValue() {
                    var value = Configuration.readString("a-key");
                    assertThat(value).isEmpty();
                }

                @Test
                public void shouldReturnEmpty() {
                    var value = Configuration.readString("a-key");
                    assertThat(value).isEmpty();
                }
            }

            @Nested
            class FromProperty {

                public static final String PROPERTY_KEY = "a.string.key";

                @Test
                void shouldGetValue() {
                    System.setProperty(PROPERTY_KEY, "a.value");
                    var value = Configuration.readString(PROPERTY_KEY);
                    assertThat(value).hasValue("a.value");
                }

                @Test
                void shouldReturnEmpty() {
                    var value = Configuration.readString(PROPERTY_KEY);
                    assertThat(value).isEmpty();
                }

                @AfterEach
                void afterEach() {
                    System.clearProperty(PROPERTY_KEY);
                }
            }
        }

        @Nested
        class AnInteger {

            @Nested
            class FromEnvironmentVariable {

                @Test
                public void shouldReturnEmpty() {
                    var value = Configuration.readInteger("a-key");
                    assertThat(value).isEmpty();
                }
            }

            @Nested
            class FromProperty {

                public static final String PROPERTY_KEY = "a.int.key";

                @Test
                void shouldGetValue() {
                    System.setProperty(PROPERTY_KEY, "42");
                    var value = Configuration.readInteger(PROPERTY_KEY);
                    assertThat(value).hasValue(42);
                }

                @Test
                void shouldReturnEmpty() {
                    var value = Configuration.readInteger(PROPERTY_KEY);
                    assertThat(value).isEmpty();
                }

                @Test
                void shouldFailWhenNotAnInteger() {
                    System.setProperty(PROPERTY_KEY, "not-a-number");
                    Throwable throwable = catchThrowable(() -> Configuration.readInteger(PROPERTY_KEY));
                    assertThat(throwable).isInstanceOf(NumberFormatException.class);
                }

                @AfterEach
                void afterEach() {
                    System.clearProperty(PROPERTY_KEY);
                }
            }
        }

        @Nested
        class ABoolean {

            @Nested
            class FromEnvironmentVariable {

                @Test
                public void shouldReturnEmpty() {
                    var value = Configuration.readInteger("a-key");
                    assertThat(value).isEmpty();
                }
            }

            @Nested
            class FromProperty {

                public static final String PROPERTY_KEY = "a.boolean.key";

                @Test
                void shouldGetTrueValue() {
                    System.setProperty(PROPERTY_KEY, "true");
                    var value = Configuration.readBoolean(PROPERTY_KEY);
                    assertThat(value).hasValue(true);
                }

                @ParameterizedTest
                @ValueSource(strings = {"false", "no", "anything else"})
                void shouldGetFalseValue(String propertyValue) {
                    System.setProperty(PROPERTY_KEY, propertyValue);
                    var value = Configuration.readBoolean(PROPERTY_KEY);
                    assertThat(value).hasValue(false);
                }

                @Test
                void shouldReturnEmpty() {
                    var value = Configuration.readInteger(PROPERTY_KEY);
                    assertThat(value).isEmpty();
                }

                @AfterEach
                void afterEach() {
                    System.clearProperty(PROPERTY_KEY);
                }
            }
        }
    }
}
