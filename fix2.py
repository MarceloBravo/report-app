path = r'D:\Prpjects\Java\report\report\src\test\java\com\saas\report\usuarios\presentation\UsuarioControllerTest.java'
with open(path, encoding='utf-8') as f:
    s = f.read()
# remove old listar stubs/verify
s = s.replace('when(listUsuariosUseCase.listar(null, null, null, null))\n', '')
s = s.replace('when(listUsuariosUseCase.listar(eq(UsuarioRol.ADMIN), eq(true), null, null))\n', '')
s = s.replace('when(listUsuariosUseCase.listar(null, null, null, null))\n', '')
s = s.replace('verify(listUsuariosUseCase).listar(null, null, null, null())', '') # no
s = s.replace('verify(listUsuariosUseCase).listar(null, null, null, null)', '')
with open(path, 'w', encoding='utf-8') as f:
    f.write(s)
print('ok')