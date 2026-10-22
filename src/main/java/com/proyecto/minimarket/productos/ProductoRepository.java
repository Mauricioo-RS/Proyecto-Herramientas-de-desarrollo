package com.proyecto.minimarket.productos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizarStock(@Param("id") Long id);

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByCategoriaIgnoreCaseAndActivoTrue(String categoria);
}
