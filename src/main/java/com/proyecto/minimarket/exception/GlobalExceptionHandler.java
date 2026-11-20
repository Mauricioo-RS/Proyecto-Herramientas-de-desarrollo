package com.proyecto.minimarket.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> crearRespuesta(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                status.value(),
                status.name(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }

    // Errores que ya tienen un estado HTTP definido en el servicio
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> manejarResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        return crearRespuesta(
                status,
                ex.getReason() != null ? ex.getReason() : "Ha ocurrido un error.",
                request
        );
    }

    // Errores de validación del DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Los datos enviados no son válidos.");

        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    // Errores de validación de parámetros, como un ID negativo
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> manejarParametros(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String mensaje = ex.getConstraintViolations()
                .stream()
                .map(error -> error.getMessage())
                .findFirst()
                .orElse("Los parámetros enviados no son válidos.");

        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    // Restricciones de la base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridad(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                "No se pudo registrar o actualizar porque existe un dato duplicado o una restricción de la base de datos.",
                request
        );
    }

    // JSON incorrecto
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> manejarJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "El formato JSON enviado no es válido.",
                request
        );
    }

    // Cualquier otro error inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor.",
                request
        );
    }
}