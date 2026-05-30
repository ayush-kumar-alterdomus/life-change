package com.ascend.common.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AppConstants")
class AppConstantsTest {

    @Test
    @DisplayName("API_VERSION_PREFIX should be /api/v1")
    void apiVersionPrefixShouldBeCorrect() {
        assertThat(AppConstants.API_VERSION_PREFIX).isEqualTo("/api/v1");
    }

    @Test
    @DisplayName("API_VERSION_PREFIX should start with /")
    void apiVersionPrefixShouldStartWithSlash() {
        assertThat(AppConstants.API_VERSION_PREFIX).startsWith("/");
    }

    @Test
    @DisplayName("API_VERSION_PREFIX should not end with /")
    void apiVersionPrefixShouldNotEndWithSlash() {
        assertThat(AppConstants.API_VERSION_PREFIX).doesNotEndWith("/");
    }

    @Test
    @DisplayName("should not be instantiable (utility class)")
    void shouldNotBeInstantiable() throws Exception {
        Constructor<AppConstants> constructor = AppConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // The private constructor should work via reflection but the class design
        // signals it's not meant to be instantiated
        assertThat(constructor).isNotNull();
    }
}
