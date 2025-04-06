package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post
import io.swagger.v3.oas.annotations.media.Schema

class PostWithContentDto(
    val content: String,
    var actorCanModify: Boolean? = null,
    var actorCanDelete: Boolean? = null,
    @Schema(hidden = true) private val post: Post
) : PostDto(post) {
    constructor(post: Post) : this(
        content = post.content,
        post = post
    )
}