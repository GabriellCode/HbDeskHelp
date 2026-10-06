# HbDeskHelp

HbDeskHelp é um sistema completo e autônomo de Help Desk (Suporte de TI), desenvolvido em **Java** com **Spring Boot**. O grande diferencial do projeto é a integração nativa com Inteligência Artificial (Ollama / Gemini) atuando como um atendente autônomo chamado **Cornelius**, capaz de avaliar, categorizar e definir a prioridade dos chamados de forma instantânea sem intervenção humana.

## ✨ Funcionalidades Principais

* **Classificação por IA (Cornelius)**: Quando um chamado é aberto, a IA faz a leitura do título e descrição e automaticamente o cataloga com uma prioridade (BAIXA, MÉDIA, ALTA, CRÍTICA).
* **Fluxo de Status e Cores**:
  * 🔴 **Pendente (Não atribuído)**: Criado pelo funcionário e aguardando suporte.
  * 🟠 **Em andamento**: O desenvolvedor assumiu a responsabilidade daquele chamado.
  * 🟡 **Pendente de Confirmação**: O dev marcou o problema como resolvido, aguardando validação de quem abriu.
  * 🟢 **Concluído**: O funcionário validou e atestou a solução final.
* **Múltiplos Níveis de Acesso (RBAC)**:
  * **Funcionário**: Apenas abre chamados e confirma a conclusão.
  * **Dev**: Visualiza, assume e resolve chamados (mas não pode criá-los ou excluí-los).
  * **Chefe de TI**: Possui acesso total, podendo deletar chamados de forma permanente, além de possuir um Dashboard exclusivo para criação e remoção de contas de usuários de qualquer nível.
* **Segurança e Testes**: Proteção avançada em todas as rotas da API via Token Interceptor e testes automatizados que cobrem quebras de permissão.

## 🚀 Tecnologias Utilizadas

* **Linguagem Principal**: Java 17
* **Backend Framework**: Spring Boot (Web, Data JPA)
* **Banco de Dados**: H2 Database (Embutido, não requer instalação complexa)
* **Inteligência Artificial**: Integração com servidor local Ollama e API do Google Gemini como Fallback (Auto-change em caso de falhas).
* **Autenticação**: Custom Token Bearer
* **Frontend Nativo Web**: HTML5, CSS3, JavaScript Vanilla
* **Build e Gerenciamento**: Maven (Script de inicialização baixa e configura o Maven em modo Portátil automaticamente para sistemas Windows).

## 🛠️ Como rodar o projeto

O projeto foi criado para rodar sem dores de cabeça de infraestrutura no Windows.

1. Baixe ou clone este repositório para o seu computador.
2. Dê um duplo-clique no arquivo **`INICIAR_SISTEMA.bat`**.
   * *O que ele fará?* Ele iniciará um script PowerShell que irá baixar o Apache Maven localmente na pasta (se não existir) e fará o build total do backend, subindo o servidor embutido na porta `8081`.
3. Abra seu navegador de preferência e acesse: `http://localhost:8081`

### Usuários Padrão para Testes
Ao iniciar o sistema pela primeira vez, o banco de dados interno cria as contas automaticamente para facilitar testes:
* **Usuário Chefe:** `chefe` | **Senha:** `123`
* **Usuário Dev:** `dev` | **Senha:** `123`
* **Usuário Funcionário:** `funcionario` | **Senha:** `123`

## 🛡️ Licença

Este projeto está sob a licença [MIT](LICENSE).

---
*Desenvolvido com dedicação por [GabriellCode](https://github.com/GabriellCode).*
