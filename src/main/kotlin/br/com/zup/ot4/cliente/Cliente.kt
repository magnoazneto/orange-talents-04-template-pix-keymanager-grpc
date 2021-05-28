package br.com.zup.ot4.cliente

import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
class Cliente(
    val clienteId: String,
    val nomeCliente: String,
    val cpf: String,
    @Embedded val instituicao: Instituicao
) {
}