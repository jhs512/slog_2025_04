package com.back.standard.util

import com.back.global.app.AppConfig
import com.back.standard.extensions.base64Decode
import com.back.standard.extensions.base64Encode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.imageio.ImageIO

object Ut {
    object str {
        @JvmStatic
        fun isBlank(str: String?): Boolean {
            return str == null || str.trim().isEmpty()
        }

        @JvmStatic
        fun lcfirst(str: String): String {
            return str[0].lowercaseChar() + str.substring(1)
        }

        @JvmStatic
        fun isNotBlank(str: String?): Boolean {
            return !isBlank(str)
        }
    }

    object json {
        private val om: ObjectMapper = AppConfig.getObjectMapper()

        @JvmStatic
        fun toString(obj: Any): String {
            return om.writeValueAsString(obj)
        }
    }

    object jwt {
        @JvmStatic
        fun toString(secret: String, expireSeconds: Long, body: Map<String, Any>): String {
            val issuedAt = Date()
            val expiration = Date(issuedAt.time + 1000L * expireSeconds)

            val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return Jwts.builder()
                .claims(body)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact()
        }

        @JvmStatic
        fun isValid(secret: String, jwtStr: String): Boolean {
            val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return try {
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                true
            } catch (e: Exception) {
                false
            }
        }

