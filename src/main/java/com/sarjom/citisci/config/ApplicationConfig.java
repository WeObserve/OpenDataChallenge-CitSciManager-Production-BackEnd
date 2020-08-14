package com.sarjom.citisci.config;

import com.sarjom.citisci.StartupListener;
import com.sarjom.citisci.config.filters.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ServletComponentScan(basePackageClasses = {StartupListener.class})
public class ApplicationConfig {
    @Bean
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("authenticationFilter");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}
