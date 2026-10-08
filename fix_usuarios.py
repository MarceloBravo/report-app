path = r'D:\Prpjects\Java\report\report\src\test\java\com\saas\report\usuarios\presentation\UsuarioControllerTest.java'
with open(path, encoding='utf-8') as f:
    s = f.read()
s = s.replace('when(listUsuariosUseCase.listar(isNull(), isNull(), isNull(), isNull()))',
              'when(listUsuariosUseCase.execute(isNull(), isNull(), any(org.springframework.data.domain.Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty())')
s = s.replace('when(listUsuariosUseCase.listar(eq(UsuarioRol.ADMIN), eq(true), isNull(), isNull()))',
              'when(listUsuariosUseCase.execute(eq(\"ADMIN\"), eq(true), any(org.springframework.data.domain.Pageable.class))).thenReturn(org.springframework.data.domain.Page.empty())')
s = s.replace('when(listUsuariosUseCase.listar(isNull(), isNull(), eq(2), eq(5)))',
              'when(listUsuariosUseCase.execute(isNull(), isNull(), org.springframework.data.domain.PageRequest.of(2,5))).thenReturn(org.springframework.data.domain.Page.empty())')
s = s.replace('verify(listUsuariosUseCase).listar(isNull(), isNull(), isNull(), isNull())',
              'verify(listUsuariosUseCase).execute(isNull(), isNull(), any(org.springframework.data.domain.Pageable.class))')
with open(path, 'w', encoding='utf-8') as f:
    f.write(s)
print('ok')