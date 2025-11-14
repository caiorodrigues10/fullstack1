/*
*  @(#)TaskUseCaseConfig.java
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
package br.com.jtech.tasklist.config.usecases;

import br.com.jtech.tasklist.application.core.usecases.TaskUseCase;
import br.com.jtech.tasklist.application.ports.input.TaskInputGateway;
import br.com.jtech.tasklist.application.ports.output.TaskOutputGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
* Classe de configuração que cria os beans dos casos de uso.
* Spring Boot usa esta classe para fazer a injeção de dependências.
* O TaskAdapter já é um @Component, então o Spring cria o bean automaticamente.
*
* @author JTech
*/
@Configuration
public class TaskUseCaseConfig {

    @Bean
    public TaskInputGateway taskInputGateway(TaskOutputGateway taskOutputGateway) {
        return new TaskUseCase(taskOutputGateway);
    }
}

