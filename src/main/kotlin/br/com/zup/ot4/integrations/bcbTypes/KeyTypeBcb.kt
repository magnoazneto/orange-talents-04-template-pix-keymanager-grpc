package br.com.zup.ot4.integrations.bcbTypes

import br.com.zup.ot4.KeyType

enum class KeyTypeBcb {
    CPF, PHONE, EMAIL, RANDOM;

    fun toInternalType(): KeyType {
        return when(this){
            CPF -> KeyType.CPF
            PHONE -> KeyType.PHONE_NUMBER
            EMAIL -> KeyType.EMAIL
            RANDOM -> KeyType.RANDOM_KEY
        }
    }
}