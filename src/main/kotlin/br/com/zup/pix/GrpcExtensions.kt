package br.com.zup.pix

import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoChave
import br.com.zup.TipoConta
import java.util.*

fun RegistraChavePixRequest.toModel(): ChavePix {
    return ChavePix(
        clienteId = this.clienteId,
        tipoChave = if (this.tipoChave == TipoChave.UNKNOWN_TIPO_CHAVE) null else this.tipoChave,
        chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
        tipoConta = if (this.tipoConta == TipoConta.UNKNOWN_TIPO_CONTA) null else this.tipoConta
    )
}