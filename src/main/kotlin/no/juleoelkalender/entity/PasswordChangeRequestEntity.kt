package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "PasswordChangeRequest")
open class PasswordChangeRequestEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Basic @Column(name = "token", nullable = false) var token: String,
        @Basic @Column(name = "email", nullable = false) var email: String,
        @Basic @Column(name = "created", nullable = false) @CreationTimestamp(source = SourceType.DB) var created: ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updated") var updatedDate: ZonedDateTime?
)