package example.dividends_project.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // 사용하지 않을 기능들 disable 설정
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // 세션방식이 아닌 jwt방식 사용하므로

                .authorizeHttpRequests(authorizeRequest ->  // 실질적인 권한 제어 부분
                        authorizeRequest
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/auth/signup", "/auth/signin").permitAll()  // 허용할 경로 설정
                                .requestMatchers("/company").permitAll()
                                .requestMatchers("/finance//dividend/").permitAll()
//                                .requestMatchers("/error/**").permitAll()
                                .anyRequest().authenticated()
                )
//                .exceptionHandling(exception ->
//                        exception.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)  // 필터 순서 정의
                .headers(
                        headersConfigurer ->
                                headersConfigurer.frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::disable
                                )
                );
        return http.build();
    }
}