package br.com.zup.pix

import br.com.zup.TipoChave
import br.com.zup.TipoConta
import io.micronaut.core.annotation.Introspected
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Introspected
class ChavePix(
    @field:NotBlank
    val clienteId: String,

    @field:NotNull
    val tipoChave: TipoChave?,

    val chave: String?,

    @NotNull
    val tipoConta: TipoConta?
) {

    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String? = null

    val criadoEm = LocalDateTime.now()


    override fun toString(): String {
        return "ChavePix(clienteId='$clienteId', tipoChave=$tipoChave, chave='$chave', tipoConta=$tipoConta, id=$id, criadoEm=$criadoEm)"
    }
}
