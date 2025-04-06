package com.back.global.security

import com.back.domain.member.member.service.MemberService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CustomOAuth2UserService(
    private val memberService: MemberService
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val providerTypeCode = userRequest
            .clientRegistration
            .registrationId
            .uppercase(Locale.getDefault())

        val attributes = oAuth2User.attributes

        val (oauthId, nickname, profileImgUrl) = when (providerTypeCode) {
            "NAVER" -> {
                val props = attributes["response"] as Map<String, String>
                Triple(props["id"]!!, props["nickname"]!!, props["profile_image"]!!)
            }

            else -> {
                val props = attributes["properties"] as Map<String, String>
                Triple(oAuth2User.name, props["nickname"]!!, props["profile_image"]!!)
            }
        }

        val username = "${providerTypeCode}__${oauthId}"
        val member = memberService.modifyOrJoin(username, nickname, profileImgUrl)

        return SecurityUser(
            id = member.id,
            username = member.username,
            password = "",
            nickname = member.nickname,
            authorities = member.authorities
        )
    }
}
