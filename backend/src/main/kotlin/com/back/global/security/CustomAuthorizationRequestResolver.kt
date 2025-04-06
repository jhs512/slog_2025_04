package com.back.global.security

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
        val authorizationRequest = request?.let { defaultResolver.resolve(it) }
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    override fun resolve(request: HttpServletRequest?, clientRegistrationId: String?): OAuth2AuthorizationRequest? {
        val authorizationRequest = request?.let { clientRegistrationId?.let { id -> defaultResolver.resolve(it, id) } }
        return customizeAuthorizationRequest(authorizationRequest, request)
    }

    private fun customizeAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest?
    ): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null || request == null) {
            return null
        }

        val redirectUrl = request.getParameter("redirectUrl")

        val additionalParameters = authorizationRequest.additionalParameters.toMutableMap()
        if (!redirectUrl.isNullOrEmpty()) {
            additionalParameters["state"] = redirectUrl
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .state(redirectUrl)
            .build()
    }
}
