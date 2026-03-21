package org.example.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "data_history")
class ElementEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 1,

    @CreationTimestamp
    @Column(nullable = false)
    var time: OffsetDateTime? = null,

    @Column(nullable = false, length = 255)
    var element: String = "",

    @Column(nullable = false, length = 255)
    var response: String = ""
)