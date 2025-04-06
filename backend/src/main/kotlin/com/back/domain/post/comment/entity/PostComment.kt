package com.back.domain.post.comment.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.global.exceptions.ServiceException
import com.back.global.jpa.entity.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne

@Entity
class PostComment : BaseTime {
    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var post: Post

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Member

    @Column(columnDefinition = "TEXT")
    lateinit var content: String

    constructor(post: Post, author: Member, content: String) {
        this.post = post
        this.author = author
        this.content = content
    }

    fun modify(content: String) {
        this.content = content
    }

    fun checkActorCanModify(actor: Member?) {
        if (actor == null) throw ServiceException("401-1", "로그인 후 이용해주세요.")

        if (actor == author) return

        throw ServiceException("403-2", "작성자만 댓글을 수정할 수 있습니다.")
    }

    fun checkActorCanDelete(actor: Member?) {
        if (actor == null) throw ServiceException("401-1", "로그인 후 이용해주세요.")

        if (actor.isAdmin) return

        if (actor == author) return

        throw ServiceException("403-2", "작성자만 댓글을 삭제할 수 있습니다.")
    }
}