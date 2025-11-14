/*
*  @(#)TaskUseCase.java
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
package br.com.jtech.tasklist.application.core.usecases;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.input.TaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.TaskOutputGateway;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
* Classe que implementa os casos de uso (regras de negócio) da aplicação.
* Esta é a camada de aplicação da arquitetura limpa.
*
* @author JTech
*/
public class TaskUseCase implements TaskInputGateway {

    private final TaskOutputGateway taskOutputGateway;

    public TaskUseCase(TaskOutputGateway taskOutputGateway) {
        this.taskOutputGateway = taskOutputGateway;
    }

    @Override
    public Task create(Task task) {
        // Garante que o status padrão seja "pendente" se não informado
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("pendente");
        }

        // Valida se já existe uma tarefa com o mesmo título (case-insensitive)
        if (task.getTitle() != null && !task.getTitle().trim().isEmpty()) {
            String trimmedTitle = task.getTitle().trim();
            Optional<Task> existingTask = taskOutputGateway.findByTitleIgnoreCase(trimmedTitle);
            if (existingTask.isPresent()) {
                throw new IllegalArgumentException(
                    String.format("Já existe uma tarefa com o título '%s' (ignorando maiúsculas/minúsculas)", trimmedTitle)
                );
            }
        }

        return taskOutputGateway.save(task);
    }

    @Override
    public List<Task> findAll() {
        return taskOutputGateway.findAll();
    }

    @Override
    public Optional<Task> findById(String id) {
        return taskOutputGateway.findById(id);
    }

    @Override
    public Optional<Task> update(String id, Task task) {
        Optional<Task> existingTask = taskOutputGateway.findById(id);

        if (existingTask.isEmpty()) {
            return Optional.empty();
        }

        Task taskToUpdate = existingTask.get();

        // Atualiza apenas os campos fornecidos
        if (task.getTitle() != null && !task.getTitle().isEmpty()) {
            // Valida se já existe outra tarefa com o mesmo título (case-insensitive)
            // Ignora a própria tarefa que está sendo atualizada
            Optional<Task> taskWithSameTitle = taskOutputGateway.findByTitleIgnoreCase(task.getTitle());
            if (taskWithSameTitle.isPresent() && !taskWithSameTitle.get().getId().equals(id)) {
                throw new IllegalArgumentException(
                    String.format("Já existe outra tarefa com o título '%s' (ignorando maiúsculas/minúsculas)", task.getTitle())
                );
            }
            taskToUpdate.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            taskToUpdate.setDescription(task.getDescription());
        }
        if (task.getStatus() != null && !task.getStatus().isEmpty()) {
            taskToUpdate.setStatus(task.getStatus());
        }

        Task updatedTask = taskOutputGateway.update(taskToUpdate);
        return Optional.of(updatedTask);
    }

    @Override
    public boolean delete(String id) {
        Optional<Task> task = taskOutputGateway.findById(id);

        if (task.isEmpty()) {
            return false;
        }

        taskOutputGateway.deleteById(id);
        return true;
    }
}

