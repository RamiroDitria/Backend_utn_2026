package utnfc.isi.back.taller.vehiculos.clients;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta de /vehicles/DecodeVinValues/{vin}?format=json */
public record VpicResponse(
        @JsonProperty("Count") Integer count,
        @JsonProperty("Message") String message,
        @JsonProperty("Results") List<Map<String, Object>> results) {
}
