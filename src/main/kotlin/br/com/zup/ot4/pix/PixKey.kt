package br.com.zup.ot4.pix

import br.com.zup.ot4.KeyType
import br.com.zup.ot4.SearchKeyResponse
import br.com.zup.ot4.account.AccountData
import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity
class PixKey(
    @Column(name = "pixKey")
    val key: String,
    @field:Enumerated(EnumType.STRING)
    val keyType: KeyType,
    @field:Enumerated(EnumType.ORDINAL)
    @field:Embedded
    val accountData: AccountData,
    val createdAt: LocalDateTime
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    @Column(name = "pixId", updatable = false, nullable = false, unique = true, columnDefinition = "BINARY(16)")
    val uuid: UUID = UUID.randomUUID()
}