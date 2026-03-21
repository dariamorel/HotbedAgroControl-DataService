package org.example.repository

import org.example.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UsersRepository : JpaRepository<UserEntity, Long>