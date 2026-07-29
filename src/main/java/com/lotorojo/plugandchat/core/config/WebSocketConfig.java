package com.lotorojo.plugandchat.core.config;


import com.lotorojo.plugandchat.core.security.WebSocketJwtInterceptor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    public WebSocketConfig(WebSocketJwtInterceptor webSocketJwtInterceptor) {
        this.webSocketJwtInterceptor = webSocketJwtInterceptor;
    }

    @PostConstruct
    public void init(){
        logger.info("Configuración cargada");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //Endpoint de entrada, por ahora todos los origins son soportados, después deberemos modificar eso
        registry.addEndpoint("/chat").setAllowedOrigins("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtInterceptor);
    }
}