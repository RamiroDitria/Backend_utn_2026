package utnfc.isi.back.taller.ordenes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient vehiculosRestClient(@Value("${servicios.vehiculos-url}") String url,
                                          @Value("${servicios.timeout-ms:3000}") int timeoutMs) {
        return crear(url, timeoutMs);
    }

    @Bean
    public RestClient notificacionesRestClient(@Value("${servicios.notificaciones-url}") String url,
                                               @Value("${servicios.timeout-ms:3000}") int timeoutMs) {
        return crear(url, timeoutMs);
    }

    private RestClient crear(String url, int timeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return RestClient.builder().baseUrl(url).requestFactory(factory).build();
    }
}
