package com.saas.report.onboarding.application.usecase;

import com.saas.report.identity.domain.user.UsuarioRol;
import com.saas.report.onboarding.application.dto.OnboardingRequest;
import com.saas.report.onboarding.application.dto.OnboardingResponse;
import com.saas.report.plan.domain.Plan;
import com.saas.report.plan.domain.PlanPrecio;
import com.saas.report.plan.domain.PlanRepository;
import com.saas.report.plan.domain.valueobject.PlanLimiteConsultasMensuales;
import com.saas.report.plan.domain.valueobject.PlanMaxUsuarios;
import com.saas.report.plan.domain.valueobject.PlanPrecioUUID;
import com.saas.report.plan.domain.valueobject.PlanUUID;
import com.saas.report.shared.exception.ResourceNotFoundException;
import com.saas.report.suscripcion.domain.Suscripcion;
import com.saas.report.suscripcion.domain.SuscripcionEstado;
import com.saas.report.suscripcion.domain.SuscripcionRepository;
import com.saas.report.tenant.domain.Tenant;
import com.saas.report.tenant.domain.TenantEstado;
import com.saas.report.tenant.domain.TenantRepository;
import com.saas.report.tenant.domain.connection.CredentialCipher;
import com.saas.report.tenant.domain.connection.TenantDbConnection;
import com.saas.report.tenant.domain.connection.TenantDbConnectionRepository;
import com.saas.report.tenant.domain.connection.TipoConexion;
import com.saas.report.tenant.domain.exception.TenantSlugAlreadyExistsException;
import com.saas.report.usuarios.domain.exception.EmailAlreadyExistsException;
import com.saas.report.usuarios.domain.emailverificacion.EmailVerificationTokenService;
import com.saas.report.usuarios.domain.usuario.Usuario;
import com.saas.report.usuarios.domain.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardClienteUseCaseTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    @Mock
    private TenantDbConnectionRepository dbConnectionRepository;

    @Mock
    private EmailVerificationTokenService emailTokenService;

    private final CredentialCipher credentialCipher = new CredentialCipher() {
        @Override
        public String cifrar(String valorPlano) {
            return "CIFRADO:" + valorPlano;
        }

        @Override
        public String descifrar(String valorCifrado) {
            return valorCifrado.replaceFirst("^CIFRADO:", "");
        }
    };

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private OnboardClienteUseCase onboardClienteUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(emailTokenService.generar(any(), anyString())).thenReturn("token-verificacion");
        onboardClienteUseCase = new OnboardClienteUseCase(
                tenantRepository, usuarioRepository, planRepository, suscripcionRepository,
                dbConnectionRepository, credentialCipher, passwordEncoder, "Starter",
                emailTokenService, "http://localhost:5173");
    }

    private Plan planGratuito() {
        PlanUUID planId = PlanUUID.generate();
        PlanPrecio tramo = new PlanPrecio(PlanPrecioUUID.generate(), planId, 1, null, BigDecimal.ZERO);
        return new Plan(planId, "Starter", "Plan gratuito", new PlanMaxUsuarios(3),
                new PlanLimiteConsultasMensuales(100), true, List.of(tramo));
    }

    private OnboardingRequest request() {
        return new OnboardingRequest(
                new OnboardingRequest.ClienteRequest("Acme", "acme"),
                new OnboardingRequest.UsuarioNuevoRequest("Juan", "juan@acme.com", "password123"),
                new OnboardingRequest.ConexionRequest("db.acme.com", 5432, "ventas",
                        "report_ro", "secreto", "public"));
    }

    @Test
    void registraClienteCompletoEnUnaTransaccion() {
        Plan plan = planGratuito();
        when(tenantRepository.existsBySubdominioSlug("acme")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@acme.com")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dbConnectionRepository.save(any(TenantDbConnection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planRepository.findAll()).thenReturn(List.of(plan));

        OnboardingResponse response = onboardClienteUseCase.registrar(request());

        assertThat(response.tenantId()).isNotNull();
        assertThat(response.usuarioId()).isNotNull();
        assertThat(response.suscripcionId()).isNotNull();
        assertThat(response.dbConnectionId()).isNotNull();
        assertThat(response.emailVerificacionUrl())
                .isEqualTo("http://localhost:5173/verificar-email?token=token-verificacion");

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuario = usuarioCaptor.getValue();
        assertThat(usuario.getRol()).isEqualTo(UsuarioRol.ADMIN);
        assertThat(usuario.getEmailVerificado()).isFalse();
        assertThat(usuario.getTenantId().getValue())
                .isEqualTo(response.tenantId());
        assertThat(passwordEncoder.matches("password123", usuario.getPassword())).isTrue();

        ArgumentCaptor<Tenant> tenantCaptor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(tenantCaptor.capture());
        assertThat(tenantCaptor.getValue().getEstado()).isEqualTo(TenantEstado.ACTIVE);

        ArgumentCaptor<Suscripcion> suscripcionCaptor = ArgumentCaptor.forClass(Suscripcion.class);
        verify(suscripcionRepository).save(suscripcionCaptor.capture());
        Suscripcion suscripcion = suscripcionCaptor.getValue();
        assertThat(suscripcion.getPlanId()).isEqualTo(plan.getPlanId());
        assertThat(suscripcion.getEstado()).isEqualTo(SuscripcionEstado.ACTIVE);
        assertThat(suscripcion.getHistorialPrecios()).hasSize(1);

        ArgumentCaptor<TenantDbConnection> conexionCaptor = ArgumentCaptor.forClass(TenantDbConnection.class);
        verify(dbConnectionRepository).save(conexionCaptor.capture());
        TenantDbConnection conexion = conexionCaptor.getValue();
        assertThat(conexion.getTipoConexion()).isEqualTo(TipoConexion.EXTERNAL_DB);
        assertThat(conexion.getSchemaName()).isEqualTo("public");
        assertThat(conexion.getDbPasswordEncrypted()).isEqualTo("CIFRADO:secreto");
        assertThat(conexion.getDbUser()).isEqualTo("report_ro");
    }

    @Test
    void usaSchemaPorDefectoSiNoSeEnvía() {
        Plan plan = planGratuito();
        when(tenantRepository.existsBySubdominioSlug("acme")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@acme.com")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(dbConnectionRepository.save(any(TenantDbConnection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planRepository.findAll()).thenReturn(List.of(plan));

        OnboardingRequest req = new OnboardingRequest(
                request().cliente(), request().usuario(),
                new OnboardingRequest.ConexionRequest("db.acme.com", 5432, "ventas",
                        "report_ro", "secreto", null));

        onboardClienteUseCase.registrar(req);

        ArgumentCaptor<TenantDbConnection> captor = ArgumentCaptor.forClass(TenantDbConnection.class);
        verify(dbConnectionRepository).save(captor.capture());
        assertThat(captor.getValue().getSchemaName()).isEqualTo("public");
    }

    @Test
    void rechazaSiElSubdominioYaExiste() {
        when(tenantRepository.existsBySubdominioSlug("acme")).thenReturn(true);

        assertThatThrownBy(() -> onboardClienteUseCase.registrar(request()))
                .isInstanceOf(TenantSlugAlreadyExistsException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void rechazaSiElEmailYaExiste() {
        when(tenantRepository.existsBySubdominioSlug("acme")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> onboardClienteUseCase.registrar(request()))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(tenantRepository, never()).save(any());
    }

    @Test
    void rechazaSiNoExistePlanGratuitoConfigurado() {
        when(tenantRepository.existsBySubdominioSlug("acme")).thenReturn(false);
        when(usuarioRepository.existsByEmail("juan@acme.com")).thenReturn(false);
        when(tenantRepository.save(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> onboardClienteUseCase.registrar(request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Starter");

        verify(suscripcionRepository, never()).save(any());
        verify(dbConnectionRepository, never()).save(any());
    }
}