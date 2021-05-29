package br.com.zup.ot4.pix

import br.com.zup.ot4.KeyType
import br.com.zup.ot4.account.AccountData
import java.util.*
import javax.persistence.*

@Entity
class PixKey(
    val key: String,
    @field:Enumerated(EnumType.STRING)
    val keyType: KeyType,
    @field:Enumerated(EnumType.ORDINAL)
    @field:Embedded
    val accountData: AccountData
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    val uuid: UUID = UUID.randomUUID()
}