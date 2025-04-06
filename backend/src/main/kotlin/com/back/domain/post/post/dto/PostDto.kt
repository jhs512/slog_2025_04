package com.back.domain.post.post.dto

import com.back.domain.post.post.entity.Post

import java.time.LocalDateTime

open class PostDto(
    val id: Long,
    val createDate: LocalDateTime,
    val modifyDate: LocalDateTime,
    val authorId: Long,
    val authorName: String,
    val authorProfileImgUrl: String,
    val title: String,
    val published: Boolean,
    val listed: Boolean,
    val thumbnailImgUrl: String
) {
    constructor(post: Post) : this(
        id = post.id,
        createDate = post.createDate,
        modifyDate = post.modifyDate,
        authorId = post.author.id,
        authorName = post.author.name,
        authorProfileImgUrl = post.author.profileImgUrlOrDefault,
        title = post.title,
        published = post.published,
        listed = post.listed,
        thumbnailImgUrl = post.thumbnailImgUrlOrDefault
    )
}