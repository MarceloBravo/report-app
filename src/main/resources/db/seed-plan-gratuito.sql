-- Semilla idempotente: plan gratuito inicial para el onboarding self-serve.
-- Solo aplica en el perfil dev (ver application-dev.yml -> spring.sql.init).

INSERT INTO plans (plan_id, nombre_plan, descripcion, max_usuarios, limite_consultas_mensuales, activo)
SELECT gen_random_uuid(), 'Starter', 'Plan gratuito de inicio', 3, 100, true
WHERE NOT EXISTS (
    SELECT 1 FROM plans WHERE nombre_plan = 'Starter'
);

INSERT INTO plan_precios (plan_precio_id, plan_id, mes_desde, mes_hasta, precio, orden)
SELECT gen_random_uuid(), p.plan_id, 1, NULL, 0.00, 0
FROM plans p
WHERE p.nombre_plan = 'Starter'
  AND NOT EXISTS (
      SELECT 1 FROM plan_precios pp
      WHERE pp.plan_id = p.plan_id AND pp.mes_desde = 1
  );