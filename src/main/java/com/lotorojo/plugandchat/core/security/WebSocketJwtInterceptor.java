package com.lotorojo.plugandchat.core.security;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import io.jsonwebtoken.JwtException;
import org.jspecify.annotations.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WebSocketJwtInterceptor implements ChannelInterceptor {

        private final JwtService jwtService;
        private final UserDetailsServiceImpl userDetailsService;

        public WebSocketJwtInterceptor(JwtService jwtService,  UserDetailsServiceImpl userDetailsService) {
            this.jwtService = jwtService;
            this.userDetailsService = userDetailsService;
        }

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (!verifyAccessor(accessor)) {
            return ChannelInterceptor.super.preSend(message, channel);
        }

        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            handleConnectInterceptor(accessor);
        } else {
            handleSubsequentRequestInterceptor(accessor);
        }
        return ChannelInterceptor.super.preSend(message, channel);
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, @Nullable Exception ex) {
        TenantContext.clear();
    }

    private boolean verifyAccessor(StompHeaderAccessor accessor) {
       return (accessor != null);
    }

    private String getToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        } else {
            throw new BadCredentialsException("Invalid Authorization header");
        }
    }

    private void handleConnectInterceptor(StompHeaderAccessor accessor) {
            String token = getToken(accessor);
            UUID tenantId = jwtService.extractTenantId(token);

            TenantContext.setCurrentTenant(tenantId);

            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(!jwtService.isTokenValid(token, userDetails)){
                throw new BadCredentialsException("Invalid token");
            }

            UsernamePasswordAuthenticationToken autheticatedUser =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

            accessor.setUser(autheticatedUser);

            if (verifySessionAttributes(accessor)){
                accessor.getSessionAttributes().put("tenantId", tenantId);
            }
    }

    private void handleSubsequentRequestInterceptor(StompHeaderAccessor accessor) {
            if (accessor.getUser() != null && verifySessionAttributes(accessor)){
                UUID tenantId = (UUID) accessor.getSessionAttributes().get("tenantId");
                if (tenantId != null) {
                    TenantContext.setCurrentTenant(tenantId);
                }
            }
        }

    private boolean verifySessionAttributes(StompHeaderAccessor accessor) {
        return accessor.getSessionAttributes() != null;
    }

}
