package br.com.zup.ot4.integracoes.itau

import br.com.zup.ot4.cliente.ClienteResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client(value = "\${erp-itau.host}")
interface ErpItauClient {

    @Get(value = "clientes/{clienteId}")
    fun consultaCliente(@PathVariable clienteId: String): HttpResponse<ClienteResponse>
}