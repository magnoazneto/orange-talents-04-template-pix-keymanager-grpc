package br.com.zup.ot4.registry.extensions

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.PixKeyRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KeyTypeExtensionKtTest{

    @Test
    fun `nao deve aceitar CPF em formato invalido`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setPixKey("magno@gmail.com")
            .setKeyType(KeyType.CPF)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("CPF deve ter formato válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CPF em branco`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("CPF deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar EMAIL em formato invalido`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setPixKey("magnogmail.com")
            .setKeyType(KeyType.EMAIL)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Email precisa ser válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar EMAIL em branco`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setKeyType(KeyType.EMAIL)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Email deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CELULAR em formato invalido`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setPixKey("magnogmail.com")
            .setKeyType(KeyType.PHONE_NUMBER)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Celular deve ter formato válido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CELULAR em branco`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setKeyType(KeyType.PHONE_NUMBER)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Celular deve ser preenchido", e.message)
        }
    }

    @Test
    fun `nao deve aceitar CHAVE ALEATORIA preenchida`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setKeyType(KeyType.RANDOM_KEY)
            .setPixKey("magno@gmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Chave pix deve nao deve ser enviada para criacao de uma chave aleatoria", e.message)
        }
    }


    @Test
    fun `nao deve aceitar CHAVE DESCONHECIDA`() {
        val pixKey = PixKeyRequest.newBuilder()
            .setKeyType(KeyType.UNKNOW_KEY_TYPE)
            .setPixKey("magno@gmail.com")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .setExternalClientId("aaa-bbb-ccc")

        assertThrows<IllegalArgumentException> {
            pixKey.keyType.validate(pixKey.pixKey)
        }.let { e ->
            assertEquals("Tipo de chave pix não reconhecido", e.message)
        }
    }


}