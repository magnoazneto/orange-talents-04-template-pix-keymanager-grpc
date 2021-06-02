package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.AccountType

enum class AccountTypeBcb {
    CACC, SVGS;

    fun toInternalType(): AccountType {
        return when(this) {
            CACC -> AccountType.CONTA_CORRENTE
            SVGS -> AccountType.CONTA_POUPANCA
        }
    }
}