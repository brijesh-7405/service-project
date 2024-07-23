/**
 *
 */
package com.workruit.us.application.security;

import com.workruit.us.application.services.ConsultancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.workruit.us.application.filters.LoginProcessingFilter;
import com.workruit.us.application.repositories.RoleRepository;
import com.workruit.us.application.repositories.UserRoleRepository;
import com.workruit.us.application.security.handlers.JwtTokenService;
import com.workruit.us.application.security.handlers.WorkruitAuthenticationSuccessHandler;
import com.workruit.us.application.security.handlers.WorkuitAuthenticationFailureHandler;
import com.workruit.us.application.security.providers.WorkruitAuthenticationProvider;
import com.workruit.us.application.services.ApplicantService;
import com.workruit.us.application.services.CompanyService;
import com.workruit.us.application.services.UserService;

/**
 * @author Santosh
 */
@EnableWebSecurity
@Configuration
public class WorkruitSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String LOGIN_ENTRY_POINT = "/login";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WorkruitAuthenticationProvider authenticationProvider = new WorkruitAuthenticationProvider();
    private @Autowired AuthenticationManager authenticationManager;
    private @Autowired WorkruitAuthenticationSuccessHandler workruitAuthenticationSuccessHandler;
    private @Autowired WorkuitAuthenticationFailureHandler workuitAuthenticationFailureHandler;
    private @Autowired UserService userService;
    private @Autowired CompanyService companyService;
    private @Autowired ConsultancyService consultancyService;
    private @Autowired ApplicantService applicantService;
    private @Autowired JwtTokenService jwtTokenService;
    private @Autowired UserRoleRepository userRoleRepository;
    private @Autowired RoleRepository roleRepository;

    protected LoginProcessingFilter setLoginProcessingFilter() {
        LoginProcessingFilter loginProcessingFilter = new LoginProcessingFilter(LOGIN_ENTRY_POINT,
                workruitAuthenticationSuccessHandler, workuitAuthenticationFailureHandler, objectMapper);
        loginProcessingFilter.setAuthenticationManager(authenticationManager);
        authenticationProvider.setUserService(userService);
        authenticationProvider.setCompanyService(companyService);
        authenticationProvider.setUserRoleRepository(userRoleRepository);
        authenticationProvider.setConsultancyService(consultancyService);
        authenticationProvider.setRoleRepository(roleRepository);
        authenticationProvider.setApplicantService(applicantService);
        return loginProcessingFilter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().configurationSource(request -> {
                    CorsConfiguration cors = new CorsConfiguration();
                    cors.setAllowedOrigins(Lists.newArrayList("*"));
                    cors.setAllowedMethods(Lists.newArrayList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    cors.setAllowedHeaders(Lists.newArrayList("*"));
                    return cors;
                }).and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/signup", LOGIN_ENTRY_POINT, "/", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg",
                        "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js")
                .permitAll().and()
                .addFilterBefore(setLoginProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**", "/swagger.json",
                "/configuration/security", "/swagger-ui.html", "/webjars/**", "/webjars/springfox-swagger-ui/**",
                "/META-INF/resources/**");
    }

}
