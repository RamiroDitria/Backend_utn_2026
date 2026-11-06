package utnfc.isi.back.taller.notificaciones.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** RN-09: reintenta periódicamente los avisos que quedaron pendientes. */
@Component
@RequiredArgsConstructor
public class ReintentoScheduler {

    private final NotificacionService notificacionService;

    @Scheduled(fixedDelayString = "${notificaciones.reintento-ms:60000}",
               initialDelayString = "${notificaciones.reintento-ms:60000}")
    public void reintentar() {
        notificacionService.reintentarPendientes();
    }
}
