package com.MoneyLog.Model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_category_user_name",
                columnNames = {"user_id", "name"}
        )
)
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Getter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Builder
    public Category(User user, String name){
        this.user = user;
        this.name = name;
    }
}
