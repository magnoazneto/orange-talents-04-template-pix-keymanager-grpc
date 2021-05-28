package br.com.zup.ot4.registro

import br.com.zup.ot4.TipoChave
import br.com.zup.ot4.TipoConta
import java.util.*

class ChavePixValidator(
    _idExternoCliente: String,
    _chavePix: String?,
    _tipoChave: TipoChave,
    _tipoConta: TipoConta,
) {
    val idExternoCliente = _idExternoCliente // validar se existe no ERP itau
    var chavePix: String? =  null
    val tipoChave: TipoChave = _tipoChave
    val tipoConta: TipoConta = _tipoConta

    init {
        val chaveTemporaria: String
        when(_tipoChave){
            TipoChave.CPF -> {
                require(!_chavePix.isNullOrBlank()) { "CPF deve ser preenchido"}
                require(_chavePix.matches("[0-9]{11}".toRegex())) { "CPF deve ter formato válido" }
                chaveTemporaria = _chavePix
            }
            TipoChave.CELULAR -> {
                require(!_chavePix.isNullOrBlank()) { "Celular deve ser preenchido"}
                require(_chavePix.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) { "Celular deve ter formato válido"}
                chaveTemporaria = _chavePix
            }
            TipoChave.EMAIL -> {
                require(!_chavePix.isNullOrBlank()) { "Email deve ser preenchido"}
                require(_chavePix.matches("^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+\\.?([a-z]+)?\$".toRegex())) { "Email precisa ser válido"}
                chaveTemporaria = _chavePix
            }
            TipoChave.ALEATORIA -> {
                require(_chavePix.isNullOrBlank()) { "Chave deve ser enviada em branco para geração de uma chave aleatória"}
                chaveTemporaria = UUID.randomUUID().toString()
            }
            else -> throw IllegalArgumentException("Tipo de chave pix não reconhecido")
        }
        this.chavePix = chaveTemporaria

        if(this.tipoConta == TipoConta.TIPO_CONTA_DESCONHECIDO) throw IllegalArgumentException("Tipo de conta desconhecido")
    }

}