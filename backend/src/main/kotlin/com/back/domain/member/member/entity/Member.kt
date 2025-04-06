package com.back.domain.member.member.entity

import com.back.global.jpa.entity.BaseTime
import com.back.standard.util.Ut
import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Entity
class Member : BaseTime {
    @Column(unique = true, length = 60)
    lateinit var username: String

    @Column(length = 65)
    lateinit var password: String

    @Column(length = 30)
    lateinit var nickname: String

    @Column(unique = true, length = 50)
    lateinit var apiKey: String

    lateinit var profileImgUrl: String

    val name: String
        get() = nickname

    val isAdmin: Boolean
        get() = "admin" == username

    constructor(id: Long, username: String, nickname: String) {
        this._id = id
        this.username = username
        this.nickname = nickname
        this.profileImgUrl = "" // TODO : 추후에 어떻게 할지 고민
    }

    constructor(username: String, password: String, nickname: String, apiKey: String, profileImgUrl: String) {
        this.username = username
        this.password = password
        this.nickname = nickname
        this.apiKey = apiKey
        this.profileImgUrl = profileImgUrl
    }

    val authoritiesAsStringList: List<String>
        get() {
            val authorities: MutableList<String> = ArrayList()

            if (isAdmin) authorities.add("ROLE_ADMIN")

            return authorities
        }

    val authorities: Collection<GrantedAuthority>
        get() = authoritiesAsStringList
            .stream()
            .map { SimpleGrantedAuthority(it) }
            .toList()

    val profileImgUrlOrDefault: String
        get() = if (Ut.str.isBlank(profileImgUrl)) "https://placehold.co/640x640?text=O_O" else profileImgUrl
}
