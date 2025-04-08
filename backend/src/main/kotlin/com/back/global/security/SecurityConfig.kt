package com.back.global.security

import com.back.global.app.AppConfig
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import com.back.standard.util.Ut
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val customOAuth2AuthenticationSuccessHandler: CustomOAuth2AuthenticationSuccessHandler,
    private val customAuthorizationRequestResolver: CustomAuthorizationRequestResolver
) {
    @Bean
    fun baseSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize(HttpMethod.GET, "/api/*/posts/{id:\\d+}", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/comments", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/genFiles", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/{postId:\\d+}/genFiles/{id:\\d+}", permitAll)
                authorize("/api/*/members/login", permitAll)
                authorize("/api/*/members/logout", permitAll)
                authorize("/api/*/members/join", permitAll)
                authorize(HttpMethod.GET, "/api/*/posts/statistics", hasRole("ADMIN"))
                authorize(HttpMethod.GET, "/api/*/adm/members/**", hasRole("ADMIN"))
                authorize("/api/*/**", authenticated)
                authorize(anyRequest, permitAll)
            }

            headers {
                frameOptions {
                    sameOrigin = true
                }
            }

            csrf { disable() }

            oauth2Login {
                authenticationSuccessHandler = customOAuth2AuthenticationSuccessHandler

                authorizationEndpoint {
                    authorizationRequestResolver = customAuthorizationRequestResolver
                }
            }

            formLogin { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(customAuthenticationFilter)

            exceptionHandling {
                authenticationEntryPoint = AuthenticationEntryPoint { request, response, authException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = 401
                    response.writer.write(
                        Ut.json.toString(
                            RsData("401-1", "사용자 인증정보가 올바르지 않습니다.", Empty())
                        )
                    )
                }

                accessDeniedHandler = AccessDeniedHandler { request, response, accessDeniedException ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = 403
                    response.writer.write(
                        Ut.json.toString(
                            RsData("403-1", "권한이 없습니다.", Empty())
                        )
                    )
                }
            }
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 허용할 오리진 설정
        configuration.allowedOrigins = listOf(
            "https://cdpn.io",
            "https://localhost:3000",
            AppConfig.getSiteBackUrl(),
            AppConfig.getSiteFrontUrl()
        )

        // 허용할 HTTP 메서드 설정
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")

        // 자격 증명 허용 설정
        configuration.allowCredentials = true

        // 허용할 헤더 설정
        configuration.allowedHeaders = listOf("*")

        // CORS 설정을 소스에 등록
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", configuration)

        return source
    }
}