package com.cms.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Adds SameSite=Strict to every Set-Cookie header produced by the application.
 * The Jakarta Servlet API < 6.1 does not expose a setProperty() method on Cookie,
 * so we post-process the header manually.
 */
@Configuration
public class SameSiteCookieConfig {

    @Bean
    public FilterRegistrationBean<SameSiteFilter> sameSiteFilter() {
        FilterRegistrationBean<SameSiteFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new SameSiteFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(0);  // Run before everything else
        return bean;
    }

    static class SameSiteFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.getHeaders("Set-Cookie").stream()
                    .filter(header -> header.contains("access_token"))
                    .filter(header -> !header.contains("SameSite"))
                    .forEach(header ->
                            httpResponse.setHeader("Set-Cookie", header + "; SameSite=Strict"));
        }
    }
}
