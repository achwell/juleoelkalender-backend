package no.juleoelkalender.entity

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "PasswordChangeRequest")
class PasswordChangeRequestEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
    @Basic @Column(name = "token", nullable = false) var token: String,
    @Basic @Column(name = "email", nullable = false) var email: String,
    @Basic @Column(name = "created", nullable = false) @CreationTimestamp(source = SourceType.DB) var created: ZonedDateTime,
    @UpdateTimestamp(source = SourceType.DB) @Column(name = "updated", nullable = false) var updatedDate: ZonedDateTime
)