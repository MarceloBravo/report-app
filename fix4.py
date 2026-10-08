path = r'D:\Prpjects\Java\report\report\src\test\java\com\saas\report\usuarios\presentation\UsuarioControllerTest.java'
with open(path, encoding='utf-8') as f:
    s = f.read()
# fix thenReturn syntax issues
s = s.replace('.thenReturn(org.springframework.data.domain.Page.empty()))', '.thenReturn(org.springframework.data.domain.Page.empty())')
# ensure proper chaining
with open(path,'w',encoding='utf-8') as f:
    f.write(s)
print('ok')