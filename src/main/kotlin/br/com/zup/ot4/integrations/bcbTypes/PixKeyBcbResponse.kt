package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.AccountData
import br.com.zup.ot4.SearchKeyResponse
import br.com.zup.ot4.keymanager.search.Organizations
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

data class PixKeyBcbResponse(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcb,
    val owner: OwnerBcb,
    val createdAt: LocalDateTime
) {
}