        @JvmStatic
        fun payload(secret: String, jwtStr: String): Map<String, Any>? {
            val secretKey: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

            return try {
                @Suppress("UNCHECKED_CAST")
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtStr)
                    .payload as Map<String, Any>?
            } catch (e: Exception) {
                null
            }
        }
    }

    object file {
        private const val ORIGINAL_FILE_NAME_SEPARATOR = "--originalFileName_"
        const val META_STR_SEPARATOR = "_metaStr--"

        private val MIME_TYPE_MAP: LinkedHashMap<String, String> = linkedMapOf(
            "application/json" to "json",
            "text/plain" to "txt",
            "text/html" to "html",
            "text/css" to "css",
            "application/javascript" to "js",
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/gif" to "gif",
            "image/webp" to "webp",
            "image/svg+xml" to "svg",
            "application/pdf" to "pdf",
            "application/xml" to "xml",
            "application/zip" to "zip",
            "application/gzip" to "gz",
            "application/x-tar" to "tar",
            "application/x-7z-compressed" to "7z",
            "application/vnd.rar" to "rar",
            "audio/mpeg" to "mp3",
            "audio/mp4" to "m4a",
            "audio/x-m4a" to "m4a",
            "audio/wav" to "wav",
            "video/quicktime" to "mov",
            "video/mp4" to "mp4",
            "video/webm" to "webm",
            "video/x-msvideo" to "avi"
        )

        @JvmStatic
        @JvmOverloads
        fun downloadByHttp(url: String, dirPath: String, uniqueFilename: Boolean = true): String {
            val client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()

            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build()

            val tempFilePath = "$dirPath/${UUID.randomUUID()}.tmp"

            mkdir(dirPath)

            // 실제 파일 다운로드
            val response = client.send(
                request,
                HttpResponse.BodyHandlers.ofFile(Path.of(tempFilePath))
            )

            // 파일 확장자 추출
            var extension = getExtensionFromResponse(response)

            if (extension == "tmp") {
                extension = getExtensionByTika(tempFilePath)
            }

            // 파일명 추출
            val filename = getFilenameWithoutExtFromUrl(url)

            val finalFilename = if (uniqueFilename)
                "${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR$filename"
            else
                filename

            val newFilePath = "$dirPath/$finalFilename.$extension"

            mv(tempFilePath, newFilePath)

            return newFilePath
        }

        @JvmStatic
        fun getExtensionByTika(filePath: String): String {
            val mineType = AppConfig.getTika().detect(filePath)
            return MIME_TYPE_MAP.getOrDefault(mineType, "tmp")
        }

        @JvmStatic
        fun mv(oldFilePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.move(
                Path.of(oldFilePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        @JvmStatic
        fun mkdir(dirPath: String) {
            val path = Path.of(dirPath)

            if (Files.exists(path)) return

            Files.createDirectories(path)
        }

        private fun getFilenameWithoutExtFromUrl(url: String): String {
            return try {
                val path = URI(url).path
                val filename = Path.of(path).fileName.toString()
                // 확장자 제거
                if (filename.contains("."))
                    filename.substring(0, filename.lastIndexOf('.'))
                else
                    filename
            } catch (e: URISyntaxException) {
                // URL에서 파일명을 추출할 수 없는 경우 타임스탬프 사용
                "download_${System.currentTimeMillis()}"
            }
        }

        private fun getExtensionFromResponse(response: HttpResponse<*>): String {
            return response.headers()
                .firstValue("Content-Type")
                .map { MIME_TYPE_MAP.getOrDefault(it, "tmp") }
                .orElse("tmp")
        }

        @JvmStatic
        fun delete(filePath: String) {
            Files.deleteIfExists(Path.of(filePath))
        }

        @JvmStatic
        fun getOriginalFileName(filePath: String): String {
            val originalFileName = Path.of(filePath).fileName.toString()

            return if (originalFileName.contains(ORIGINAL_FILE_NAME_SEPARATOR))
                originalFileName.substring(originalFileName.indexOf(ORIGINAL_FILE_NAME_SEPARATOR) + ORIGINAL_FILE_NAME_SEPARATOR.length).base64Decode()
            else
                originalFileName
        }

        @JvmStatic
        fun getFileExt(filePath: String): String {
            val filename = getOriginalFileName(filePath)

            return if (filename.contains("."))
                filename.substring(filename.lastIndexOf('.') + 1)
            else
                ""
        }

        @JvmStatic
        fun getFileSize(filePath: String): Int {
            return Files.size(Path.of(filePath)).toInt()
        }

        @JvmStatic
        fun rm(filePath: String) {
            val path = Path.of(filePath)

            if (!Files.exists(path)) return

            if (Files.isRegularFile(path)) {
                // 파일이면 바로 삭제
                Files.delete(path)
            } else {
                // 디렉터리면 내부 파일들 삭제 후 디렉터리 삭제
                Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
                    @Throws(IOException::class)
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        Files.delete(file)
                        return FileVisitResult.CONTINUE
                    }

                    @Throws(IOException::class)
                    override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                        Files.delete(dir)
                        return FileVisitResult.CONTINUE
                    }
                })
            }
        }

        @JvmStatic
        fun getFileExtTypeCodeFromFileExt(ext: String): String {
            return when (ext) {
                "jpeg", "jpg", "gif", "png", "svg", "webp" -> "img"
                "mp4", "avi", "mov" -> "video"
                "mp3", "m4a" -> "audio"
                else -> "etc"
            }
        }

        @JvmStatic
        fun getFileExtType2CodeFromFileExt(ext: String): String {
            return when (ext) {
                "jpeg", "jpg" -> "jpg"
                else -> ext
            }
        }

        @JvmStatic
        fun getMetadata(filePath: String): Map<String, Any> {
            val ext = getFileExt(filePath)
            val fileExtTypeCode = getFileExtTypeCodeFromFileExt(ext)

            return if (fileExtTypeCode == "img") getImgMetadata(filePath) else emptyMap()
        }

        private fun getImgMetadata(filePath: String): Map<String, Any> {
            val metadata = LinkedHashMap<String, Any>()

            try {
                ImageIO.createImageInputStream(File(filePath)).use { input ->
                    val readers = ImageIO.getImageReaders(input)

                    if (!readers.hasNext()) {
                        throw IOException("지원되지 않는 이미지 형식: $filePath")
                    }

                    val reader = readers.next()
                    reader.input = input

                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)

                    metadata["width"] = width
                    metadata["height"] = height

                    reader.dispose()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return metadata
        }

        @JvmStatic
        @JvmOverloads
        fun toFile(multipartFile: MultipartFile?, dirPath: String, metaStr: String = ""): String {
            if (multipartFile == null) return ""
            if (multipartFile.isEmpty) return ""

            val fileName = if (str.isBlank(metaStr))
                "${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR${multipartFile.originalFilename!!.base64Encode()}"
            else
                "$metaStr$META_STR_SEPARATOR${UUID.randomUUID()}$ORIGINAL_FILE_NAME_SEPARATOR${multipartFile.originalFilename!!.base64Encode()}"

            val filePath = "$dirPath/$fileName"

            mkdir(dirPath)
            multipartFile.transferTo(File(filePath))

            return filePath
        }

        @JvmStatic
        fun copy(filePath: String, newFilePath: String) {
            mkdir(Paths.get(newFilePath).parent.toString())

            Files.copy(
                Path.of(filePath),
                Path.of(newFilePath),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        @JvmStatic
        fun getContentType(fileExt: String): String {
            return MIME_TYPE_MAP.entries
                .find { it.value == fileExt }
                ?.key ?: ""
        }

        @JvmStatic
        fun withNewExt(fileName: String, fileExt: String): String {
            return if (fileName.contains("."))
                fileName.substring(0, fileName.lastIndexOf('.') + 1) + fileExt
            else
                "$fileName.$fileExt"
        }

        @JvmStatic
        fun getFileExtTypeCodeFromFilePath(filePath: String): String {
            val ext = getFileExt(filePath)
            return getFileExtTypeCodeFromFileExt(ext)
        }

        @JvmStatic
        fun getMetadataStrFromFileName(filePath: String): String {
            val fileName = Path.of(filePath).fileName.toString()
            return if (fileName.contains(META_STR_SEPARATOR))
                fileName.substring(0, fileName.indexOf(META_STR_SEPARATOR))
            else
                ""
        }
    }

    object cmd {
        @JvmStatic
        fun runAsync(cmd: String) {
            Thread { run(cmd) }.start()
        }

        @JvmStatic
        fun run(cmd: String) {
            try {
                val processBuilder = ProcessBuilder("bash", "-c", cmd)
                val process = processBuilder.start()
                process.waitFor(1, TimeUnit.MINUTES)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    object date {
        @JvmStatic
        fun getCurrentDateFormatted(pattern: String): String {
            val simpleDateFormat = SimpleDateFormat(pattern)
            return simpleDateFormat.format(Date())
        }

        @JvmStatic
        fun patternOf(date: LocalDateTime, pattern: String): String {
            return date.format(java.time.format.DateTimeFormatter.ofPattern(pattern))
        }
    }

    object url {
        @JvmStatic
        fun encode(str: String): String {
            return try {
                URLEncoder.encode(str, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                str
            }
        }

        @JvmStatic
        fun removeDomain(url: String): String {
            return url.replaceFirst("https?://[^/]+".toRegex(), "")
        }
    }
}