package com.saas.report.conexion.infrastructure.schema.strategy;

public class PostgresqlSchemaQueryProviderStrategy implements SchemaQueryProvider {
    
    @Override
    public String getSchemaQuery() {
        return """
            SELECT 
                c.table_name,
                c.column_name,
                c.data_type,
                c.is_identity,
                c.is_nullable = 'YES' AS es_nullable,
                
                -- Evaluación de PK
                EXISTS (
                    SELECT 1 
                    FROM information_schema.table_constraints tc_pk
                    JOIN information_schema.key_column_usage kcu_pk
                    ON tc_pk.constraint_name = kcu_pk.constraint_name
                    AND tc_pk.constraint_schema = kcu_pk.constraint_schema
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
                    kcu_ref.table_name AS tabla_referenciada,
                    kcu_ref.column_name AS columna_referenciada
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name
                AND tc.constraint_schema = kcu.constraint_schema
                JOIN information_schema.referential_constraints rc
                ON tc.constraint_name = rc.constraint_name
                AND tc.constraint_schema = rc.constraint_schema
                JOIN information_schema.key_column_usage kcu_ref
                ON rc.unique_constraint_name = kcu_ref.constraint_name
                AND rc.unique_constraint_schema = kcu_ref.constraint_schema
                AND kcu.ordinal_position = kcu_ref.ordinal_position
                WHERE tc.constraint_type = 'FOREIGN KEY'
            ) fk 
            ON c.table_schema = fk.table_schema
            AND c.table_name = fk.table_name
            AND c.column_name = fk.column_name

            WHERE c.table_schema = ?
            AND c.table_name NOT LIKE 'pg_%'
            AND c.table_name NOT IN ('schema_migrations', 'flyway_schema_history')
            ORDER BY c.table_name, c.ordinal_position
        """;
    }
    
}
