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
import java.util.UUID

@Entity
@Table(name = "BeerCalendar")
class BeerCalendarEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
    @Basic @Column(name = "calendar_day", nullable = false) var day: Int,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "beerId", referencedColumnName = "id", nullable = false) var beer: BeerEntity,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "calendarId", referencedColumnName = "id", nullable = false) var calendar: CalendarEntity
)