package org.example.repository

import org.example.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UsersRepository : JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u WHERE u.ipAddress = :ipAddress AND u.topic = :topic AND u.userName = :userName AND u.password = :password AND u.port = :port")
    fun findExistingUser(
        @Param("ipAddress") ipAddress: String,
        @Param("topic") topic: String,
        @Param("userName") userName: String,
        @Param("password") password: String,
        @Param("port") port: Int
    ): UserEntity?
}