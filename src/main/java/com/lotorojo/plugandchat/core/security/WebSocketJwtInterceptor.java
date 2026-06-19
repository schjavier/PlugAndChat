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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;
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

        if (!verifyAccessor(accessor) || accessor.getCommand() == null) {
            return ChannelInterceptor.super.preSend(message, channel);
        }

        switch (accessor.getCommand()) {
            case CONNECT -> handleConnectInterceptor(accessor);
            case SUBSCRIBE, SEND -> {
                handleSubsequentRequestInterceptor(accessor);
                validateDestinationTenant(accessor);
            }
            default -> handleSubsequentRequestInterceptor(accessor);
        }

        return ChannelInterceptor.super.preSend(message, channel);

    }



    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, @Nullable Exception ex) {
        TenantContext.clear();
    }

    // private utility methods


    private void handleConnectInterceptor(StompHeaderAccessor accessor) {
            String token = getToken(accessor);
            UUID tenantId = jwtService.extractTenantId(token);

            TenantContext.setCurrentTenant(tenantId);

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            UsernamePasswordAuthenticationToken autheticatedUser;

            if("GUEST".equals(role)) {
                if(!jwtService.isTokenValid(token)) {
                    throw new BadCredentialsException("Invalid guest token");
                }
                autheticatedUser = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("ROLE_GUEST")));
            }else {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(!jwtService.isTokenValid(token, userDetails)){
                    throw new BadCredentialsException("Invalid token");
                }

                autheticatedUser =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
            }

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

    private void validateDestinationTenant(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null) {
            return;
        }

        UUID userTenantId = null;
        if (verifySessionAttributes(accessor)) {
            userTenantId = (UUID) accessor.getSessionAttributes().get("tenantId");
        }

        if (userTenantId == null) {
            throw new BadCredentialsException("Access denied");
        }

        String[] parts = destination.split("/");
        if (parts.length > 3 && "tenants".equals(parts[2])){
            try {
                UUID destinationTenantId = UUID.fromString(parts[3]);
                compareTenantIds(userTenantId, destinationTenantId);
            } catch (IllegalArgumentException e) {
                throw new BadCredentialsException("Access denied: Invalid destination tenant");
            }
        } else {
            throw new BadCredentialsException("Access denied: Destination doesn't comply with expected structure");
        }

    }

    private void compareTenantIds(UUID userTenantId, UUID destinationTenantId) {
            if (!destinationTenantId.equals(userTenantId)) {
                throw new BadCredentialsException("Access denied: Destination Tenant mismatch");
            }
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

    private boolean verifySessionAttributes(StompHeaderAccessor accessor) {
        return accessor.getSessionAttributes() != null;
    }

}
