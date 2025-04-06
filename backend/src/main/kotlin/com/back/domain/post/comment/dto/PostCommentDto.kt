package com.back.domain.post.comment.dto

import com.back.domain.post.comment.entity.PostComment
import java.time.LocalDateTime

data class PostCommentDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val content: String
) {
    constructor(postComment: PostComment) : this(
        id = postComment.id,
        createDate = postComment.createDate,
        modifyDate = postComment.modifyDate,
        postId = postComment.post.id,
        authorId = postComment.author.id,
        authorName = postComment.author.name,
        content = postComment.content
    )
}