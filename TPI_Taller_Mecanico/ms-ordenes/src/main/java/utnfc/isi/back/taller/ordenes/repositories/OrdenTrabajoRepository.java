package utnfc.isi.back.taller.ordenes.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import utnfc.isi.back.taller.ordenes.models.OrdenTrabajo;
import utnfc.isi.back.taller.ordenes.models.enums.EstadoOrden;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {

    /** RN-02: un vehículo no puede tener dos órdenes abiertas. */
    boolean existsByVehiculoIdAndEstadoNot(Long vehiculoId, EstadoOrden estado);

    List<OrdenTrabajo> findByEstado(EstadoOrden estado);

    List<OrdenTrabajo> findByMecanicoAsignado(String mecanicoAsignado);

    List<OrdenTrabajo> findByMecanicoAsignadoAndEstado(String mecanicoAsignado, EstadoOrden estado);
}
