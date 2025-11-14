/*
*  @(#)TaskUseCaseTest.java
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
import br.com.jtech.tasklist.application.ports.output.TaskOutputGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
* Testes unitários para a classe TaskUseCase.
* Testa todas as operações CRUD e regras de negócio.
*
* @author JTech
*/
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Caso de Uso de Tarefas")
class TaskUseCaseTest {

    @Mock
    private TaskOutputGateway taskOutputGateway;

    @InjectMocks
    private TaskUseCase taskUseCase;

    private Task task;
    private String taskId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID().toString();
        task = Task.builder()
            .id(taskId)
            .title("Tarefa de Teste")
            .description("Descrição da tarefa")
            .status("pendente")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Deve criar uma tarefa com sucesso")
    void shouldCreateTaskSuccessfully() {
        // Arrange
        Task newTask = Task.builder()
            .title("Nova Tarefa")
            .description("Descrição")
            .build();

        Task savedTask = Task.builder()
            .id(taskId)
            .title("Nova Tarefa")
            .description("Descrição")
            .status("pendente")
            .build();

        when(taskOutputGateway.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskUseCase.create(newTask);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(taskId);
        assertThat(result.getStatus()).isEqualTo("pendente");
        verify(taskOutputGateway, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Deve definir status padrão como 'pendente' quando não informado")
    void shouldSetDefaultStatusWhenNotProvided() {
        // Arrange
        Task newTask = Task.builder()
            .title("Nova Tarefa")
            .description("Descrição")
            .status(null)
            .build();

        Task savedTask = Task.builder()
            .id(taskId)
            .title("Nova Tarefa")
            .description("Descrição")
            .status("pendente")
            .build();

        when(taskOutputGateway.save(any(Task.class))).thenReturn(savedTask);

        // Act
        Task result = taskUseCase.create(newTask);

        // Assert
        assertThat(result.getStatus()).isEqualTo("pendente");
    }

    @Test
    @DisplayName("Deve buscar todas as tarefas")
    void shouldFindAllTasks() {
        // Arrange
        Task task1 = Task.builder().id(UUID.randomUUID().toString()).title("Tarefa 1").build();
        Task task2 = Task.builder().id(UUID.randomUUID().toString()).title("Tarefa 2").build();
        List<Task> tasks = Arrays.asList(task1, task2);

        when(taskOutputGateway.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = taskUseCase.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(taskOutputGateway, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar tarefa por ID com sucesso")
    void shouldFindTaskByIdSuccessfully() {
        // Arrange
        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        Optional<Task> result = taskUseCase.findById(taskId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(taskId);
        assertThat(result.get().getTitle()).isEqualTo("Tarefa de Teste");
        verify(taskOutputGateway, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando tarefa não encontrada")
    void shouldReturnEmptyWhenTaskNotFound() {
        // Arrange
        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskUseCase.findById(taskId);

        // Assert
        assertThat(result).isEmpty();
        verify(taskOutputGateway, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("Deve atualizar tarefa com sucesso")
    void shouldUpdateTaskSuccessfully() {
        // Arrange
        Task updatedData = Task.builder()
            .title("Tarefa Atualizada")
            .description("Nova Descrição")
            .status("concluída")
            .build();

        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.of(task));
        when(taskOutputGateway.update(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            return t;
        });

        // Act
        Optional<Task> result = taskUseCase.update(taskId, updatedData);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Tarefa Atualizada");
        assertThat(result.get().getDescription()).isEqualTo("Nova Descrição");
        assertThat(result.get().getStatus()).isEqualTo("concluída");
        verify(taskOutputGateway, times(1)).findById(taskId);
        verify(taskOutputGateway, times(1)).update(any(Task.class));
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao tentar atualizar tarefa inexistente")
    void shouldReturnEmptyWhenUpdatingNonExistentTask() {
        // Arrange
        Task updatedData = Task.builder()
            .title("Tarefa Atualizada")
            .build();

        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskUseCase.update(taskId, updatedData);

        // Assert
        assertThat(result).isEmpty();
        verify(taskOutputGateway, times(1)).findById(taskId);
        verify(taskOutputGateway, never()).update(any(Task.class));
    }

    @Test
    @DisplayName("Deve deletar tarefa com sucesso")
    void shouldDeleteTaskSuccessfully() {
        // Arrange
        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.of(task));
        doNothing().when(taskOutputGateway).deleteById(taskId);

        // Act
        boolean result = taskUseCase.delete(taskId);

        // Assert
        assertThat(result).isTrue();
        verify(taskOutputGateway, times(1)).findById(taskId);
        verify(taskOutputGateway, times(1)).deleteById(taskId);
    }

    @Test
    @DisplayName("Deve retornar false ao tentar deletar tarefa inexistente")
    void shouldReturnFalseWhenDeletingNonExistentTask() {
        // Arrange
        when(taskOutputGateway.findById(taskId)).thenReturn(Optional.empty());

        // Act
        boolean result = taskUseCase.delete(taskId);

        // Assert
        assertThat(result).isFalse();
        verify(taskOutputGateway, times(1)).findById(taskId);
        verify(taskOutputGateway, never()).deleteById(anyString());
    }
}

