package br.com.zup.ot4.keymanager

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyManagerServiceGrpc
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.SearchAllRequest
import br.com.zup.ot4.account.AccountData
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.account.Instituicao
import br.com.zup.ot4.account.Titular
import br.com.zup.ot4.integrations.BcbClient
import br.com.zup.ot4.integrations.bcbTypes.BankAccountBcb
import br.com.zup.ot4.integrations.bcbTypes.KeyTypeBcb
import br.com.zup.ot4.integrations.bcbTypes.OwnerBcb
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbResponse
import br.com.zup.ot4.pix.KeyConverter
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class KeyManagerEndpointSearchAllTest(
    val pixKeyRepository: PixKeyRepository,
    val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub
){

    @Inject
    private lateinit var bcbClient: BcbClient

    private lateinit var pixKey1: PixKey
    private lateinit var pixKey2: PixKey

    companion object {
        val CLIENT_ID: UUID = UUID.randomUUID()
    }


    @BeforeEach
    internal fun setUp() {
        pixKeyRepository.deleteAll()
        pixKey1 = PixKey(
            "magno@gmail.com",
            KeyType.EMAIL,
            AccountData(
                AccountType.CONTA_CORRENTE,
                "ITAU UNIBANCO SA",
                "60701190",
                "123",
                "12345",
                CLIENT_ID.toString(),
                "Magno Azevedo",
                "00000000000"
            ),
            LocalDateTime.now()
        )
        pixKey2 = PixKey(
            "noaz4rt@gmail.com",
            KeyType.EMAIL,
            AccountData(
                AccountType.CONTA_POUPANCA,
                "ITAU UNIBANCO SA",
                "60701190",
                "123",
                "54321",
                CLIENT_ID.toString(),
                "Magno Azevedo",
                "00000000000"
            ),
            LocalDateTime.now()
        )

        pixKeyRepository.save(pixKey1)
        pixKeyRepository.save(pixKey2)

    }

    @Test
    fun `deve retornar todas as chaves de um clientID`() {
        val keys =grpcClient.searchAll(SearchAllRequest
                                    .newBuilder()
                                    .setExternalClientId(CLIENT_ID.toString())
                                    .build())

        assertEquals(2, keys.keysCount)
    }

    @Test
    fun `deve retornar lista vazia para um cliente sem chaves`() {
        val keys =grpcClient.searchAll(SearchAllRequest
            .newBuilder()
            .setExternalClientId(UUID.randomUUID().toString())
            .build())

        assertEquals(0, keys.keysCount)
    }

    @Test
    fun `nao deve efetuar buscar para clientId nulo`() {
        assertThrows<StatusRuntimeException> {
            grpcClient.searchAll(SearchAllRequest
                .newBuilder()
                .build())
        }.let { e ->
            assertEquals(Status.INVALID_ARGUMENT.code, e.status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", e.status.description)
        }
    }

    private fun accountDataResponse(): AccountDataResponse {
        return AccountDataResponse(
            tipo = AccountType.CONTA_CORRENTE,
            instituicao = Instituicao("ITAÚ UNIBANCO S.A.", "60701190"),
            agencia = "123",
            numero = "12345",
            titular = Titular(KeyManagerEndpointSearchTest.CLIENT_ID.toString(), "Magno Azevedo", "00000000000")
        )
    }
}

