package com.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类DTO
 * 
 * @author Warehouse Team
 */
public class GoodsCategoryDTO {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean enabled;
    private Long parentId;
    private String parentName;
    private Integer level;
    private String path;
    private List<GoodsCategoryDTO> children;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public GoodsCategoryDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public List<GoodsCategoryDTO> getChildren() { return children; }
    public void setChildren(List<GoodsCategoryDTO> children) { this.children = children; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    /**
     * 创建请求DTO
     */
    public static class CreateRequest {
        @NotBlank(message = "分类编码不能为空")
        @Size(max = 50, message = "分类编码长度不能超过50个字符")
        private String code;

        @NotBlank(message = "分类名称不能为空")
        @Size(max = 100, message = "分类名称长度不能超过100个字符")
        private String name;

        @Size(max = 500, message = "描述长度不能超过500个字符")
        private String description;

        private Integer sortOrder = 0;
        private Boolean enabled = true;
        private Long parentId;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }

    /**
     * 更新请求DTO
     */
    public static class UpdateRequest {
        @Size(max = 100, message = "分类名称长度不能超过100个字符")
        private String name;

        @Size(max = 500, message = "描述长度不能超过500个字符")
        private String description;

        private Integer sortOrder;
        private Boolean enabled;
        private Long parentId;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
    }
}
