package br.com.zup.ot4.keymanager.registry

import br.com.zup.ot4.KeyType

fun KeyType.validate(key: String?) {
    when(this){
        KeyType.CPF -> {
            require(!key.isNullOrBlank()) { "CPF deve ser preenchido"}
            require(key.matches("[0-9]{11}".toRegex())) { "CPF deve ter formato válido" }
        }
        KeyType.PHONE_NUMBER -> {
            require(!key.isNullOrBlank()) { "Celular deve ser preenchido"}
            require(key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) { "Celular deve ter formato válido"}
        }
        KeyType.EMAIL -> {
            require(!key.isNullOrBlank()) { "Email deve ser preenchido"}
            require(key.matches("^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+\\.?([a-z]+)?\$".toRegex())) { "Email precisa ser válido"}
        }
        KeyType.RANDOM_KEY -> {
            require(key.isNullOrBlank()) { "Chave pix deve nao deve ser enviada para criacao de uma chave aleatoria"}
        }
        else -> throw IllegalArgumentException("Tipo de chave pix não reconhecido")
    }
}
