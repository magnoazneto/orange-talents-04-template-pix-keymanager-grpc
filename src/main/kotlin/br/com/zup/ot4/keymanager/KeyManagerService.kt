package br.com.zup.ot4.keymanager

import br.com.zup.ot4.*
import br.com.zup.ot4.SearchKeyRequest.PixFilterCase.*
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.integrations.bcbTypes.DeletePixKeyBcbRequest
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbRequest
import br.com.zup.ot4.keymanager.registry.validateRequestParams
import br.com.zup.ot4.keymanager.remove.validate
import br.com.zup.ot4.keymanager.search.validateKey
import br.com.zup.ot4.keymanager.search.validatePixData
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.shared.Transaction
import io.micronaut.http.HttpStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.IllegalArgumentException

@Singleton
class KeyManagerService(
    @Inject val transaction: Transaction,
    @Inject val itauClient: ErpItauClient,
    @Inject val pixKeyRepository: PixKeyRepository,
    @Inject val bcbClient: BcbClient
) {

    fun register(request: PixKeyRequest): PixKey {
        request.validateRequestParams(pixKeyRepository)

        val accountDataResponse = itauClient.searchAccount(request.externalClientId, request.accountType.toString())
        check(accountDataResponse.status != HttpStatus.NOT_FOUND){ "Conta não encontrada no ERP" }
        check(accountDataResponse.status == HttpStatus.OK){ "Erro ao buscar dados da conta no ERP" }

        val responseBcb = bcbClient.registerPixKey(PixKeyBcbRequest(request, accountDataResponse.body()!!))

        check(responseBcb.status != HttpStatus.UNPROCESSABLE_ENTITY) { "Chave PIX já cadastrada no BCB" }
        check(responseBcb.status == HttpStatus.CREATED) { "Não foi possível cadastrar chave no BCB" }

        val newPixKey = PixKey(
            key = responseBcb.body()!!.key,
            keyType = request.keyType,
            accountData = accountDataResponse.body()!!.toModel(),
            createdAt = responseBcb.body()!!.createdAt
        )

        transaction.saveAndCommit(newPixKey)

        return newPixKey
    }

    fun remove(request: RemoveKeyRequest){
        val validatedPixKey = request.validate(pixKeyRepository)
        val bcbResponse = bcbClient.removePixKey(
            validatedPixKey.key, DeletePixKeyBcbRequest(
                validatedPixKey.key,
                validatedPixKey.accountData.ispb
            )
        )
        check(bcbResponse.status == HttpStatus.OK) { "Falha na remoção de chave no BCB" }
        transaction.exec { pixKeyRepository.deleteById(validatedPixKey.id!!) }
    }

    fun search(request: SearchKeyRequest): SearchKeyResponse {
        return when(request.pixFilterCase){
            PIXDATA -> searchByPixData(request)
            KEY -> searchByKey(request)
            else -> throw IllegalArgumentException("Critério de busca desconhecido")
        }
    }

    private fun searchByPixData(request: SearchKeyRequest): SearchKeyResponse {
        val pixKey = request.validatePixData(pixKeyRepository, bcbClient)
        return pixKey.toSearchKeyResponse()
    }

    private fun searchByKey(request: SearchKeyRequest): SearchKeyResponse{
        val possiblePixKey = request.validateKey(pixKeyRepository)
        return if(possiblePixKey.isPresent){
            val pixKey = possiblePixKey.get()
            pixKey.toSearchKeyResponse()
        } else {
            val responseBcb = bcbClient.searchPixKey(request.key)
            check(responseBcb.status != HttpStatus.NOT_FOUND) { "A chave ${request.key} não está registrada no Bacen nem no sistema interno"}
            check(responseBcb.status == HttpStatus.OK) { "Erro de comunicação com o Bacen" }

            val pixKeyBcb = responseBcb.body()!!
            pixKeyBcb.toSearchKeyResponse()
        }
    }
}