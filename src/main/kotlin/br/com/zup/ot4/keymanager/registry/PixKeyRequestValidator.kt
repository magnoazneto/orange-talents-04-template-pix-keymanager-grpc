package br.com.zup.ot4.keymanager.registry

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.keymanager.registry.validate
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.shared.exceptions.ExistingPixKeyException
import com.sun.xml.fastinfoset.algorithm.UUIDEncodingAlgorithm
import io.micronaut.http.HttpStatus
import java.util.*

fun PixKeyRequest.validateRequestParams(
    pixKeyRepository: PixKeyRepository
) {
    keyType.validate(pixKey)
    UUID.fromString(externalClientId) // just to throw IllegalArgument on invalid UUID

    require(accountType != AccountType.UNKNOW_ACCOUNT_TYPE) { "Tipo de Conta desconhecido" }
    if(pixKeyRepository.existsByKey(pixKey)) throw ExistingPixKeyException("Chave PIX igual já cadastrada")

}