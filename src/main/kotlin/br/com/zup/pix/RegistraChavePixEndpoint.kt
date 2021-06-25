package br.com.zup.pix

import br.com.zup.*
import br.com.zup.shared.errors.ErrorHandler
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.groups.Default

@Singleton
@ErrorHandler
class RegistraChavePixEndpoint(
    @Inject val validator: Validator
) : KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {

        val chavePix = request.toModel()
        val constrains = validator.validate(chavePix, Default::class.java)
        if (constrains.isNotEmpty()) throw ConstraintViolationException(constrains)

        println(chavePix.toString())
    }
}

fun RegistraChavePixRequest.toModel(): ChavePix {
    return ChavePix(
        clienteId = this.clienteId,
        tipoChave = if (this.tipoChave == TipoChave.UNKNOWN_TIPO_CHAVE) null else this.tipoChave,
        chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave,
        tipoConta = if (this.tipoConta == TipoConta.UNKNOWN_TIPO_CONTA) null else this.tipoConta
    )
}
