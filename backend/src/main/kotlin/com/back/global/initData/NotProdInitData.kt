package com.back.global.initData

import com.back.domain.member.member.service.MemberService
import com.back.domain.post.genFile.entity.PostGenFile
import com.back.domain.post.post.service.PostService
import com.back.global.app.AppConfig.Companion.getGenFileDirPath
import com.back.global.app.AppConfig.Companion.isTest
import com.back.global.app.CustomConfigProperties
import com.back.standard.sampleResource.SampleResource
import com.back.standard.util.Ut.file.rm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import java.util.stream.IntStream

@Profile("!prod")
@Configuration
class NotProdInitData(
    private val customConfigProperties: CustomConfigProperties,
    private val memberService: MemberService,
    private val postService: PostService
) {
    @Autowired
    @Lazy
    private lateinit var self: NotProdInitData

    @Bean
    fun baseInitDataApplicationRunner(): ApplicationRunner {
        return ApplicationRunner {
            self.work1()
            self.work2()
        }
    }

    @Transactional
    fun work1() {
        if (memberService.count() > 0) return

        if (isTest()) {
            rm(getGenFileDirPath())
        }

        val memberSystem = memberService.join("system", "1234", "시스템", "")
        memberSystem.apiKey = "system"

        val memberAdmin = memberService.join("admin", "1234", "관리자", "")
        memberAdmin.apiKey = "admin"

        val memberUser1 = memberService.join("user1", "1234", "유저1", "")
        memberUser1.apiKey = "user1"

        val memberUser2 = memberService.join("user2", "1234", "유저2", "")
        memberUser2.apiKey = "user2"

        val memberUser3 = memberService.join("user3", "1234", "유저3", "")
        memberUser3.apiKey = "user3"

        val memberUser4 = memberService.join("user4", "1234", "유저4", "")
        memberUser4.apiKey = "user4"

        val memberUser5 = memberService.join("user5", "1234", "유저5", "")
        memberUser5.apiKey = "user5"

        val memberUser6 = memberService.join("user6", "1234", "유저6", "")
        memberUser6.apiKey = "user6"

        for (notProdMember in customConfigProperties.notProdMembers) {
            val member = memberService.join(
                notProdMember.username,
                "",
                notProdMember.nickname,
                notProdMember.profileImgUrl
            )

            member.apiKey = notProdMember.apiKey()
        }
    }

    @Transactional
    fun work2() {
        if (postService!!.count() > 0) return

        val memberUser1 = memberService!!.findByUsername("user1").get()
        val memberUser2 = memberService.findByUsername("user2").get()
        val memberUser3 = memberService.findByUsername("user3").get()
        val memberUser4 = memberService.findByUsername("user4").get()
        val memberUser5 = memberService.findByUsername("user5").get()
        val memberUser6 = memberService.findByUsername("user6").get()

        val post1 = postService.write(
            memberUser1,
            "축구 하실 분?",
            "14시 까지 22명을 모아야 합니다.",
            true,
            true
        )
        post1.addComment(memberUser2, "저요!")
        post1.addComment(memberUser3, "저도 할래요.")

        val post2 = postService.write(
            memberUser1,
            "배구 하실 분?",
            "15시 까지 12명을 모아야 합니다.",
            true,
            true
        )
        post2.addComment(memberUser4, "저요!, 저 배구 잘합니다.")

        val post3 = postService.write(
            memberUser2,
            "농구 하실 분?",
            "16시 까지 10명을 모아야 합니다.",
            true,
            true
        )

        val post4 = postService.write(
            memberUser3,
            "발야구 하실 분?",
            "17시 까지 14명을 모아야 합니다.",
            true,
            true
        )

        val post5 = postService.write(
            memberUser4,
            "피구 하실 분?",
            "18시 까지 18명을 모아야 합니다.",
            true,
            true
        )

        val post6 = postService.write(
            memberUser4,
            "발야구를 밤에 하실 분?",
            "22시 까지 18명을 모아야 합니다.",
            false,
            false
        )

        val post7 = postService.write(
            memberUser4,
            "발야구를 새벽 1시에 하실 분?",
            "새벽 1시 까지 17명을 모아야 합니다.",
            true,
            false
        )

        val post8 = postService.write(
            memberUser4,
            "발야구를 새벽 3시에 하실 분?",
            "새벽 3시 까지 19명을 모아야 합니다.",
            false,
            true
        )

        val post9 = postService.write(
            memberUser4,
            "테이블테니스를 하실 분있나요?",
            "테이블테니스 강력 추천합니다.",
            true,
            true
        )

        val genFile1FilePath = SampleResource.IMG_GIF_SAMPLE1.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile1FilePath)

        var genFile2FilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.attachment, genFile2FilePath)
        post9.deleteGenFile(PostGenFile.TypeCode.attachment, 2)

        genFile2FilePath = SampleResource.IMG_JPG_SAMPLE2.makeCopy()
        post9.putGenFile(PostGenFile.TypeCode.attachment, 3, genFile2FilePath)

        val genFile3FilePath = SampleResource.IMG_JPG_SAMPLE3.makeCopy()
        post9.addGenFile(PostGenFile.TypeCode.thumbnail, genFile3FilePath)

        val newGenFile3FilePath = SampleResource.IMG_JPG_SAMPLE4.makeCopy()
        val postGenFile3 = post9.modifyGenFile(PostGenFile.TypeCode.thumbnail, 1, newGenFile3FilePath)

        post9.thumbnailGenFile = postGenFile3

        val post10 = postService.write(
            memberUser4,
            "테니스 하실 분있나요?",
            "테니스 강력 추천합니다.",
            true,
            true
        )

        val genFile4FilePath = SampleResource.IMG_WEBP_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile4FilePath)

        val genFile5FilePath = SampleResource.AUDIO_M4A_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile5FilePath)

        val genFile6FilePath = SampleResource.AUDIO_MP3_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile6FilePath)

        val genFile7FilePath = SampleResource.AUDIO_MP3_SAMPLE2.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile7FilePath)

        val genFile8FilePath = SampleResource.VIDEO_MOV_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile8FilePath)

        val genFile9FilePath = SampleResource.VIDEO_MP4_SAMPLE1.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile9FilePath)

        val genFile10FilePath = SampleResource.VIDEO_MP4_SAMPLE2.makeCopy()
        post10.addGenFile(PostGenFile.TypeCode.attachment, genFile10FilePath)

        IntStream.rangeClosed(11, 100).forEach { i: Int ->
            postService.write(
                memberUser5,
                "테스트 게시물 $i",
                "테스트 게시물 $i 내용",
                i % 3 != 0,
                i % 4 != 0
            )
        }

        IntStream.rangeClosed(101, 200).forEach { i: Int ->
            postService.write(
                memberUser6,
                "테스트 게시물 $i",
                "테스트 게시물 $i 내용",
                i % 5 != 0,
                i % 6 != 0
            )
        }
    }
}