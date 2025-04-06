package com.back.domain.post.genFile.controller

import com.back.domain.post.post.service.PostService
import com.back.standard.util.Ut.url.encode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.FileInputStream

@Controller
@RequestMapping("/post/genFile")
@Tag(name = "PostGenFileController", description = "파일 다운로드 등 다양한 기능 제공")
class PostGenFileController(
    private val postService: PostService
) {
    @GetMapping("/download/{postId}/{fileName}")
    @Operation(summary = "파일 다운로드")
    fun download(
        @PathVariable postId: Long,
        @PathVariable fileName: String,
        request: HttpServletRequest
    ): ResponseEntity<Resource> {
        val post = postService.findById(postId).get()

        val genFile = post.genFiles
            .stream()
            .filter { it.fileName == fileName }
            .findFirst()
            .get()

        val filePath = genFile.filePath

        val resource: Resource = InputStreamResource(FileInputStream(filePath))

        var contentType = request.servletContext.getMimeType(filePath)

        if (contentType == null) contentType = "application/octet-stream"

        val downloadFileName = encode(genFile.originalFileName).replace("%20", " ")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$downloadFileName\"")
            .contentType(MediaType.parseMediaType(contentType)).body(resource)
    }
}