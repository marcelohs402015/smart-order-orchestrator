#!/bin/bash

# ============================================================================
# Script de Setup de Secrets no Google Cloud Secret Manager
# ============================================================================
# Este script cria os secrets necessários no GCP Secret Manager e configura
# as permissões adequadas para o Service Account do Cloud Run.
#
# Uso:
#   ./setup-secrets-gcp.sh
#
# Pré-requisitos:
#   - gcloud CLI instalado e autenticado
#   - Permissões de administrador no projeto GCP
#   - Variáveis de ambiente configuradas (ou editar no script)
# ============================================================================

set -e  # Parar em caso de erro

# ============================================================================
# Configuração
# ============================================================================

# Definir variáveis (ou usar variáveis de ambiente)
PROJECT_ID="${GCP_PROJECT_ID:-seu-projeto-gcp}"
REGION="${GCP_REGION:-us-central1}"

# Obter número do projeto
PROJECT_NUMBER=$(gcloud projects describe "$PROJECT_ID" --format="value(projectNumber)")

# Service Account padrão do Cloud Run
SERVICE_ACCOUNT="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ============================================================================
# Funções Auxiliares
# ============================================================================

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "ℹ️  $1"
}

# ============================================================================
# Validação
# ============================================================================

echo "============================================================================"
echo "Setup de Secrets no Google Cloud Secret Manager"
echo "============================================================================"
echo ""
print_info "Projeto GCP: $PROJECT_ID"
print_info "Região: $REGION"
print_info "Service Account: $SERVICE_ACCOUNT"
echo ""

# Verificar se gcloud está instalado
if ! command -v gcloud &> /dev/null; then
    print_error "gcloud CLI não está instalado. Instale em: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Verificar se está autenticado
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q .; then
    print_error "Você não está autenticado no gcloud. Execute: gcloud auth login"
    exit 1
fi

# Verificar se o projeto existe
if ! gcloud projects describe "$PROJECT_ID" &> /dev/null; then
    print_error "Projeto '$PROJECT_ID' não encontrado ou sem acesso."
    exit 1
fi

# Configurar projeto atual
gcloud config set project "$PROJECT_ID"
print_success "Projeto configurado: $PROJECT_ID"

# ============================================================================
# Habilitar APIs Necessárias
# ============================================================================

echo ""
echo "============================================================================"
echo "Habilitando APIs necessárias..."
echo "============================================================================"

gcloud services enable secretmanager.googleapis.com --project="$PROJECT_ID"
print_success "Secret Manager API habilitada"

# ============================================================================
# Criar Secrets
# ============================================================================

echo ""
echo "============================================================================"
echo "Criando Secrets..."
echo "============================================================================"

# Função para criar secret se não existir
create_secret_if_not_exists() {
    local SECRET_NAME=$1
    local SECRET_VALUE=$2
    
    if gcloud secrets describe "$SECRET_NAME" --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Secret '$SECRET_NAME' já existe. Pulando criação."
    else
        echo -n "$SECRET_VALUE" | gcloud secrets create "$SECRET_NAME" \
            --project="$PROJECT_ID" \
            --data-file=- \
            --replication-policy="automatic"
        print_success "Secret '$SECRET_NAME' criado"
    fi
}

# Solicitar valores dos secrets (ou usar variáveis de ambiente)
echo ""
print_info "Por segurança, os valores dos secrets serão solicitados interativamente."
echo ""

# Database Password
if [ -z "$DATABASE_PASSWORD" ]; then
    read -sp "Digite a senha do banco de dados (PostgreSQL): " DATABASE_PASSWORD
    echo ""
fi
create_secret_if_not_exists "database-password" "$DATABASE_PASSWORD"

# AbacatePay API Key
if [ -z "$ABACATEPAY_API_KEY" ]; then
    read -sp "Digite a chave de API do AbacatePay: " ABACATEPAY_API_KEY
    echo ""
fi
create_secret_if_not_exists "abacatepay-api-key" "$ABACATEPAY_API_KEY"

# OpenAI API Key
if [ -z "$OPENAI_API_KEY" ]; then
    read -sp "Digite a chave de API do OpenAI: " OPENAI_API_KEY
    echo ""
fi
create_secret_if_not_exists "openai-api-key" "$OPENAI_API_KEY"

# ============================================================================
# Configurar Permissões IAM
# ============================================================================

echo ""
echo "============================================================================"
echo "Configurando Permissões IAM..."
echo "============================================================================"

# Função para adicionar permissão se não existir
add_iam_policy_if_not_exists() {
    local SECRET_NAME=$1
    local MEMBER=$2
    local ROLE=$3
    
    # Verificar se a permissão já existe
    if gcloud secrets get-iam-policy "$SECRET_NAME" --project="$PROJECT_ID" \
        --format="value(bindings[].members)" | grep -q "$MEMBER"; then
        print_warning "Permissão já existe para '$MEMBER' no secret '$SECRET_NAME'"
    else
        gcloud secrets add-iam-policy-binding "$SECRET_NAME" \
            --project="$PROJECT_ID" \
            --member="serviceAccount:$MEMBER" \
            --role="$ROLE"
        print_success "Permissão adicionada para '$MEMBER' no secret '$SECRET_NAME'"
    fi
}

# Adicionar permissões para o Service Account do Cloud Run
add_iam_policy_if_not_exists "database-password" "$SERVICE_ACCOUNT" "roles/secretmanager.secretAccessor"
add_iam_policy_if_not_exists "abacatepay-api-key" "$SERVICE_ACCOUNT" "roles/secretmanager.secretAccessor"
add_iam_policy_if_not_exists "openai-api-key" "$SERVICE_ACCOUNT" "roles/secretmanager.secretAccessor"

# ============================================================================
# Verificação Final
# ============================================================================

echo ""
echo "============================================================================"
echo "Verificação Final"
echo "============================================================================"

# Listar todos os secrets criados
echo ""
print_info "Secrets criados:"
gcloud secrets list --project="$PROJECT_ID" --filter="name:database-password OR name:abacatepay-api-key OR name:openai-api-key" --format="table(name,createTime)"

echo ""
print_success "Setup concluído com sucesso!"
echo ""
print_info "Próximos passos:"
echo "  1. Configure o Cloud Run para usar os secrets:"
echo "     gcloud run services update smart-order-backend \\"
echo "       --set-secrets=\"DATABASE_PASSWORD=database-password:latest,ABACATEPAY_API_KEY=abacatepay-api-key:latest,OPENAI_API_KEY=openai-api-key:latest\""
echo ""
echo "  2. Ou use no deploy:"
echo "     gcloud run deploy smart-order-backend \\"
echo "       --set-secrets=\"DATABASE_PASSWORD=database-password:latest,ABACATEPAY_API_KEY=abacatepay-api-key:latest,OPENAI_API_KEY=openai-api-key:latest\""
echo ""
print_warning "IMPORTANTE: Nunca commite os valores dos secrets no Git!"
echo ""

