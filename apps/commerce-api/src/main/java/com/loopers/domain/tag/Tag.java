package com.loopers.domain.tag;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "color", nullable = false, length = 7)
    private String color;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    protected Tag() {}

    private Tag(User owner, String name, String color, boolean isDefault) {
        guardName(name);
        guardColor(color);
        this.owner = owner;
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
    }

    public static Tag create(User owner, String name, String color) {
        return new Tag(owner, name, color, false);
    }

    public static Tag createDefault(User owner, String name, String color) {
        return new Tag(owner, name, color, true);
    }

    public User getOwner() { return owner; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public boolean isDefault() { return isDefault; }

    public void changeName(String newName) {
        guardName(newName);
        this.name = newName;
    }

    public void changeColor(String newColor) {
        guardColor(newColor);
        this.color = newColor;
    }

    public void guardDeletable() {
        if (this.isDefault) {
            throw new CoreException(ErrorType.BAD_REQUEST, "기본 태그는 삭제할 수 없습니다.");
        }
    }

    private void guardName(String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "태그 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > 20) {
            throw new CoreException(ErrorType.BAD_REQUEST, "태그 이름은 최대 20자까지 입력할 수 있습니다.");
        }
    }

    private void guardColor(String color) {
        if (color == null || !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "색상은 #RRGGBB 형식이어야 합니다.");
        }
    }
}
