package javaservices.streamingservice.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.web.cors.CorsConfiguration;
import java.util.Collections;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route(GET("/myEndpoint").and(accept(APPLICATION_JSON)),)
                .withAttribute("org.springframework.web.reactive.function.server.RouterFunctions.CORSConfiguration", corsConfiguration());
    }

    private CorsConfiguration corsConfiguration() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("*")); // Allow all origins
        corsConfig.setAllowedMethods(Collections.singletonList("*")); // Allow all methods
        corsConfig.setAllowedHeaders(Collections.singletonList("*")); // Allow all headers
        corsConfig.setAllowCredentials(true); // Allow credentials
        corsConfig.setMaxAge(3600L); // Set the max age
        return corsConfig;
    }
}
