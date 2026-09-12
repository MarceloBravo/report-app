package com.saas.report.onboarding.infrastructure;

import com.saas.report.onboarding.application.event.TenantRegistradoEvent;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.connection.TenantDbConnectionRepository;
import com.saas.report.tenant.domain.schema.EsquemaCache;
import com.saas.report.tenant.domain.schema.EsquemaTenant;
import com.saas.report.tenant.domain.schema.SchemaExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TenantSchemaSyncListener {

    private static final Logger log = LoggerFactory.getLogger(TenantSchemaSyncListener.class);

    private final TenantDbConnectionRepository dbConnectionRepository;
    private final SchemaExtractor schemaExtractor;
    private final EsquemaCache esquemaCache;

    public TenantSchemaSyncListener(TenantDbConnectionRepository dbConnectionRepository,
                                    SchemaExtractor schemaExtractor,
                                    EsquemaCache esquemaCache) {
        this.dbConnectionRepository = dbConnectionRepository;
        this.schemaExtractor = schemaExtractor;
        this.esquemaCache = esquemaCache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void extraerEsquema(TenantRegistradoEvent evento) {
        try {
            TenantDbConnection conexion = dbConnectionRepository.findByTenantId(evento.tenantId())
                    .orElseThrow(() -> new IllegalStateException(
                            "No existe conexión registrada para el tenant " + evento.tenantId().getValue()));
            EsquemaTenant esquema = schemaExtractor.extraer(conexion);
            esquemaCache.guardar(evento.tenantId(), esquema);
            log.debug("Esquema extraído y cacheado para el tenant {}", evento.tenantId().getValue());
        } catch (Exception e) {
            log.warn("No se pudo extraer el esquema de la BD para el tenant {}: {}",
                    evento.tenantId().getValue(), e.getMessage());
        }
    }
}