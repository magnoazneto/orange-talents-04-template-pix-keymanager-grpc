package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.account.Titular
import br.com.zup.ot4.pix.PixKey

class OwnerBcb(
    val name: String,
    val taxIdNumber: String,
    val type: OwnerTypeBcb = OwnerTypeBcb.NATURAL_PERSON
) {
    constructor(owner: Titular) : this(
        owner.nome,
        owner.cpf
    )
}
