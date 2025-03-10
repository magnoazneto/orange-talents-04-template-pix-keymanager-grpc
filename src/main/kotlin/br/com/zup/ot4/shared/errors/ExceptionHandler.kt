package br.com.zup.ot4.shared.errors

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.Metadata

interface ExceptionHandler<E: Exception> {

    /**
     * Handle exception and maps it to StatusWithDetails
     */
    fun handle(e: E): StatusWithDetails

    /**
     * Verifies whether this instance can handle the specified exception or not
     */
    fun supports(e: Exception): Boolean

    /**
     * simple wrapper for Status and Metadata (trailers)
     */
    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()){
        constructor(se: StatusRuntimeException): this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status) : this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException()
        }
    }
}