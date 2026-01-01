package no.juleoelkalender.entity

import jakarta.persistence.*
import org.hibernate.validator.constraints.Length
import java.util.UUID

@Entity
@Table(name = "Role")
open class RoleEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID,
        @Column(unique = true, nullable = false) @Enumerated(EnumType.STRING) var name: @Length(min = 1, max = 50) RoleNameEntity,
        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(
                name = "roles_authorities",
                joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
                inverseJoinColumns = [JoinColumn(name = "authority_id", referencedColumnName = "id")]
        ) var authorities: MutableSet<AuthorityEntity>,
        @OneToMany(mappedBy = "role", fetch = FetchType.LAZY) var users: MutableSet<UserEntity>
)