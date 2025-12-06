package com.marcelo.orchestrator.domain.port;

import java.util.UUID;

/**
 * Porta (Port) para análise de risco utilizando Inteligência Artificial.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) para integração
 * com serviços de IA (OpenAI, MCP - Model Context Protocol) que analisam o risco de pedidos
 * baseado em metadados, histórico do cliente, padrões de comportamento, etc.</p>
 * 
 * <h3>Arquitetura Hexagonal e IA:</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na Infrastructure com cliente HTTP para OpenAI/MCP</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes de API, prompts, modelos, etc.</li>
 * </ul>
 * 
 * <h3>Por que Análise de Risco com IA?</h3>
 * <ul>
 *   <li><strong>Modernização:</strong> Utiliza IA generativa para análise semântica avançada</li>
 *   <li><strong>Precisão:</strong> Análise contextual baseada em múltiplos fatores</li>
 *   <li><strong>Escalabilidade:</strong> Processa grandes volumes sem regras hardcoded</li>
 *   <li><strong>Adaptabilidade:</strong> IA aprende com padrões e se adapta a novos cenários</li>
 * </ul>
 * 
 * <h3>Benefícios da Abordagem Hexagonal:</h3>
 * <ul>
 *   <li><strong>Testabilidade:</strong> Fácil mockar para testes (não precisa chamar OpenAI real)</li>
 *   <li><strong>Flexibilidade:</strong> Trocar OpenAI por outro provedor sem alterar domínio</li>
 *   <li><strong>Resiliência:</strong> Pode implementar fallback quando IA está indisponível</li>
 *   <li><strong>Cache:</strong> Pode cachear resultados na implementação sem afetar domínio</li>
 * </ul>
 * 
 * <h3>Dados Analisados (exemplos):</h3>
 * <ul>
 *   <li>Valor do pedido e histórico do cliente</li>
 *   <li>Frequência de pedidos</li>
 *   <li>Padrões de comportamento</li>
 *   <li>Dados de geolocalização</li>
 *   <li>Método de pagamento</li>
 * </ul>
 * 
 * @author Marcelo
 */
public interface RiskAnalysisPort {
    
    /**
     * Analisa o risco de um pedido utilizando IA.
     * 
     * <p>A implementação deve enviar metadados do pedido para o serviço de IA
     * e receber uma classificação de risco (LOW, HIGH). Em caso de falha na integração,
     * pode retornar PENDING ou um valor padrão (fail-safe).</p>
     * 
     * @param request Dados do pedido e contexto para análise
     * @return Resultado da análise com nível de risco
     */
    RiskAnalysisResult analyzeRisk(RiskAnalysisRequest request);
}

