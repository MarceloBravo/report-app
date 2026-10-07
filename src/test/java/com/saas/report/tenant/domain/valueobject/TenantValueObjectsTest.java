package com.saas.report.tenant.domain.valueobject;

import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.exception.InvalidUUIDException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantValueObjectsTest {

    @Test
    void idsDeTenantRechazanNulo() {
        assertThatThrownBy(() -> new TenantUUID(null)).isInstanceOf(InvalidUUIDException.class);
        assertThatThrownBy(() -> new TenantDbConnectionUUID(null)).isInstanceOf(InvalidUUIDException.class);
    }

    @Test
    void idsDeTenantExponenSuValor() {
        UUID uuid = UUID.randomUUID();

        assertThat(new TenantUUID(uuid).getValue()).isEqualTo(uuid);
        assertThat(new TenantDbConnectionUUID(uuid).getValue()).isEqualTo(uuid);
    }

    @Test
    void generateProduceValoresDistintos() {
        assertThat(TenantUUID.generate().getValue()).isNotNull();
        assertThat(TenantUUID.generate()).isNotEqualTo(TenantUUID.generate());
        assertThat(TenantDbConnectionUUID.generate()).isNotEqualTo(TenantDbConnectionUUID.generate());
    }

    @Test
    void losIdsDeUnMismoValorSonIguales() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertThat(new TenantUUID(uuid)).isEqualTo(new TenantUUID(uuid));
        assertThat(new TenantUUID(uuid).hashCode()).isEqualTo(new TenantUUID(uuid).hashCode());
        assertThat(new TenantDbConnectionUUID(uuid)).isEqualTo(new TenantDbConnectionUUID(uuid));
    }
}