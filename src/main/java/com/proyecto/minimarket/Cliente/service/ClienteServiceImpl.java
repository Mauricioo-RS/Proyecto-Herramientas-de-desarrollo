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

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente crear(Cliente cliente) {

        if (clienteRepository.existsByDni(cliente.getDni())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un cliente registrado con ese DNI."
            );
        }

        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()
                && clienteRepository.existsByEmailIgnoreCase(cliente.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un cliente registrado con ese correo electrónico."
            );
        }

        return clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente buscarPorId(Long id) {

        validarId(id);

        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado con ID: " + id
                ));
    }

    @Override
    public Cliente actualizar(Long id, Cliente clienteActualizado) {

        Cliente cliente = buscarPorId(id);

        if (clienteActualizado.getDni() != null
                && !cliente.getDni().equals(clienteActualizado.getDni())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI de un cliente no se puede modificar."
            );
        }

        String nuevoEmail = clienteActualizado.getEmail();

        if (nuevoEmail != null && !nuevoEmail.isBlank()
                && !nuevoEmail.equalsIgnoreCase(cliente.getEmail())
                && clienteRepository.existsByEmailIgnoreCaseAndIdNot(nuevoEmail, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo electrónico ya pertenece a otro cliente."
            );
        }

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setApellido(clienteActualizado.getApellido());
        cliente.setEmail(nuevoEmail);
        cliente.setTelefono(clienteActualizado.getTelefono());

        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Long id) {

        Cliente cliente = buscarPorId(id);

        clienteRepository.delete(cliente);
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del cliente debe ser un número mayor que cero."
            );
        }
    }
}