package com.proyecto.minimarket.Cliente.repository;

import com.proyecto.minimarket.Cliente.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Busca un cliente por su DNI
    Optional<Cliente> findByDni(String dni);

    // Verifica si ya existe un cliente con ese DNI
    boolean existsByDni(String dni);
}