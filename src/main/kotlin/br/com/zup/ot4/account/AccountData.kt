package br.com.zup.ot4.account

import br.com.zup.ot4.AccountType
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
data class AccountData(
    @field:Enumerated(EnumType.STRING)
    val accountType: AccountType,
    val organizationName: String,
    val ispb: String,
    val agency: String,
    val accountNumber: String,
    val holderId: String,
    val holderName: String,
    val holderCpf: String
) {
}