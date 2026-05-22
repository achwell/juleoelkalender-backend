package no.juleoelkalender.entity

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "Device")
class DeviceEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
    @Basic @Column var mobileVendor: String,
    @Basic @Column var mobileModel: String,
    @Basic @Column(nullable = false) var mobile: Boolean = false,
    @Basic @Column var osName: String,
    @Basic @Column var osVersion: String,
    @Basic @Column var browserName: String,
    @Basic @Column var browserVersion: String,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false) var user: UserEntity,
    @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt", nullable = false) var createdDate: ZonedDateTime,
    @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt", nullable = false) var updatedDate: ZonedDateTime
)