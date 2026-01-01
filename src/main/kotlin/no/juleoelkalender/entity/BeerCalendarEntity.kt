package no.juleoelkalender.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "BeerCalendar")
open class BeerCalendarEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Basic @Column(name = "calendar_day", nullable = false) var day: Int,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "beerId", referencedColumnName = "id", nullable = false) var beer: BeerEntity,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false) var calendar: CalendarEntity
)