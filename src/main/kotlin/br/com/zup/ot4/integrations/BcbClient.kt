package br.com.zup.ot4.integrations

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import jdk.jfr.ContentType
import java.awt.PageAttributes

@Client(value = "\${bcp.host}")
interface BcbClient {

    @Post(
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun registerPixKey(@Body pixKeyBcbRequest: PixKeyBcbRequest)
}