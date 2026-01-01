package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "Device")
open class DeviceEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Basic @Column var mobileVendor: String,
        @Basic @Column var mobileModel: String,
        @Basic @Column var mobile: Boolean,
        @Basic @Column var osName: String,
        @Basic @Column var osVersion: String,
        @Basic @Column var browserName: String,
        @Basic @Column var browserVersion: String,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false) var user: UserEntity,
        @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt") var createdDate: ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt") var updatedDate: ZonedDateTime?
)