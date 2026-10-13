package com.saas.report.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackages;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleDependencyRulesTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.saas.report");

    private static final String[] BUSINESS_MODULES = {
            "auth", "consulta", "suscripcion", "usuarios", "onboarding"
    };

    private static final String[] ALLOWED_MODULES = {
            "shared", "identity", "plan", "tenant", "conexion"
    };

    private static final Map<String, List<String>> ALLOWED_BUSINESS_MODULES = Map.of(
            "auth", List.of("usuarios", "onboarding"),
            "consulta", List.of("suscripcion"),
            "onboarding", List.of("usuarios", "suscripcion")
    );

    @Test
    void sharedMustNotDependOnBusinessModules() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..shared..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "..auth..",
                        "..consulta..",
                        "..suscripcion..",
                        "..usuarios..",
                        "..onboarding.."
                );
        rule.check(CLASSES);
    }

    @Test
    void businessModulesMustOnlyDependOnAllowedModulesOrThemselves() {
        List<String> violaciones = new ArrayList<>();
        for (String modulo : BUSINESS_MODULES) {
            DescribedPredicate<JavaClass> dependenciaProhibida =
                    resideInAPackage("com.saas.report..")
                            .and(resideOutsideOfPackages(paquetesPermitidosPara(modulo)));

            ArchRule rule = noClasses()
                    .that().resideInAPackage(".." + modulo + "..")
                    .should().dependOnClassesThat(dependenciaProhibida)
                    .because("el módulo '" + modulo
                            + "' solo puede depender de shared, identity, plan, tenant, conexion, "
                            + "de sí mismo o de los módulos de negocio que tenga permitidos");

            EvaluationResult resultado = rule.evaluate(CLASSES);
            if (resultado.hasViolation()) {
                violaciones.add(modulo + " -> " + resultado.getFailureReport().toString());
            }
        }
        if (!violaciones.isEmpty()) {
            throw new AssertionError("Dependencias cruzadas entre módulos de negocio:\n"
                    + String.join("\n\n", violaciones));
        }
    }

    private static String[] paquetesPermitidosPara(String modulo) {
        List<String> permitidos = new ArrayList<>();
        permitidos.add(".." + modulo + "..");
        for (String permitido : ALLOWED_MODULES) {
            permitidos.add(".." + permitido + "..");
        }
        for (String permitido : ALLOWED_BUSINESS_MODULES.getOrDefault(modulo, List.of())) {
            permitidos.add(".." + permitido + "..");
        }
        return permitidos.toArray(new String[0]);
    }
}
