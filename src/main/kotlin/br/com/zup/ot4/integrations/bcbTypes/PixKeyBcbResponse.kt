package br.com.zup.ot4.integrations.bcbTypes

import java.time.LocalDateTime

data class PixKeyBcbResponse(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb,
    val createdAt: LocalDateTime
) {
}