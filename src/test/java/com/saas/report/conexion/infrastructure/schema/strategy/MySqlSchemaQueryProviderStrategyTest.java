package com.saas.report.conexion.infrastructure.schema.strategy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MySqlSchemaQueryProviderStrategyTest {

    private final MySqlSchemaQueryProviderStrategy strategy = new MySqlSchemaQueryProviderStrategy();

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
                "c.extra = 'auto_increment' AS es_identity",
                "es_primary_key",
                "es_foreign_key",
                "tabla_referenciada",
                "columna_referenciada");
    }

    @Test
    void excluyeLosEsquemasDeSistemaPropiosDeMysql() {
        assertThat(strategy.getSchemaQuery())
                .contains("'information_schema', 'mysql', 'performance_schema', 'sys'")
                .doesNotContain("'pg_catalog'");
    }

    @Test
    void ordenaPorTablaYPosicionDeColumna() {
        assertThat(strategy.getSchemaQuery())
                .contains("ORDER BY c.table_schema, c.table_name, c.ordinal_position");
    }

    @Test
    void noExponeValoresDeColumnaComoLiteralesDeTexto() {
        assertThat(strategy.getSchemaQuery())
                .doesNotContain("'YES' = c.is_nullable")
                .contains("c.is_nullable = 'YES' AS es_nullable");
    }
}
