/*
*  @(#)TaskInputGateway.java
*
*  Copyright (c) J-Tech Solucoes em Informatica.
*  All Rights Reserved.
*
*  This software is the confidential and proprietary information of J-Tech.
*  ("Confidential Information"). You shall not disclose such Confidential
*  Information and shall use it only in accordance with the terms of the
*  license agreement you entered into with J-Tech.
*
*/
package br.com.jtech.tasklist.application.ports.input;

import br.com.jtech.tasklist.application.core.domains.Task;

import java.util.List;
import java.util.Optional;

/**
* Interface que define os casos de uso (use cases) da aplicação.
* Esta é a camada de entrada da arquitetura limpa.
*
* @author JTech
*/
public interface TaskInputGateway {

    /**
     * Cria uma nova tarefa.
     *
     * @param task Tarefa a ser criada
     * @return Tarefa criada com ID gerado
     */
    Task create(Task task);

    /**
     * Busca todas as tarefas.
     *
     * @return Lista de todas as tarefas
     */
    List<Task> findAll();

    /**
     * Busca uma tarefa por ID.
     *
     * @param id ID da tarefa
     * @return Tarefa encontrada ou Optional vazio se não encontrada
     */
    Optional<Task> findById(String id);

    /**
     * Atualiza uma tarefa existente.
     *
     * @param id ID da tarefa a ser atualizada
     * @param task Dados atualizados da tarefa
     * @return Tarefa atualizada ou Optional vazio se não encontrada
     */
    Optional<Task> update(String id, Task task);

    /**
     * Deleta uma tarefa por ID.
     *
     * @param id ID da tarefa a ser deletada
     * @return true se a tarefa foi deletada, false se não foi encontrada
     */
    boolean delete(String id);
}

