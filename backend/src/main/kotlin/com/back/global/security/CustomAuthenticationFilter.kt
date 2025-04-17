package com.back.global.security

import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.service.MemberService
import com.back.global.rq.Rq
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class CustomAuthenticationFilter(
    private val memberService: MemberService,
    private val rq: Rq
) : OncePerRequestFilter() {

    private data class AuthTokens(val apiKey: String, val accessToken: String)

    private fun getAuthTokensFromRequest(): AuthTokens? {
        val authorization = rq.getHeader("Authorization")

        if (!authorization.isNullOrEmpty() && authorization.startsWith("Bearer ")) {
            val token = authorization.removePrefix("Bearer ")
            val tokenBits = token.split(" ", limit = 2)

            if (tokenBits.size == 2) {
                return AuthTokens(tokenBits[0], tokenBits[1])
            }
        }

        val apiKey = rq.getCookieValue("apiKey")
        val accessToken = rq.getCookieValue("accessToken")

        return if (!apiKey.isNullOrEmpty() && !accessToken.isNullOrEmpty()) {
            AuthTokens(apiKey, accessToken)
        } else {
            null
        }
    }

    private fun refreshAccessToken(member: Member) {
        rq.refreshAccessToken(member)
    }

    private fun refreshAccessTokenByApiKey(apiKey: String): Member {
        val member = memberService.findByApiKey(apiKey).get()
        refreshAccessToken(member)
        return member
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.requestURI.startsWith("/api/") && !request.requestURI.startsWith("/ws")) {
            filterChain.doFilter(request, response)
            return
        }

        if (request.requestURI in listOf("/api/v1/members/login", "/api/v1/members/logout", "/api/v1/members/join")) {
            filterChain.doFilter(request, response)
            return
        }

        val authTokens = getAuthTokensFromRequest()

        if (authTokens == null) {
            filterChain.doFilter(request, response)
            return
        }

        val (apiKey, accessToken) = authTokens
        var member = memberService.getMemberFromAccessToken(accessToken)

        if (member == null) {
            member = refreshAccessTokenByApiKey(apiKey)
        }

        rq.setLogin(member)

        filterChain.doFilter(request, response)
    }
}