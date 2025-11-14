/*
*  @(#)TaskControllerTest.java
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
* Testes unitários para o TaskController.
* Testa todos os endpoints da API REST.
*
* @author JTech
*/
@WebMvcTest(TaskController.class)
@DisplayName("Testes do Controller de Tarefas")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskInputGateway taskInputGateway;

    @Autowired
    private ObjectMapper objectMapper;

    private Task task;
    private TaskRequest taskRequest;
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

        taskRequest = TaskRequest.builder()
            .title("Tarefa de Teste")
            .description("Descrição da tarefa")
            .status("pendente")
            .build();
    }

    @Test
    @DisplayName("POST /tasks - Deve criar tarefa com sucesso")
    void shouldCreateTaskSuccessfully() throws Exception {
        // Arrange
        when(taskInputGateway.create(any(Task.class))).thenReturn(task);

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.title").value("Tarefa de Teste"))
            .andExpect(jsonPath("$.description").value("Descrição da tarefa"))
            .andExpect(jsonPath("$.status").value("pendente"));

        verify(taskInputGateway, times(1)).create(any(Task.class));
    }

    @Test
    @DisplayName("POST /tasks - Deve retornar 400 quando título está vazio")
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        // Arrange
        TaskRequest invalidRequest = TaskRequest.builder()
            .title("")
            .description("Descrição")
            .build();

        // Act & Assert
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(taskInputGateway, never()).create(any(Task.class));
    }

    @Test
    @DisplayName("GET /tasks - Deve listar todas as tarefas")
    void shouldFindAllTasks() throws Exception {
        // Arrange
        Task task2 = Task.builder()
            .id(UUID.randomUUID().toString())
            .title("Tarefa 2")
            .description("Descrição 2")
            .status("concluída")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        List<Task> tasks = Arrays.asList(task, task2);
        when(taskInputGateway.findAll()).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(taskId))
            .andExpect(jsonPath("$[1].title").value("Tarefa 2"));

        verify(taskInputGateway, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /tasks/{id} - Deve buscar tarefa por ID com sucesso")
    void shouldFindTaskByIdSuccessfully() throws Exception {
        // Arrange
        when(taskInputGateway.findById(taskId)).thenReturn(Optional.of(task));

        // Act & Assert
        mockMvc.perform(get("/tasks/{id}", taskId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskId))
            .andExpect(jsonPath("$.title").value("Tarefa de Teste"))
            .andExpect(jsonPath("$.description").value("Descrição da tarefa"))
            .andExpect(jsonPath("$.status").value("pendente"));

        verify(taskInputGateway, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("GET /tasks/{id} - Deve retornar 404 quando tarefa não encontrada")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        // Arrange
        when(taskInputGateway.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/tasks/{id}", taskId))
            .andExpect(status().isNotFound());

        verify(taskInputGateway, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("PUT /tasks/{id} - Deve atualizar tarefa com sucesso")
    void shouldUpdateTaskSuccessfully() throws Exception {
        // Arrange
        Task updatedTask = Task.builder()
            .id(taskId)
            .title("Tarefa Atualizada")
            .description("Nova Descrição")
            .status("concluída")
            .createdAt(task.getCreatedAt())
            .updatedAt(LocalDateTime.now())
            .build();

        when(taskInputGateway.update(eq(taskId), any(Task.class))).thenReturn(Optional.of(updatedTask));

        TaskRequest updateRequest = TaskRequest.builder()
            .title("Tarefa Atualizada")
            .description("Nova Descrição")
            .status("concluída")
            .build();

        // Act & Assert
        mockMvc.perform(put("/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Tarefa Atualizada"))
            .andExpect(jsonPath("$.description").value("Nova Descrição"))
            .andExpect(jsonPath("$.status").value("concluída"));

        verify(taskInputGateway, times(1)).update(eq(taskId), any(Task.class));
    }

    @Test
    @DisplayName("PUT /tasks/{id} - Deve retornar 404 quando tarefa não encontrada")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        // Arrange
        when(taskInputGateway.update(eq(taskId), any(Task.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
            .andExpect(status().isNotFound());

        verify(taskInputGateway, times(1)).update(eq(taskId), any(Task.class));
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - Deve deletar tarefa com sucesso")
    void shouldDeleteTaskSuccessfully() throws Exception {
        // Arrange
        when(taskInputGateway.delete(taskId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
            .andExpect(status().isNoContent());

        verify(taskInputGateway, times(1)).delete(taskId);
    }

    @Test
    @DisplayName("DELETE /tasks/{id} - Deve retornar 404 quando tarefa não encontrada")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        // Arrange
        when(taskInputGateway.delete(taskId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/tasks/{id}", taskId))
            .andExpect(status().isNotFound());

        verify(taskInputGateway, times(1)).delete(taskId);
    }
}

