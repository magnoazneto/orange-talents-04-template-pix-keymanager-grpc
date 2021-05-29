package br.com.zup.ot4.registry.extensions

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.registry.ExistingPixKeyException
import io.micronaut.http.HttpStatus

fun PixKeyRequest.toValidPixKey(
    itauClient: ErpItauClient,
    pixKeyRepository: PixKeyRepository
) : PixKey {

    keyType.validate(pixKey)

    if(accountType == AccountType.UNKNOW_ACCOUNT_TYPE) throw IllegalArgumentException("Tipo de Conta desconhecido")
    if(pixKeyRepository.existsByKey(pixKey)) throw ExistingPixKeyException("Chave PIX igual já cadastrada")

    val response = itauClient.searchAccount(externalClientId, accountType.toString())
    if (response.status == HttpStatus.NOT_FOUND) throw IllegalArgumentException("Conta não encontrada no ERP")

    return PixKey(
        key = pixKey,
        keyType = keyType,
        accountData = response.body()!!.toModel()
    )
}