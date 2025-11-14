/*
*  @(#)TaskOutputGateway.java
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
package br.com.jtech.tasklist.application.ports.output;

import br.com.jtech.tasklist.application.core.domains.Task;

import java.util.List;
import java.util.Optional;

/**
* Interface que define as operações de persistência (repositório).
* Esta é a camada de saída da arquitetura limpa.
*
* @author JTech
*/
public interface TaskOutputGateway {

    /**
     * Salva uma nova tarefa.
     *
     * @param task Tarefa a ser salva
     * @return Tarefa salva com ID gerado
     */
    Task save(Task task);

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
     * @param task Tarefa atualizada
     * @return Tarefa atualizada
     */
    Task update(Task task);

    /**
     * Deleta uma tarefa por ID.
     *
     * @param id ID da tarefa a ser deletada
     */
    void deleteById(String id);

    /**
     * Busca uma tarefa por título ignorando maiúsculas/minúsculas.
     *
     * @param title Título da tarefa (case-insensitive)
     * @return Tarefa encontrada ou Optional vazio se não encontrada
     */
    Optional<Task> findByTitleIgnoreCase(String title);
}

