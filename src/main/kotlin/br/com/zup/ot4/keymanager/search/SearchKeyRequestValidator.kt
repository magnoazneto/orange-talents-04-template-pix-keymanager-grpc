package br.com.zup.ot4.keymanager.search

import br.com.zup.ot4.SearchKeyRequest
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.shared.exceptions.PixKeyNotFoundException
import io.micronaut.http.HttpStatus
import java.util.*

fun SearchKeyRequest.validatePixData(pixKeyRepository: PixKeyRepository, bcbClient: BcbClient): PixKey {
    require(!this.pixData.externalClientId.isNullOrBlank()){ "Identificador do cliente não pode ser vazio ou nulo"}
    require(!this.pixData.pixId.isNullOrBlank()){ "Pix id não pode ser vazia ou nula"}
    val possiblePixKey = pixKeyRepository.findByUuid(UUID.fromString(pixData.pixId))
    if(possiblePixKey.isEmpty)
        throw PixKeyNotFoundException("Chave pix não encontrada para esse id")

    val pixKey = possiblePixKey.get()
    check(pixKey.accountData.holderId == this.pixData.externalClientId) { "Chave só pode ser consultada pelo proprietário" }

    val responseBcb = bcbClient.searchPixKey(pixKey.key)
    check(responseBcb.status != HttpStatus.NOT_FOUND) { "Chave não encontrada no BCB" }
    check(responseBcb.status == HttpStatus.OK) { "Erro ao buscar chave no BCB" }

    return pixKey
}

fun SearchKeyRequest.validateKey(pixKeyRepository: PixKeyRepository): Optional<PixKey> {
    require(!this.key.isNullOrBlank()) { "Chave Pix não deve ser nula ou vazia" }
    require(this.key.length <= 77) { "Chave Pix deve possuir tamanho máximo de 77 caracteres" }

    return pixKeyRepository.findByKey(this.key)
}