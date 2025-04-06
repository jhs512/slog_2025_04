package com.back.domain.base.genFile.genFile.entity

import com.back.global.app.AppConfig
import com.back.global.jpa.entity.BaseTime
import com.back.standard.util.Ut
import jakarta.persistence.MappedSuperclass


@MappedSuperclass
abstract class GenFile : BaseTime {
    var fileNo: Int = 0
    lateinit var originalFileName: String
    lateinit var metadata: String
    lateinit var fileDateDir: String
    lateinit var fileExt: String
    lateinit var fileExtTypeCode: String
    lateinit var fileExtType2Code: String
    lateinit var fileName: String
    var fileSize: Int = 0

    constructor(fileNo: Int) {
        this.fileNo = fileNo
    }

    override fun equals(other: Any?): Boolean {
        return other != null &&
                this::class == other::class && // javaClass 비교를 더 안전하게 변경
                other is GenFile &&
                fileNo == other.fileNo &&
                getOwnerModelId() == other.getOwnerModelId() &&
                getTypeCodeAsStr() == other.getTypeCodeAsStr()
    }

    override fun hashCode(): Int {
        return listOf(fileNo, getOwnerModelId(), getTypeCodeAsStr()).hashCode()
    }

    val filePath: String
        get() = AppConfig.getGenFileDirPath() + "/" + modelName + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName

    val ownerModelName: String
        get() = modelName.replace("GenFile", "")

    val downloadUrl: String
        get() = AppConfig.getSiteBackUrl() + "/" + ownerModelName + "/genFile/download/" + getOwnerModelId() + "/" + fileName

    val publicUrl: String
        get() = AppConfig.getSiteBackUrl() + "/gen/" + modelName + "/" + getTypeCodeAsStr() + "/" + fileDateDir + "/" + fileName + "?modifyDate=" + Ut.date.patternOf(
            modifyDate,
            "yyyy-MM-dd--HH-mm-ss"
        ) + "&" + metadata

    protected abstract fun getOwnerModelId(): Long
    protected abstract fun getTypeCodeAsStr(): String
}