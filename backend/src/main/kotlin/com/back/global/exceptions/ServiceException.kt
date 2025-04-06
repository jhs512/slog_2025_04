package com.back.global.exceptions

import com.back.global.rsData.RsData
import com.back.standard.base.Empty

class ServiceException(private val resultCode: String, private val msg: String) : RuntimeException(
    "$resultCode : $msg"
) {
    val rsData: RsData<Empty>
        get() = RsData(resultCode, msg)
}
