path = r'D:\Prpjects\Java\report\report\src\test\java\com\saas\report\usuarios\presentation\UsuarioControllerTest.java'
with open(path, encoding='utf-8') as f:
    s = f.read()
s = s.replace('any(AuthPrincipal.class)', 'any(com.saas.report.usuarios.application.dto.CreadorPor.class)')
s = s.replace('AuthPrincipal("root@saas.com", "u0", null, "PLATFORM_ADMIN")', 'new com.saas.report.usuarios.application.dto.CreadorPor("root@saas.com", "PLATFORM_ADMIN")')
with open(path, 'w', encoding='utf-8') as f:
    f.write(s)
print('ok')