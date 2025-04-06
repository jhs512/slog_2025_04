package com.back.domain.post.post.controller

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.post.service.PostService
import com.back.standard.search.PostSearchKeywordTypeV1
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithUserDetails
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
class ApiV1PostControllerTest {
    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("1번글 조회")
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/1")
            )
            .andDo(MockMvcResultHandlers.print())

        val post = postService.findById(1).get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(post.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorId").value(post.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value(post.author.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorProfileImgUrl").value(post.author.profileImgUrlOrDefault)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(post.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(post.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.published").value(post.isPublished()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.listed").value(post.isListed()))
    }

    @Test
    @DisplayName("존재하지 않는 1000000번글 조회, 404")
    fun t2() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/1000000")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."))
    }

    @Test
    @DisplayName("글 작성")
    fun t3() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "title": "제목 new",
                            "content": "내용 new",
                            "published": true,
                            "listed": false
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        val post = postService.findLatest().get()

        Assertions.assertThat(post.author).isEqualTo(actor)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${post.id}번 글이 작성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(post.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").value(post.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").value(post.author.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.authorProfileImgUrl").value(post.author.profileImgUrlOrDefault)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value(post.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.published").value(post.isPublished()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.listed").value(post.isListed()))
    }

    @Test
    @DisplayName("글 작성, with no input")
    fun t4() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "title": "",
                            "content": ""
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("write"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                    content-NotBlank-must not be blank
                    content-Size-size must be between 2 and 10000000
                    title-NotBlank-must not be blank
                    title-Size-size must be between 2 and 100
                    """.trimIndent()
                )
            )
    }

    @Test
    @DisplayName("글 작성, with no actor")
    fun t5() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts")
                    .content(
                        """
                        {
                            "title": "제목 new",
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
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("글 수정")
    fun t6() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val post = postService.findById(1).get()

        val oldModifyDate = post.modifyDate

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/1")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "title": "축구 하실 분 계신가요?",
                            "content": "14시 까지 22명을 모아야 진행이 됩니다.",
                            "published": true,
                            "listed": false
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 글이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(1))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.not(Matchers.startsWith(oldModifyDate.toString().substring(0, 20))))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").value(post.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").value(post.author.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.authorProfileImgUrl").value(post.author.profileImgUrlOrDefault)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("축구 하실 분 계신가요?"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.published").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.listed").value(false))
    }

    @Test
    @DisplayName("글 수정, with no input")
    fun t7() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/1")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "title": "",
                            "content": ""
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("400-1"))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.msg").value(
                    """
                    content-NotBlank-must not be blank
                    content-Size-size must be between 2 and 10000000
                    title-NotBlank-must not be blank
                    title-Size-size must be between 2 and 100
                    """.trimIndent()
                )
            )
    }

    @Test
    @DisplayName("글 수정, with no actor")
    fun t8() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/1")
                    .content(
                        """
                        {
                            "title": "축구 하실 분 계신가요?",
                            "content": "14시 까지 22명을 모아야 진행이 됩니다."
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("글 수정, with no permission")
    fun t9() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.put("/api/v1/posts/1")
                    .header("Authorization", "Bearer $actorAuthToken")
                    .content(
                        """
                        {
                            "title": "축구 하실 분 계신가요?",
                            "content": "14시 까지 22명을 모아야 진행이 됩니다."
                        }
                        """.trimIndent()
                    )
                    .contentType(
                        MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("작성자만 글을 수정할 수 있습니다."))
    }

    @Test
    @DisplayName("글 삭제")
    @WithUserDetails("user1")
    fun t10() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/1")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1번 글이 삭제되었습니다."))

        Assertions.assertThat(postService.findById(1)).isEmpty()
    }

    @Test
    @DisplayName("글 삭제, with not exist post id")
    fun t11() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/1000000")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("404-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("해당 데이터가 존재하지 않습니다."))
    }

    @Test
    @DisplayName("글 삭제, with no actor")
    fun t12() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/1")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("사용자 인증정보가 올바르지 않습니다."))
    }

    @Test
    @DisplayName("글 삭제, with no permission")
    fun t13() {
        val actor = memberService.findByUsername("user2").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/1")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("작성자만 글을 삭제할 수 있습니다."))
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with 작성자")
    fun t14() {
        val actor = memberService.findByUsername("user4").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/6")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        val post = postService.findById(6).get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(post.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorId").value(post.author.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.authorName").value(post.author.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.authorProfileImgUrl").value(post.author.profileImgUrlOrDefault)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault))
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(post.title))
            .andExpect(MockMvcResultMatchers.jsonPath("$.content").value(post.content))
            .andExpect(MockMvcResultMatchers.jsonPath("$.published").value(post.isPublished()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.listed").value(post.isListed()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actorCanDelete").value(true))
            .andExpect(MockMvcResultMatchers.jsonPath("$.actorCanModify").value(true))
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with no actor")
    fun t15() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/6")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("401-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("비밀글 입니다. 로그인 후 이용해주세요."))
    }

    @Test
    @DisplayName("비공개글 6번글 조회, with no permission")
    fun t16() {
        val actor = memberService.findByUsername("user1").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/6")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("비공개글은 작성자만 볼 수 있습니다."))
    }

    @Test
    @DisplayName("다건 조회")
    fun t17() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts?page=1&pageSize=3")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByListedPaged(true, 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("다건 조회 with searchKeyword=축구")
    fun t18() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts?page=1&pageSize=3&searchKeyword=축구")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByListedPaged(true, PostSearchKeywordTypeV1.title, "축구", 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("다건 조회 with searchKeywordType=content&searchKeyword=18명")
    fun t19() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts?page=1&pageSize=3&searchKeywordType=content&searchKeyword=18명")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByListedPaged(true, PostSearchKeywordTypeV1.content, "18명", 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("내글 다건 조회")
    fun t20() {
        val actor = memberService.findByUsername("user4").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/mine?page=1&pageSize=3")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByAuthorPaged(actor, 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("mine"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("내글 다건 조회 with searchKeyword=발야구")
    fun t21() {
        val actor = memberService.findByUsername("user4").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/mine?page=1&pageSize=3&searchKeyword=발야구")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByAuthorPaged(actor, PostSearchKeywordTypeV1.title, "발야구", 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("mine"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("내글 다건 조회 with searchKeywordType=content&searchKeyword=18명")
    fun t22() {
        val actor = memberService.findByUsername("user4").get()
        val actorAuthToken = memberService.genAuthToken(actor)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/mine?page=1&pageSize=3&searchKeywordType=content&searchKeyword=18명")
                    .header("Authorization", "Bearer $actorAuthToken")
            )
            .andDo(MockMvcResultHandlers.print())

        val postPage = postService
            .findByAuthorPaged(actor, PostSearchKeywordTypeV1.content, "18명", 1, 3)

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("mine"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(postPage.totalElements))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(postPage.totalPages))
            .andExpect(MockMvcResultMatchers.jsonPath("$.currentPageNumber").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pageSize").value(3))

        val posts = postPage.content

        for (i in posts.indices) {
            val post = posts[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].id").value(post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].createDate")
                        .value(Matchers.startsWith(post.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].modifyDate")
                        .value(Matchers.startsWith(post.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].authorId").value(post.author.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorName").value(post.author.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].authorProfileImgUrl")
                        .value(post.author.profileImgUrlOrDefault)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].thumbnailImgUrl").value(post.thumbnailImgUrlOrDefault)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].title").value(post.title))
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].content").doesNotExist())
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$.items[$i].published").value(post.isPublished())
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$.items[$i].listed").value(post.isListed()))
        }
    }

    @Test
    @DisplayName("관리자는 통계를 볼 수 있다.")
    @WithUserDetails("admin")
    fun t23() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/statistics")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("statistics"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPostCount").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPublishedPostCount").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalListedPostCount").isNumber())
    }

    @Test
    @DisplayName("일반 유저는 통계를 볼 수 없다.")
    @WithUserDetails("user1")
    fun t24() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/statistics")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("403-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("권한이 없습니다."))
    }

    @Test
    @DisplayName("임시글 생성")
    @WithUserDetails("user1")
    fun t25() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value(Matchers.containsString("번 임시글이 생성되었습니다.")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.createDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.modifyDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorProfileImgUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.thumbnailImgUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.published").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.listed").isBoolean())
    }

    @Test
    @DisplayName("임시글 생성, 이미 임시글이 있다면 생성하지 않음")
    @WithUserDetails("user1")
    fun t26() {
        val resultActions1 = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        val resultActions2 = mvc
            .perform(
                MockMvcRequestBuilders.post("/api/v1/posts/temp")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions2
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value(Matchers.containsString("번 임시글을 불러옵니다.")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.createDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.modifyDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorId").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorName").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.authorProfileImgUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.thumbnailImgUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.published").isBoolean())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.listed").isBoolean())
    }

    @Test
    @DisplayName("1번글의 마지막 수정날짜가 2900-01-01T00:00:00 이후라면 조회, 아니라면 412")
    fun t27() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/1?lastModifyDateAfter=2900-01-01T00:00:00")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.status().isPreconditionFailed())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("412-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("변경된 데이터가 없습니다."))
    }
}