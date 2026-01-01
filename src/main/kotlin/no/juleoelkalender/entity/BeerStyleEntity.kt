package no.juleoelkalender.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "beer_style")
open class BeerStyleEntity(@Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID?, @Basic @Column(name = "name", nullable = false) var name: String)