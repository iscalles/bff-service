package ms_bff.bffService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate es el cliente HTTP que el BFF usa para hablar con los microservicios.
 * Lo registramos como @Bean para poder inyectarlo con @Autowired o @RequiredArgsConstructor.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
