package br.com.zup.ot4.registro

import br.com.zup.ot4.ChavePixRequest
import br.com.zup.ot4.ChavePixResponse
import br.com.zup.ot4.KeyManagerServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerEndpoint(
    @Inject val transaction: Transaction
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registrar(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {
        try{
             val validator = ChavePixValidator(
                request.idExternoCliente,
                request.chavePix,
                request.tipoChave,
                request.tipoConta
            )

            val chavePix = ChavePix(
                validator.idExternoCliente,
                validator.chavePix!!,
                validator.tipoChave,
                validator.tipoConta
            ).also { transaction.saveAndCommit(it) }

            val response = ChavePixResponse.newBuilder()
                .setPixId(chavePix.uuid.toString()).build()

            responseObserver.onNext(response)
            responseObserver.onCompleted()

        } catch (e: IllegalArgumentException){
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }
    }
}