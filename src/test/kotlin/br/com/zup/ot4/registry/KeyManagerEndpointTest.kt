package br.com.zup.ot4.registry

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.account.Instituicao
import br.com.zup.ot4.account.Titular
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.pix.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyManagerEndpointTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
){

    @Inject
    lateinit var itauClient: ErpItauClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.deleteAll()
    }

    /**
     * deve registrar chave valida (happy path)
     * nao deve registrar chave duplicada
     * nao deve registrar chave pix quando nao encontrar dados do cliente
     * nao deve registrar chave pix qundo parametros forem invalidos
     */


    @Test // path 1/4 - deve registrar chave válida (happy path)
    fun `deve registrar uma chave pix valida`() {
        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        val response = grpcClient.register(PixKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setKeyType(KeyType.EMAIL)
            .setPixKey("rponte@gmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()
        )

        with(response){
            assertNotNull(pixId)
        }
    }

    @Test // path 2/4
    fun `nao deve registrar uma chave pix duplicada`() {
        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        grpcClient.register(PixKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setKeyType(KeyType.EMAIL)
            .setPixKey("rponte@gmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()
        )

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                PixKeyRequest.newBuilder()
                    .setExternalClientId(CLIENT_ID.toString())
                    .setKeyType(KeyType.EMAIL)
                    .setPixKey("rponte@gmail.com")
                    .setAccountType(AccountType.CONTA_CORRENTE)
                    .build()
            )
        }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("Chave PIX igual já cadastrada", error.status.description)
    }

    @Test // path 3/4
    fun `nao deve registrar chave pix quando nao encontrar dados da conta`() {
        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(
                PixKeyRequest.newBuilder()
                    .setExternalClientId(CLIENT_ID.toString())
                    .setKeyType(KeyType.EMAIL)
                    .setPixKey("rponte@gmail.com")
                    .setAccountType(AccountType.CONTA_CORRENTE)
                    .build()
            )
        }

        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Conta não encontrada no ERP", error.status.description)
    }

    @Test // path 4/4
    fun `nao deve registrar chave pix quando dados forem invalidos`() {
        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        assertThrows<StatusRuntimeException> {
            grpcClient.register(
                PixKeyRequest.newBuilder()
                    .setExternalClientId(CLIENT_ID.toString())
                    .setKeyType(KeyType.EMAIL)
                    .setPixKey("rpontegmail.com")
                    .setAccountType(AccountType.CONTA_CORRENTE)
                    .build()
            )
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Email precisa ser válido", e.status.description)
        }

        assertThrows<StatusRuntimeException> {
            grpcClient.register(
                PixKeyRequest.newBuilder()
                    .setExternalClientId(CLIENT_ID.toString())
                    .setKeyType(KeyType.CPF)
                    .setPixKey("magno@gmail.com")
                    .setAccountType(AccountType.CONTA_CORRENTE)
                    .build()
            )
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("CPF deve ter formato válido", e.status.description)
        }
    }

    @MockBean(ErpItauClient::class)
    fun itauClient(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(
            @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
        ): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub? {
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun accountDataResponse(): AccountDataResponse {
        return AccountDataResponse(
            tipo = AccountType.CONTA_CORRENTE,
            instituicao = Instituicao("ITAU UNIBANCO SA", "11111"),
            agencia = "123",
            numero = "12345",
            titular = Titular("aaaa-bbbb-cccc", "Rafael Ponte", "00000000000")
        )
    }
}