package com.back.domain.post.genFile.controller

import com.back.domain.post.genFile.entity.PostGenFile
import com.back.domain.post.post.service.PostService
import com.back.standard.util.Ut.url.removeDomain
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PostGenFileControllerTest {
    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다운로드 테스트")
    fun t1() {
        val post9 = postService.findById(9).get()

        val postGenFile1: PostGenFile = post9.genFiles.first()

        val downloadUrl = removeDomain(postGenFile1.downloadUrl)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get(downloadUrl)
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("download"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(
                MockMvcResultMatchers.header().string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${postGenFile1.originalFileName}\""
                )
            )
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_GIF))
    }
}