/*
*  @(#)TaskRepository.java
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
package br.com.jtech.tasklist.adapters.output.repositories;

import br.com.jtech.tasklist.adapters.output.repositories.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
* Interface Repository para operações de persistência de Tarefas.
* Spring Data JPA fornece automaticamente a implementação.
*
* @author JTech
*/
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    /**
     * Busca uma tarefa por título ignorando maiúsculas/minúsculas.
     * Usa query nativa com LOWER() e LIMIT 1 para garantir busca case-insensitive
     * e retornar apenas o primeiro resultado encontrado.
     *
     * @param title Título da tarefa (case-insensitive)
     * @return Optional contendo a tarefa encontrada, se existir
     */
    @Query(value = "SELECT * FROM tasks WHERE LOWER(TRIM(title)) = LOWER(TRIM(:title)) LIMIT 1", nativeQuery = true)
    Optional<TaskEntity> findByTitleIgnoreCase(@Param("title") String title);
}

