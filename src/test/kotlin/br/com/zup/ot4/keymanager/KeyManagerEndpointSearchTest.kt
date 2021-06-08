package br.com.zup.ot4.keymanager

import br.com.zup.ot4.*
import br.com.zup.ot4.account.AccountData
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.account.Instituicao
import br.com.zup.ot4.account.Titular
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.bcbTypes.*
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyManagerEndpointSearchTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
) {

    @Inject
    private lateinit var bcbClient: BcbClient

    private lateinit var pixKey: PixKey

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }


    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.deleteAll()
        pixKey = PixKey(
            "magno@gmail.com",
            KeyType.EMAIL,
            AccountData(
                AccountType.CONTA_CORRENTE,
                "ITAU UNIBANCO SA",
                "11111",
                "123",
                "12345",
                CLIENT_ID.toString(),
                "Magno Azevedo",
                "00000000000"
            ),
            LocalDateTime.now()
        )

        pixKeyRepository.save(pixKey)

        `when`(bcbClient.searchPixKey(pixKey.key))
            .thenReturn(HttpResponse.ok(pixKeyBcbResponse()))
    }


    @Test // 1/10- happy path
    fun `deve retornar chave pix para chave valida`() {
        val response = grpcClient.search(
            SearchKeyRequest.newBuilder()
                .setKey(pixKey.key)
                .build()
        )
        assertEquals(pixKey.accountData.holderId,response.externalClientId)
    }

    @Test // path 2/10
    fun `deve retornar chave pix para pixId e clientId validos`() {
        val response = grpcClient.search(
                SearchKeyRequest.newBuilder()
                    .setPixData(SearchKeyRequest.PixData.newBuilder()
                        .setExternalClientId(pixKey.accountData.holderId)
                        .setPixId(pixKey.uuid.toString()))
                    .build()
                )
        assertEquals(pixKey.accountData.holderId,response.externalClientId)
        assertEquals(pixKey.key ,response.key)
    }

    @Test // path 3/10
    fun `nao deve retornar chave pix quando faltarem dados`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder().build())
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Critério de busca desconhecido", e.status.description)
        }
    }
    //

    @Test // path 4/10
    fun `nao deve retornar chave quando apenas pixId for informado no PixData`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                    .setPixData(SearchKeyRequest.PixData.newBuilder()
                        .setPixId(pixKey.uuid.toString()))
                    .build())
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Identificador do cliente não pode ser vazio ou nulo", e.status.description)
        }

    }

    @Test // path 5/10
    fun `nao deve retornar chave quando apenas externalClientId for informado no PixData`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                .setPixData(SearchKeyRequest.PixData.newBuilder()
                    .setExternalClientId(pixKey.accountData.holderId))
                .build())
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Pix id não pode ser vazia ou nula", e.status.description)
        }
    }

    @Test // path 6/10
    fun `nao deve retornar chave se chave nao existir no banco`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                .setPixData(SearchKeyRequest.PixData.newBuilder()
                    .setExternalClientId(pixKey.accountData.holderId)
                    .setPixId(UUID.randomUUID().toString()))
                .build())
        }.let { e ->
            assertEquals(Status.NOT_FOUND.code, e.status.code)
            assertEquals("Chave pix não encontrada para esse id", e.status.description)
        }

    }

    @Test // path 7/10
    fun `nao deve retornar chave de um proprietario diferente`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                .setPixData(SearchKeyRequest.PixData.newBuilder()
                    .setExternalClientId(UUID.randomUUID().toString())
                    .setPixId(pixKey.uuid.toString()))
                .build())
        }.let { e ->
            assertEquals(Status.FAILED_PRECONDITION.code, e.status.code)
            assertEquals("Chave só pode ser consultada pelo proprietário", e.status.description)
        }

    }

    @Test // path 8/10
    fun `nao deve retornar chave se nao houver registro no BCB`() {
        val randomKey = UUID.randomUUID().toString()

        `when`(bcbClient.searchPixKey(randomKey))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                .setKey(randomKey)
                .build())
        }.let { e ->
            assertEquals(Status.FAILED_PRECONDITION.code, e.status.code)
            assertEquals("A chave $randomKey não está registrada no Bacen nem no sistema interno", e.status.description)
        }
    }

    @Test // path 9/10
    fun `nao deve retornar chave caso conexao com o BCB retorne erro`() {
        val randomKey = UUID.randomUUID().toString()

        `when`(bcbClient.searchPixKey(randomKey))
            .thenReturn(HttpResponse.badRequest())

        assertThrows<StatusRuntimeException> {
            grpcClient.search(SearchKeyRequest.newBuilder()
                .setKey(randomKey)
                .build())
        }.let { e ->
            assertEquals(Status.FAILED_PRECONDITION.code, e.status.code)
            assertEquals("Erro de comunicação com o Bacen", e.status.description)
        }
    }

    @Test // path 10/10
    fun `deve retornar dados internos em branco se a chave vier do BCB`() {
        val randomKey = UUID.randomUUID().toString()

        `when`(bcbClient.searchPixKey(randomKey))
            .thenReturn(HttpResponse.ok(pixKeyBcbResponse()))

        val response = grpcClient.search(
            SearchKeyRequest.newBuilder()
                .setKey(randomKey)
                .build()
        )

        assertEquals("", response.externalClientId)
        assertEquals("", response.pixId)
        assertEquals(pixKey.accountData.holderCpf, response.ownerCpf)
    }


    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return mock(BcbClient::class.java)
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

    private fun pixKeyBcbResponse(): PixKeyBcbResponse {
        return PixKeyBcbResponse(
            KeyTypeBcb.EMAIL,
            "magno@gmail.com",
            BankAccountBcb(accountDataResponse()),
            OwnerBcb(accountDataResponse().titular),
            LocalDateTime.now()
        )
    }

    private fun accountDataResponse(): AccountDataResponse {
        return AccountDataResponse(
            tipo = AccountType.CONTA_CORRENTE,
            instituicao = Instituicao("ITAÚ UNIBANCO S.A.", "60701190"),
            agencia = "123",
            numero = "12345",
            titular = Titular(CLIENT_ID.toString(), "Magno Azevedo", "00000000000")
        )
    }

}