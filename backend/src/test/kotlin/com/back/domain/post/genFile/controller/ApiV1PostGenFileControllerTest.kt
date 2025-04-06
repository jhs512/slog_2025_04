package com.back.domain.post.genFile.controller

import com.back.domain.post.genFile.entity.PostGenFile
import com.back.domain.post.post.service.PostService
import com.back.global.app.AppConfig.Companion.getTempDirPath
import com.back.standard.sampleResource.SampleResource
import com.back.standard.util.Ut.file.copy
import com.back.standard.util.Ut.file.mv
import com.back.standard.util.Ut.file.rm
import org.assertj.core.api.Assertions
import org.hamcrest.Matchers
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import java.io.FileInputStream

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiV1PostGenFileControllerTest {
    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    @DisplayName("다건 조회")
    fun t1() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/9/genFiles")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("items"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        val postGenFiles: List<PostGenFile> = postService
            .findById(9)
            .get()
            .genFiles

        for (i in postGenFiles.indices) {
            val postGenFile = postGenFiles[i]
            resultActions
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].id").value(postGenFile.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].createDate")
                        .value(Matchers.startsWith(postGenFile.createDate.toString().substring(0, 20)))
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].modifyDate")
                        .value(Matchers.startsWith(postGenFile.modifyDate.toString().substring(0, 20)))
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].postId").value(postGenFile.post.id))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].typeCode").value(postGenFile.typeCode.name)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].fileExtTypeCode")
                        .value(postGenFile.fileExtTypeCode)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].fileExtType2Code")
                        .value(postGenFile.fileExtType2Code)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileSize").value(postGenFile.fileSize))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileNo").value(postGenFile.fileNo))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileExt").value(postGenFile.fileExt))
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].fileDateDir").value(postGenFile.fileDateDir)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].originalFileName")
                        .value(postGenFile.originalFileName)
                )
                .andExpect(
                    MockMvcResultMatchers.jsonPath("$[$i].downloadUrl").value(postGenFile.downloadUrl)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].publicUrl").value(postGenFile.publicUrl))
                .andExpect(MockMvcResultMatchers.jsonPath("$[$i].fileName").value(postGenFile.fileName))
        }
    }

    @Test
    @DisplayName("새 파일 등록")
    @WithUserDetails("user4")
    fun t2() {
        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.attachment)
                    .file(
                        MockMultipartFile(
                            "files",
                            SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                            FileInputStream(newFilePath)
                        )
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("makeNewItems"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("1개의 파일이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].modifyDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postId").value(9))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].typeCode").value(PostGenFile.TypeCode.attachment.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileNo").value(4))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileDateDir").isString())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileName").isString())

        rm(newFilePath)
    }

    @Test
    @DisplayName("단건 조회")

    fun t3() {
        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.get("/api/v1/posts/9/genFiles/1")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("item"))
            .andExpect(MockMvcResultMatchers.status().isOk())

        val postGenFile = postService
            .findById(9)
            .get()
            .getGenFileById(1)
            .get()

        resultActions
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(postGenFile.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.createDate")
                    .value(Matchers.startsWith(postGenFile.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.modifyDate")
                    .value(Matchers.startsWith(postGenFile.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.postId").value(postGenFile.post.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.typeCode").value(postGenFile.typeCode.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileExtTypeCode").value(postGenFile.fileExtTypeCode))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileExtType2Code").value(postGenFile.fileExtType2Code))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileSize").value(postGenFile.fileSize))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileNo").value(postGenFile.fileNo))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileExt").value(postGenFile.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileDateDir").value(postGenFile.fileDateDir))
            .andExpect(MockMvcResultMatchers.jsonPath("$.originalFileName").value(postGenFile.originalFileName))
            .andExpect(MockMvcResultMatchers.jsonPath("$.downloadUrl").value(postGenFile.downloadUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.publicUrl").value(postGenFile.publicUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.fileName").value(postGenFile.fileName))
    }

    @Test
    @DisplayName("새 파일 등록(다건)")
    @WithUserDetails("user4")
    fun t4() {
        val newFilePath1 = SampleResource.IMG_JPG_SAMPLE1.makeCopy()
        val newFilePath2 = SampleResource.IMG_JPG_SAMPLE2.makeCopy()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.attachment)
                    .file(
                        MockMultipartFile(
                            "files",
                            SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                            FileInputStream(newFilePath1)
                        )
                    )
                    .file(
                        MockMultipartFile(
                            "files",
                            SampleResource.IMG_JPG_SAMPLE2.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE2.getContentType(),
                            FileInputStream(newFilePath2)
                        )
                    )
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("makeNewItems"))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("201-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("2개의 파일이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].createDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].modifyDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].postId").value(9))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].typeCode").value(PostGenFile.TypeCode.attachment.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileNo").value(4))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileDateDir").isString())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[0].originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].fileName").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].createDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].modifyDate").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].postId").value(9))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].typeCode").value(PostGenFile.TypeCode.attachment.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[1].fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE2.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[1].fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE2.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileSize").isNumber())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileNo").value(5))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[1].fileExt").value(SampleResource.IMG_JPG_SAMPLE2.fileExt)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileDateDir").isString())
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data[1].originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE2.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].downloadUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].publicUrl").isString())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].fileName").isString())

        rm(newFilePath1)
        rm(newFilePath2)
    }

    @Test
    @DisplayName("파일 삭제")
    @WithUserDetails("user4")
    fun t5() {
        val postGenFile = postService
            .findById(9)
            .get()
            .getGenFileById(1)
            .get()

        val originFilePath = postGenFile.filePath
        val copyFilePath = getTempDirPath() + "/copy_" + postGenFile.fileName
        copy(originFilePath, copyFilePath)

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.delete("/api/v1/posts/9/genFiles/1")
            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("delete"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${postGenFile.id}번 파일이 삭제되었습니다."))

        mv(copyFilePath, originFilePath)
    }

    @Test
    @DisplayName("파일 수정, with id")
    @WithUserDetails("user4")
    fun t6() {
        val postGenFile = postService
            .findById(9)
            .get()
            .getGenFileById(1)
            .get()

        val originFilePath = postGenFile.filePath
        val copyFilePath = getTempDirPath() + "/copy_" + postGenFile.fileName
        copy(originFilePath, copyFilePath)

        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/9/genFiles/1")
                    .file(
                        MockMultipartFile(
                            "file",
                            SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                            FileInputStream(newFilePath)
                        )
                    )
                    .with { request: MockHttpServletRequest ->
                        request.method = "PUT"
                        request
                    }

            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${postGenFile.id}번 파일이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(postGenFile.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(postGenFile.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(postGenFile.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(postGenFile.post.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.typeCode").value(postGenFile.typeCode.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileSize").value(postGenFile.fileSize))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileNo").value(postGenFile.fileNo))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileDateDir").value(postGenFile.fileDateDir))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.downloadUrl").value(postGenFile.downloadUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.publicUrl").value(postGenFile.publicUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileName").value(postGenFile.fileName))

        mv(copyFilePath, originFilePath)
    }

    @Test
    @DisplayName("파일 수정, with typeCode And fileNo")
    @WithUserDetails("user4")
    fun t7() {
        val postGenFile = postService
            .findById(9)
            .get()
            .getGenFileByTypeCodeAndFileNo(PostGenFile.TypeCode.thumbnail, 1)
            .get()

        val originFilePath = postGenFile.filePath
        val copyFilePath = getTempDirPath() + "/copy_" + postGenFile.fileName
        copy(originFilePath, copyFilePath)

        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/9/genFiles/" + PostGenFile.TypeCode.thumbnail + "/1")
                    .file(
                        MockMultipartFile(
                            "file",
                            SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                            FileInputStream(newFilePath)
                        )
                    )
                    .with { request: MockHttpServletRequest ->
                        request.method = "PUT"
                        request
                    }

            )
            .andDo(MockMvcResultHandlers.print())

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${postGenFile.id}번 파일이 수정되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(postGenFile.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(postGenFile.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(postGenFile.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(postGenFile.post.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.typeCode").value(postGenFile.typeCode.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileSize").value(postGenFile.fileSize))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileNo").value(postGenFile.fileNo))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileDateDir").value(postGenFile.fileDateDir))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.downloadUrl").value(postGenFile.downloadUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.publicUrl").value(postGenFile.publicUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileName").value(postGenFile.fileName))

        mv(copyFilePath, originFilePath)
    }

    @Test
    @DisplayName("썸네일 이미지가 등록되면 해당 글에서도 직접 참조가 가능해야 한다.")
    @WithUserDetails("user4")
    fun t8() {
        val newFilePath = SampleResource.IMG_JPG_SAMPLE1.makeCopy()

        val resultActions = mvc
            .perform(
                MockMvcRequestBuilders.multipart("/api/v1/posts/5/genFiles/" + PostGenFile.TypeCode.thumbnail + "/1")
                    .file(
                        MockMultipartFile(
                            "file",
                            SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName(),
                            SampleResource.IMG_JPG_SAMPLE1.getContentType(),
                            FileInputStream(newFilePath)
                        )
                    )
                    .with { request: MockHttpServletRequest ->
                        request.method = "PUT"
                        request
                    }

            )
            .andDo(MockMvcResultHandlers.print())

        val postGenFile = postService
            .findById(5)
            .get()
            .getGenFileByTypeCodeAndFileNo(PostGenFile.TypeCode.thumbnail, 1)
            .get()

        resultActions
            .andExpect(MockMvcResultMatchers.handler().handlerType(ApiV1PostGenFileController::class.java))
            .andExpect(MockMvcResultMatchers.handler().methodName("modify"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("200-1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.msg").value("${postGenFile.id}번 파일이 생성되었습니다."))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(postGenFile.id))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.createDate")
                    .value(Matchers.startsWith(postGenFile.createDate.toString().substring(0, 20)))
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.modifyDate")
                    .value(Matchers.startsWith(postGenFile.modifyDate.toString().substring(0, 20)))
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.postId").value(postGenFile.post.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.typeCode").value(postGenFile.typeCode.name))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtTypeCode")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtTypeCode)
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.fileExtType2Code")
                    .value(SampleResource.IMG_JPG_SAMPLE1.fileExtType2Code)
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileSize").value(postGenFile.fileSize))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileNo").value(postGenFile.fileNo))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileExt").value(SampleResource.IMG_JPG_SAMPLE1.fileExt))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileDateDir").value(postGenFile.fileDateDir))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.data.originalFileName")
                    .value(SampleResource.IMG_JPG_SAMPLE1.getOriginalFileName())
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.downloadUrl").value(postGenFile.downloadUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.publicUrl").value(postGenFile.publicUrl))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.fileName").value(postGenFile.fileName))

        Assertions.assertThat(postGenFile.post.thumbnailGenFile)
            .isEqualTo(postGenFile)

        Assertions.assertThat(postGenFile.post.thumbnailImgUrlOrDefault)
            .isEqualTo(postGenFile.publicUrl)

        rm(postGenFile.filePath)
    }
}