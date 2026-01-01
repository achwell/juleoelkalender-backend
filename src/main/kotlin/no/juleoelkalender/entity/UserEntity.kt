package no.juleoelkalender.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import org.apache.commons.lang3.ObjectUtils
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.SourceType
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.Stream

@Entity
@Table(name = "User")
open class UserEntity(
        @Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id", updatable = false, nullable = false) var id: UUID?,
        @Basic @Column(name = "first_name", nullable = false) var firstName: String,
        @Basic @Column(name = "middle_name") var middleName: String?,
        @Basic @Column(name = "last_name", nullable = false) var lastName: String,
        @Basic @Column(name = "email", nullable = false) var email: @Email String,
        @Basic @Column(name = "password", nullable = false) var password: String,
        @Basic @Column(name = "area") var area: String?,
        @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false) var role: RoleEntity,
        @Basic @Column(name = "locked", nullable = false) var locked: Boolean,
        @OneToMany(mappedBy = "user") var beers: MutableSet<BeerEntity>,
        @OneToMany(mappedBy = "user") var devices: MutableSet<DeviceEntity>,
        @ManyToMany(fetch = FetchType.LAZY) @JoinTable(name = "user_calendar_token", joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "calendar_token_id")]) var calendarToken: MutableSet<CalendarTokenEntity>,
        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY) var reviews: MutableSet<ReviewEntity>,
        @Basic @Column(name = "last_login_date") var lastLoginDate: ZonedDateTime?,
        @CreationTimestamp(source = SourceType.DB) @Column(name = "createdAt", updatable = false, nullable = false) var createdDate: ZonedDateTime,
        @UpdateTimestamp(source = SourceType.DB) @Column(name = "updatedAt") var updatedDate: ZonedDateTime?,
        @Basic @Column(name = "facebook_user_id") var facebookUserId: String?,
        @Basic @Column(name = "image_url") var imageUrl: String?,
        @Basic @Column(name = "image_height") var imageHeight: Int?,
        @Basic @Column(name = "image_width") var imageWidth: Int?,
        @Basic @Column(name = "image_silhouette", nullable = false) var imageSilhouette: Boolean = false,
) {

    @get:Transient
    val name: String
        get() = Stream.of<@NotNull String?>(this.firstName, this.middleName, this.lastName)
                .filter { `object`: String? -> ObjectUtils.isNotEmpty(`object`) }
                .collect(Collectors.joining(" "))
}
