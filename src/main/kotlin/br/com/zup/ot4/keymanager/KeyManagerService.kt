package br.com.zup.ot4.keymanager

import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.RemoveKeyRequest
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.integrations.bcbTypes.DeletePixKeyBcbRequest
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbRequest
import br.com.zup.ot4.keymanager.registry.validateRequestParams
import br.com.zup.ot4.keymanager.remove.validate
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.shared.Transaction
import br.com.zup.ot4.shared.exceptions.ExistingPixKeyException
import io.micronaut.http.HttpStatus
import javax.inject.Inject
import javax.inject.Singleton

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

        val responseBcb = bcbClient.registerPixKey(PixKeyBcbRequest(request, accountDataResponse.body()!!))

        check(responseBcb.status == HttpStatus.CREATED) { "Não foi possível cadastrar chave no BCB" }

        println(responseBcb.body())
        val newPixKey = PixKey(
            key = responseBcb.body()!!.key,
            keyType = request.keyType,
            accountData = accountDataResponse.body()!!.toModel()
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
}