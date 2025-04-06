package com.back.domain.post.post.entity

import com.back.domain.member.member.entity.Member
import com.back.domain.post.comment.entity.PostComment
import com.back.domain.post.genFile.entity.PostGenFile
import com.back.global.exceptions.ServiceException
import com.back.global.jpa.entity.BaseTime
import com.back.global.rsData.RsData
import com.back.standard.base.Empty
import com.back.standard.util.Ut
import jakarta.persistence.*

import java.util.*
import java.util.stream.Collectors

@Entity
class Post : BaseTime {
    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: Member

    @Column(length = 100)
    lateinit var title: String

    @Column(columnDefinition = "TEXT")
    lateinit var content: String

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val comments: MutableList<PostComment> = mutableListOf()

    @OneToMany(mappedBy = "post", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val genFiles: MutableList<PostGenFile> = mutableListOf()

    // OneToOne 은 레이지 로딩이 안된다.
    @ManyToOne(fetch = FetchType.LAZY)
    var thumbnailGenFile: PostGenFile? = null

    var published: Boolean = false

    var listed: Boolean = false

    fun isPublished(): Boolean {
        return published
    }

    fun isListed(): Boolean {
        return listed
    }

    constructor(author: Member, title: String, content: String, published: Boolean, listed: Boolean) {
        this.author = author
        this.title = title
        this.content = content
        this.published = published
        this.listed = listed
    }

    fun addComment(author: Member, content: String): PostComment {
        val comment = PostComment(
            this,
            author,
            content
        )

        comments.add(comment)

        return comment
    }

    val commentsByOrderByIdDesc: List<PostComment>
        get() = comments.reversed()

    fun getCommentById(commentId: Long): Optional<PostComment> {
        return comments.stream()
            .filter { comment -> comment.id == commentId }
            .findFirst()
    }

    fun removeComment(postComment: PostComment) {
        comments.remove(postComment)
    }

    fun getCheckActorCanDeleteRs(actor: Member?): RsData<Empty> {
        if (actor == null) return RsData("401-1", "로그인 후 이용해주세요.")

        if (actor.isAdmin) return RsData.OK

        if (actor == author) return RsData.OK

        return RsData("403-1", "작성자만 글을 삭제할 수 있습니다.")
    }

    fun checkActorCanDelete(actor: Member?) {
        Optional.of(
            getCheckActorCanDeleteRs(actor)
        )
            .filter { rsData -> rsData.isFail }
            .ifPresent { rsData ->
                throw ServiceException(rsData.resultCode, rsData.msg)
            }
    }

    fun getCheckActorCanModifyRs(actor: Member?): RsData<Empty> {
        if (actor == null) return RsData("401-1", "로그인 후 이용해주세요.")

        if (actor == author) return RsData.OK

        return RsData("403-1", "작성자만 글을 수정할 수 있습니다.")
    }

    fun checkActorCanModify(actor: Member?) {
        Optional.of(
            getCheckActorCanModifyRs(actor)
        )
            .filter { rsData -> rsData.isFail }
            .ifPresent { rsData ->
                throw ServiceException(rsData.resultCode, rsData.msg)
            }
    }

    fun getCheckActorCanReadRs(actor: Member?): RsData<Empty> {
        if (actor == null) return RsData("401-1", "로그인 후 이용해주세요.")

        if (actor.isAdmin) return RsData.OK

        if (actor == author) return RsData.OK

        return RsData("403-1", "비공개글은 작성자만 볼 수 있습니다.")
    }

    fun checkActorCanRead(actor: Member?) {
        Optional.of(
            getCheckActorCanReadRs(actor)
        )
            .filter { rsData -> rsData.isFail }
            .ifPresent { rsData ->
                throw ServiceException(rsData.resultCode, rsData.msg)
            }
    }

    private fun processGenFile(
        oldPostGenFile: PostGenFile?,
        typeCode: PostGenFile.TypeCode,
        fileNo: Int,
        filePath: String
    ): PostGenFile {
        val isModify = oldPostGenFile != null
        val originalFileName = Ut.file.getOriginalFileName(filePath)
        val metadataStrFromFileName = Ut.file.getMetadataStrFromFileName(filePath)
        val fileExt = Ut.file.getFileExt(filePath)
        val fileExtTypeCode = Ut.file.getFileExtTypeCodeFromFileExt(fileExt)
        val fileExtType2Code = Ut.file.getFileExtType2CodeFromFileExt(fileExt)

        var metadataStr = Ut.file.getMetadata(filePath).entries.stream()
            .map { it.key + "=" + it.value }
            .collect(Collectors.joining("&"))

        if (Ut.str.isNotBlank(metadataStrFromFileName)) {
            metadataStr = if (Ut.str.isNotBlank(metadataStr))
                "$metadataStr&$metadataStrFromFileName"
            else
                metadataStrFromFileName
        }

        val fileName = if (isModify) Ut.file.withNewExt(oldPostGenFile!!.fileName, fileExt) else UUID.randomUUID()
            .toString() + "." + fileExt
        val fileSize = Ut.file.getFileSize(filePath)
        val actualFileNo = if (fileNo == 0) getNextGenFileNo(typeCode) else fileNo

        val genFile = if (isModify) oldPostGenFile!! else PostGenFile(
            this,
            typeCode,
            actualFileNo
        )

        genFile.originalFileName = originalFileName
        genFile.metadata = metadataStr
        genFile.fileDateDir = Ut.date.getCurrentDateFormatted("yyyy_MM_dd")
        genFile.fileExt = fileExt
        genFile.fileExtTypeCode = fileExtTypeCode
        genFile.fileExtType2Code = fileExtType2Code
        genFile.fileName = fileName
        genFile.fileSize = fileSize

        if (!isModify) genFiles.add(genFile)

        if (isModify) {
            Ut.file.rm(genFile.filePath)
        }

        Ut.file.mv(filePath, genFile.filePath)

        return genFile
    }

    fun addGenFile(typeCode: PostGenFile.TypeCode, filePath: String): PostGenFile {
        return addGenFile(typeCode, 0, filePath)
    }

    private fun addGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile {
        return processGenFile(null, typeCode, fileNo, filePath)
    }

    private fun getNextGenFileNo(typeCode: PostGenFile.TypeCode): Int {
        return genFiles.stream()
            .filter { genFile -> genFile.typeCode == typeCode }
            .mapToInt { genFile -> genFile.fileNo }
            .max()
            .orElse(0) + 1
    }

    fun getGenFileById(id: Long): Optional<PostGenFile> {
        return genFiles.stream()
            .filter { genFile -> genFile.id == id }
            .findFirst()
    }

    fun getGenFileByTypeCodeAndFileNo(typeCode: PostGenFile.TypeCode, fileNo: Int): Optional<PostGenFile> {
        return genFiles.stream()
            .filter { genFile -> genFile.typeCode == typeCode }
            .filter { genFile -> genFile.fileNo == fileNo }
            .findFirst()
    }

    fun deleteGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int) {
        getGenFileByTypeCodeAndFileNo(typeCode, fileNo)
            .ifPresent { this.deleteGenFile(it) }
    }

