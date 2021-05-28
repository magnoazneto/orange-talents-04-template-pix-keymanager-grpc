package br.com.zup.ot4.registro

import br.com.zup.ot4.ChavePixRequest
import br.com.zup.ot4.TipoChave
import br.com.zup.ot4.TipoConta
import br.com.zup.ot4.integracoes.itau.ErpItauClient
import br.com.zup.ot4.pix.ChavePix
import br.com.zup.ot4.pix.ChavePixRepository
import io.micronaut.http.HttpStatus

fun ChavePixRequest.converteParaChaveValida(
    itauClient: ErpItauClient,
    chavePixRepository: ChavePixRepository
) : ChavePix {
    when(tipoChave){
        TipoChave.CPF -> {
            require(!chavePix.isNullOrBlank()) { "CPF deve ser preenchido"}
            require(chavePix.matches("[0-9]{11}".toRegex())) { "CPF deve ter formato válido" }
        }
        TipoChave.CELULAR -> {
            require(!chavePix.isNullOrBlank()) { "Celular deve ser preenchido"}
            require(chavePix.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) { "Celular deve ter formato válido"}
        }
        TipoChave.EMAIL -> {
            require(!chavePix.isNullOrBlank()) { "Email deve ser preenchido"}
            require(chavePix.matches("^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+\\.?([a-z]+)?\$".toRegex())) { "Email precisa ser válido"}
        }
        TipoChave.ALEATORIA -> {
            require(chavePix.isNullOrBlank())
        }
        else -> throw IllegalArgumentException("Tipo de chave pix não reconhecido")
    }

    if(tipoConta == TipoConta.TIPO_CONTA_DESCONHECIDO) throw IllegalArgumentException("Tipo de Conta desconhecido")
    if(chavePixRepository.existsByChave(chavePix)) throw ChavePixExistenteException("Chave PIX igual já cadastrada")

    val response = itauClient.consultaCliente(idExternoCliente)
    if (response.status == HttpStatus.NOT_FOUND) {
        throw IllegalArgumentException("Cliente não encontrado no ERP")
    }

    return ChavePix(
        chave = chavePix,
        tipoChave = tipoChave,
        tipoConta = tipoConta,
        cliente = response.body()!!.toClient()
    )
}