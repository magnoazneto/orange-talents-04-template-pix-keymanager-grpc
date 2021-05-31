package br.com.zup.ot4.keymanager.remove

import br.com.zup.ot4.RemoveKeyRequest
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.PixKeyRepository
import br.com.zup.ot4.shared.exceptions.PixKeyNotFoundException
import java.util.*

fun RemoveKeyRequest.validate(
    pixKeyRepository: PixKeyRepository
): PixKey {
    val possiblePixKey = pixKeyRepository.findByUuid(UUID.fromString(pixId))
    if(!possiblePixKey.isPresent)
        throw PixKeyNotFoundException("Chave pix não encontrada para o pixId: $pixId")
    val pixKey = possiblePixKey.get()
    if(pixKey.accountData.holderId != externalClientId)
        throw IllegalStateException("A chave só pode ser removida pelo próprio dono.")

    return pixKey
}