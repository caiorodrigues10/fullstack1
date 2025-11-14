/*
*  @(#)TaskRequest.java
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
package br.com.jtech.tasklist.adapters.input.protocols;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* DTO (Data Transfer Object) para receber dados de criação/atualização de tarefas.
* Contém as validações de entrada da API.
*
* @author JTech
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    /**
     * Título da tarefa - obrigatório e não pode estar vazio.
     */
    @NotBlank(message = "O título da tarefa é obrigatório")
    @Size(max = 255, message = "O título não pode ter mais de 255 caracteres")
    private String title;

    /**
     * Descrição da tarefa - opcional.
     */
    private String description;

    /**
     * Status da tarefa - opcional, padrão é "pendente".
     * Valores aceitos: "pendente", "concluída", "em_andamento", etc.
     */
    private String status;
}

