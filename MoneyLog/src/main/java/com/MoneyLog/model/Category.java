package com.MoneyLog.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "category")
    private List<Expense> expenses = new ArrayList<>();

    @Builder
    public Category(User user, String name){
        this.user = user;
        this.name = name;
    }
}
