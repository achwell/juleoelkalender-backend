package no.juleoelkalender.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID

@Entity
@Table(name = "Beer")
open class BeerEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Basic @Column(name = "name", nullable = false) var name: @NotNull String,
        @Basic @Column(name = "style", nullable = false) var style: @NotNull String,
        @Basic @Column(name = "description") var description: String?,
        @Basic @Column(name = "abv", nullable = false) var abv: @NotNull Double,
        @Basic @Column(name = "ibu", nullable = false) var ibu: @NotNull Double,
        @Basic @Column(name = "ebc", nullable = false) var ebc: @NotNull Double,
        @Basic @Column(name = "recipe") var recipe: String?,
        @Basic @Column(name = "untapped") var untapped: String?,
        @Basic @Column(name = "brewedDate", nullable = false) var brewedDate: @NotNull ZonedDateTime,
        @Basic @Column(name = "bottleDate", nullable = false) var bottleDate: @NotNull ZonedDateTime,
        @Basic @Column(name = "archived", nullable = false) var archived: @NotNull Boolean,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "userId", referencedColumnName = "id", nullable = false) var user: @NotNull UserEntity,
        @OneToMany(mappedBy = "beer", fetch = FetchType.LAZY) var beerCalendars: MutableSet<BeerCalendarEntity>,
        @OneToMany(mappedBy = "beer", fetch = FetchType.LAZY) var reviews: MutableSet<ReviewEntity>,
        @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt", nullable = false, updatable = false) var createdDate: @NotNull ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt") var updatedDate: ZonedDateTime?,
        @Column(name = "desired_date") var desiredDate: @Min(1) @Max(24) Int?
)