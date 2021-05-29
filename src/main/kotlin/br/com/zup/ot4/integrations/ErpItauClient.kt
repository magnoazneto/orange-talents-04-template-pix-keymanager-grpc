package br.com.zup.ot4.integrations

import br.com.zup.ot4.account.AccountDataResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client(value = "\${erp-itau.host}")
interface ErpItauClient {

    @Get(value = "clientes/{clienteId}/contas")
    fun searchAccount(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<AccountDataResponse>
}