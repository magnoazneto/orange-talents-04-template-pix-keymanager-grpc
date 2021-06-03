package br.com.zup.ot4.keymanager

import br.com.zup.ot4.*
import br.com.zup.ot4.account.AccountData
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.bcbTypes.DeletePixKeyBcbRequest
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbRequest
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
internal class KeyManagerEndpointRemoveTest(
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
                "ITAU",
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

        `when`(bcbClient.removePixKey(pixKey.key, DeletePixKeyBcbRequest(pixKey.key, pixKey.accountData.ispb)))
            .thenReturn(HttpResponse.ok(RemoveKeyResponse.newBuilder().setSuccess(true).build()))
    }

    @Test
    fun `deve deletar chave pix com argumentos validos`() {
        val request = RemoveKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setPixId(pixKey.uuid.toString())
            .build()

        val response = grpcClient.remove(request)

        assertTrue(response.success)
    }

    @Test
    fun `nao deve deletar chave pix inexistente`() {
        val request = RemoveKeyRequest.newBuilder()
            .setExternalClientId(CLIENT_ID.toString())
            .setPixId(UUID.randomUUID().toString())
            .build()

        assertThrows<StatusRuntimeException> {
            grpcClient.remove(request)
        }.let { e ->
            assertEquals(Status.NOT_FOUND.code, e.status.code)
        }
    }

    @Test
    fun `nao deve deletar chave pix com clientId invalido`() {
        val request = RemoveKeyRequest.newBuilder()
            .setExternalClientId(UUID.randomUUID().toString())
            .setPixId(pixKey.uuid.toString())
            .build()

        assertThrows<StatusRuntimeException> {
            grpcClient.remove(request)
        }.let { e ->
            assertEquals(Status.FAILED_PRECONDITION.code, e.status.code)
        }
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

}