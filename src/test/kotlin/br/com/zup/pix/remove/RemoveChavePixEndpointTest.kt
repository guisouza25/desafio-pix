package br.com.zup.pix.remove

import br.com.zup.KeymanagerRemoveServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.TipoChave
import br.com.zup.TipoConta
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.ContaAssociada
import br.com.zup.pix.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
class RemoveChavePixEndpointTest(
    val repository: ChavePixRepository,
    private val grpcClient: KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub
) {

    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(chave(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = TipoChave.CPF,
            chave = "02467781054",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }


    @Test
    fun `deve remover chave existente`() {

        val response = grpcClient
            .remove(RemoveChavePixRequest.newBuilder()
                .setClienteId(CHAVE_EXISTENTE.clienteId)
                .setPixId(CHAVE_EXISTENTE.id)
                .build())

        with(response) {
            Assertions.assertEquals(clienteId, CHAVE_EXISTENTE.clienteId)
            Assertions.assertEquals(pixId, CHAVE_EXISTENTE.id)
            Assertions.assertFalse(repository.existsByChave(CHAVE_EXISTENTE.chave!!))
        }
    }

    @Test
    fun `nao deve remover chave quando nao pertencer ao cliente ou nao for encontrada`() {

        val thrown = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setClienteId(UUID.randomUUID().toString())
                .setPixId(UUID.randomUUID().toString())
                .build())
        }

        with(thrown) {
            Assertions.assertEquals(status.code, Status.NOT_FOUND.code)
            Assertions.assertEquals(status.description, "chave pix não encontrada ou não pertence ao cliente")
        }
    }

    @Test
    fun `nao deve remover chave quando os parametros forem invalidos`() {

        val thrown = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            Assertions.assertEquals(status.code, Status.INVALID_ARGUMENT.code)
            Assertions.assertEquals(status.description, "Dados inválidos")
            MatcherAssert.assertThat(violations(), containsInAnyOrder(
                    Pair("pixId", "não deve estar em branco"),
                    Pair("clienteId", "não deve estar em branco"),
                    Pair("pixId", "não é um formato válido de UUID"),
                    Pair("clienteId", "não é um formato válido de UUID"),
                    Pair("chave", "não deve estar em branco"),
                )
            )
        }
    }


    @Factory
    class GrpcClientFactory {
        @Bean
        fun clientStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub {
            return KeymanagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        clienteId: String,
        tipoChave: TipoChave,
        chave: String,
        tipoConta: TipoConta
    ): ChavePix {

        val chave = ChavePix(
            clienteId = clienteId,
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = tipoConta
        )
        chave.associaConta(
            ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "291900"
        )
        )
        return chave
    }
}