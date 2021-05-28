package br.com.zup.ot4.cliente

import javax.persistence.Embeddable

@Embeddable
data class Instituicao(
    val nome: String,
    val ispb: String
)