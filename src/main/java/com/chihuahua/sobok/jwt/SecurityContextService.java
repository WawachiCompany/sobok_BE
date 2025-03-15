package com.chihuahua.sobok.jwt;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class SecurityContextService {
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void saveSecurityContext(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.getContext();
        securityContextRepository.saveContext(context, request, response);
    }
}
