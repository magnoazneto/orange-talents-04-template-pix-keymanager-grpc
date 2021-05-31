package br.com.zup.ot4.registry.extensions

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.keymanager.registry.validate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KeyTypeExtensionKtTest{

    lateinit var pixKey: PixKeyRequest.Builder

    @BeforeEach
    internal fun setUp() {
        pixKey = PixKeyRequest.newBuilder()
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")
    }

    @Test
    fun `nao deve aceitar CPF em formato invalido`() {
        pixKey.setPixKey("magno@gmail.com")
            .setKeyType(KeyType.CPF).build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("CPF deve ter formato válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CPF em branco`() {
        pixKey.setKeyType(KeyType.CPF)
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("CPF deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar EMAIL em formato invalido`() {
        pixKey.setKeyType(KeyType.EMAIL)
            .setPixKey("magnogmail.com")
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Email precisa ser válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar EMAIL em branco`() {
        pixKey.setKeyType(KeyType.EMAIL)
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Email deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CELULAR em formato invalido`() {
        pixKey.setKeyType(KeyType.PHONE_NUMBER)
            .setPixKey("magnogmail.com")
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Celular deve ter formato válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CELULAR em branco`() {
        pixKey.setKeyType(KeyType.PHONE_NUMBER)
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Celular deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CHAVE ALEATORIA preenchida`() {
        pixKey.setKeyType(KeyType.RANDOM_KEY)
            .setPixKey("magno@gmail.com")
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Chave pix deve nao deve ser enviada para criacao de uma chave aleatoria", e.message)
        }
    }


    @Test
    fun `nao deve aceitar CHAVE DESCONHECIDA`() {
        pixKey.setKeyType(KeyType.UNKNOW_KEY_TYPE)
            .build()

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Tipo de chave pix não reconhecido", e.message)
        }
    }


}