package br.com.zup.ot4.registro

import br.com.zup.ot4.ChavePixRequest
import br.com.zup.ot4.ChavePixResponse
import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.compartilhado.Transaction
import br.com.zup.ot4.integracoes.itau.ErpItauClient
import br.com.zup.ot4.pix.ChavePixRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManagerEndpoint(
    @Inject val transaction: Transaction,
    @Inject val itauClient: ErpItauClient,
    @Inject val chavePixRepository: ChavePixRepository
) : KeyManagerServiceGrpc.KeyManagerServiceImplBase() {

    override fun registrar(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>
    ) {
        try{
            val chavePix = request.converteParaChaveValida(itauClient, chavePixRepository)
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
        } catch (e: ChavePixExistenteException) {
            responseObserver.onError(Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
                .asRuntimeException())
        }
    }
}
