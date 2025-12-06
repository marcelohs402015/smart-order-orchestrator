package com.marcelo.orchestrator.domain.port;

import com.marcelo.orchestrator.domain.model.Order;
import com.marcelo.orchestrator.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta (Port) para persistência de pedidos.
 * 
 * <p>Esta interface define o <strong>contrato de saída</strong> (Outbound Port) na Arquitetura Hexagonal.
 * O domínio define o que precisa (contrato), e a camada Infrastructure implementa como fazer (adaptador).</p>
 * 
 * <h3>Padrão Ports and Adapters (Hexagonal Architecture):</h3>
 * <ul>
 *   <li><strong>Port:</strong> Esta interface - define o contrato que o domínio precisa</li>
 *   <li><strong>Adapter:</strong> Implementação na camada Infrastructure (ex: JpaRepository)</li>
 *   <li><strong>Inversão de Dependência:</strong> Domínio não depende de JPA, JPA depende do domínio</li>
 * </ul>
 * 
 * <h3>Benefícios desta Abordagem:</h3>
 * <ul>
 *   <li><strong>Testabilidade:</strong> Fácil criar mocks para testes unitários</li>
 *   <li><strong>Flexibilidade:</strong> Trocar JPA por MongoDB, Cassandra, etc. sem alterar domínio</li>
 *   <li><strong>Isolamento:</strong> Domínio não conhece detalhes de implementação (JPA, SQL, etc.)</li>
 *   <li><strong>Clean Architecture:</strong> Dependências apontam para dentro (domínio no centro)</li>
 * </ul>
 * 
 * <h3>Responsabilidades:</h3>
 * <p>Esta porta é responsável apenas por <strong>persistência</strong>. Regras de negócio
 * (validações, cálculos, transições de estado) ficam na entidade {@link Order}.</p>
 * 
 * @author Marcelo
 */
public interface OrderRepositoryPort {
    
    /**
     * Salva um pedido (cria novo ou atualiza existente).
     * 
     * @param order Pedido a ser salvo
     * @return Pedido salvo (pode conter dados adicionais gerados pela persistência)
     */
    Order save(Order order);
    
    /**
     * Busca um pedido pelo ID.
     * 
     * @param id Identificador único do pedido
     * @return Optional contendo o pedido se encontrado, vazio caso contrário
     */
    Optional<Order> findById(UUID id);
    
    /**
     * Busca um pedido pelo número do pedido.
     * 
     * @param orderNumber Número único do pedido (ex: "ORD-1234567890")
     * @return Optional contendo o pedido se encontrado, vazio caso contrário
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Busca todos os pedidos.
     * 
     * @return Lista de todos os pedidos (pode ser vazia)
     */
    List<Order> findAll();
    
    /**
     * Busca pedidos por status.
     * 
     * @param status Status desejado
     * @return Lista de pedidos com o status especificado (pode ser vazia)
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Remove um pedido pelo ID.
     * 
     * @param id Identificador único do pedido a ser removido
     */
    void deleteById(UUID id);
    
    /**
     * Verifica se um pedido existe pelo ID.
     * 
     * @param id Identificador único do pedido
     * @return {@code true} se existe, {@code false} caso contrário
     */
    boolean existsById(UUID id);
}

