package br.com.zup.ot4.integrations

import br.com.zup.ot4.integrations.bcbTypes.DeletePixKeyBcbRequest
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbRequest
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client(value = "\${bcb.host}")
interface BcbClient {

    @Post(produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun registerPixKey(@Body pixKeyBcbRequest: PixKeyBcbRequest): HttpResponse<PixKeyBcbResponse>

    @Delete(value = "/{key}",produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun removePixKey(@PathVariable key: String, @Body removeKeyRequest: DeletePixKeyBcbRequest): HttpResponse<Any>
}