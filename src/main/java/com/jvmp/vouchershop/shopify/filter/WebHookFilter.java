package com.jvmp.vouchershop.shopify.filter;

import com.jvmp.vouchershop.controller.HmacWebhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class WebHookFilter implements Filter {

    private final String secret;

    public WebHookFilter(String secret) {
        this.secret = secret;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        RequestWrapper wrapper = new RequestWrapper((HttpServletRequest) request);
        String base64EncodedHeader = wrapper.getHeader("X-Shopify-Hmac-SHA256");
        String body = wrapper.getBody();

        log.info("Webhook triggered. Shopify header X-Shopify-Hmac-SHA256: {}", base64EncodedHeader);
        log.info("Body {}", body);

        try {
            String calculatedHash = HmacWebhook.encode(secret, body);
            if (base64EncodedHeader.equals(calculatedHash))
                chain.doFilter(request, response);
            else {
                log.error("Shopify identity not recognized. Content hash comparison: ");
                log.error("Provided  : {}", base64EncodedHeader);
                log.error("Calculated: {}", calculatedHash);

                HttpServletResponse res = (HttpServletResponse) response;
                res.sendError(HttpStatus.UNAUTHORIZED.value());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("No i co sie stalo sie?", e.getMessage());
            log.error("Cause:", e);
        }
    }

    @Override
    public void destroy() {
        //
    }
}