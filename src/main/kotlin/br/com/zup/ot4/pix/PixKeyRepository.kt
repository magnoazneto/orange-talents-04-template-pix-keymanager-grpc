package br.com.zup.ot4.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {
    fun existsByKey(chavePix: String): Boolean
}