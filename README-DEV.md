# 库房库存管理系统 - 本地开发环境

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 16.0 或更高版本
- **MySQL**: 8.0 或更高版本

### 数据库配置

1. 启动MySQL服务
2. 创建数据库（可选，应用会自动创建）：
   ```sql
   CREATE DATABASE warehouse_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 确保MySQL root用户密码为 `root`，或修改 `backend/src/main/resources/application.yml` 中的数据库配置

### 启动方式

#### 方式一：使用PowerShell脚本（推荐）
```powershell
# 右键点击 start-dev.ps1 -> 使用PowerShell运行
# 或在PowerShell中执行：
.\start-dev.ps1
```

#### 方式二：使用简单批处理脚本
```bash
# 双击运行
start-simple.bat
```

#### 方式三：手动启动

1. **启动后端服务**：
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端服务**：
   ```bash
   cd frontend
   npm install  # 首次运行需要安装依赖
   npm run dev
   ```

### 访问地址

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/swagger-ui.html（如果配置了Swagger）

### 默认账号

- **用户名**: admin
- **密码**: admin123

## 🛠️ 开发配置

### 后端配置

- **端口**: 8080
- **数据库**: MySQL (localhost:3306/warehouse_db)
- **用户名**: root
- **密码**: root
- **JPA**: 开发模式，自动创建表结构
- **日志级别**: DEBUG

### 前端配置

- **端口**: 3000
- **API地址**: http://localhost:8080/api
- **热重载**: 已启用
- **开发工具**: Vue DevTools 支持

## 📁 项目结构

```
warehouse-inventory-system/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/      # Java源码
│   ├── src/main/resources/ # 配置文件
│   ├── target/            # Maven构建输出（可删除）
│   └── pom.xml            # Maven配置
├── frontend/               # Vue 3 前端
│   ├── src/               # 前端源码
│   ├── public/            # 静态资源
│   ├── dist/              # 前端构建输出（可删除）
│   ├── node_modules/      # NPM依赖（可删除重装）
│   └── package.json       # NPM配置
├── start-dev.ps1          # PowerShell启动脚本
├── start-simple.bat       # 简单启动脚本
├── clean-build.ps1        # 清理构建目录脚本
└── README-DEV.md          # 开发文档
```

## 🔧 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库用户名密码是否正确
- 检查防火墙设置

### 2. 前端无法访问后端API
- 确认后端服务已启动（端口8080）
- 检查跨域配置
- 查看浏览器控制台错误信息

### 3. 热重载不生效
- 重启开发服务器
- 清除浏览器缓存
- 检查文件保存是否成功

## 🧹 清理构建文件

如果需要清理构建生成的文件：

```bash
# 使用PowerShell脚本（推荐）
.\clean-build.ps1

# 或使用批处理脚本
clean-build.bat
```

**可以安全删除的目录**：
- `backend/target/` - Maven构建输出
- `frontend/dist/` - 前端构建输出
- `frontend/node_modules/` - NPM依赖（可重新安装）
- `backend/logs/` - 应用日志

## 📝 开发注意事项

1. **数据库**: 开发环境使用 `create-drop` 模式，每次重启会重新创建表结构
2. **日志**: 开发环境日志级别为DEBUG，会输出详细的SQL语句
3. **热重载**: 后端支持Spring Boot DevTools热重载，前端支持Vite热重载
4. **测试数据**: 系统启动时会自动插入测试数据
5. **构建文件**: target和dist目录可以随时删除，会在下次构建时重新生成

## 🎯 下一步

- 查看 `docs/PROGRESS.md` 了解开发进度
- 开始开发新功能或修复Bug
- 运行测试确保代码质量
