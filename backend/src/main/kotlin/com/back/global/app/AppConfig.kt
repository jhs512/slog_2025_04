package com.back.global.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.tika.Tika
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class AppConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    companion object {
        private lateinit var environment: Environment
        private lateinit var objectMapper: ObjectMapper
        private lateinit var tika: Tika
        private lateinit var siteCookieDomain: String
        private lateinit var siteFrontUrl: String
        private lateinit var siteBackUrl: String
        private lateinit var genFileDirPath: String
        private lateinit var springServletMultipartMaxFileSize: String
        private lateinit var springServletMultipartMaxRequestSize: String
        private var resourcesSampleDirPath: String? = null

        @JvmStatic
        fun isProd(): Boolean = environment.matchesProfiles("prod")

        @JvmStatic
        fun isDev(): Boolean = environment.matchesProfiles("dev")

        @JvmStatic
        fun isTest(): Boolean = environment.matchesProfiles("test")

        @JvmStatic
        fun isNotProd(): Boolean = !isProd()

        @JvmStatic
        fun getObjectMapper(): ObjectMapper = objectMapper

        @JvmStatic
        fun getTika(): Tika = tika

        @JvmStatic
        fun getSiteCookieDomain(): String = siteCookieDomain

        @JvmStatic
        fun getSiteFrontUrl(): String = siteFrontUrl

        @JvmStatic
        fun getSiteBackUrl(): String = siteBackUrl

        @JvmStatic
        fun getGenFileDirPath(): String = genFileDirPath

        @JvmStatic
        fun getSpringServletMultipartMaxFileSize(): String = springServletMultipartMaxFileSize

        @JvmStatic
        fun getSpringServletMultipartMaxRequestSize(): String = springServletMultipartMaxRequestSize

        @JvmStatic
        fun getTempDirPath(): String = System.getProperty("java.io.tmpdir")

        @JvmStatic
        fun getResourcesSampleDirPath(): String {
            if (resourcesSampleDirPath == null) {
                val resource = ClassPathResource("sample")

                resourcesSampleDirPath = if (resource.exists()) {
                    resource.file.absolutePath
                } else {
                    "src/main/resources/sample"
                }
            }

            return resourcesSampleDirPath!!
        }
    }

    @Autowired
    fun setEnvironment(environment: Environment) {
        Companion.environment = environment
    }

    @Autowired
    fun setObjectMapper(objectMapper: ObjectMapper) {
        Companion.objectMapper = objectMapper
    }

    @Autowired
    fun setTika(tika: Tika) {
        Companion.tika = tika
    }

    @Value("\${custom.site.cookieDomain}")
    fun setSiteCookieDomain(siteCookieDomain: String) {
        Companion.siteCookieDomain = siteCookieDomain
    }

    @Value("\${custom.site.frontUrl}")
    fun setSiteFrontUrl(siteFrontUrl: String) {
        Companion.siteFrontUrl = siteFrontUrl
    }

    @Value("\${custom.site.backUrl}")
    fun setSiteBackUrl(siteBackUrl: String) {
        Companion.siteBackUrl = siteBackUrl
    }

    @Value("\${custom.genFile.dirPath}")
    fun setGenFileDirPath(genFileDirPath: String) {
        Companion.genFileDirPath = genFileDirPath
    }

    @Value("\${spring.servlet.multipart.max-file-size}")
    fun setSpringServletMultipartMaxFileSize(springServletMultipartMaxFileSize: String) {
        Companion.springServletMultipartMaxFileSize = springServletMultipartMaxFileSize
    }

    @Value("\${spring.servlet.multipart.max-request-size}")
    fun setSpringServletMultipartMaxRequestSize(springServletMultipartMaxRequestSize: String) {
        Companion.springServletMultipartMaxRequestSize = springServletMultipartMaxRequestSize
    }
}