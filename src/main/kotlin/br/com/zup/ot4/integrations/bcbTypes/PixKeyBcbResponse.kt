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

    fun toSearchKeyResponse(): SearchKeyResponse{
        val createdAtInstant = createdAt.atZone(ZoneId.of("UTC")).toInstant()
        return SearchKeyResponse.newBuilder()
            .setKeyType(keyType.toInternalType())
            .setKey(key)
            .setOwnerName(owner.name)
            .setOwnerCpf(owner.taxIdNumber)
            .setAccountData(
                AccountData.newBuilder()
                .setOrganizationName(Organizations.name(bankAccount.participant))
                .setBranch(bankAccount.branch)
                .setAccountNumber(bankAccount.accountNumber)
                .setAccountType(bankAccount.accountType.toInternalType())
            )
            .setCreatedAt(
                Timestamp.newBuilder()
                .setSeconds(createdAtInstant.epochSecond)
                .setNanos(createdAtInstant.nano)
                .build())
            .build()
    }
}