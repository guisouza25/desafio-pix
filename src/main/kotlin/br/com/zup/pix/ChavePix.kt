package br.com.zup.pix

import br.com.zup.TipoChave
import br.com.zup.TipoConta
import br.com.zup.integration.itau.ContaClientResponse
import br.com.zup.shared.validators.ValidUUID
import io.micronaut.core.annotation.Introspected
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@ChaveValida
@Introspected
class ChavePix(

    @field:NotBlank @field:ValidUUID
    val clienteId: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave?,

    @field:NotBlank
    val chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta?
) {

    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String? = null

    @Embedded
    var conta: ContaAssociada? = null
        private set

    val criadoEm: LocalDateTime = LocalDateTime.now()


    override fun toString(): String {
        return "ChavePix(clienteId='$clienteId', tipoChave=$tipoChave, chave='$chave', tipoConta=$tipoConta, id=$id, criadoEm=$criadoEm)"
    }

    fun associaConta(conta: ContaAssociada) {
        this.conta = conta
    }
}
