package com.back.global.initData

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import com.back.standard.util.Ut.cmd.runAsync
import com.back.standard.util.Ut.file.downloadByHttp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

@Profile("dev")
@Configuration
class DevInitData(
    private val memberService: MemberService,
    private val postService: PostService
) {
    @Autowired
    @Lazy
    private lateinit var self: DevInitData

    @Bean
    fun devInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            downloadByHttp("http://localhost:8080/v3/api-docs/apiV1", ".", false)

            val cmd =
                "yes | npx --package typescript --package openapi-typescript openapi-typescript apiV1.json -o ../frontend/src/lib/backend/apiV1/schema.d.ts"

            runAsync(cmd)
        }
    }
}