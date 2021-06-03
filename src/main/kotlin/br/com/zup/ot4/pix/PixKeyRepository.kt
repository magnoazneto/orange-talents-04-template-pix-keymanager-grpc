package br.com.zup.ot4.pix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixKeyRepository : JpaRepository<PixKey, Long> {
    fun existsByKey(chavePix: String): Boolean
    fun findByUuid(pixId: UUID): Optional<PixKey>
    fun findByKey(key: String): Optional<PixKey>
    @Query("select * from pix_key where holder_id=:holderId", nativeQuery = true)
    fun findAllByOwnerId(holderId: String?): Set<PixKey>
}