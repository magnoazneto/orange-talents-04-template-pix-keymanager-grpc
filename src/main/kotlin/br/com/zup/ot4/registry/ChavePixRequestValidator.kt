package br.com.zup.ot4.registry


import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import io.micronaut.http.HttpStatus

fun PixKeyRequest.toValidPixKey(
    itauClient: ErpItauClient,
    pixKeyRepository: PixKeyRepository
) : PixKey {
    when(keyType){
        KeyType.CPF -> {
            require(!pixKey.isNullOrBlank()) { "CPF deve ser preenchido"}
            require(pixKey.matches("[0-9]{11}".toRegex())) { "CPF deve ter formato válido" }
        }
        KeyType.PHONE_NUMBER -> {
            require(!pixKey.isNullOrBlank()) { "Celular deve ser preenchido"}
            require(pixKey.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) { "Celular deve ter formato válido"}
        }
        KeyType.EMAIL -> {
            require(!pixKey.isNullOrBlank()) { "Email deve ser preenchido"}
            require(pixKey.matches("^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+\\.?([a-z]+)?\$".toRegex())) { "Email precisa ser válido"}
        }
        KeyType.RANDOM_KEY -> {
            require(pixKey.isNullOrBlank()) { "Chave pix deve nao deve ser enviada para criacao de uma chave aleatoria"}
        }
        else -> throw IllegalArgumentException("Tipo de chave pix não reconhecido")
    }

    if(accountType == AccountType.UNKNOW_ACCOUNT_TYPE) throw IllegalArgumentException("Tipo de Conta desconhecido")
    if(pixKeyRepository.existsByKey(pixKey)) throw ChavePixExistenteException("Chave PIX igual já cadastrada")

    val response = itauClient.searchAccount(externalClientId, accountType.toString())
    if (response.status == HttpStatus.NOT_FOUND) {
        throw IllegalArgumentException("Conta não encontrada no ERP")
    }

    return PixKey(
        key = pixKey,
        keyType = keyType,
        accountData = response.body()!!.toModel()
    )
}