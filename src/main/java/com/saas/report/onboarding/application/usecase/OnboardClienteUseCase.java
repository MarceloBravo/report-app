package com.saas.report.onboarding.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.onboarding.application.dto.OnboardingRequest;
import com.saas.report.onboarding.application.dto.OnboardingResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionHistorialPrecio;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionHistorialPrecioUUID;
import com.saas.report.suscripcion.domain.valueobject.SuscripcionUUID;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.conexion.domain.connection.CredentialCipher;
import com.saas.report.shared.domain.valueobject.MotorBaseDatos;
import com.saas.report.conexion.domain.connection.TenantDbConnection;
import com.saas.report.conexion.domain.connection.TenantDbConnectionRepository;
import com.saas.report.conexion.domain.connection.TipoConexion;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import com.saas.report.conexion.domain.valueobject.TenantDbConnectionUUID;
import com.saas.report.shared.domain.valueobject.TenantUUID;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.shared.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import com.saas.report.shared.domain.valueobject.UsuarioUUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class OnboardClienteUseCase {

    private static final String DEFAULT_SCHEMA = "public";

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PlanRepository planRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final TenantDbConnectionRepository dbConnectionRepository;
    private final CredentialCipher credentialCipher;
    private final PasswordEncoder passwordEncoder;
    private final String freePlanName;
    private final EmailVerificationTokenService emailTokenService;
    private final String frontendBaseUrl;

    public OnboardClienteUseCase(TenantRepository tenantRepository,
                                 UsuarioRepository usuarioRepository,
                                 PlanRepository planRepository,
                                 SuscripcionRepository suscripcionRepository,
                                 TenantDbConnectionRepository dbConnectionRepository,
                                 CredentialCipher credentialCipher,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${app.onboarding.free-plan-name}") String freePlanName,
                                 EmailVerificationTokenService emailTokenService,
                                 @Value("${app.onboarding.frontend-base-url}") String frontendBaseUrl) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.dbConnectionRepository = dbConnectionRepository;
        this.credentialCipher = credentialCipher;
        this.passwordEncoder = passwordEncoder;
        this.freePlanName = freePlanName;
        this.emailTokenService = emailTokenService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional
    public OnboardingResponse registrar(OnboardingRequest request) {
        String subdominio = request.cliente().subdominioSlug();
        if (tenantRepository.existsBySubdominioSlug(subdominio)) {
            throw new TenantSlugAlreadyExistsException(
                    "Ya existe un tenant con el subdominio '" + subdominio + "'");
        }
        String email = request.usuario().email();
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya está registrado");
        }

        Tenant tenant = tenantRepository.save(new Tenant(TenantUUID.generate(),
                request.cliente().nombreEmpresa(), subdominio, TenantEstado.ACTIVE));

        Usuario usuario = usuarioRepository.save(new Usuario(UsuarioUUID.generate(),
                tenant.getTenantId(),
                request.usuario().nombre(),
                email,
                passwordEncoder.encode(request.usuario().password()),
                UsuarioRol.ADMIN,
                true,
                false));

        Suscripcion suscripcion = crearSuscripcionInicial(tenant.getTenantId());

        TenantDbConnection conexion = crearConexion(tenant.getTenantId(), request.conexion());

        String emailVerificacionUrl = crearEmailVerificacionUrl(usuario.getUsuarioId(), email);

        return new OnboardingResponse(
                tenant.getTenantId().getValue(),
                usuario.getUsuarioId().getValue(),
                suscripcion.getSuscripcionId().getValue(),
                conexion.getDbConnectionId().getValue(),
                emailVerificacionUrl);
    }

    private String crearEmailVerificacionUrl(UsuarioUUID usuarioId, String email) {
        String token = emailTokenService.generar(usuarioId, email);
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        return base + "/verificar-email?token=" + token;
    }

    private Suscripcion crearSuscripcionInicial(TenantUUID tenantId) {
        Plan planGratuito = buscarPlanGratuito();
        LocalDate hoy = LocalDate.now();

        Suscripcion suscripcion = new Suscripcion(SuscripcionUUID.generate(),
                tenantId, planGratuito.getPlanId(), hoy, SuscripcionEstado.ACTIVE, hoy.plusMonths(1));

        PlanPrecio tramoInicial = planGratuito.getPrecioParaMes(1);
        suscripcion.addHistorialPrecio(new SuscripcionHistorialPrecio(
                SuscripcionHistorialPrecioUUID.generate(),
                suscripcion,
                hoy,
                null,
                tramoInicial.getPrecio(),
                tramoInicial.getPlanPrecioId()));

        return suscripcionRepository.save(suscripcion);
    }

    private Plan buscarPlanGratuito() {
        return planRepository.findAll().stream()
                .filter(Plan::isActivo)
                .filter(plan -> plan.getNombrePlan().equalsIgnoreCase(freePlanName))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan gratuito '" + freePlanName + "' no configurado"));
    }

    private TenantDbConnection crearConexion(TenantUUID tenantId, OnboardingRequest.ConexionRequest request) {
        String passwordCifrada = credentialCipher.cifrar(request.dbPassword());
        String schema = request.schemaName() == null || request.schemaName().isBlank()
                ? schemaPorDefecto(request.motorBd(), request.dbName()) : request.schemaName();

        return dbConnectionRepository.save(new TenantDbConnection(
                TenantDbConnectionUUID.generate(),
                tenantId,
                request.host(),
                request.puerto(),
                request.dbName(),
                request.dbUser(),
                passwordCifrada,
                TipoConexion.EXTERNAL_DB,
                schema,
                request.motorBd()));
    }

    private String schemaPorDefecto(MotorBaseDatos motorBd, String dbName) {
        if (MotorBaseDatos.MYSQL.equals(motorBd)) {
            return dbName;
        }
        return DEFAULT_SCHEMA;
    }
}