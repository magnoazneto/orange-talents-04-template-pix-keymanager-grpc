package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.PixKeyRequest
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.pix.PixKey
import br.com.zup.ot4.pix.extensions.toBcbType

class PixKeyBcbRequest(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb
) {
    constructor(pixKey: PixKeyRequest, accountData: AccountDataResponse): this (
        keyType = pixKey.keyType.toBcbType(),
        key = pixKey.pixKey,
        bankAccount = BankAccountBcb(accountData),
        owner = OwnerBcb(accountData.titular)
    )
}
