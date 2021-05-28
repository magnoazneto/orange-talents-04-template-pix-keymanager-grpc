package br.com.zup.ot4.integracoes.itau

import br.com.zup.ot4.cliente.Cliente
import br.com.zup.ot4.cliente.Instituicao
import javax.persistence.Embedded

data class ClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    @Embedded val instituicao: Instituicao
) {

    fun toClient(): Cliente {
        return Cliente(id, nome, cpf, instituicao)
    }
}
