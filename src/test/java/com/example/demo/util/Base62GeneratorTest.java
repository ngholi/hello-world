package com.example.demo.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class Base62GeneratorTest {

    private final Base62Generator generator = new Base62Generator();

    @Test
    void shouldGenerateCodeOfCorrectLength() {
        String code = generator.generate();
        assertThat(code).hasSize(8);
    }

    @Test
    void shouldGenerateDifferentCodes() {
        String code1 = generator.generate();
        String code2 = generator.generate();
        assertThat(code1).isNotEqualTo(code2);
    }

    @Test
    void shouldOnlyContainBase62Characters() {
        String code = generator.generate();
        assertThat(code).matches("^[a-zA-Z0-9]*$");
    }
}
