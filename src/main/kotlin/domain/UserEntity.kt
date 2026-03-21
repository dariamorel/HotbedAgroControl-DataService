package org.example.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "ip_address", nullable = false, length = 255)
    var ipAddress: String = ""

    @Column(nullable = false, length = 255)
    var topic: String = ""

    @Column(name = "user_name", nullable = false, length = 255)
    var userName: String = ""

    @Column(nullable = false, length = 255)
    var password: String = ""

    @Column(nullable = false)
    var port: Int = 0
}