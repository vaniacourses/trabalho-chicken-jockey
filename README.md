## 🧰 Artefatos
  - 📄 Link do Plano de Testes: [Google Docs](https://docs.google.com/document/d/13EUyOmss-Ym6IbgvCHP-StI8qOjlkMFlMTvpD7NzFrg/edit?usp=sharing)
  - 🧪 Testes Manuais realizados no TestLink: [TestLink - Vania UFF](http://vania.ic.uff.br/testlink/)
  - 📁 Pasta contendo todos os documentos dos testes: [Documentos](https://github.com/vaniacourses/trabalho-chicken-jockey/tree/main/documentos)
  - 🧪 Testes Automatizados (JUnit e Selenium): Os testes estão localizados em: `pdv/src/test/java/net/originmobi/pdv/`
  - 📁 Projeto de Teste: **CJ: Chicken Jockey**
  - 👥 Cada integrante elaborou seu próprio plano de testes para a execução dos testes manuais.
  - ➕ Se quiser verificar individualmente as contribuições de cada membro em cada etapa, basta navegar entre as branchs disponíveis no repositório.
## 🔐 **Login para acessar o sistema:**
  - Nome: **gerente**
  - Senha: **123**
## ⚙️ **Requisitos técnicos:**
  - Para que os scripts do banco funcionem, é preciso que se crie um banco de dados chamado **"pdv"**
  - Necessário ter **Spring**, **Docker** e **MySQL** instalados
  - Necessário ter o **Lombok** instalado em sua IDE
  - Versão do Java: **8**
  - Para conexão com o banco, caso queira rodar sem o docker, crie um arquivo **`application.properties`** a partir do **`application-example.properties`** que está na pasta **`resources`**
  - Para executar o docker: execute os comandos **`mvn clean package`**, **`docker-compose up -d`**, e **`docker-compose down`** para derrubar o container
  - para conectar ao banco do docker, a url é **`jdbc:mysql://localhost:3308/pdv`**, banco **`pdv`**, senha **`pdv`** e usuário **`pdv`** 
