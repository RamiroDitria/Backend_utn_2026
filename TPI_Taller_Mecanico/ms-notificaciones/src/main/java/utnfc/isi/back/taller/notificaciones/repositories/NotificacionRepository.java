package utnfc.isi.back.taller.notificaciones.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import utnfc.isi.back.taller.notificaciones.models.Notificacion;
import utnfc.isi.back.taller.notificaciones.models.enums.EstadoNotificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByOrdenIdOrderByFechaCreacionDesc(Long ordenId);

    List<Notificacion> findByEstado(EstadoNotificacion estado);
}
