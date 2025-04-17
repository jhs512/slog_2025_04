package com.back.domain.chat.message.controller

import com.back.global.security.SecurityUser
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/chat/rooms/{roomId}/messages")
@Tag(name = "ApiV1ChatMessageController", description = "API 채팅 메세지 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
class ApiV1ChatMessageController {

    @MessageMapping("/chat.rooms.{roomId}.messages.create")
    @SendTo("/topic/chat.rooms.{roomId}.messages")
    fun createChatMessage(
        chatMessageCreateDto: ChatMessageCreateDto,
        @DestinationVariable roomId: Long,
        authentication: Authentication,
        @AuthenticationPrincipal usernamePasswordAuthenticationToken: UsernamePasswordAuthenticationToken
    ): ChatMessageResultDto {
        val securityUser = usernamePasswordAuthenticationToken.principal as SecurityUser

        return ChatMessageResultDto(
            type = "CREATED",
            senderId = securityUser.id,
            message = chatMessageCreateDto.message
        )
    }
}

data class ChatMessageCreateDto(
    val message: String
)

data class ChatMessageResultDto(
    val type: String,
    val senderId: Long,
    val message: String
)