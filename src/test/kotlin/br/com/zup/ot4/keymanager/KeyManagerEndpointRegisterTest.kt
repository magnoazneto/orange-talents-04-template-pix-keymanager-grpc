package br.com.zup.ot4.keymanager

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.account.Instituicao
import br.com.zup.ot4.account.Titular
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.ErpItauClient
import br.com.zup.ot4.integrations.bcbTypes.*
import br.com.zup.ot4.pix.PixKey
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
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyManagerEndpointRegisterTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
){

    private lateinit var pixKeyBcbRequest: PixKeyBcbRequest
    private lateinit var pixRequest: PixKeyRequest

    @Inject
    lateinit var itauClient: ErpItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.deleteAll()

        pixRequest = PixKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setKeyType(KeyType.EMAIL)
            .setPixKey("rponte@gmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountDataResponse()))

        pixKeyBcbRequest = PixKeyBcbRequest(pixRequest, accountDataResponse())

        `when`(bcbClient.registerPixKey(pixKeyBcbRequest))
            .thenReturn(HttpResponse.created(pixKeyBcbResponse()))
    }


    @Test // path 1/6 (happy path)
    fun `deve registrar uma chave pix valida`() {

        val response = grpcClient.register(pixRequest)
        assertNotNull(response.pixId)
    }

    @Test // path 2/6 - erp ok / bcb ok / chave duplicada
    fun `nao deve registrar uma chave pix duplicada`() {

        grpcClient.register(pixRequest)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(pixRequest)
        }
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("Chave PIX igual já cadastrada", error.status.description)
    }

    @Test // path 3/6 - erp !ok / bcb ok / dados ok
    fun `nao deve registrar chave pix quando nao encontrar dados da conta`() {

        `when`(itauClient.searchAccount(clienteId = CLIENT_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(pixRequest)
        }

        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Conta não encontrada no ERP", error.status.description)
    }

    @Test // path 4/6 - erp ok / bcb ok / dados invalidos
    fun `nao deve registrar chave pix quando dados forem invalidos`() {
        val pixRequest = PixKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setKeyType(KeyType.EMAIL)
            .setPixKey("rpontegmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertThrows<StatusRuntimeException> {
            grpcClient.register(pixRequest)
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Email precisa ser válido", e.status.description)
        }
    }

    @Test // path 5/6 - erp ok / bcb !ok / dados ok
    fun `nao deve registrar chave pix quando ja existir chave no BCB`() {

        `when`(bcbClient.registerPixKey(pixKeyBcbRequest))
            .thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(pixRequest)
        }

        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Chave PIX já cadastrada no BCB", error.status.description)
    }

    @Test // path 6/6 - erp ok / bcb !ok / dados ok
    fun `nao deve registrar chave pix quando BCB retornar erro`() {

        `when`(bcbClient.registerPixKey(pixKeyBcbRequest))
            .thenReturn(HttpResponse.badRequest())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.register(pixRequest)
        }

        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Não foi possível cadastrar chave no BCB", error.status.description)
    }

    @MockBean(ErpItauClient::class)
    fun itauClient(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
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

    private fun pixKeyBcbResponse(): PixKeyBcbResponse {
        return PixKeyBcbResponse(
            KeyTypeBcb.EMAIL,
            "rponte@gmail.com",
            BankAccountBcb(accountDataResponse()),
            OwnerBcb(accountDataResponse().titular),
            LocalDateTime.now()
        )
    }

    private fun pixKey(): PixKey {
        return PixKey("rponte@gmail.com", KeyType.EMAIL, accountDataResponse().toModel(), LocalDateTime.now())
    }
}