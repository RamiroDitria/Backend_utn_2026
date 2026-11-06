package utnfc.isi.back.taller.vehiculos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import utnfc.isi.back.taller.vehiculos.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
