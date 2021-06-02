package br.com.zup.ot4.keymanager

import br.com.zup.ot4.*
import br.com.zup.ot4.shared.errors.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class KeyManagerEndpoint(
    @Inject val keyManagerService: KeyManagerService
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun register(
        request: PixKeyRequest,
        responseObserver: StreamObserver<PixKeyResponse>
    ) {
        val newPixKey = keyManagerService.register(request)
        with(responseObserver){
            onNext(PixKeyResponse.newBuilder().setPixId(newPixKey.uuid.toString()).build())
            onCompleted()
        }
    }

    override fun remove(
        request: RemoveKeyRequest,
        responseObserver: StreamObserver<RemoveKeyResponse>
    ) {
        keyManagerService.remove(request)
        responseObserver.onNext(RemoveKeyResponse.newBuilder().setSuccess(true).build())
        responseObserver.onCompleted()
    }

    override fun search(
        request: SearchKeyRequest,
        responseObserver: StreamObserver<SearchKeyResponse>
    ) {
        val response: SearchKeyResponse = keyManagerService.search(request)
        with(responseObserver) {
            onNext(response)
            onCompleted()
        }
    }
}
