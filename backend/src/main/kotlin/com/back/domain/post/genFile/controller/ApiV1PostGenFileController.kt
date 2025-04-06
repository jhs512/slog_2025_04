package com.back.domain.post.genFile.controller

import com.back.domain.post.genFile.dto.PostGenFileDto
import com.back.domain.post.genFile.entity.PostGenFile
import com.back.domain.post.post.service.PostService
import com.back.global.app.AppConfig.Companion.getTempDirPath
import com.back.global.exceptions.ServiceException
import com.back.global.rq.Rq
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import com.back.standard.util.Ut.file.getFileExtTypeCodeFromFilePath
import com.back.standard.util.Ut.file.rm
import com.back.standard.util.Ut.file.toFile
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag

import org.springframework.http.MediaType
import org.springframework.lang.NonNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/posts/{postId}/genFiles")
@Tag(name = "ApiV1PostGenFileController", description = "API 글 파일 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1PostGenFileController(
    private val postService: PostService,
    private val rq: Rq
) {
    @PostMapping(value = ["/{typeCode}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "다건등록")
    @Transactional
    fun makeNewItems(
        @PathVariable postId: Long,
        @PathVariable typeCode: PostGenFile.TypeCode,
        @NonNull @RequestPart("files") files: Array<MultipartFile>
    ): RsData<List<PostGenFileDto>> {
        val actor = rq.actor!!

        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        post.checkActorCanMakeNewGenFile(actor)

        val postGenFiles: MutableList<PostGenFile> = ArrayList()

        for (file in files) {
            if (file.isEmpty) continue

            val filePath = toFile(file, getTempDirPath())

            postGenFiles.add(
                post.addGenFile(
                    typeCode,
                    filePath
                )
            )
        }

        postService.flush()

        return RsData(
            "201-1",
            "${postGenFiles.size}개의 파일이 생성되었습니다.",
            postGenFiles.stream().map { PostGenFileDto(it) }.toList()
        )
    }


    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "다건조회")
    fun items(
        @PathVariable postId: Long
    ): List<PostGenFileDto> {
        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        return post
            .genFiles
            .stream()
            .map { PostGenFileDto(it) }
            .toList()
    }


    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "단건조회")
    fun item(
        @PathVariable postId: Long,
        @PathVariable id: Long
    ): PostGenFileDto {
        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postGenFile = post.getGenFileById(id).orElseThrow {
            ServiceException(
                "404-2",
                "${id}번 파일은 존재하지 않습니다."
            )
        }

        return PostGenFileDto(postGenFile)
    }


    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "삭제")
    fun delete(
        @PathVariable postId: Long,
        @PathVariable id: Long
    ): RsData<Empty> {
        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postGenFile = post.getGenFileById(id).orElseThrow {
            ServiceException(
                "404-2",
                "${id}번 파일은 존재하지 않습니다."
            )
        }

        post.deleteGenFile(postGenFile)

        return RsData(
            "200-1",
            "${id}번 파일이 삭제되었습니다."
        )
    }


    @PutMapping(value = ["/{id}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    @Operation(summary = "수정")
    fun modify(
        @PathVariable postId: Long,
        @PathVariable id: Long,
        @NonNull @RequestPart("file") file: MultipartFile
    ): RsData<PostGenFileDto> {
        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val postGenFile = post.getGenFileById(id).orElseThrow {
            ServiceException(
                "404-2",
                "${id}번 파일은 존재하지 않습니다."
            )
        }

        val filePath = toFile(file, getTempDirPath())

        post.modifyGenFile(postGenFile, filePath)

        return RsData(
            "200-1",
            "${id}번 파일이 수정되었습니다.",
            PostGenFileDto(postGenFile)
        )
    }

    @PutMapping(value = ["/{typeCode}/{fileNo}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    @Operation(summary = "수정")
    fun modify(
        @PathVariable postId: Long,
        @PathVariable typeCode: PostGenFile.TypeCode,
        @PathVariable fileNo: Int,
        @NonNull @RequestPart("file") file: MultipartFile?,
        @RequestParam(defaultValue = "") metaStr: String
    ): RsData<PostGenFileDto> {
        if (typeCode == PostGenFile.TypeCode.thumbnail && fileNo > 1) {
            throw ServiceException("400-1", "썸네일은 1개만 등록할 수 있습니다.")
        }

        val post = postService.findById(postId).orElseThrow {
            ServiceException(
                "404-1",
                "${postId}번 글은 존재하지 않습니다."
            )
        }

        val filePath = toFile(
            file,
            getTempDirPath(),
            metaStr
        )

        if (typeCode == PostGenFile.TypeCode.thumbnail && getFileExtTypeCodeFromFilePath(filePath) != "img") {
            rm(filePath)

            throw ServiceException("400-2", "썸네일은 이미지 파일만 등록할 수 있습니다.")
        }

        val postGenFile = post.putGenFile(typeCode, fileNo, filePath)

        val justCreated = postGenFile.id == 0L

        if (typeCode == PostGenFile.TypeCode.thumbnail) {
            // 만약에 등록된게 썸네일 이라면
            // 해당 썸네일의 주인(글)에도 직접 참조를 넣는다.
            post.thumbnailGenFile = postGenFile
        }

        postService.flush()

        return RsData(
            "200-1",
            if (justCreated) "${postGenFile.id}번 파일이 생성되었습니다." else "${postGenFile.id}번 파일이 수정되었습니다.",
            PostGenFileDto(postGenFile)
        )
    }
}