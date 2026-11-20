package com.proyecto.minimarket.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(ResponseStatusException exception) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarCamposInvalidos(MethodArgumentNotValidException exception) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay campos inválidos.");
        error.setProperty("errores", exception.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .toList());
        return error;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail manejarJsonInvalido() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo JSON falta o tiene un formato inválido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarRestriccion(DataIntegrityViolationException exception) {
        // H2 y PostgreSQL usan 23505. MySQL identifica duplicados con el código 1062.
        for (Throwable causa = exception.getCause(); causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql && ("23505".equals(sql.getSQLState()) || sql.getErrorCode() == 1062)) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Ya existe un registro con esos datos.");
            }
        }
        throw exception;
    }
}