    fun deleteGenFile(postGenFile: PostGenFile) {
        Ut.file.rm(postGenFile.filePath)
        genFiles.remove(postGenFile)
    }

    fun modifyGenFile(postGenFile: PostGenFile, filePath: String): PostGenFile {
        return processGenFile(postGenFile, postGenFile.typeCode, postGenFile.fileNo, filePath)
    }

    fun modifyGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile {
        val postGenFile = getGenFileByTypeCodeAndFileNo(
            typeCode,
            fileNo
        ).get()

        return modifyGenFile(postGenFile, filePath)
    }

    fun putGenFile(typeCode: PostGenFile.TypeCode, fileNo: Int, filePath: String): PostGenFile {
        val opPostGenFile = getGenFileByTypeCodeAndFileNo(
            typeCode,
            fileNo
        )

        return if (opPostGenFile.isPresent) {
            modifyGenFile(typeCode, fileNo, filePath)
        } else {
            addGenFile(typeCode, fileNo, filePath)
        }
    }

    fun checkActorCanMakeNewGenFile(actor: Member?) {
        Optional.of(
            getCheckActorCanMakeNewGenFileRs(actor)
        )
            .filter { rsData -> rsData.isFail }
            .ifPresent { rsData ->
                throw ServiceException(rsData.resultCode, rsData.msg)
            }
    }

    fun getCheckActorCanMakeNewGenFileRs(actor: Member?): RsData<Empty> {
        if (actor == null) return RsData("401-1", "로그인 후 이용해주세요.")

        if (actor == author) return RsData.OK

        return RsData("403-1", "작성자만 파일을 업로드할 수 있습니다.")
    }

    val isTemp: Boolean
        get() = !published && "임시글" == title

    val thumbnailImgUrlOrDefault: String
        get() = Optional.ofNullable(thumbnailGenFile)
            .map { it.publicUrl }
            .orElse("https://placehold.co/1200x1200?text=POST $id&darkInvertible=1")
}