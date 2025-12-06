# Scripts de Infraestrutura

Scripts para configuração e gerenciamento da infraestrutura do projeto.

## 📋 Conteúdo

- `docker-compose.yml`: Configuração completa do PostgreSQL e PgAdmin
- `init-scripts/`: Scripts SQL executados na inicialização do banco

## 🚀 Como Usar

### Iniciar Infraestrutura

```bash
# Na raiz do projeto backend
cd scripts

# Iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f

# Parar serviços
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados!)
docker-compose down -v
```

### Verificar Status

```bash
# Verificar containers rodando
docker-compose ps

# Verificar saúde do PostgreSQL
docker-compose exec postgres pg_isready -U postgres
```

### Acessar PgAdmin (Interface Web)

1. Inicie os serviços: `docker-compose up -d`
2. Acesse: http://localhost:5050
3. Login:
   - Email: `admin@smartorder.local`
   - Password: `admin`
4. Adicione servidor:
   - Host: `postgres` (nome do serviço no Docker)
   - Port: `5432`
   - Database: `smartorder`
   - Username: `postgres`
   - Password: `postgres`

### Conectar via Cliente SQL

```bash
# Via psql (se instalado localmente)
psql -h localhost -p 5432 -U postgres -d smartorder

# Via Docker
docker-compose exec postgres psql -U postgres -d smartorder
```

## 🔧 Configuração da Aplicação

Configure as variáveis de ambiente no `application.properties` ou `.env`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smartorder
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Ou use variáveis de ambiente:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/smartorder
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres
```

## 📝 Scripts de Inicialização

Scripts na pasta `init-scripts/` são executados automaticamente na primeira inicialização do PostgreSQL.

- `01-init-database.sql`: Script inicial (extensões, schemas, etc.)

Para adicionar mais scripts, crie arquivos numerados:
- `02-custom-script.sql`
- `03-another-script.sql`

## 🗑️ Limpeza

```bash
# Parar e remover tudo (incluindo volumes)
docker-compose down -v

# Remover imagens
docker-compose down --rmi all
```

## ⚠️ Notas Importantes

1. **Dados Persistem**: Volumes Docker mantêm dados entre reinicializações
2. **Portas**: Certifique-se que portas 5432 e 5050 não estão em uso
3. **Produção**: Estas configurações são para desenvolvimento. Ajuste para produção!

