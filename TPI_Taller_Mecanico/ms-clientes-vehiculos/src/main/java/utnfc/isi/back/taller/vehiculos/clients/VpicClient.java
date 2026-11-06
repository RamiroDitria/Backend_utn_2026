package utnfc.isi.back.taller.vehiculos.clients;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Integración con la API pública NHTSA vPIC.
 * Si la API no responde o no reconoce el VIN devuelve Optional.empty()
 * y el servicio pasa a la carga manual (los datos quedan como no verificados).
 */
@Component
public class VpicClient {

    private static final Logger log = LoggerFactory.getLogger(VpicClient.class);

    private final RestClient restClient;

    public VpicClient(RestClient vpicRestClient) {
        this.restClient = vpicRestClient;
    }

    public Optional<DatosVin> decodificar(String vin) {
        try {
            VpicResponse respuesta = restClient.get()
                    .uri("/vehicles/DecodeVinValues/{vin}?format=json", vin)
                    .retrieve()
                    .body(VpicResponse.class);

            if (respuesta == null || respuesta.results() == null || respuesta.results().isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> datos = respuesta.results().get(0);
            String marca = texto(datos, "Make");
            if (marca == null) {
                log.info("vPIC no reconoció el VIN {}", vin);
                return Optional.empty();
            }
            return Optional.of(new DatosVin(
                    marca,
                    texto(datos, "Model"),
                    entero(texto(datos, "ModelYear")),
                    armarMotor(texto(datos, "DisplacementL"), texto(datos, "EngineCylinders")),
                    texto(datos, "FuelTypePrimary")));
        } catch (RestClientException e) {
            log.warn("No se pudo consultar vPIC para el VIN {}: {}", vin, e.getMessage());
            return Optional.empty();
        }
    }

    private String texto(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        if (valor == null) {
            return null;
        }
        String s = valor.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private Integer entero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String armarMotor(String cilindrada, String cilindros) {
        if (cilindrada == null && cilindros == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (cilindrada != null) {
            sb.append(cilindrada).append("L");
        }
        if (cilindros != null) {
            sb.append(sb.length() > 0 ? " " : "").append(cilindros).append(" cil.");
        }
        return sb.toString();
    }
}
