package br.com.zup.ot4.account

import br.com.zup.ot4.AccountType

data class AccountDataResponse(
    val tipo: AccountType,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular
){
    fun toModel(): AccountData {
        return AccountData(
            accountType = tipo,
            organizationName = instituicao.nome,
            ispb = instituicao.ispb,
            agency = agencia,
            accountNumber = numero,
            holderId = titular.id,
            holderName = titular.nome,
            holderCpf = titular.cpf
        )
    }
}


data class Instituicao(val nome: String, val ispb: String)

data class Titular(val id: String, val nome: String, val cpf: String)