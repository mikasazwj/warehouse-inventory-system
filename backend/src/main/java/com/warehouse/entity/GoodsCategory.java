package com.warehouse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * 货物分类实体类
 * 支持树形结构的分类管理
 * 
 * @author Warehouse Team
 */
@Entity
@Table(name = "goods_categories", indexes = {
    @Index(name = "idx_category_code", columnList = "code", unique = true),
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_parent_id", columnList = "parent_id")
})
public class GoodsCategory extends BaseEntity {

    @NotBlank(message = "分类编码不能为空")
    @Size(max = 50, message = "分类编码长度不能超过50个字符")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 父分类
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GoodsCategory parent;

    /**
     * 子分类列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, name ASC")
    private List<GoodsCategory> children = new ArrayList<>();

    /**
     * 分类层级（根分类为1）
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /**
     * 分类路径（用/分隔，如：/1/2/3/）
     */
    @Column(name = "path", length = 500)
    private String path;

    // Constructors
    public GoodsCategory() {
    }

    public GoodsCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public GoodsCategory(String code, String name, GoodsCategory parent) {
        this.code = code;
        this.name = name;
        this.parent = parent;
        if (parent != null) {
            this.level = parent.getLevel() + 1;
            this.path = parent.getPath() + parent.getId() + "/";
        }
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public GoodsCategory getParent() {
        return parent;
    }

    public void setParent(GoodsCategory parent) {
        this.parent = parent;
        if (parent != null) {
            this.level = parent.getLevel() + 1;
            this.path = parent.getPath() + parent.getId() + "/";
        } else {
            this.level = 1;
            this.path = "/";
        }
    }

    public List<GoodsCategory> getChildren() {
        return children;
    }

    public void setChildren(List<GoodsCategory> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // Business methods
    
    /**
     * 判断是否为根分类
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 判断是否为叶子分类
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * 添加子分类
     */
    public void addChild(GoodsCategory child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
    }

    /**
     * 移除子分类
     */
    public void removeChild(GoodsCategory child) {
        if (children != null) {
            children.remove(child);
            child.setParent(null);
        }
    }

    /**
     * 获取完整的分类名称路径
     */
    public String getFullName() {
        if (parent == null) {
            return name;
        }
        return parent.getFullName() + " > " + name;
    }

    /**
     * 判断是否为指定分类的祖先
     */
    public boolean isAncestorOf(GoodsCategory category) {
        if (category == null || category.getPath() == null) {
            return false;
        }
        return category.getPath().contains("/" + this.getId() + "/");
    }

    /**
     * 判断是否为指定分类的后代
     */
    public boolean isDescendantOf(GoodsCategory category) {
        if (category == null) {
            return false;
        }
        return category.isAncestorOf(this);
    }

    @Override
    public String toString() {
        return "GoodsCategory{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", enabled=" + enabled +
                '}';
    }
}
