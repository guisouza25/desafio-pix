package br.com.zup.pix

import br.com.zup.KeymanagerRegistraServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.RegistraChavePixResponse
import br.com.zup.integration.itau.ContasItauClient
import br.com.zup.shared.errors.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.groups.Default

@Singleton
@ErrorHandler
class RegistraChavePixEndpoint(
    @Inject val validator: Validator,
    @Inject val itauClient: ContasItauClient,
    @Inject val repository: ChavePixRepository
) : KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceImplBase() {

    override fun registra(request: RegistraChavePixRequest,
                          responseObserver: StreamObserver<RegistraChavePixResponse>?) {

        val chavePix = request.toModel()
        val constrains = validator.validate(chavePix, Default::class.java)
        if (constrains.isNotEmpty()) throw ConstraintViolationException(constrains)

        if (repository.existsByChave(chavePix.chave)) throw IllegalStateException("Chave pix já existente")

        val response = itauClient.buscaContaPorTipo(chavePix.clienteId, chavePix.tipoConta!!.name)
        val conta = response?.body().toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        chavePix.associaConta(conta)
        repository.save(chavePix)

        println(chavePix.toString())
    }
}


