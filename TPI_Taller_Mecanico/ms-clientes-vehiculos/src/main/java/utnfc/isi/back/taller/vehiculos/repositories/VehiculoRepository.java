package utnfc.isi.back.taller.vehiculos.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import utnfc.isi.back.taller.vehiculos.models.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    Optional<Vehiculo> findByPatente(String patente);

    boolean existsByPatente(String patente);

    List<Vehiculo> findByClienteId(Long clienteId);
}
