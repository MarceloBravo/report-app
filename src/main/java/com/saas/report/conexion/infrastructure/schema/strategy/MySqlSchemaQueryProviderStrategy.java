package com.saas.report.conexion.infrastructure.schema.strategy;

public class MySqlSchemaQueryProviderStrategy implements SchemaQueryProvider {
    
    @Override
    public String getSchemaQuery() {
        return """
            SELECT 
                c.table_name,
                c.column_name,
                c.data_type,
                c.extra = 'auto_increment' AS es_identity,
                c.is_nullable = 'YES' AS es_nullable,
                
                -- Evaluación de PK
                EXISTS (
                    SELECT 1 
                    FROM information_schema.table_constraints tc_pk
                    JOIN information_schema.key_column_usage kcu_pk
                    ON tc_pk.constraint_name = kcu_pk.constraint_name
                    AND tc_pk.table_schema = kcu_pk.constraint_schema
                    WHERE tc_pk.constraint_type = 'PRIMARY KEY'
                    AND kcu_pk.table_schema = c.table_schema
                    AND kcu_pk.table_name = c.table_name
                    AND kcu_pk.column_name = c.column_name
                ) AS es_primary_key,

                -- Evaluación y datos de FK
                (fk.constraint_name IS NOT NULL) AS es_foreign_key,
                fk.tabla_referenciada,
                fk.columna_referenciada

            FROM information_schema.columns c

            -- Unimos únicamente las restricciones de tipo FOREIGN KEY para evitar duplicados por otros constraints
            LEFT JOIN (
                SELECT 
                    kcu.table_schema,
                    kcu.table_name,
                    kcu.column_name,
                    tc.constraint_name,
                    kcu.referenced_table_name AS tabla_referenciada,
                    kcu.referenced_column_name AS columna_referenciada
                FROM information_schema.key_column_usage kcu
                JOIN information_schema.table_constraints tc
                ON kcu.constraint_name = tc.constraint_name
                AND kcu.constraint_schema = tc.constraint_schema
                WHERE tc.constraint_type = 'FOREIGN KEY'
            ) fk ON c.table_schema = fk.table_schema 
                 AND c.table_name = fk.table_name 
                 AND c.column_name = fk.column_name

            WHERE c.table_schema = ?
              AND c.table_schema NOT IN ('information_schema', 'mysql', 'performance_schema', 'sys')
            ORDER BY c.table_schema, c.table_name, c.ordinal_position;
        """;
    }
    
}
