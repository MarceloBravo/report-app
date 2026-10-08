package com.saas.report.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleDependencyRulesTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.saas.report");

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
    void businessModulesMustNotDependOnOtherBusinessModules() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage("..auth..", "..consulta..", "..suscripcion..", "..usuarios..", "..onboarding..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..auth..", "..consulta..", "..suscripcion..", "..usuarios..", "..onboarding..")
                .andShould().dependOnClassesThat()
                .resideOutsideOfPackages("..shared..", "..identity..", "..plan..", "..tenant..", "..conexion..");
        rule.check(CLASSES);
    }
}