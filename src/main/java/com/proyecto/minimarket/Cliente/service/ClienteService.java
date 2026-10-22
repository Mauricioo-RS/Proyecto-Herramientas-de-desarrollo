package com.proyecto.minimarket.Cliente.service;

import com.proyecto.minimarket.Cliente.model.Cliente;

import java.util.List;

public interface ClienteService {

    Cliente crear(Cliente cliente);

    List<Cliente> listarTodos();

    Cliente buscarPorId(Long id);

    Cliente actualizar(Long id, Cliente cliente);

    void eliminar(Long id);
}