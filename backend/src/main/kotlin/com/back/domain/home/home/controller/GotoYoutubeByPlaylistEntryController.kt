package com.back.domain.home.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/goto/youtube/{playlistCode}")
class GotoYoutubeByPlaylistEntryController(
) {
    @GetMapping("/{number}")
    @ResponseBody
    fun gotoYoutubeByNumber(
            @PathVariable playlistCode:String,
            @PathVariable number:Int
    ):

    String {
        return """
                    <html>
                        <head>
                            <meta http-equiv="refresh" content="3;url=https://www.youtube.com/playlist?list=$playlistCode">
                        </head>
                        <body>
                            <h1>Redirecting...</h1>
                            <h2><span style="color:red;">${number}번</span>째 영상을 확인해주세요.</h2>
                            <p>Redirecting to <a href="https://www.youtube.com/playlist?list=$playlistCode">https://www.youtube.com/playlist?list=$playlistCode</a></p>
                        </body>
                    </html>
                """.trimIndent()
    }
}