/*
 * @(#)GlobalExceptionHandler.java
 *
 * Copyright (c) J-Tech Solucoes em Informatica.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of J-Tech.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with J-Tech.
 */
package br.com.jtech.tasklist.config.infra.utils;

import br.com.jtech.tasklist.config.infra.exceptions.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler global de exceções para interceptar e tratar todas as exceções da API.
 * Retorna mensagens de erro em português para melhor experiência do usuário.
 *
 * @author JTech
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação do Spring (Bean Validation).
     *
     * @param ex Exceção de validação
     * @return Resposta com erro 400 (Bad Request) e lista de erros de validação
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage("Erro de validação nos dados fornecidos");
        error.setTimestamp(LocalDateTime.now());
        error.setSubErrors(subErrors(ex));
        error.setDebugMessage("Um ou mais campos não atendem aos requisitos de validação");
        return buildResponseEntity(error);
    }

    /**
     * Trata exceções de argumento inválido (ex: duplicatas, regras de negócio).
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 400 (Bad Request)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage(ex.getMessage() != null && !ex.getMessage().isEmpty()
            ? ex.getMessage()
            : "Argumento inválido fornecido");
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getLocalizedMessage());
        return buildResponseEntity(error);
    }

    /**
     * Trata erros quando o corpo da requisição não pode ser lido (JSON inválido, etc.).
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 400 (Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage("Erro ao processar o corpo da requisição. Verifique se o JSON está formatado corretamente.");
        error.setTimestamp(LocalDateTime.now());

        String causeMessage = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        if (causeMessage != null && causeMessage.contains("JSON")) {
            error.setDebugMessage("Formato JSON inválido: " + causeMessage);
        } else {
            error.setDebugMessage("Erro ao deserializar o corpo da requisição");
        }

        return buildResponseEntity(error);
    }

    /**
     * Trata erros quando um parâmetro obrigatório está faltando na requisição.
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 400 (Bad Request)
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        error.setMessage(String.format("Parâmetro obrigatório '%s' não foi fornecido", ex.getParameterName()));
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    /**
     * Trata erros quando o tipo de um parâmetro não corresponde ao esperado.
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        String requiredType = ex.getRequiredType() != null
            ? ex.getRequiredType().getSimpleName()
            : "tipo desconhecido";
        error.setMessage(String.format(
            "O valor '%s' fornecido para o parâmetro '%s' não é do tipo esperado (%s)",
            ex.getValue(), ex.getName(), requiredType));
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    /**
     * Trata erros quando o método HTTP não é suportado para o endpoint.
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 405 (Method Not Allowed)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        ApiError error = new ApiError(HttpStatus.METHOD_NOT_ALLOWED);
        String supportedMethods = ex.getSupportedMethods() != null
            ? String.join(", ", ex.getSupportedMethods())
            : "nenhum";
        error.setMessage(String.format(
            "Método HTTP '%s' não é suportado para este endpoint. Métodos suportados: %s",
            ex.getMethod(), supportedMethods));
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getMessage());
        return buildResponseEntity(error);
    }

    /**
     * Trata erros quando o endpoint não é encontrado (404).
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 404 (Not Found)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND);
        error.setMessage(String.format("Endpoint '%s %s' não encontrado", ex.getHttpMethod(), ex.getRequestURL()));
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage("Verifique se a URL e o método HTTP estão corretos");
        return buildResponseEntity(error);
    }

    /**
     * Trata erros de integridade de dados do banco (ex: violação de constraints).
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 400 (Bad Request) ou 409 (Conflict)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST);
        String message = ex.getMessage();

        // Tenta identificar o tipo de violação e retornar mensagem mais amigável
        if (message != null) {
            if (message.contains("duplicate key") || message.contains("UNIQUE")) {
                error.setMessage("Já existe um registro com os dados fornecidos. Verifique se não está tentando criar um registro duplicado.");
                error.setStatus(HttpStatus.CONFLICT);
            } else if (message.contains("foreign key") || message.contains("FOREIGN KEY")) {
                error.setMessage("Não é possível realizar esta operação pois existem registros relacionados que dependem deste dado.");
            } else if (message.contains("not-null") || message.contains("NOT NULL")) {
                error.setMessage("Um ou mais campos obrigatórios não foram fornecidos.");
            } else {
                error.setMessage("Erro de integridade de dados. Verifique se os dados fornecidos estão corretos.");
            }
        } else {
            error.setMessage("Erro de integridade de dados no banco de dados.");
        }

        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage(ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage());
        return buildResponseEntity(error);
    }

    /**
     * Trata erros de acesso a dados não transitórios (ex: problemas de conexão com banco).
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 503 (Service Unavailable)
     */
    @ExceptionHandler(NonTransientDataAccessException.class)
    public ResponseEntity<ApiError> handleNonTransientDataAccessException(NonTransientDataAccessException ex) {
        ApiError error = new ApiError(HttpStatus.SERVICE_UNAVAILABLE);
        error.setMessage("Serviço temporariamente indisponível. Tente novamente mais tarde.");
        error.setTimestamp(LocalDateTime.now());
        error.setDebugMessage("Erro de acesso ao banco de dados: " +
            (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
        return buildResponseEntity(error);
    }

    /**
     * Trata exceções genéricas não tratadas (deve ser o último handler).
     * Captura qualquer exceção que não foi tratada pelos handlers específicos.
     *
     * @param ex Exceção lançada
     * @return Resposta com erro 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR);
        error.setMessage("Ocorreu um erro interno no servidor. Nossa equipe foi notificada e está trabalhando para resolver o problema.");
        error.setTimestamp(LocalDateTime.now());

        // Em produção, não expor detalhes técnicos
        String debugMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        error.setDebugMessage("Erro interno: " + debugMessage);

        // Log da exceção completa para debug (será visível nos logs do servidor)
        ex.printStackTrace();

        return buildResponseEntity(error);
    }

    /**
     * Constrói a resposta HTTP com o erro formatado.
     *
     * @param apiError Objeto de erro formatado
     * @return ResponseEntity com o erro
     */
    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Converte erros de validação do Spring em lista de ApiSubError.
     *
     * @param ex Exceção de validação
     * @return Lista de erros de validação formatados
     */
    private List<ApiSubError> subErrors(MethodArgumentNotValidException ex) {
        List<ApiSubError> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String message = translateValidationMessage(fieldError.getDefaultMessage(), fieldError.getField());
            ApiValidationError api = new ApiValidationError(
                ex.getObjectName(),
                fieldError.getField(),
                fieldError.getRejectedValue(),
                message
            );
            errors.add(api);
        }
        return errors;
    }

    /**
     * Traduz mensagens de validação do inglês para português.
     *
     * @param defaultMessage Mensagem padrão do Spring
     * @param field Nome do campo
     * @return Mensagem traduzida
     */
    private String translateValidationMessage(String defaultMessage, String field) {
        if (defaultMessage == null) {
            return "Campo inválido";
        }

        // Traduções comuns de mensagens de validação
        if (defaultMessage.contains("must not be null") || defaultMessage.contains("must not be empty")) {
            return String.format("O campo '%s' é obrigatório", field);
        }
        if (defaultMessage.contains("must not be blank")) {
            return String.format("O campo '%s' não pode estar vazio", field);
        }
        if (defaultMessage.contains("size must be between")) {
            return String.format("O campo '%s' possui tamanho inválido", field);
        }
        if (defaultMessage.contains("must be a valid")) {
            return String.format("O campo '%s' possui formato inválido", field);
        }
        if (defaultMessage.contains("must be greater than") || defaultMessage.contains("must be less than")) {
            return String.format("O campo '%s' possui valor fora do intervalo permitido", field);
        }

        // Se não encontrar tradução específica, retorna a mensagem original
        return defaultMessage;
    }
}