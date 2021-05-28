package br.com.zup.ot4.pix

import br.com.zup.ot4.TipoChave
import br.com.zup.ot4.TipoConta
import br.com.zup.ot4.cliente.Cliente
import java.util.*
import javax.persistence.*

@Entity
class ChavePix(
    val chave: String,
    @field:Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,
    @field:Enumerated(EnumType.ORDINAL)
    val tipoConta: TipoConta,
    @field:Embedded val cliente: Cliente
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
    val uuid: UUID = UUID.randomUUID()
}