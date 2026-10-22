package com.proyecto.minimarket.Cliente.controller;

import com.proyecto.minimarket.Cliente.dto.ClienteRequestDTO;
import com.proyecto.minimarket.Cliente.dto.ClienteResponseDTO;
import com.proyecto.minimarket.Cliente.mapper.ClienteMapper;
import com.proyecto.minimarket.Cliente.model.Cliente;
import com.proyecto.minimarket.Cliente.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteMapper clienteMapper;

    public ClienteController(ClienteService clienteService, ClienteMapper clienteMapper) {
        this.clienteService = clienteService;
        this.clienteMapper = clienteMapper;
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        Cliente cliente = clienteMapper.toEntity(dto);
        Cliente creado = clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteMapper.toResponseDTO(creado));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        List<ClienteResponseDTO> clientes = clienteService.listarTodos()
                .stream()
                .map(clienteMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(clienteMapper.toResponseDTO(cliente));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        Cliente cliente = clienteMapper.toEntity(dto);
        Cliente actualizado = clienteService.actualizar(id, cliente);
        return ResponseEntity.ok(clienteMapper.toResponseDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}