package com.proyecto.minimarket.controller;

import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = {VentaController.class, DetalleVentaController.class})
public class VentaExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> manejarValidacion(ResponseStatusException exception) {
        ProblemDetail error = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(error);
    }
}
