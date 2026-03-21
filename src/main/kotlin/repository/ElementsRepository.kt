package org.example.repository

import org.example.domain.ElementEntity
import org.example.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime

interface ElementsRepository : JpaRepository<ElementEntity, Long> {
    @Query(
        "SELECT e FROM ElementEntity e " +
                "WHERE e.userId = :userId AND e.element = :element " +
                "AND e.time >= :startTime AND e.time <= :endTime " +
                "ORDER BY e.time ASC"
    )
    fun findAllElements(
        @Param("userId") userId: Long,
        @Param("element") element: String,
        @Param("startTime") startTime: OffsetDateTime,
        @Param("endTime") endTime: OffsetDateTime
    ): List<ElementEntity>
}