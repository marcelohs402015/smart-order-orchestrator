# Variáveis de Ambiente - Smart Order Orchestrator

Scripts para carregar variáveis de ambiente do projeto.

## 📋 Scripts Disponíveis

### Linux/Mac (Bash)
```bash
source variaveis/load-env.sh
# ou
. variaveis/load-env.sh
```

### Windows (PowerShell) - **Recomendado**
```powershell
. .\variaveis\load-env.ps1
# ou
& .\variaveis\load-env.ps1
```

### Windows (CMD)
```cmd
call variaveis\load-env.bat
```

## 🚀 Uso Rápido

### 1. Carregar variáveis de ambiente

**Linux/Mac:**
```bash
source variaveis/load-env.sh
```

**Windows PowerShell:**
```powershell
. .\variaveis\load-env.ps1
```

**Windows CMD:**
```cmd
call variaveis\load-env.bat
```

### 2. Verificar se foram carregadas

**Linux/Mac:**
```bash
echo $DATABASE_URL
echo $OPENAI_API_KEY
```

**Windows PowerShell:**
```powershell
$env:DATABASE_URL
$env:OPENAI_API_KEY
```

**Windows CMD:**
```cmd
echo %DATABASE_URL%
echo %OPENAI_API_KEY%
```

### 3. Executar a aplicação

```bash
cd backend
mvn spring-boot:run
```

## 📝 Variáveis Configuradas

| Variável | Descrição | Valor Padrão |
|----------|-----------|--------------|
| `DATABASE_URL` | URL de conexão PostgreSQL | `jdbc:postgresql://localhost:5432/smartorder` |
| `DATABASE_USERNAME` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `postgres` |
| `ABACATEPAY_API_KEY` | Chave API AbacatePay | (configurada) |
| `ABACATEPAY_BASE_URL` | URL base AbacatePay | `https://api.abacatepay.com/v1` |
| `OPENAI_API_KEY` | Chave API OpenAI | (configurada) |
| `OPENAI_MODEL` | Modelo OpenAI | `gpt-3.5-turbo` |
| `OPENAI_BASE_URL` | URL base OpenAI | `https://api.openai.com/v1` |
| `MESSAGE_BROKER_TYPE` | Tipo de message broker | `IN_MEMORY` |

## ⚠️ Importante

### Windows PowerShell
- Use `.` (ponto) antes do caminho para que as variáveis fiquem disponíveis no shell atual
- Se executar apenas `.\variaveis\load-env.ps1`, as variáveis serão carregadas apenas no processo filho

### Linux/Mac
- Use `source` ou `.` para que as variáveis fiquem disponíveis no shell atual
- Se executar apenas `./variaveis/load-env.sh`, as variáveis serão carregadas apenas no processo filho

## 🔒 Segurança

⚠️ **ATENÇÃO:** Os scripts contêm chaves de API. 

- **Desenvolvimento:** As chaves estão nos scripts para facilitar o desenvolvimento local
- **Produção:** Use Google Cloud Secret Manager (veja `docs/SEGURANCA-GERENCIAMENTO-SECRETS.md`)

## 📚 Documentação Relacionada

- [README Principal](../../README.md)
- [Segurança e Secrets](../../docs/SEGURANCA-GERENCIAMENTO-SECRETS.md)
- [Configuração da Aplicação](../../backend/src/main/resources/application.yml)

