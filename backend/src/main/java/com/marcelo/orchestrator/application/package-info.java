/**
 * Camada de Aplicação (Application Layer).
 * 
 * <p>Esta camada contém os casos de uso (Use Cases) e orquestração da aplicação.
 * É responsável por coordenar as operações de negócio, mas não contém regras de negócio
 * (que ficam no domínio).</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Casos de uso (Use Cases)</li>
 *   <li>Commands e DTOs de aplicação</li>
 *   <li>Mappers entre camadas</li>
 *   <li>Orquestração de fluxos (Saga Pattern)</li>
 * </ul>
 * 
 * <h3>Dependências:</h3>
 * <p>Depende apenas do Domain. Não conhece Infrastructure ou Presentation.</p>
 * 
 * @author Marcelo
 */
package com.marcelo.orchestrator.application;

