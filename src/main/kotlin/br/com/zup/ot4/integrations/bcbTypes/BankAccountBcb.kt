package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.account.AccountData
import br.com.zup.ot4.account.AccountDataResponse
import br.com.zup.ot4.pix.extensions.toBcbType

class BankAccountBcb(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeBcb
) {
    constructor(data: AccountDataResponse) : this(
        data.instituicao.ispb,
        data.agencia,
        data.numero,
        data.tipo.toBcbType()
    )
}
