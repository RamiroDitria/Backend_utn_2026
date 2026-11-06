package utnfc.isi.back.taller.notificaciones.clients;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import utnfc.isi.back.taller.notificaciones.models.Notificacion;
import utnfc.isi.back.taller.notificaciones.models.enums.TipoNotificacion;

/**
 * Integración con WhatsApp Cloud API (Meta). Los mensajes que inicia la empresa
 * se envían con plantillas aprobadas en Meta:
 *  - plantilla de presupuesto: {{1}} nombre, {{2}} número de orden, {{3}} total
 *  - plantilla de vehículo listo: {{1}} nombre, {{2}} número de orden
 * Si whatsapp.enabled=false el envío se simula en el log (útil para desarrollo y demo).
 * Lanza RestClientException si el envío falla, para que el servicio lo reintente.
 */
@Component
public class WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppClient.class);

    private final RestClient restClient;
    private final boolean habilitado;
    private final String phoneNumberId;
    private final String plantillaPresupuesto;
    private final String plantillaVehiculoListo;
    private final String idioma;

    public WhatsAppClient(RestClient whatsappRestClient,
                          @Value("${whatsapp.enabled:false}") boolean habilitado,
                          @Value("${whatsapp.phone-number-id:}") String phoneNumberId,
                          @Value("${whatsapp.plantilla-presupuesto}") String plantillaPresupuesto,
                          @Value("${whatsapp.plantilla-vehiculo-listo}") String plantillaVehiculoListo,
                          @Value("${whatsapp.idioma:es_AR}") String idioma) {
        this.restClient = whatsappRestClient;
        this.habilitado = habilitado;
        this.phoneNumberId = phoneNumberId;
        this.plantillaPresupuesto = plantillaPresupuesto;
        this.plantillaVehiculoListo = plantillaVehiculoListo;
        this.idioma = idioma;
    }

    public void enviar(Notificacion notificacion) {
        String nombre = Objects.requireNonNullElse(notificacion.getNombreCliente(), "cliente");
        String orden = "#" + notificacion.getOrdenId();

        String plantilla;
        List<Map<String, String>> parametros;
        if (notificacion.getTipo() == TipoNotificacion.PRESUPUESTO) {
            plantilla = plantillaPresupuesto;
            parametros = List.of(texto(nombre), texto(orden), texto(formatearMonto(notificacion.getMonto())));
        } else {
            plantilla = plantillaVehiculoListo;
            parametros = List.of(texto(nombre), texto(orden));
        }

        if (!habilitado) {
            log.info("[WhatsApp simulado] plantilla={} telefono={} parametros={}",
                    plantilla, notificacion.getTelefono(), parametros);
            return;
        }

        Map<String, Object> cuerpo = Map.of(
                "messaging_product", "whatsapp",
                "to", notificacion.getTelefono().replaceAll("[^0-9]", ""),
                "type", "template",
                "template", Map.of(
                        "name", plantilla,
                        "language", Map.of("code", idioma),
                        "components", List.of(Map.of("type", "body", "parameters", parametros))));

        restClient.post()
                .uri("/{phoneNumberId}/messages", phoneNumberId)
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, String> texto(String valor) {
        return Map.of("type", "text", "text", valor);
    }

    private String formatearMonto(BigDecimal monto) {
        return monto == null ? "-" : "$" + monto.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
