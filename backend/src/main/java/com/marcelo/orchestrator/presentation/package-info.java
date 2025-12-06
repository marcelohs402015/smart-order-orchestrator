/**
 * Camada de Apresentação (Presentation Layer).
 * 
 * <p>Esta camada contém os controllers REST e DTOs de entrada/saída.
 * É responsável por receber requisições HTTP e transformá-las em chamadas
 * para a camada de aplicação.</p>
 * 
 * <h3>Responsabilidades:</h3>
 * <ul>
 *   <li>Controllers REST</li>
 *   <li>DTOs de request/response</li>
 *   <li>Validação de entrada (Bean Validation)</li>
 *   <li>Exception handlers globais</li>
 *   <li>Documentação da API (Swagger/OpenAPI)</li>
 * </ul>
 * 
 * <h3>Dependências:</h3>
 * <p>Depende apenas da Application. Não conhece Domain ou Infrastructure diretamente.</p>
 * 
 * @author Marcelo
 */
package com.marcelo.orchestrator.presentation;

