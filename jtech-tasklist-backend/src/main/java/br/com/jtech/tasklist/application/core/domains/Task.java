/*
*  @(#)Task.java
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
package br.com.jtech.tasklist.application.core.domains;

import br.com.jtech.tasklist.adapters.input.protocols.TaskRequest;
import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
* Classe de domínio que representa uma Tarefa.
* Esta é a entidade de negócio, independente de frameworks e persistência.
*
* @author JTech
*/
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private String id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converte uma lista de entidades JPA para uma lista de domínios.
     */
    public static List<Task> of(List<TaskEntity> entities) {
        return entities.stream().map(Task::of).toList();
    }

    /**
     * Converte uma entidade JPA para o domínio.
     */
    public static Task of(TaskEntity entity) {
        return Task.builder()
            .id(entity.getId().toString())
            .title(entity.getTitle())
            .description(entity.getDescription())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }

    /**
     * Converte o domínio para uma entidade JPA.
     */
    public TaskEntity toEntity() {
        TaskEntity.TaskEntityBuilder builder = TaskEntity.builder()
            .title(this.title)
            .description(this.description)
            .status(this.status);

        if (this.id != null && !this.id.isEmpty()) {
            builder.id(UUID.fromString(this.id));
        }

        return builder.build();
    }

    /**
     * Converte um Request DTO para o domínio.
     */
    public static Task of(TaskRequest request) {
        return Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus() != null ? request.getStatus() : "pendente")
            .build();
    }
}

