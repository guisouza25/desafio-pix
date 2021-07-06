package br.com.zup.pix.remove

import br.com.zup.pix.ChavePixRepository
import br.com.zup.shared.validators.ValidUUID
import io.grpc.Status
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank


@Validated
@Singleton
class RemoveChavePixService(
    @Inject val repository: ChavePixRepository
) {

    fun remove(@NotBlank @ValidUUID clienteId: String?,
               @NotBlank @ValidUUID pixId: String?
    ) {

        val chave = repository.findByIdAndClienteId(pixId!!, clienteId!!)
            .orElseThrow { Status.NOT_FOUND
                .withDescription("chave pix não encontrada ou não pertence ao cliente")
                .asRuntimeException()
            }

        repository.delete(chave)
    }
}
