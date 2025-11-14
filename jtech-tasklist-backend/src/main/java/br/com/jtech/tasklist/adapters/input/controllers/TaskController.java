/*
*  @(#)TaskController.java
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
package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.input.protocols.TaskResponse;
import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.TaskInputGateway;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static br.com.jtech.tasklist.application.core.domains.Task.of;

/**
* Controller REST que expõe os endpoints da API de Tarefas.
*
* Endpoints disponíveis:
* - POST /tasks - Criar nova tarefa
* - GET /tasks - Listar todas as tarefas
* - GET /tasks/{id} - Buscar tarefa por ID
* - PUT /tasks/{id} - Atualizar tarefa
* - DELETE /tasks/{id} - Deletar tarefa
*
* @author JTech
*/
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskInputGateway taskInputGateway;

    /**
     * Cria uma nova tarefa.
     *
     * @param request Dados da tarefa a ser criada
     * @return Tarefa criada com status 201 (Created)
     */
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        Task task = of(request);
        Task createdTask = taskInputGateway.create(task);
        TaskResponse response = toResponse(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todas as tarefas.
     *
     * @return Lista de tarefas com status 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> findAll() {
        List<Task> tasks = taskInputGateway.findAll();
        List<TaskResponse> responses = tasks.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca uma tarefa por ID.
     *
     * @param id ID da tarefa
     * @return Tarefa encontrada com status 200 (OK) ou 404 (Not Found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> findById(@PathVariable String id) {
        return taskInputGateway.findById(id)
            .map(task -> ResponseEntity.ok(toResponse(task)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Atualiza uma tarefa existente.
     *
     * @param id ID da tarefa a ser atualizada
     * @param request Dados atualizados da tarefa
     * @return Tarefa atualizada com status 200 (OK) ou 404 (Not Found)
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable String id,
            @Valid @RequestBody TaskRequest request) {
        Task task = of(request);
        return taskInputGateway.update(id, task)
            .map(updatedTask -> ResponseEntity.ok(toResponse(updatedTask)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deleta uma tarefa por ID.
     *
     * @param id ID da tarefa a ser deletada
     * @return Status 204 (No Content) se deletada ou 404 (Not Found) se não encontrada
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = taskInputGateway.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Converte um domínio Task para um DTO TaskResponse.
     */
    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
            .id(task.getId())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .build();
    }
}

