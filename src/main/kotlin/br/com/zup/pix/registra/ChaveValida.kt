package br.com.zup.pix

import br.com.zup.TipoChave
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ChaveValidaValidator::class])
annotation class ChaveValida(
    val message: String = "chave Pix inválida (\${validatedValue.tipoChave})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)
class ChaveValidaValidator : ConstraintValidator<ChaveValida, ChavePix> {

    override fun isValid(
        value: ChavePix,
        context: ConstraintValidatorContext?
    ): Boolean {

        val tipo = value.tipoChave
        val chave = value.chave

        if (tipo == null || chave.isNullOrBlank()) return true

        val valid = when (tipo) {
            TipoChave.CPF -> CPFValidator().run {
                initialize(null)
                isValid(chave, context)
            }
            TipoChave.EMAIL -> EmailValidator().isValid(chave, context)

            TipoChave.CELULAR -> chave!!.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())

            else -> true
        }

        if(!valid) context?.setField("chave")

        return valid
    }

    private fun ConstraintValidatorContext.setField(fieldName: String) {
        this.disableDefaultConstraintViolation()
        this.buildConstraintViolationWithTemplate(this.defaultConstraintMessageTemplate)
            .addPropertyNode(fieldName)
            .addConstraintViolation()
    }
}
