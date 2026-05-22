package no.juleoelkalender.entity

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "CalendarToken")
class CalendarTokenEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
    @Basic @Column(name = "token", nullable = false) var token: String,
    @Basic @Column(name = "name", nullable = false) var name: String,
    @Basic @Column(name = "active", nullable = false) var active: Boolean = false,
    @OneToMany(mappedBy = "calendarToken", fetch = FetchType.LAZY) var calendars: MutableSet<CalendarEntity>,
    @ManyToMany @JoinTable(name = "user_calendar_token", joinColumns = [JoinColumn(name = "calendar_token_id")], inverseJoinColumns = [JoinColumn(name = "user_id")]) var users: MutableSet<UserEntity>,
    @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt", nullable = false) var createdDate: ZonedDateTime,
    @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt", nullable = false) var updatedDate: ZonedDateTime
)