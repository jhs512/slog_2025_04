package com.back.domain.post.comment.controller

import com.back.domain.post.comment.dto.PostCommentDto
import com.back.domain.post.post.service.PostService
import com.back.global.exceptions.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@Tag(name = "ApiV1PostCommentController", description = "API 댓글 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostCommentController(
    private val postService: PostService,
    private val rq: Rq
) {
    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건조회")
    fun items(
        @PathVariable postId: Long
    ): List<PostCommentDto> {
        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        return post
            .comments
            .stream()
            .map { PostCommentDto(it) }
            .toList()
    }


    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    fun delete(
        @PathVariable postId: Long,
        @PathVariable id: Long
    ): RsData<Empty> {
        val actor = rq.actor!!

        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postComment = post.getCommentById(id).orElseThrow {
            ServiceException(
                "404-2",
                "${id}번 댓글은 존재하지 않습니다."
            )
        }

        postComment.checkActorCanDelete(actor)

        post.removeComment(postComment)

        return RsData(
            "200-1",
            "${id}번 댓글이 삭제되었습니다."
        )
    }


    data class PostCommentModifyReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "수정")
    fun modify(
        @PathVariable postId: Long,
        @PathVariable id: Long,
        @RequestBody @Valid reqBody: PostCommentModifyReqBody
    ): RsData<PostCommentDto> {
        val actor = rq.actor!!

        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postComment = post.getCommentById(id).orElseThrow {
            ServiceException(
                "404-2",
                "${id}번 댓글은 존재하지 않습니다."
            )
        }

        postComment.checkActorCanModify(actor)

        postComment.modify(reqBody.content)

        return RsData(
            "200-1",
            "${id}번 댓글이 수정되었습니다.",
            PostCommentDto(postComment)
        )
    }


    data class PostCommentWriteReqBody(
        @field:NotBlank @field:Size(min = 2, max = 100) val content: String
    )

    @PostMapping
    @Transactional
    @Operation(summary = "작성")
    fun write(
        @PathVariable postId: Long,
        @RequestBody @Valid reqBody: PostCommentWriteReqBody
    ): RsData<PostCommentDto> {
        val actor = rq.actor!!

        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postComment = post.addComment(
            actor,
            reqBody.content
        )

        postService.flush()

        return RsData(
            "201-1",
            "${postComment.id}번 댓글이 생성되었습니다.",
            PostCommentDto(postComment)
        )
    }
}