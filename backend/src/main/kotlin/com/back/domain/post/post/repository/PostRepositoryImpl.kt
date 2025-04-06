package com.back.domain.post.post.repository

import com.back.domain.member.member.entity.Member
import com.back.domain.post.post.entity.Post
import com.back.domain.post.post.entity.QPost
import com.back.standard.search.PostSearchKeywordTypeV1
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils

class PostRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : PostRepositoryCustom {
    override fun findByKw(
        kwType: PostSearchKeywordTypeV1,
        kw: String,
        author: Member?,
        published: Boolean?,
        listed: Boolean?,
        pageable: Pageable
    ): Page<Post> {
        val builder = BooleanBuilder()

        if (author != null) {
            builder.and(QPost.post.author.eq(author))
        }

        if (published != null) {
            builder.and(QPost.post.published.eq(published))
        }

        if (listed != null) {
            builder.and(QPost.post.listed.eq(listed))
        }

        if (kw.isNotBlank()) {
            applyKeywordFilter(kwType, kw, builder)
        }

        val postsQuery = createPostsQuery(builder)
        applySorting(pageable, postsQuery)

        postsQuery.offset(pageable.offset).limit(pageable.pageSize.toLong())

        val totalQuery = createTotalQuery(builder)

        return PageableExecutionUtils.getPage(postsQuery.fetch(), pageable) { totalQuery.fetchOne()!! }
    }

    private fun applyKeywordFilter(kwType: PostSearchKeywordTypeV1, kw: String, builder: BooleanBuilder) {
        when (kwType) {
            PostSearchKeywordTypeV1.title -> builder.and(QPost.post.title.containsIgnoreCase(kw))
            PostSearchKeywordTypeV1.content -> builder.and(QPost.post.content.containsIgnoreCase(kw))
            PostSearchKeywordTypeV1.author -> builder.and(QPost.post.author.nickname.containsIgnoreCase(kw))
            else -> builder.and(
                QPost.post.title.containsIgnoreCase(kw)
                    .or(QPost.post.content.containsIgnoreCase(kw))
                    .or(QPost.post.author.nickname.containsIgnoreCase(kw))
            )
        }
    }

    private fun createPostsQuery(builder: BooleanBuilder): JPAQuery<Post> {
        return jpaQueryFactory
            .select(QPost.post)
            .from(QPost.post)
            .where(builder)
    }

    private fun applySorting(pageable: Pageable, postsQuery: JPAQuery<Post>) {
        for (o in pageable.sort) {
            val pathBuilder: PathBuilder<*> = PathBuilder<Any?>(QPost.post.type, QPost.post.metadata)

            postsQuery.orderBy(
                OrderSpecifier(
                    if (o.isAscending) Order.ASC else Order.DESC,
                    pathBuilder[o.property] as Expression<Comparable<*>>
                )
            )
        }
    }

    private fun createTotalQuery(builder: BooleanBuilder): JPAQuery<Long> {
        return jpaQueryFactory
            .select(QPost.post.count())
            .from(QPost.post)
            .where(builder)
    }
}