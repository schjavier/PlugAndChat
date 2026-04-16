package com.lotorojo.plugandchat.core.security;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String tenantIdHeader = request.getHeader("X-Tenant-ID");

        try {
            if (tenantIdHeader != null && !tenantIdHeader.isBlank()){
                UUID tenantId = UUID.fromString(tenantIdHeader);
                TenantContext.setCurrentTenant(tenantId);
            }

            filterChain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid Tenant ID format");
        }
        finally {
            TenantContext.clear();
        }
    }
}
