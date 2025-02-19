package com.example.domains;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    private String id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "user_id", unique = true)
    private String chatId;
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;
    @Column(name = "v_card", unique = true)
    private String vCard;
    @Column(name = "user_id",unique = true)
    private long userId;
    @Column(name = "role")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    @Column(name = "lang")
    @Builder.Default
    private String lang = "en";

}

