package no.juleoelkalender.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.validator.constraints.Length
import java.util.UUID

@Entity
@Table(name = "Authority")
class AuthorityEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
    @Column(unique = true, nullable = false) var name: @Length(min = 1, max = 50) String,
    @ManyToMany @JoinTable(name = "roles_authorities", joinColumns = [JoinColumn(name = "authority_id")], inverseJoinColumns = [JoinColumn(name = "role_id")]) var users: MutableSet<UserEntity>
)