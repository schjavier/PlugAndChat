package com.lotorojo.plugandchat.core.security;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   UserDetailsService userDetailsService,
                                       @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            processAuthentication(request);

        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
            return;
        }

        try{
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

        private void processAuthentication(HttpServletRequest request){
            Optional<String> token = getTokenFromHeader(request)
                    .or( () -> getTokenFromCookie(request));

            if (token.isPresent()){
                handleJwtAuthentication(token.get());
                return;
            }

            String tenantHeader = request.getHeader("X-Tenant-ID");
            if(tenantHeader != null && !tenantHeader.isBlank()){
                TenantContext.setCurrentTenant(UUID.fromString(tenantHeader));
            }
        }

        private Optional<String> getTokenFromHeader(HttpServletRequest request){

            return Optional.ofNullable(request.getHeader("Authorization"))
                    .filter(header -> header.startsWith("Bearer "))
                    .map(header -> header.substring(7))
                    .filter(token -> !token.isBlank());
        }

        private Optional<String> getTokenFromCookie(HttpServletRequest request){

            return Optional.ofNullable(request.getCookies())
                    .stream()
                    .flatMap(Arrays::stream)
                    .filter((cookie) -> "auth_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(token -> token != null && !token.isBlank())
                    .findFirst();
        }


        private void handleJwtAuthentication(String token){
            UUID tenantId = jwtService.extractTenantId(token);
            String email = jwtService.extractUsername(token);

            TenantContext.setCurrentTenant(tenantId);

            if (email == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            String role = jwtService.extractRole(token);

            if("GUEST".equals(role)){

                if (jwtService.isTokenValid(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, null, List.of(new SimpleGrantedAuthority("ROLE_GUEST")));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } else {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails)) {
                    setSecurityContext(userDetails);
                }
            }
        }

        private void setSecurityContext(UserDetails userDetails) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

    }
