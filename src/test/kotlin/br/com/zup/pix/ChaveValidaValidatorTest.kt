package br.com.zup.pix

import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoChave
import br.com.zup.TipoConta
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import javax.validation.Validator

@MicronautTest
class ChaveValidaValidatorTest(
    private val validator: Validator
) {

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando cpf for um numero valido`() {
            val constrains = validator.validate(chave(TipoChave.CPF, "11624313779"))
            with(constrains) {
                Assertions.assertTrue(isEmpty())
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for um numero invalido`() {
            val constrains = validator.validate(chave(TipoChave.CPF, "11122233344"))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "chave Pix inválida (CPF)")
            }
        }

        @Test
        fun `nao deve ser valido quando cpf nao for informado`() {
            val constrains = validator.validate(chave(TipoChave.CPF, ""))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for nulo`() {
            val constrains = validator.validate(chave(TipoChave.CPF, null))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }


    }

    @Nested
    inner class Email {

        @Test
        fun `deve ser valido quando email for um endereco valido`() {
            val constrains = validator.validate(chave(TipoChave.EMAIL, "exemplo@exemplo.com.br"))
            with(constrains) {
                Assertions.assertTrue(isEmpty())
            }
        }

        @Test
        fun `nao deve ser valido quando email estiver em formato invalido`() {
            val constrains = validator.validate(chave(TipoChave.EMAIL, "exemploexemplo.com.br"))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "chave Pix inválida (EMAIL)")
            }
        }

        @Test
        fun `nao deve ser valido quando email nao for informado`() {
            val constrains = validator.validate(chave(TipoChave.EMAIL, ""))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for nulo`() {
            val constrains = validator.validate(chave(TipoChave.EMAIL, null))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }


    }

    @Nested
    inner class Celular {

        @Test
        fun `deve ser valido quando celular for um numero valido`() {
            val constrains = validator.validate(chave(TipoChave.CELULAR, "+5524988138043"))
            with(constrains) {
                Assertions.assertTrue(isEmpty())
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            val constrains = validator.validate(chave(TipoChave.CELULAR, "24988138043"))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "chave Pix inválida (CELULAR)")
            }
        }

        @Test
        fun `nao deve ser valido quando email nao for informado`() {
            val constrains = validator.validate(chave(TipoChave.CELULAR, ""))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }

        @Test
        fun `nao deve ser valido quando cpf for nulo`() {
            val constrains = validator.validate(chave(TipoChave.CELULAR, null))
            with(constrains) {
                Assertions.assertTrue(isNotEmpty())
                Assertions.assertEquals(first().message, "não deve estar em branco")
            }
        }


    }

    @Nested
    inner class Aleatoria {

        @Test
        fun `deve ser valido quando chave for vazio`() {
            val chave = chave(TipoChave.ALEATORIA, "")

           val request = RegistraChavePixRequest.newBuilder()
                .setChave(chave.chave)
                .setClienteId(chave.clienteId)
                .setTipoChave(chave.tipoChave)
                .setTipoConta(chave.tipoConta)
                .build()
            val constrains = validator.validate(request.toModel())

            with(constrains) {
                Assertions.assertTrue(isEmpty())
            }
        }
    }

    private fun chave(
        tipoChave: TipoChave?,
        chave: String?,
    ): ChavePix {
        val chave = ChavePix(
            clienteId = UUID.randomUUID().toString(),
            tipoChave = tipoChave,
            chave = chave,
            tipoConta = TipoConta.CONTA_CORRENTE
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