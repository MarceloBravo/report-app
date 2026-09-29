package com.saas.report.shared.presentation.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void proyectaUnaPaginaCompleta() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 10);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(5);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isFalse();
    }

    @Test
    void marcaLaPrimeraYUltimaPagina() {
        Page<String> primera = new PageImpl<>(List.of("a"), PageRequest.of(0, 2), 5);
        Page<String> ultima = new PageImpl<>(List.of("e"), PageRequest.of(2, 2), 5);

        assertThat(PageResponse.from(primera).first()).isTrue();
        assertThat(PageResponse.from(primera).last()).isFalse();
        assertThat(PageResponse.from(ultima).first()).isFalse();
        assertThat(PageResponse.from(ultima).last()).isTrue();
    }

    @Test
    void proyectaUnaPaginaVacia() {
        PageResponse<String> response = PageResponse.from(Page.empty());

        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isZero();
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isOne();
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
    }
}
