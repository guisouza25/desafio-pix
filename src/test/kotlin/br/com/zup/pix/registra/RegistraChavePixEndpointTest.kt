package br.com.zup.pix.registra

import br.com.zup.KeymanagerRegistraServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoChave
import br.com.zup.TipoConta
import br.com.zup.integration.itau.ContaClientResponse
import br.com.zup.integration.itau.ContasItauClient
import br.com.zup.integration.itau.InstituicaoResponse
import br.com.zup.integration.itau.TitularResponse
import br.com.zup.pix.ChavePix
import br.com.zup.pix.ChavePixRepository
import br.com.zup.pix.ContaAssociada
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject

/**
 * TIP: Necessario desabilitar o controle transacional (transactional=false) pois o gRPC Server
 * roda numa thread separada, caso contrário não será possível preparar cenário dentro do método @Test
 */
@MicronautTest(transactional = false)
class RegistraChavePixEndpointTest(
    private val grpcClient: KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub,
    private val repository: ChavePixRepository
) {

    @Inject
    lateinit var itauClient: ContasItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
    }

    @BeforeEach
    fun seteup() {
        repository.deleteAll()
    }


    // TODO: escrever cenários de exceptions na chamada do serviços satelites e dados inválidos

    @Test
    fun `deve registrar uma chave pix`() {

        Mockito.`when`(itauClient.buscaContaPorTipo(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(contaClientResponse()))

        val response = grpcClient
            .registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoChave(TipoChave.CPF)
                .setChave("02467781054")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
            )
        with(response) {
            Assertions.assertNotNull(pixId)
            Assertions.assertNotNull(repository.findById(pixId).get().conta)
            Assertions.assertEquals(clienteId, CLIENTE_ID)
            Assertions.assertTrue(repository.existsById(pixId))
        }
    }

    @Test
    fun `nao deve registrar uma chave quando ja existente`() {

        repository.save(chave(
            clienteId = CLIENTE_ID,
            tipoChave = TipoChave.CPF,
            chave = "02467781054",
            tipoConta = TipoConta.CONTA_CORRENTE
        ))

        val thrown = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoChave(TipoChave.CPF)
                .setChave("02467781054")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
            )
        }

        with(thrown) {
            Assertions.assertEquals(status.code, Status.FAILED_PRECONDITION.code)
            Assertions.assertEquals(status.description, "chave pix já existente")
        }
    }

    @Test
    fun `nao deve regsistrar uma chave quando cliente inexistente`() {

        Mockito.`when`(itauClient.buscaContaPorTipo(CLIENTE_ID, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound(null))

        val thrown = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.registra(RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID)
                .setTipoChave(TipoChave.CPF)
                .setChave("02467781054")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
            )
        }

        with(thrown) {
            Assertions.assertEquals(status.code, Status.FAILED_PRECONDITION.code)
            Assertions.assertEquals(status.description, "cliente não encontrado no Itau")
        }
    }


    @MockBean(ContasItauClient::class)
    fun itauClient(): ContasItauClient {
        return Mockito.mock(ContasItauClient::class.java)
    }

    @Factory
    class GrpcClientFactory {
        @Bean
        fun clientStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRegistraServiceGrpc.KeymanagerRegistraServiceBlockingStub {
            return KeymanagerRegistraServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun contaClientResponse(): ContaClientResponse {
        return ContaClientResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("ITAÚ UNIBANCO S.A.", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Rafael M C Ponte", "02467781054")
        )
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
        chave.associaConta(ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "291900"
        ))
        return chave
    }
}
