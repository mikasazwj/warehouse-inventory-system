<template>
  <div class="goods-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">
            <el-icon class="title-icon"><Box /></el-icon>
            货物管理
          </h1>
          <p class="page-subtitle">管理系统中的所有货物信息和分类</p>
        </div>
        <div class="header-actions" :class="{ 'mobile-actions': isMobile }">
          <el-button @click="categoryDialogVisible = true" :size="isMobile ? 'small' : 'default'">
            <el-icon><Collection /></el-icon>
            分类管理
          </el-button>
          <el-button type="primary" @click="handleAdd" :size="isMobile ? 'small' : 'default'">
            <el-icon><Plus /></el-icon>
            新增货物
          </el-button>
          <el-button @click="importDialogVisible = true" :size="isMobile ? 'small' : 'default'">
            <el-icon><Upload /></el-icon>
            导入数据
          </el-button>
          <GoodsExport
            :selected-goods="selectedRows"
            :current-page-data="tableData"
            :search-params="searchForm"
            :categories="categories"
            :size="isMobile ? 'small' : 'default'"
            @refresh="loadData"
          />
        </div>
      </div>
    </div>

    <!-- 搜索和统计区域 -->
    <div class="search-section">
      <div class="search-card">
        <div class="search-header">
          <h3>筛选条件</h3>
          <div class="search-stats">
            <span class="stat-item">
              <el-icon><Box /></el-icon>
              总计：{{ pagination.total }} 个货物
            </span>
            <span class="stat-item">
              <el-icon><CircleCheck /></el-icon>
              启用：{{ enabledCount }} 个
            </span>
            <span class="stat-item">
              <el-icon><CircleClose /></el-icon>
              禁用：{{ disabledCount }} 个
            </span>
          </div>
        </div>
        <el-form :model="searchForm" class="search-form" :class="{ 'mobile-form': isMobile }">
          <div class="form-content">
            <div class="search-inputs">
              <el-form-item label="名称" class="search-item">
                <el-input
                  v-model="searchForm.keyword"
                  placeholder="请输入货物名称或编码"
                  clearable
                  prefix-icon="Search"
                  class="search-input"
                />
              </el-form-item>
              <el-form-item label="分类" class="search-item">
                <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable class="search-select">
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  >
                    <el-icon><Collection /></el-icon>
                    {{ category.name }}
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="状态" class="search-item">
                <el-select v-model="searchForm.enabled" placeholder="请选择状态" clearable class="search-select">
                  <el-option label="启用" :value="true">
                    <el-icon><CircleCheck /></el-icon>
                    启用
                  </el-option>
                  <el-option label="禁用" :value="false">
                    <el-icon><CircleClose /></el-icon>
                    禁用
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                <span v-if="!isMobile">搜索</span>
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                <span v-if="!isMobile">重置</span>
              </el-button>
            </div>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>货物列表</h3>
          <div class="table-actions">
            <el-button size="small" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          class="goods-table"
          empty-text="暂无货物数据"
          :header-cell-style="{ background: '#f8f9fa', color: '#606266', fontWeight: '600' }"
        >
          <el-table-column prop="code" label="货物编码" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="code-cell">
                <el-icon class="code-icon"><Postcard /></el-icon>
                <span class="code-text">{{ row.code }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="货物名称" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="name-cell">
                <el-icon class="name-icon"><Box /></el-icon>
                <span class="name-text">{{ row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="category-cell">
                <el-icon class="category-icon"><Collection /></el-icon>
                <span class="category-text">{{ row.categoryName || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="unit" label="单位" width="80" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="unit-cell">
                <el-icon class="unit-icon"><Scale /></el-icon>
                <span class="unit-text">{{ row.unit || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="specification" label="规格/型号" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="spec-cell">
                <el-icon class="spec-icon"><Document /></el-icon>
                <span class="spec-text">{{ row.specification || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="库存信息" width="120" align="center">
            <template #default="{ row }">
              <div class="stock-cell">
                <div class="stock-info">
                  <span class="stock-label">最小库存</span>
                  <span class="stock-value">{{ row.minStock || 0 }}</span>
                </div>
                <div class="stock-info">
                  <span class="stock-label">最大库存</span>
                  <span class="stock-value">{{ row.maxStock || 0 }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.enabled ? 'success' : 'danger'"
                effect="dark"
                class="status-tag"
              >
                <div class="status-content">
                  <el-icon><component :is="row.enabled ? 'CircleCheck' : 'CircleClose'" /></el-icon>
                  <span>{{ row.enabled ? '启用' : '禁用' }}</span>
                </div>
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="创建时间" width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="time-cell">
                <el-icon class="time-icon"><Clock /></el-icon>
                <span class="time-text">{{ formatDateTime(row.createdTime) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 80 : undefined" :min-width="isMobile ? 80 : 280" fixed="right" align="center">
            <template #default="{ row }">
              <!-- 桌面端按钮组 -->
              <div class="action-buttons desktop-actions">
                <el-button type="primary" size="small" @click="handleView(row)" class="action-btn">
                  <el-icon><View /></el-icon>
                  <span class="btn-text">查看</span>
                </el-button>
                <el-button type="warning" size="small" @click="handleEdit(row)" class="action-btn">
                  <el-icon><Edit /></el-icon>
                  <span class="btn-text">编辑</span>
                </el-button>
                <el-button
                  :type="row.enabled ? 'danger' : 'success'"
                  size="small"
                  @click="handleToggleStatus(row)"
                  class="action-btn"
                >
                  <el-icon><Switch /></el-icon>
                  <span class="btn-text">{{ row.enabled ? '禁用' : '启用' }}</span>
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDelete(row)"
                  :disabled="!row.enabled"
                  class="action-btn"
                >
                  <el-icon><Delete /></el-icon>
                  <span class="btn-text">删除</span>
                </el-button>
              </div>

              <!-- 移动端下拉菜单 -->
              <div class="mobile-actions">
                <el-dropdown trigger="click" @command="(command) => handleMobileAction(command, row)">
                  <el-button type="primary" size="small">
                    操作
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="view">
                        <el-icon><View /></el-icon>
                        查看详情
                      </el-dropdown-item>
                      <el-dropdown-item command="edit">
                        <el-icon><Edit /></el-icon>
                        编辑信息
                      </el-dropdown-item>
                      <el-dropdown-item command="toggle">
                        <el-icon><Switch /></el-icon>
                        {{ row.enabled ? '禁用货物' : '启用货物' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" :disabled="!row.enabled" divided>
                        <el-icon><Delete /></el-icon>
                        删除货物
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="isMobile ? [5, 10, 20] : [10, 20, 50, 100]"
            :layout="isMobile ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
            :small="isMobile"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="goods-pagination"
          />
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :width="isMobile ? '95%' : '1000px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="goods-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header goods-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Plus v-if="!form.id" /><Edit v-else /></el-icon>
              </div>
              <div class="title-content">
                <h2>{{ dialogTitle }}</h2>
                <p>{{ form.id ? '修改货物信息和规格' : '创建新的货物档案' }}</p>
              </div>
            </div>
            <el-button @click="dialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 基本信息卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>基本信息</span>
          </div>
          <el-form
            ref="formRef"
            :model="form"
            :rules="formRules"
            :label-width="isMobile ? '80px' : '100px'"
            :label-position="isMobile ? 'top' : 'right'"
          >
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="货物编码" prop="code">
              <el-input
                v-model="form.code"
                placeholder="系统自动生成"
                readonly
                disabled
                style="background-color: #f5f7fa;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="货物名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入货物名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="规格/型号" prop="specification">
              <el-input v-model="form.specification" placeholder="请输入规格/型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如：个、箱、kg" />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 8">
            <el-form-item label="状态" prop="enabled">
              <el-switch
                v-model="form.enabled"
                active-text="启用"
                inactive-text="禁用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="isMobile ? 10 : 20">
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="最低库存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="isMobile ? 24 : 12">
            <el-form-item label="最高库存" prop="maxStock">
              <el-input-number v-model="form.maxStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入货物描述"
          />
        </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :width="isMobile ? '95%' : '1000px'"
      :fullscreen="isMobile"
      class="goods-detail-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header goods-detail-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="title-content">
                <h2>货物详情</h2>
                <p>查看货物的详细信息和规格参数</p>
              </div>
            </div>
            <el-button @click="viewDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <div class="detail-content" v-if="viewData">
          <!-- 基本信息卡片 -->
          <div class="info-card">
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>基本信息</span>
            </div>
            <div class="card-content">
              <el-descriptions :column="isMobile ? 1 : 2" border>
          <el-descriptions-item label="货物编码">{{ viewData.code }}</el-descriptions-item>
          <el-descriptions-item label="货物名称">{{ viewData.name }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ viewData.categoryName }}</el-descriptions-item>

          <el-descriptions-item label="单位">{{ viewData.unit }}</el-descriptions-item>
          <el-descriptions-item label="规格/型号">{{ viewData.specification }}</el-descriptions-item>
          <el-descriptions-item label="最低库存">{{ viewData.minStock }}</el-descriptions-item>
          <el-descriptions-item label="最高库存">{{ viewData.maxStock }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewData.enabled ? 'success' : 'danger'">
              {{ viewData.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(viewData.createdTime) }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog
      v-model="categoryDialogVisible"
      :width="isMobile ? '95%' : '800px'"
      :fullscreen="isMobile"
      class="category-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header category-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <div class="title-content">
                <h2>分类管理</h2>
                <p>管理货物分类信息和层级结构</p>
              </div>
            </div>
            <el-button @click="categoryDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 分类列表卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            <span>分类列表</span>
          </div>
          <div class="category-management">
            <div class="category-header">
              <el-button type="primary" size="small" @click="handleAddCategory">
                <el-icon><Plus /></el-icon>
                新增分类
              </el-button>
            </div>
            <el-table :data="categories" size="small">
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="description" label="描述" />
          <el-table-column label="操作" width="150" align="center">
            <template #default="{ row }">
              <div style="display: flex; gap: 8px; justify-content: center;">
                <el-button type="primary" size="small" @click="handleEditCategory(row)">
                  编辑
                </el-button>
                <el-button type="danger" size="small" @click="handleDeleteCategory(row)">
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
            </el-table>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="categoryFormVisible"
      :width="isMobile ? '95%' : '700px'"
      :fullscreen="isMobile"
      class="category-form-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header category-form-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Plus v-if="!categoryForm.id" /><Edit v-else /></el-icon>
              </div>
              <div class="title-content">
                <h2>{{ categoryForm.id ? '编辑分类' : '新增分类' }}</h2>
                <p>{{ categoryForm.id ? '修改分类信息' : '创建新的分类' }}</p>
              </div>
            </div>
            <el-button @click="categoryFormVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 分类信息卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>分类信息</span>
          </div>
          <el-form
            :model="categoryForm"
            :rules="categoryRules"
            ref="categoryFormRef"
            :label-width="isMobile ? '70px' : '80px'"
            :label-position="isMobile ? 'top' : 'right'"
          >
            <el-form-item label="分类编码" prop="code">
              <el-input v-model="categoryForm.code" placeholder="请输入分类编码" :disabled="!!categoryForm.id" />
            </el-form-item>
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="categoryFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 导入数据对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      :width="isMobile ? '95%' : '1000px'"
      :close-on-click-modal="false"
      :fullscreen="isMobile"
      class="import-dialog modern-dialog"
      :show-close="false"
      append-to-body
      :z-index="3000"
      destroy-on-close
    >
      <template #header>
        <div class="dialog-header import-header">
          <div class="header-content">
            <div class="dialog-title">
              <div class="title-icon">
                <el-icon><Upload /></el-icon>
              </div>
              <div class="title-content">
                <h2>导入货物数据</h2>
                <p>批量导入货物信息到系统</p>
              </div>
            </div>
            <el-button @click="importDialogVisible = false" class="dialog-close" text>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <!-- 导入步骤卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Operation /></el-icon>
            <span>导入步骤</span>
          </div>
          <div class="import-content">
            <!-- 步骤指示器 -->
            <el-steps :active="importStep" finish-status="success" align-center class="import-steps">
              <el-step title="选择文件" />
              <el-step title="数据预览" />
              <el-step title="导入完成" />
            </el-steps>
          </div>
        </div>

        <!-- 导入内容卡片 -->
        <div class="form-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            <span>{{ importStep === 0 ? '文件上传' : importStep === 1 ? '数据预览' : '导入结果' }}</span>
          </div>

        <!-- 步骤1: 文件选择 -->
        <div v-if="importStep === 0" class="step-content">
          <div class="upload-area">
            <el-upload
              ref="uploadRef"
              class="upload-dragger"
              drag
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-exceed="handleExceed"
              accept=".xlsx,.xls"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将Excel文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  只能上传 .xlsx/.xls 文件，且不超过 10MB
                </div>
              </template>
            </el-upload>
          </div>

          <!-- 模板下载 -->
          <div class="template-section">
            <el-divider content-position="center">或</el-divider>
            <div class="template-download">
              <el-icon><Document /></el-icon>
              <span>没有模板？</span>
              <el-button type="primary" link @click="downloadTemplate">
                下载导入模板
              </el-button>
            </div>
          </div>

          <!-- 导入说明 -->
          <div class="import-tips">
            <h4>导入说明：</h4>
            <ul>
              <li>支持 Excel 格式文件（.xlsx, .xls）</li>
              <li>第一行必须是表头，包含：货物名称、分类编码、单位、规格/型号、最小库存、最大库存、描述</li>
              <li><strong>货物名称 + 分类 + 规格/型号</strong> 完全相同的货物会被跳过</li>
              <li>如果分类编码不存在，系统会自动创建该分类</li>
              <li>货物编码由系统自动生成，无需手动填写</li>
              <li>最小库存和最大库存必须是数字</li>
              <li>单次最多导入 1000 条数据</li>
              <li><em>示例：相同名称的货物可以有不同规格，如"iPhone15 128G黑色"和"iPhone15 256G白色"</em></li>
            </ul>
          </div>
        </div>

        <!-- 步骤2: 数据预览 -->
        <div v-if="importStep === 1" class="step-content">
          <div class="preview-info">
            <el-alert
              :title="`共解析到 ${previewData.length} 条数据，请确认后导入`"
              type="info"
              :closable="false"
              show-icon
            />
          </div>

          <div class="preview-table">
            <el-table
              :data="previewData.slice(0, 10)"
              border
              size="small"
              max-height="300"
              style="width: 100%"
            >
              <el-table-column prop="code" label="货物编码" width="120" />
              <el-table-column prop="name" label="货物名称" width="150" />
              <el-table-column prop="categoryCode" label="分类编码" width="100" />
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="specification" label="规格/型号" width="120" />
              <el-table-column prop="minStock" label="最小库存" width="100" />
              <el-table-column prop="maxStock" label="最大库存" width="100" />
              <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
            </el-table>
          </div>

          <div v-if="previewData.length > 10" class="preview-more">
            <el-text type="info">仅显示前10条数据，共 {{ previewData.length }} 条</el-text>
          </div>
        </div>

        <!-- 步骤3: 导入结果 -->
        <div v-if="importStep === 2" class="step-content">
          <div class="import-result">
            <el-result
              :icon="importResult.success ? 'success' : 'error'"
              :title="importResult.success ? '导入成功' : '导入失败'"
              :sub-title="importResult.message"
            >
              <template #extra>
                <div v-if="importResult.success" class="result-stats">
                  <el-statistic title="成功导入" :value="importResult.successCount" />
                  <el-statistic v-if="importResult.failCount > 0" title="失败数量" :value="importResult.failCount" />
                  <el-statistic v-if="importResult.warnings && importResult.warnings.length > 0" title="警告数量" :value="importResult.warnings.length" />
                </div>

                <div v-if="importResult.warnings && importResult.warnings.length > 0" class="warning-list">
                  <h4>警告信息：</h4>
                  <el-scrollbar max-height="150px">
                    <ul>
                      <li v-for="(warning, index) in importResult.warnings" :key="index" class="warning-item">
                        第 {{ warning.row }} 行：{{ warning.message }}
                      </li>
                    </ul>
                  </el-scrollbar>
                </div>

                <div v-if="importResult.errors && importResult.errors.length > 0" class="error-list">
                  <h4>错误详情：</h4>
                  <el-scrollbar max-height="200px">
                    <ul>
                      <li v-for="(error, index) in importResult.errors" :key="index" class="error-item">
                        第 {{ error.row }} 行：{{ error.message }}
                      </li>
                    </ul>
                  </el-scrollbar>
                </div>
              </template>
            </el-result>
          </div>
        </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleImportCancel">取消</el-button>
          <el-button v-if="importStep === 0" type="primary" @click="handleFileUpload" :disabled="!selectedFile">
            解析文件
          </el-button>
          <el-button v-if="importStep === 1" @click="importStep = 0">上一步</el-button>
          <el-button v-if="importStep === 1" type="primary" @click="handleImportConfirm" :loading="importLoading">
            确认导入
          </el-button>
          <el-button v-if="importStep === 2" type="primary" @click="handleImportFinish">
            完成
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'
import dayjs from 'dayjs'
import { useDeviceDetection } from '@/utils/responsive'
import * as XLSX from 'xlsx'
import { Plus, Edit, View, Close, InfoFilled, FolderOpened, List, Upload, Operation, Document } from '@element-plus/icons-vue'
import GoodsExport from '@/components/GoodsExport.vue'

// 响应式检测
const { isMobile, isTablet, isDesktop } = useDeviceDetection()

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const categoryFormVisible = ref(false)
const importDialogVisible = ref(false)
const tableData = ref([])
const viewData = ref(null)
const categories = ref([])

const formRef = ref()
const categoryFormRef = ref()
const uploadRef = ref()

// 导入相关数据
const importStep = ref(0)
const importLoading = ref(false)
const selectedFile = ref(null)
const previewData = ref([])
const importResult = ref({
  success: false,
  message: '',
  successCount: 0,
  failCount: 0,
  errors: [],
  warnings: []
})



// 统计数据
const enabledCount = computed(() => tableData.value.filter(item => item.enabled).length)
const disabledCount = computed(() => tableData.value.filter(item => !item.enabled).length)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: null,
  enabled: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  id: null,
  code: '',
  name: '',
  categoryId: null,
  unit: '',
  specification: '',
  minStock: 0,
  maxStock: 0,
  enabled: true,
  description: ''
})

// 分类表单
const categoryForm = reactive({
  id: null,
  code: '',
  name: '',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入货物名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

const categoryRules = {
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return form.id ? '编辑货物' : '新增货物'
})

// 方法
const formatDateTime = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

const loadData = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId,
      enabled: searchForm.enabled
    }
    
    const response = await request.get('/goods', params)
    if (response.success) {
      tableData.value = response.data.content || []
      pagination.total = response.data.totalElements || 0
    }
  } catch (error) {
    console.error('加载货物数据失败:', error)
    // 清空数据
    tableData.value = []
    pagination.total = 0
    if (error.code !== 'ERR_CONNECTION_REFUSED') {
      ElMessage.error('加载货物数据失败')
    }
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const response = await request.get('/goods-categories')
    if (response.success) {
      categories.value = response.data || []
    }
  } catch (error) {
    console.error('加载分类数据失败:', error)
    categories.value = [
      { id: 1, name: '电子产品', description: '电子设备类产品' },
      { id: 2, name: '办公用品', description: '办公相关用品' }
    ]
  }
}



const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.enabled = null
  pagination.page = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  loadData()
}

const handleAdd = () => {
  resetForm()
  // 为新增货物自动生成编码
  form.code = 'GOODS' + Date.now()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleView = (row) => {
  viewData.value = row
  viewDialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    const action = row.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}该货物吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.put(`/goods/${row.id}/toggle-status`)
    if (response.success) {
      ElMessage.success(`${action}成功`)
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    const url = form.id ? `/goods/${form.id}` : '/goods/create'
    const method = form.id ? 'put' : 'post'
    
    const response = await request[method](url, form)
    if (response.success) {
      ElMessage.success(form.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadData()
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除货物"${row.name}"吗？删除后不可恢复！`,
      '危险操作',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )

    const response = await request.delete(`/goods/${row.id}`)
    if (response.success) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleMobileAction = (command, row) => {
  switch (command) {
    case 'view':
      handleView(row)
      break
    case 'edit':
      handleEdit(row)
      break
    case 'toggle':
      handleToggleStatus(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}





const handleRefresh = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    categoryId: null,
    unit: '',
    specification: '',
    minStock: 0,
    maxStock: 0,
    enabled: true,
    description: ''
  })
  formRef.value?.resetFields()
}

// 导入相关方法
const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能选择一个文件')
}

const downloadTemplate = () => {
  try {
    // 创建模板数据
    const templateData = [
      ['货物名称', '分类编码', '单位', '规格/型号', '最小库存', '最大库存', '描述'],
      ['笔记本电脑', 'COMPUTER', '台', '联想ThinkPad-8G', 5, 50, '办公用笔记本电脑 8G内存'],
      ['笔记本电脑', 'COMPUTER', '台', '联想ThinkPad-16G', 3, 30, '办公用笔记本电脑 16G内存'],
      ['无线鼠标', 'ACCESSORY', '个', '罗技M705-黑色', 10, 100, '无线办公鼠标 黑色'],
      ['无线鼠标', 'ACCESSORY', '个', '罗技M705-白色', 8, 80, '无线办公鼠标 白色'],
      ['机械键盘', 'ACCESSORY', '个', 'Cherry MX-红轴', 8, 80, '机械轴键盘 红轴'],
      ['机械键盘', 'ACCESSORY', '个', 'Cherry MX-青轴', 5, 50, '机械轴键盘 青轴'],
      ['iPhone15', 'MOBILE', '台', '128G-黑色', 5, 50, 'iPhone15 128G 黑色版'],
      ['iPhone15', 'MOBILE', '台', '256G-白色', 3, 30, 'iPhone15 256G 白色版']
    ]

    // 创建工作簿和工作表
    const workbook = XLSX.utils.book_new()
    const worksheet = XLSX.utils.aoa_to_sheet(templateData)

    // 设置列宽
    const colWidths = [
      { wch: 20 }, // 货物名称
      { wch: 12 }, // 分类编码
      { wch: 8 },  // 单位
      { wch: 15 }, // 规格/型号
      { wch: 10 }, // 最小库存
      { wch: 10 }, // 最大库存
      { wch: 25 }  // 描述
    ]
    worksheet['!cols'] = colWidths

    // 添加工作表到工作簿
    XLSX.utils.book_append_sheet(workbook, worksheet, '货物数据')

    // 生成Excel文件
    const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
    const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })

    // 下载文件
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '货物导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('Excel模板下载成功')
  } catch (error) {
    console.error('模板生成失败:', error)
    ElMessage.error('模板生成失败，请重试')
  }
}

const handleFileUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  try {
    importLoading.value = true

    // 使用FileReader读取文件
    const fileReader = new FileReader()

    fileReader.onload = (e) => {
      try {
        const data = new Uint8Array(e.target.result)
        const workbook = XLSX.read(data, { type: 'array' })

        // 获取第一个工作表
        const firstSheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[firstSheetName]

        // 将工作表转换为JSON数组
        const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })

        if (jsonData.length < 2) {
          ElMessage.error('文件内容为空或格式不正确')
          importLoading.value = false
          return
        }

        // 第一行是表头，从第二行开始是数据
        const headers = jsonData[0]
        const dataRows = jsonData.slice(1)

        console.log('表头:', headers)
        console.log('数据行数:', dataRows.length)

        // 验证表头格式
        const expectedHeaders = ['货物名称', '分类编码', '单位', '规格/型号', '最小库存', '最大库存', '描述']
        const headerValid = expectedHeaders.every((header, index) =>
          headers[index] && headers[index].toString().trim() === header
        )

        if (!headerValid) {
          ElMessage.error('Excel表头格式不正确，请使用正确的模板格式')
          console.log('期望的表头:', expectedHeaders)
          console.log('实际的表头:', headers)
          importLoading.value = false
          return
        }

        // 解析数据行
        const parsedData = []
        for (let i = 0; i < dataRows.length; i++) {
          const row = dataRows[i]

          // 跳过空行
          if (!row || row.every(cell => !cell || cell.toString().trim() === '')) {
            continue
          }

          const rowData = {
            name: row[0] ? row[0].toString().trim() : '',
            categoryCode: row[1] ? row[1].toString().trim() : '',
            unit: row[2] ? row[2].toString().trim() : '',
            specification: row[3] ? row[3].toString().trim() : '',
            minStock: row[4] ? parseFloat(row[4]) || 0 : 0,
            maxStock: row[5] ? parseFloat(row[5]) || 0 : 0,
            description: row[6] ? row[6].toString().trim() : ''
          }

          // 验证必填字段
          if (!rowData.name) {
            console.warn(`第${i + 2}行数据不完整，跳过:`, rowData)
            continue
          }

          parsedData.push(rowData)
        }

        if (parsedData.length === 0) {
          ElMessage.error('没有找到有效的数据行')
          importLoading.value = false
          return
        }

        previewData.value = parsedData
        importStep.value = 1
        importLoading.value = false
        ElMessage.success(`文件解析成功，共解析到 ${parsedData.length} 条数据`)

      } catch (parseError) {
        console.error('文件解析错误:', parseError)
        ElMessage.error('文件解析失败，请检查文件格式是否正确')
        importLoading.value = false
      }
    }

    fileReader.onerror = () => {
      ElMessage.error('文件读取失败')
      importLoading.value = false
    }

    // 开始读取文件
    fileReader.readAsArrayBuffer(selectedFile.value)

  } catch (error) {
    console.error('文件处理失败:', error)
    ElMessage.error('文件处理失败，请重试')
    importLoading.value = false
  }
}

const handleImportConfirm = async () => {
  try {
    importLoading.value = true

    // 调用后端API导入数据
    const response = await request.post('/goods/import', {
      data: previewData.value
    })

    if (response.success) {
      importResult.value = {
        success: true,
        message: '数据导入成功',
        successCount: response.data.successCount || previewData.value.length,
        failCount: response.data.failCount || 0,
        errors: response.data.errors || [],
        warnings: response.data.warnings || []
      }
    } else {
      importResult.value = {
        success: false,
        message: response.message || '导入失败',
        successCount: 0,
        failCount: previewData.value.length,
        errors: [{ row: 1, message: response.message }],
        warnings: []
      }
    }

    importStep.value = 2

    // 刷新数据
    if (importResult.value.success) {
      loadData()
    }
  } catch (error) {
    console.error('导入失败:', error)
    importResult.value = {
      success: false,
      message: '导入过程中发生错误',
      successCount: 0,
      failCount: previewData.value.length,
      errors: [{ row: 1, message: error.message || '未知错误' }],
      warnings: []
    }
    importStep.value = 2
  } finally {
    importLoading.value = false
  }
}

const handleImportCancel = () => {
  importDialogVisible.value = false
  resetImportData()
}

const handleImportFinish = () => {
  importDialogVisible.value = false
  resetImportData()
}

const resetImportData = () => {
  importStep.value = 0
  selectedFile.value = null
  previewData.value = []
  importResult.value = {
    success: false,
    message: '',
    successCount: 0,
    failCount: 0,
    errors: [],
    warnings: []
  }
  uploadRef.value?.clearFiles()
}


// 分类管理方法
const handleAddCategory = () => {
  categoryForm.id = null
  categoryForm.code = ''
  categoryForm.name = ''
  categoryForm.description = ''
  categoryFormVisible.value = true
}

const handleEditCategory = (row) => {
  Object.assign(categoryForm, row)
  categoryFormVisible.value = true
}

const handleDeleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/goods-categories/${row.id}`)
    if (response.success) {
      ElMessage.success('删除成功')
      loadCategories()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
    }
  }
}

const handleSubmitCategory = async () => {
  try {
    await categoryFormRef.value.validate()
    
    const url = categoryForm.id ? `/goods-categories/${categoryForm.id}` : '/goods-categories'
    const method = categoryForm.id ? 'put' : 'post'
    
    const response = await request[method](url, categoryForm)
    if (response.success) {
      ElMessage.success(categoryForm.id ? '更新成功' : '创建成功')
      categoryFormVisible.value = false
      loadCategories()
    }
  } catch (error) {
    console.error('提交分类失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadData()
  loadCategories()

})
</script>

<style lang="scss" scoped>
// 现代化弹窗样式
.modern-dialog {
  :deep(.el-dialog) {
    z-index: 3000 !important;
  }

  :deep(.el-overlay) {
    z-index: 2999 !important;
  }

  :deep(.el-dialog__header) {
    padding: 0;
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.el-dialog__body) {
    padding: 0;
  }

  :deep(.el-dialog__footer) {
    padding: 0;
    border-top: 1px solid #f0f0f0;
  }

  // 弹窗头部
  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    .header-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .header-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(10px);

        .el-icon {
          font-size: 24px;
        }

        &.view-icon {
          background: rgba(52, 199, 89, 0.2);
        }
      }

      .header-text {
        h3 {
          margin: 0 0 4px 0;
          font-size: 20px;
          font-weight: 600;
        }

        p {
          margin: 0;
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }

    .close-btn {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: white;
      transition: all 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
        transform: scale(1.05);
      }

      .el-icon {
        font-size: 18px;
      }
    }
  }

  // 弹窗主体
  .dialog-body {
    padding: 32px;
    background: #fafbfc;
    min-height: 400px;

    // 表单分组样式
    .form-section {
      background: white;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border: 1px solid #e6e8eb;

      &:last-child {
        margin-bottom: 0;
      }

      .section-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        .el-icon {
          font-size: 18px;
          color: #667eea;
        }
      }

      .el-form-item {
        margin-bottom: 20px;

        .el-form-item__label {
          font-weight: 500;
          color: #606266;
        }

        .el-input, .el-select, .el-textarea {
          :deep(.el-input__wrapper),
          :deep(.el-select__wrapper),
          :deep(.el-textarea__inner) {
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;

            &:hover {
              box-shadow: 0 2px 6px rgba(102, 126, 234, 0.15);
            }

            &.is-focus {
              box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }
          }
        }
      }
    }

    // 详情卡片样式
    .info-card {
      background: white;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border: 1px solid #e6e8eb;

      &:last-child {
        margin-bottom: 0;
      }

      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 20px;
        padding-bottom: 12px;
        border-bottom: 1px solid #f0f0f0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;

        .el-icon {
          font-size: 18px;
          color: #667eea;
        }
      }

      .card-content {
        :deep(.el-descriptions) {
          .el-descriptions__header {
            margin-bottom: 16px;
          }

          .el-descriptions__body {
            .el-descriptions__table {
              .el-descriptions__cell {
                padding: 12px 16px;
                border: 1px solid #f0f0f0;

                &.is-bordered-label {
                  background: #fafbfc;
                  font-weight: 500;
                  color: #606266;
                }
              }
            }
          }
        }
      }
    }
  }
}

.goods-management {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
}

/* 页面头部样式 */
.page-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  flex: 1;
  min-width: 200px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 32px;
  color: #4f46e5;
}

.page-subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  font-weight: 400;
}

.header-actions {
  display: flex !important;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  visibility: visible !important;
  opacity: 1 !important;

  .el-button {
    white-space: nowrap;
    transition: all 0.3s ease;
    display: inline-flex !important;
    visibility: visible !important;
    box-sizing: border-box !important;
  }

  > * {
    display: block !important;
    visibility: visible !important;
  }

  /* 确保所有按钮基础样式一致 */
  .el-button,
  > * .el-button {
    box-sizing: border-box !important;
    vertical-align: middle !important;
    text-align: center !important;
  }
}

/* 搜索区域样式 */
.search-section {
  margin-bottom: 24px;
}

.search-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.search-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.search-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.stat-item .el-icon {
  color: #4f46e5;
}

.search-form {
  margin: 0;
}

.form-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 20px;
  flex-wrap: wrap;
}

.search-inputs {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  flex: 1;
}

.search-item {
  margin-bottom: 0;
}

.search-input,
.search-select {
  width: 200px;
}

.search-actions {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

/* 表格区域样式 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f8f9fa;
}

.table-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
}

.table-actions {
  display: flex;
  gap: 12px;
}

.goods-table {
  width: 100%;
}

/* 表格单元格样式 */
.code-cell,
.name-cell,
.category-cell,
.unit-cell,
.spec-cell,
.time-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.code-icon,
.name-icon,
.category-icon,
.unit-icon,
.spec-icon,
.time-icon {
  color: #4f46e5;
  font-size: 16px;
}

.code-text,
.name-text,
.category-text,
.unit-text,
.spec-text,
.time-text {
  font-weight: 500;
}

.stock-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stock-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.stock-label {
  color: #6b7280;
}

.stock-value {
  font-weight: 600;
  color: #374151;
}

.status-tag {
  border-radius: 20px;
  padding: 4px 12px;
}

.status-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: nowrap;
  align-items: center;
}

.desktop-actions {
  display: flex;
}

.mobile-actions {
  display: none;
}

.action-btn {
  padding: 4px 8px !important;
  font-size: 12px;
  min-width: auto;
  white-space: nowrap;
}

.action-btn .el-icon {
  margin-right: 4px;
}

.btn-text {
  font-size: 12px;
}

/* 分页样式 */
.pagination-container {
  padding: 20px 24px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #e5e7eb;
  background: #f8f9fa;
}

.goods-pagination {
  --el-pagination-button-color: #4f46e5;
  --el-pagination-hover-color: #4f46e5;
}

/* 移动端适配 */
.mobile-form .form-content {
  flex-direction: column;
  align-items: stretch;
}

.mobile-form .search-inputs {
  flex-direction: column;
  gap: 16px;
}

.mobile-form .search-input,
.mobile-form .search-select {
  width: 100%;
}

.mobile-form .search-actions {
  justify-content: center;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .goods-management {
    padding: 16px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    align-items: stretch;
  }

  /* 这个样式被更具体的媒体查询覆盖 */

  .search-card {
    padding: 20px;
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-stats {
    justify-content: space-around;
  }

  .desktop-actions {
    display: none;
  }

  .mobile-actions {
    display: block;
  }

  .table-header {
    padding: 16px 20px;
  }

  .pagination-container {
    padding: 16px 20px;
  }
}

/* 大屏幕适配 (1024px+) */
@media (min-width: 1024px) {
  .header-actions {
    justify-content: flex-start;
    gap: 12px;
    flex-wrap: wrap;
  }

  .header-actions .el-button,
  .header-actions > * .el-button {
    min-width: auto;
    max-width: none;
    font-size: 14px;
  }
}

/* 现代化弹窗移动端适配 */
@media (max-width: 768px) {
  .modern-dialog {
    .dialog-header {
      padding: 16px 20px;

      .header-content {
        gap: 12px;

        .header-icon {
          width: 40px;
          height: 40px;

          .el-icon {
            font-size: 20px;
          }
        }

        .header-text {
          h3 {
            font-size: 18px;
          }

          p {
            font-size: 13px;
          }
        }
      }

      .close-btn {
        width: 36px;
        height: 36px;
      }
    }

    .dialog-body {
      padding: 16px;

      .form-section,
      .info-card {
        padding: 16px;
        margin-bottom: 16px;

        .section-title,
        .card-header {
          font-size: 14px;
          padding-bottom: 8px;
          margin-bottom: 16px;
        }

        .el-form-item {
          margin-bottom: 16px;

          .el-form-item__label {
            font-size: 14px;
          }
        }
      }
    }
  }
}

/* 平板端适配 (769px - 1023px) */
@media (max-width: 1023px) and (min-width: 769px) {
  .header-actions {
    justify-content: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .header-actions .el-button,
  .header-actions > * .el-button {
    flex: 1;
    min-width: 140px;
    max-width: 180px;
    font-size: 14px;
  }
}

/* 中等屏幕适配 (481px - 768px) */
@media (max-width: 768px) and (min-width: 481px) {
  .header-actions {
    justify-content: center !important;
    gap: 12px !important;
    flex-wrap: wrap !important;
    align-items: center !important;
    padding: 0 20px !important;
  }

  .header-actions .el-button,
  .header-actions > * .el-button,
  .header-actions .goods-export-btn {
    flex: 1 !important;
    min-width: 120px !important;
    max-width: 150px !important;
    height: 36px !important;
    font-size: 13px !important;
    padding: 8px 12px !important;
    display: inline-flex !important;
    align-items: center !important;
    justify-content: center !important;
    line-height: 1 !important;
    border-radius: 6px !important;
    box-sizing: border-box !important;

    /* 覆盖Element Plus的size样式 */
    &.el-button--small {
      height: 36px !important;
      padding: 8px 12px !important;
      font-size: 13px !important;
    }

    &.el-button--default {
      height: 36px !important;
      padding: 8px 12px !important;
      font-size: 13px !important;
    }

    .el-icon {
      margin-right: 4px !important;
      font-size: 14px !important;
    }
  }
}

@media (max-width: 480px) {
  .goods-management {
    padding: 12px;
  }

  .page-title {
    font-size: 24px;
  }

  .search-stats {
    flex-direction: column;
    gap: 12px;
  }

  .stat-item {
    justify-content: center;
  }
}

/* 表单和弹窗样式 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.detail-content {
  padding: 20px 0;
}

.category-management .category-header {
  margin-bottom: 16px;
}

/* 导入弹窗样式 */
.import-content {
  padding: 20px 0;
}

.import-steps {
  margin-bottom: 30px;
}

.step-content {
  min-height: 300px;
}

.upload-area {
  margin-bottom: 20px;
}

.upload-dragger {
  width: 100%;
}

.template-section {
  margin: 20px 0;
}

.template-download {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #6b7280;
}

.import-tips {
  background: #f0f9ff;
  border: 1px solid #e0f2fe;
  border-radius: 8px;
  padding: 16px;
  margin-top: 20px;
}

.import-tips h4 {
  margin: 0 0 12px 0;
  color: #0369a1;
  font-size: 14px;
  font-weight: 600;
}

.import-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #374151;
}

.import-tips li {
  margin-bottom: 4px;
  font-size: 13px;
  line-height: 1.5;
}

.preview-info {
  margin-bottom: 16px;
}

.preview-table {
  margin-bottom: 16px;
}

.preview-more {
  text-align: center;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}

.import-result {
  text-align: center;
}

.result-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin: 20px 0;
}

.warning-list {
  margin-top: 20px;
  text-align: left;
}

.warning-list h4 {
  margin: 0 0 12px 0;
  color: #f59e0b;
  font-size: 14px;
  font-weight: 600;
}

.warning-list ul {
  margin: 0;
  padding-left: 20px;
}

.warning-item {
  margin-bottom: 8px;
  color: #f59e0b;
  font-size: 13px;
  line-height: 1.5;
}

.error-list {
  margin-top: 20px;
  text-align: left;
}

.error-list h4 {
  margin: 0 0 12px 0;
  color: #dc2626;
  font-size: 14px;
  font-weight: 600;
}

.error-list ul {
  margin: 0;
  padding-left: 20px;
}

.error-item {
  margin-bottom: 8px;
  color: #dc2626;
  font-size: 13px;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 工具类 */
.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

/* 移动端适配 */
@media (max-width: 768px) {
  /* 弹窗适配 */
  .goods-dialog .el-dialog__body,
  .goods-detail-dialog .el-dialog__body,
  .category-dialog .el-dialog__body,
  .category-form-dialog .el-dialog__body,
  .import-dialog .el-dialog__body {
    padding: 15px !important;
  }

  /* 表单适配 */
  .el-form-item {
    margin-bottom: 15px !important;
  }

  .el-form-item__label {
    font-size: 14px !important;
    line-height: 1.4 !important;
  }

  /* 按钮适配 */
  .el-button {
    padding: 8px 15px !important;
    font-size: 14px !important;
  }

  /* 表格适配 */
  .el-table .el-table__cell {
    padding: 8px 0 !important;
  }

  .el-table th.el-table__cell {
    font-size: 13px !important;
  }

  .el-table td.el-table__cell {
    font-size: 13px !important;
  }

  /* 输入框适配 */
  .el-input__inner,
  .el-textarea__inner {
    font-size: 14px !important;
  }

  /* 选择器适配 */
  .el-select .el-input__inner {
    font-size: 14px !important;
  }

  /* 描述列表适配 */
  .el-descriptions__label {
    font-size: 13px !important;
    width: 80px !important;
  }

  .el-descriptions__content {
    font-size: 13px !important;
  }

  /* 分页适配 */
  .el-pagination {
    text-align: center !important;
  }

  .el-pagination .el-pager li {
    min-width: 28px !important;
    height: 28px !important;
    line-height: 28px !important;
    font-size: 13px !important;
  }

  .el-pagination .btn-prev,
  .el-pagination .btn-next {
    min-width: 28px !important;
    height: 28px !important;
    line-height: 28px !important;
  }

  /* 搜索区域适配 */
  .search-form .el-form-item {
    margin-bottom: 10px !important;
  }

  /* 头部操作按钮适配 */
  .header-actions {
    flex-direction: column !important;
    gap: 12px !important;
    align-items: center !important;
    padding: 0 20px !important;
    margin: 16px 0 !important;
    width: 100% !important;
    box-sizing: border-box !important;
  }

  .header-actions .el-button,
  .header-actions > * .el-button,
  .header-actions .goods-export-btn {
    width: 100% !important;
    max-width: 280px !important;
    min-width: auto !important;
    height: 44px !important;
    padding: 12px 16px !important;
    justify-content: center !important;
    display: flex !important;
    align-items: center !important;
    font-size: 14px !important;
    line-height: 1 !important;
    border-radius: 8px !important;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
    margin: 0 !important;
    box-sizing: border-box !important;

    /* 覆盖Element Plus默认样式 */
    &.el-button--small {
      height: 44px !important;
      padding: 12px 16px !important;
      font-size: 14px !important;
    }

    &.el-button--default {
      height: 44px !important;
      padding: 12px 16px !important;
      font-size: 14px !important;
    }

    .el-icon {
      margin-right: 6px !important;
      font-size: 16px !important;
    }
  }

  /* 确保GoodsExport组件的按钮也应用样式 */
  .header-actions > div {
    width: 100% !important;
    display: flex !important;
    justify-content: center !important;
  }

  /* 页面头部适配 */
  .page-header {
    flex-direction: column !important;
    text-align: center !important;
    gap: 15px !important;
  }

  .header-content h1 {
    font-size: 20px !important;
    margin-bottom: 5px !important;
  }

  .page-subtitle {
    font-size: 13px !important;
  }

  /* 导入步骤适配 */
  .import-steps .el-step__title {
    font-size: 13px !important;
  }

  /* 文件上传适配 */
  .upload-area {
    padding: 20px 10px !important;
  }

  /* 导入说明适配 */
  .import-tips {
    font-size: 13px !important;
  }

  .import-tips ul {
    padding-left: 15px !important;
  }

  .import-tips li {
    margin-bottom: 5px !important;
  }
}
</style>

<!-- 全局样式 - 货物管理对话框 -->
<style>
/* 货物新增/编辑对话框样式 */
.goods-dialog .goods-header {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.goods-dialog .goods-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.goods-dialog .goods-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.goods-dialog .goods-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.goods-dialog .goods-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.goods-dialog .goods-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.goods-dialog .goods-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.goods-dialog .goods-header {
  position: relative !important;
}

.goods-dialog .goods-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.goods-dialog .goods-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.goods-dialog .goods-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 分类管理对话框样式 */
.category-dialog .category-header {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.category-dialog .category-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.category-dialog .category-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.category-dialog .category-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.category-dialog .category-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.category-dialog .category-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.category-dialog .category-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.category-dialog .category-header {
  position: relative !important;
}

.category-dialog .category-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.category-dialog .category-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.category-dialog .category-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 分类编辑对话框样式 */
.category-form-dialog .category-form-header {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.category-form-dialog .category-form-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.category-form-dialog .category-form-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.category-form-dialog .category-form-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.category-form-dialog .category-form-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.category-form-dialog .category-form-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.category-form-dialog .category-form-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.category-form-dialog .category-form-header {
  position: relative !important;
}

.category-form-dialog .category-form-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.category-form-dialog .category-form-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.category-form-dialog .category-form-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 导入数据对话框样式 */
.import-dialog .import-header {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.import-dialog .import-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.import-dialog .import-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.import-dialog .import-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.import-dialog .import-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.import-dialog .import-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.import-dialog .import-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.import-dialog .import-header {
  position: relative !important;
}

.import-dialog .import-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.import-dialog .import-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.import-dialog .import-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 货物查看详情对话框样式 */
.goods-detail-dialog .goods-detail-header {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%) !important;
  color: white !important;
  padding: 24px 32px !important;
  margin: -20px -24px 0 -24px !important;
  border-radius: 12px 12px 0 0 !important;
}

.goods-detail-dialog .goods-detail-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.goods-detail-dialog .goods-detail-header .dialog-title {
  display: flex !important;
  align-items: center !important;
  gap: 16px !important;
}

.goods-detail-dialog .goods-detail-header .title-icon {
  width: 48px !important;
  height: 48px !important;
  background: rgba(255, 255, 255, 0.2) !important;
  border-radius: 12px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  backdrop-filter: blur(10px) !important;
}

.goods-detail-dialog .goods-detail-header .title-icon .el-icon {
  font-size: 24px !important;
  color: white !important;
}

.goods-detail-dialog .goods-detail-header .title-content h2 {
  margin: 0 !important;
  font-size: 24px !important;
  font-weight: 600 !important;
  color: white !important;
}

.goods-detail-dialog .goods-detail-header .title-content p {
  margin: 4px 0 0 0 !important;
  font-size: 14px !important;
  color: rgba(255, 255, 255, 0.8) !important;
}

.goods-detail-dialog .goods-detail-header {
  position: relative !important;
}

.goods-detail-dialog .goods-detail-header .header-content {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  width: 100% !important;
}

.goods-detail-dialog .goods-detail-header .dialog-close {
  color: white !important;
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  backdrop-filter: blur(10px) !important;
  flex-shrink: 0 !important;
  margin-left: auto !important;
}

.goods-detail-dialog .goods-detail-header .dialog-close:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

/* 货物详情内容区域样式 */
.goods-detail-dialog .dialog-body {
  padding: 32px 0 !important;
  max-height: 70vh !important;
  overflow-y: auto !important;
}

.goods-detail-dialog .detail-content {
  display: flex !important;
  flex-direction: column !important;
  gap: 24px !important;
}

.goods-detail-dialog .info-card {
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08) !important;
  border: 1px solid #f1f5f9 !important;
  overflow: hidden !important;
  transition: all 0.3s ease !important;
}

.goods-detail-dialog .info-card:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12) !important;
}

.goods-detail-dialog .info-card .card-header {
  padding: 20px 24px 16px 24px !important;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%) !important;
  border-bottom: 1px solid #e2e8f0 !important;
  display: flex !important;
  align-items: center !important;
  gap: 12px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  color: #1e293b !important;
}

.goods-detail-dialog .info-card .card-header .el-icon {
  font-size: 20px !important;
  color: #8b5cf6 !important;
}

.goods-detail-dialog .info-card .card-content {
  padding: 24px !important;
}

/* 美化描述列表 */
.goods-detail-dialog .el-descriptions {
  border-radius: 8px !important;
  overflow: hidden !important;
}

.goods-detail-dialog .el-descriptions__header {
  background: #f8fafc !important;
  padding: 16px !important;
}

.goods-detail-dialog .el-descriptions__body {
  background: white !important;
}

.goods-detail-dialog .el-descriptions__table {
  border-collapse: separate !important;
  border-spacing: 0 !important;
}

.goods-detail-dialog .el-descriptions__cell {
  border: 1px solid #e2e8f0 !important;
  padding: 12px 16px !important;
}

.goods-detail-dialog .el-descriptions__label {
  background: #f8fafc !important;
  font-weight: 600 !important;
  color: #374151 !important;
  width: 120px !important;
}

.goods-detail-dialog .el-descriptions__content {
  background: white !important;
  color: #1e293b !important;
  font-weight: 500 !important;
}

/* 状态标签样式 */
.goods-detail-dialog .status-badge {
  display: inline-flex !important;
  align-items: center !important;
  gap: 6px !important;
  padding: 6px 12px !important;
  border-radius: 20px !important;
  font-size: 14px !important;
  font-weight: 500 !important;
}

.goods-detail-dialog .status-badge.enabled {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0) !important;
  color: #15803d !important;
  border: 1px solid #22c55e !important;
}

.goods-detail-dialog .status-badge.disabled {
  background: linear-gradient(135deg, #fee2e2, #fecaca) !important;
  color: #dc2626 !important;
  border: 1px solid #ef4444 !important;
}

.goods-detail-dialog .status-badge .el-icon {
  font-size: 14px !important;
}

/* 移动端响应式 */
@media (max-width: 768px) {
  .goods-detail-dialog .dialog-body {
    padding: 24px 0 !important;
  }

  .goods-detail-dialog .detail-content {
    gap: 16px !important;
  }

  .goods-detail-dialog .info-card .card-content {
    padding: 16px !important;
  }

  .goods-detail-dialog .el-descriptions__label {
    width: 100px !important;
  }

  .goods-detail-dialog .el-descriptions__cell {
    padding: 8px 12px !important;
  }
}
</style>
