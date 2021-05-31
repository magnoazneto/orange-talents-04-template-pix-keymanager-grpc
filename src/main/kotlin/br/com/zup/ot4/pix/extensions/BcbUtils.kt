package br.com.zup.ot4.pix.extensions

import br.com.zup.ot4.AccountType
import br.com.zup.ot4.KeyType
import br.com.zup.ot4.integrations.bcbTypes.AccountTypeBcb
import br.com.zup.ot4.integrations.bcbTypes.KeyTypeBcb
import java.lang.IllegalStateException

fun KeyType.toBcbType(): KeyTypeBcb {
    return when(this){
        KeyType.CPF -> KeyTypeBcb.CPF
        KeyType.EMAIL -> KeyTypeBcb.EMAIL
        KeyType.PHONE_NUMBER -> KeyTypeBcb.PHONE
        KeyType.RANDOM_KEY -> KeyTypeBcb.RANDOM
        else -> throw IllegalStateException("Tipo de chave pix desconhecido")
    }
}

fun AccountType.toBcbType(): AccountTypeBcb {
    return when(this){
        AccountType.CONTA_CORRENTE -> AccountTypeBcb.CACC
        AccountType.CONTA_POUPANCA -> AccountTypeBcb.SVGS
        else -> throw IllegalStateException("Tipo de conta desconhecido")
    }
}