package com.proyecto.minimarket.Cliente.service;

import com.proyecto.minimarket.Cliente.model.Cliente;
import com.proyecto.minimarket.Cliente.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    // Inyección de dependencias por constructor (buena práctica recomendada)
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente crear(Cliente cliente) {
        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un cliente registrado con ese DNI.");
        }
        return clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
    }

    @Override
    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        Cliente cliente = buscarPorId(id);
        if (clienteActualizado.getDni() != null && !cliente.getDni().equals(clienteActualizado.getDni())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El DNI de un cliente no se puede modificar.");
        }

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellido(clienteActualizado.getApellido());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setTelefono(clienteActualizado.getTelefono());

        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id); // valida que exista antes de intentar eliminar
        clienteRepository.deleteById(id);
    }
}
