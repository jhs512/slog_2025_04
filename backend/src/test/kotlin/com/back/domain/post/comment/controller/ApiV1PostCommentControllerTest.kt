package com.back.domain.post.comment.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.comment.entity.PostComment
import com.back.domain.post.post.service.PostService
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostCommentControllerTest {
    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다건 조회")
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/1/comments")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        val comments: List<PostComment> = postService
            .findById(1).get().comments

        for (i in comments.indices) {
            val postComment = comments[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].id").value(postComment.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].createDate")
                        .value(Matchers.startsWith(postComment.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].modifyDate")
                        .value(Matchers.startsWith(postComment.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].authorId").value(postComment.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].authorName").value(postComment.author.name)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].content").value(postComment.content))
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    fun t2() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/1/comments/1")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."))
    }

    @Test
    @DisplayName("댓글 수정")
    fun t3() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/1/comments/1")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "content": "내용 new"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 댓글이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.createDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.modifyDate").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").value(actor.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").value(actor.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("내용 new"))
    }

    @Test
    @DisplayName("댓글 등록")
    fun t4() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/1/comments")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "content": "내용 new"
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val post = postService.findById(1).get()
        val lastPostComment: PostComment = post.comments.last()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostCommentController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${lastPostComment.id}번 댓글이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(lastPostComment.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(lastPostComment.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(lastPostComment.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").value(lastPostComment.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").value(lastPostComment.author.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value(lastPostComment.content))
    }
}