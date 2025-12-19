package com.back.global.webMvc

import com.back.global.app.AppConfig.Companion.getGenFileDirPath
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebMvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = File(getGenFileDirPath()).absolutePath

        registry.addResourceHandler("/gen/**")
            .addResourceLocations("file:///$absolutePath/")
    }
}