package br.com.zup.ot4.pix
import br.com.zup.ot4.SearchAllResponse
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
    }
}