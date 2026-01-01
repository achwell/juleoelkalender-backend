package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.validator.constraints.Length
import java.util.UUID

@Entity
@Table(name = "Authority")
open class AuthorityEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Column(unique = true, nullable = false) var name: @Length(min = 1, max = 50) String,
        @ManyToMany @JoinTable(name = "roles_authorities", joinColumns = [JoinColumn(name = "authority_id")], inverseJoinColumns = [JoinColumn(name = "role_id")]) var users: MutableSet<UserEntity>
)