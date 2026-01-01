package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "Review", uniqueConstraints = [UniqueConstraint(columnNames = ["beerId", "calendarId", "reviewerId"])])
open class ReviewEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Basic @Column(name = "ratingLabel", nullable = false) var ratingLabel: Double,
        @Basic @Column(name = "ratingLooks", nullable = false) var ratingLooks: Double,
        @Basic @Column(name = "ratingSmell", nullable = false) var ratingSmell: Double,
        @Basic @Column(name = "ratingTaste", nullable = false) var ratingTaste: Double,
        @Basic @Column(name = "ratingFeel", nullable = false) var ratingFeel: Double,
        @Basic @Column(name = "ratingOverall", nullable = false) var ratingOverall: Double,
        @Basic @Column(name = "comment") var comment: String?,
        @Basic @Column(name = "createdAt", nullable = false, updatable = false) @CreationTimestamp(source = SourceType.DB) var createdAt: ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt") private var updatedDate: ZonedDateTime?,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "beerId", referencedColumnName = "id", nullable = false) var beer: BeerEntity,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false) var calendar: CalendarEntity,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewerId", referencedColumnName = "id", nullable = false) var user: UserEntity
)