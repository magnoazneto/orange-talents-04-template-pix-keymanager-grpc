package br.com.zup.ot4.keymanager

import br.com.zup.ot4.*
import br.com.zup.ot4.shared.Transaction
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.keymanager.registry.toValidPixKey
import br.com.zup.ot4.keymanager.remove.validate
import br.com.zup.ot4.shared.errors.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class KeyManagerEndpoint(
    @Inject val transaction: Transaction,
    @Inject val itauClient: ErpItauClient,
    @Inject val pixKeyRepository: PixKeyRepository
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun register(
        request: PixKeyRequest,
        responseObserver: StreamObserver<PixKeyResponse>
    ) {
        val chavePix = request.toValidPixKey(itauClient, pixKeyRepository)
        transaction.saveAndCommit(chavePix)

        with(responseObserver){
            onNext(PixKeyResponse.newBuilder().setPixId(chavePix.uuid.toString()).build())
            onCompleted()
        }
    }

    override fun remove(
        request: RemoveKeyRequest,
        responseObserver: StreamObserver<RemoveKeyResponse>
    ) {
        val validatedPixKey = request.validate(pixKeyRepository)
        transaction.exec { pixKeyRepository.deleteById(validatedPixKey.id!!) }

        responseObserver.onNext(RemoveKeyResponse.newBuilder().setSuccess(true).build())
        responseObserver.onCompleted()
    }
}
