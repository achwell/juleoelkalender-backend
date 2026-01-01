package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "Calendar", uniqueConstraints = [UniqueConstraint(columnNames = ["calendar_year", "name"])])
open class CalendarEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID?,
        @Basic @Column(name = "name", nullable = false) var name: String,
        @Basic @Column(name = "calendar_year", nullable = false) var year: Int,
        @Basic @Column(name = "published", nullable = false) var published: Boolean,
        @Basic @Column(name = "archived", nullable = false) var archived: Boolean,
        @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY) var beerCalendars: MutableSet<BeerCalendarEntity>,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "calendarTokenId", referencedColumnName = "id", nullable = false) var calendarToken: CalendarTokenEntity,
        @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY) var reviews: MutableSet<ReviewEntity>,
        @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt", nullable = false, updatable = false) var createdDate: ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt") var updatedDate: ZonedDateTime?
)