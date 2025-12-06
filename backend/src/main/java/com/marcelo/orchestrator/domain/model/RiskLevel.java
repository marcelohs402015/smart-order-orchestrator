package com.marcelo.orchestrator.domain.model;

/**
 * Enum que representa o nível de risco de um pedido após análise por IA.
 * 
 * <p>Utilizado para classificação semântica de risco através de integração
 * com serviços de IA (OpenAI/MCP). A análise considera metadados do pedido,
 * histórico do cliente, padrões de comportamento, etc.</p>
 * 
 * <h3>Uso no Domínio:</h3>
 * <ul>
 *   <li><strong>LOW:</strong> Pedido considerado seguro, processamento normal.</li>
 *   <li><strong>HIGH:</strong> Pedido com indicadores de risco, requer revisão manual.</li>
 *   <li><strong>PENDING:</strong> Análise ainda não foi realizada ou está em processamento.</li>
 * </ul>
 * 
 * <h3>Benefícios da Classificação de Risco:</h3>
 * <ul>
 *   <li>Prevenção de fraudes</li>
 *   <li>Otimização de processos (pedidos LOW processados automaticamente)</li>
 *   <li>Tomada de decisão baseada em dados e IA</li>
 * </ul>
 * 
 * @author Marcelo
 */
public enum RiskLevel {
    
    /**
     * Risco baixo - pedido considerado seguro.
     * Processamento pode seguir fluxo automatizado.
     */
    LOW,
    
    /**
     * Risco alto - pedido requer atenção e possível revisão manual.
     * Pode indicar possível fraude ou comportamento suspeito.
     */
    HIGH,
    
    /**
     * Análise de risco pendente ou em processamento.
     * Estado inicial antes da análise ser concluída.
     */
    PENDING
}

