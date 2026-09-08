package com.proyecto.minimarket.Cliente.service;

import com.proyecto.minimarket.Cliente.model.Cliente;
import com.proyecto.minimarket.Cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;

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
            throw new RuntimeException("Ya existe un cliente registrado con el DNI: " + cliente.getDni());
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
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    @Override
    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        Cliente cliente = buscarPorId(id); // reutiliza el método anterior, ya valida que exista

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellido(clienteActualizado.getApellido());
        cliente.setEmail(clienteActualizado.getEmail());
        cliente.setTelefono(clienteActualizado.getTelefono());
        // Nota: el DNI normalmente no se actualiza, por eso no se toca aquí

        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        buscarPorId(id); // valida que exista antes de intentar eliminar
        clienteRepository.deleteById(id);
    }
}