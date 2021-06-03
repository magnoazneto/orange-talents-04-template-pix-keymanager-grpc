package br.com.zup.ot4.pix
import br.com.zup.ot4.AccountData
import br.com.zup.ot4.SearchAllResponse
import br.com.zup.ot4.SearchKeyResponse
import br.com.zup.ot4.integrations.bcbTypes.PixKeyBcbResponse
import br.com.zup.ot4.keymanager.search.Organizations
import com.google.protobuf.Timestamp

import java.time.ZoneId

class KeyConverter(
) {
    companion object {
        fun toPixDetails(pixKey: PixKey): SearchAllResponse.PixDetails {
            val createdAtInstant = pixKey.createdAt.atZone(ZoneId.of("UTC")).toInstant()
            return SearchAllResponse.PixDetails.newBuilder()
                .setPixId(pixKey.uuid.toString())
                .setExternalClientId(pixKey.accountData.holderId)
                .setKeyType(pixKey.keyType)
                .setKey(pixKey.key)
                .setAccountType(pixKey.accountData.accountType)
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setSeconds(createdAtInstant.epochSecond)
                        .setNanos(createdAtInstant.nano)
                        .build()
                )
                .build()
        }

        fun toSearchKeyResponse(pixKey: PixKey): SearchKeyResponse {
            val createdAtInstant = pixKey.createdAt.atZone(ZoneId.of("UTC")).toInstant()
            return SearchKeyResponse.newBuilder()
                .setPixId(pixKey.uuid.toString())
                .setExternalClientId(pixKey.accountData.holderId)
                .setKeyType(pixKey.keyType)
                .setKey(pixKey.key)
                .setOwnerName(pixKey.accountData.holderName)
                .setOwnerCpf(pixKey.accountData.holderCpf)
                .setAccountData(
                    br.com.zup.ot4.AccountData.newBuilder()
                        .setOrganizationName(pixKey.accountData.organizationName)
                        .setBranch(pixKey.accountData.agency)
                        .setAccountNumber(pixKey.accountData.accountNumber)
                        .setAccountType(pixKey.accountData.accountType)
                )
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setSeconds(createdAtInstant.epochSecond)
                        .setNanos(createdAtInstant.nano)
                        .build())
                .build()
        }

        fun toSearchKeyResponse(pixKey: PixKeyBcbResponse): SearchKeyResponse{
            val createdAtInstant = pixKey.createdAt.atZone(ZoneId.of("UTC")).toInstant()
            return SearchKeyResponse.newBuilder()
                .setKeyType(pixKey.keyType.toInternalType())
                .setKey(pixKey.key)
                .setOwnerName(pixKey.owner.name)
                .setOwnerCpf(pixKey.owner.taxIdNumber)
                .setAccountData(
                    AccountData.newBuilder()
                        .setOrganizationName(Organizations.name(pixKey.bankAccount.participant))
                        .setBranch(pixKey.bankAccount.branch)
                        .setAccountNumber(pixKey.bankAccount.accountNumber)
                        .setAccountType(pixKey.bankAccount.accountType.toInternalType())
                )
                .setCreatedAt(
                    Timestamp.newBuilder()
                        .setSeconds(createdAtInstant.epochSecond)
                        .setNanos(createdAtInstant.nano)
                        .build())
                .build()
        }
    }
}