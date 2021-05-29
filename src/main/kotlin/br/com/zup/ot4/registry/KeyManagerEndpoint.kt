package br.com.zup.ot4.registry

import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.PixKeyResponse
import br.com.zup.ot4.shared.Transaction
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.pix.PixKeyRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

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
        try{
            val chavePix = request.toValidPixKey(itauClient, pixKeyRepository)
            transaction.saveAndCommit(chavePix)

            with(responseObserver){
                onNext(PixKeyResponse.newBuilder().setPixId(chavePix.uuid.toString()).build())
                onCompleted()
            }

        } catch (e: IllegalArgumentException){
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        } catch (e: ChavePixExistenteException) {
            responseObserver.onError(Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }
    }
}
