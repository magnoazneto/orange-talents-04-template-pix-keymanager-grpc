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
import java.util.*
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
            request.valida()
            val clienteResponse = itauClient.consultaCliente(request.idExternoCliente)
            if(clienteResponse.status == HttpStatus.NOT_FOUND){
                responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("id do cliente não encontrado no ERP")
                    .asRuntimeException())
                return
            }

            val chavePix = ChavePix(
                chave = request.chavePix ?: UUID.randomUUID().toString(),
                tipoChave = request.tipoChave,
                tipoConta = request.tipoConta,
                cliente = clienteResponse.body()!!.toClient()
            )

            transaction.saveAndCommit(chavePix)

            with(responseObserver){
                onNext(ChavePixResponse.newBuilder().setPixId(chavePix.uuid.toString()).build())
                onCompleted()
            }

        } catch (e: IllegalArgumentException){
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }
    }
}
