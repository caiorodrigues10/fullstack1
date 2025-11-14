/*
*  @(#)TaskAdapter.java
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
package br.com.jtech.tasklist.adapters.output;

import br.com.jtech.tasklist.adapters.output.repositories.TaskRepository;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;
import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.output.TaskOutputGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static br.com.jtech.tasklist.application.core.domains.Task.of;

/**
* Adapter que implementa a interface de saída (Output Gateway).
* Responsável por converter entre o domínio e a entidade JPA.
*
* @author JTech
*/
@Component
@RequiredArgsConstructor
public class TaskAdapter implements TaskOutputGateway {

    private final TaskRepository taskRepository;

    @Override
    public Task save(Task task) {
        TaskEntity entity = task.toEntity();
        TaskEntity savedEntity = taskRepository.save(entity);
        return of(savedEntity);
    }

    @Override
    public List<Task> findAll() {
        List<TaskEntity> entities = taskRepository.findAll();
        return Task.of(entities);
    }

    @Override
    public Optional<Task> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Optional<TaskEntity> entity = taskRepository.findById(uuid);
            return entity.map(Task::of);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Task update(Task task) {
        TaskEntity entity = task.toEntity();
        TaskEntity updatedEntity = taskRepository.save(entity);
        return of(updatedEntity);
    }

    @Override
    public void deleteById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            taskRepository.deleteById(uuid);
        } catch (IllegalArgumentException e) {
            // ID inválido, não faz nada
        }
    }

    @Override
    public Optional<Task> findByTitleIgnoreCase(String title) {
        Optional<TaskEntity> entity = taskRepository.findByTitleIgnoreCase(title);
        return entity.map(Task::of);
    }
}

