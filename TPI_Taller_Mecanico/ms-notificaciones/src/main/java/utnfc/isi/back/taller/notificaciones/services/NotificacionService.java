package utnfc.isi.back.taller.notificaciones.services;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import utnfc.isi.back.taller.notificaciones.clients.WhatsAppClient;
import utnfc.isi.back.taller.notificaciones.dtos.NotificacionRequest;
import utnfc.isi.back.taller.notificaciones.models.Notificacion;
import utnfc.isi.back.taller.notificaciones.models.enums.EstadoNotificacion;
import utnfc.isi.back.taller.notificaciones.repositories.NotificacionRepository;

/**
 * F12 - Enviar notificaciones.
 * RN-09: si el envío falla queda PENDIENTE y se reintenta; tras N intentos queda FALLIDA.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final WhatsAppClient whatsAppClient;
    private final int maxIntentos;

    public NotificacionService(NotificacionRepository notificacionRepository, WhatsAppClient whatsAppClient,
                               @Value("${notificaciones.max-intentos:5}") int maxIntentos) {
        this.notificacionRepository = notificacionRepository;
        this.whatsAppClient = whatsAppClient;
        this.maxIntentos = maxIntentos;
    }

    @Transactional
    public Notificacion crear(NotificacionRequest request) {
        Notificacion notificacion = new Notificacion();
        notificacion.setOrdenId(request.ordenId());
        notificacion.setTipo(request.tipo());
        notificacion.setTelefono(request.telefono());
        notificacion.setNombreCliente(request.nombreCliente());
        notificacion.setMonto(request.monto());
        notificacion.setEstado(EstadoNotificacion.PENDIENTE);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacionRepository.save(notificacion);
        intentarEnvio(notificacion);
        return notificacion;
    }

    public List<Notificacion> listarPorOrden(Long ordenId) {
        return notificacionRepository.findByOrdenIdOrderByFechaCreacionDesc(ordenId);
    }

    @Transactional
    public void reintentarPendientes() {
        notificacionRepository.findByEstado(EstadoNotificacion.PENDIENTE).forEach(this::intentarEnvio);
    }

    private void intentarEnvio(Notificacion notificacion) {
        notificacion.setIntentos(notificacion.getIntentos() + 1);
        try {
            whatsAppClient.enviar(notificacion);
            notificacion.setEstado(EstadoNotificacion.ENVIADA);
            notificacion.setFechaEnvio(LocalDateTime.now());
            notificacion.setUltimoError(null);
        } catch (RestClientException e) {
            String error = e.getMessage() == null ? "Error desconocido" : e.getMessage();
            notificacion.setUltimoError(error.length() > 500 ? error.substring(0, 500) : error);
            notificacion.setEstado(notificacion.getIntentos() >= maxIntentos
                    ? EstadoNotificacion.FALLIDA
                    : EstadoNotificacion.PENDIENTE);
            log.warn("Falló el envío de la notificación {} (intento {}): {}",
                    notificacion.getId(), notificacion.getIntentos(), error);
        }
    }
}
