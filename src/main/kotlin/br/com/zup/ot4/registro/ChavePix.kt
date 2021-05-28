package br.com.zup.ot4.registro

import br.com.zup.ot4.TipoChave
import br.com.zup.ot4.TipoConta
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    val idExternoCliente: String,
    val chave: String,
    @field:Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,
    @field:Enumerated(EnumType.ORDINAL)
    val tipoConta: TipoConta
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    val uuid: UUID = UUID.randomUUID()
}