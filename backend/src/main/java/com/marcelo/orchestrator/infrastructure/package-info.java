/**
 * Camada de Infraestrutura (Infrastructure Layer).
 * 
 * <p>Esta camada contém os adaptadores (Adapters) que implementam as portas (Ports)
 * definidas no domínio. É responsável por integrações externas e detalhes técnicos.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Persistência (JPA, Repositórios)</li>
 *   <li>Integrações externas (APIs, Gateways)</li>
 *   <li>Configurações técnicas (JPA, HTTP clients)</li>
 *   <li>Mappers entre Domain e Infrastructure</li>
 * </ul>
 * 
 * <h3>Dependências:</h3>
 * <p>Depende do Domain e Application. Implementa as portas definidas no domínio.</p>
 * 
 * @author Marcelo
 */
package com.marcelo.orchestrator.infrastructure;

