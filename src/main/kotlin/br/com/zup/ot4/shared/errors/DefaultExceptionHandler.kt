package br.com.zup.ot4.shared.errors

import br.com.zup.ot4.shared.exceptions.ExistingPixKeyException
import br.com.zup.ot4.shared.errors.ExceptionHandler.StatusWithDetails
import br.com.zup.ot4.shared.exceptions.PixKeyNotFoundException
import io.grpc.Status
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when(e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is ExistingPixKeyException -> Status.ALREADY_EXISTS.withDescription(e.message)
            is PixKeyNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            else -> Status.UNKNOWN.withDescription(e.message)
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}