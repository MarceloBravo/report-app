package com.saas.report.tenant.infrastructure.schema.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlSchemaQueryProviderStrategyTest {

    private final PostgresqlSchemaQueryProviderStrategy strategy = new PostgresqlSchemaQueryProviderStrategy();

    @Test
    void implementaElContratoSchemaQueryProvider() {
        assertThat(strategy).isInstanceOf(SchemaQueryProvider.class);
    }

    @Test
    void retornaUnaQueryNoVacia() {
        assertThat(strategy.getSchemaQuery()).isNotBlank();
    }

    @Test
    void exponeUnUnicoParametroBindParaElEsquema() {
        assertThat(strategy.getSchemaQuery()).contains("WHERE c.table_schema = ?");
        assertThat(strategy.getSchemaQuery()).doesNotContain("?,");
    }

    @Test
    void seleccionaLasColumnasNecesariasParaConstruirElEsquema() {
        assertThat(strategy.getSchemaQuery()).contains(
                "c.table_name",
                "c.column_name",
                "c.data_type",
                "c.is_identity",
                "es_primary_key",
                "es_foreign_key",
                "tabla_referenciada",
                "columna_referenciada");
    }

    @Test
    void excluyeLasTablasDeSistemaDePostgresql() {
        assertThat(strategy.getSchemaQuery())
                .contains("c.table_name NOT LIKE 'pg_%'")
                .contains("c.table_name NOT IN ('schema_migrations', 'flyway_schema_history')");
    }

    @Test
    void ordenaPorTablaYPosicionDeColumna() {
        assertThat(strategy.getSchemaQuery())
                .contains("ORDER BY c.table_name, c.ordinal_position");
    }

    @Test
    void noExponeValoresDeColumnaComoLiteralesDeTexto() {
        assertThat(strategy.getSchemaQuery())
                .doesNotContain("'YES' = c.is_nullable")
                .contains("c.is_nullable = 'YES' AS es_nullable");
    }

    @Test
    void noUsaColumnasInexistentesDeKeyColumnUsage() {
        assertThat(strategy.getSchemaQuery())
                .doesNotContain("kcu.referenced_table_name")
                .doesNotContain("kcu.referenced_column_name");
    }

    @Test
    void obtieneLaTablaYColumnaReferenciadaConLasVistasPropiasDePostgresql() {
        assertThat(strategy.getSchemaQuery())
                .contains("information_schema.referential_constraints")
                .contains("kcu_ref.table_name AS tabla_referenciada")
                .contains("kcu_ref.column_name AS columna_referenciada");
    }

    @Test
    void emparejaLaColumnaDeLaFkConLaPosicionDeLaColumnaReferenciada() {
        assertThat(strategy.getSchemaQuery())
                .contains("kcu.ordinal_position = kcu_ref.ordinal_position");
    }
}
