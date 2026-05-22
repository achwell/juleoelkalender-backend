package no.juleoelkalender.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.validator.constraints.Length
import java.util.UUID

@Entity
@Table(name = "Role")
class RoleEntity(
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