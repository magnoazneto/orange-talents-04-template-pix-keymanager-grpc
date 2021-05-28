package br.com.zup.ot4.registro

import br.com.zup.ot4.ChavePixRequest
import br.com.zup.ot4.ChavePixResponse
import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.compartilhado.Transaction
import br.com.zup.ot4.integracoes.itau.ErpItauClient
import br.com.zup.ot4.pix.ChavePix
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerEndpoint(
    @Inject val transaction: Transaction,
    @Inject val itauClient: ErpItauClient
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registrar(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {
        try{
            val httpResponse = itauClient.consultaCliente(request.idExternoCliente)
            if(httpResponse.status == HttpStatus.NOT_FOUND){
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("id do cliente não encontrado no ERP")
                    .asRuntimeException())
            }

            val clienteResponse = httpResponse.body()

            val validator = ChavePixValidator(
                request.idExternoCliente,
                request.chavePix,
                request.tipoChave,
                request.tipoConta
            )

            val chavePix = ChavePix(
                validator.chavePix!!,
                validator.tipoChave,
                validator.tipoConta,
                clienteResponse!!.toClient()
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