# TruliaClone - Nền Tảng Thương Mại Điện Tử Bất Động Sản

Nền tảng bất động sản toàn diện dựa trên kiến trúc microservices, cho phép người dùng tìm kiếm, đăng tin và giao dịch bất động sản với trải nghiệm liền mạch.

![TruliaClone Banner](https://placeholder-for-banner-image.com)

## Tổng Quan Dự Án

TruliaClone là ứng dụng thương mại điện tử bất động sản full-stack được mô phỏng theo Trulia, được xây dựng với kiến trúc microservices hiện đại. Nền tảng này kết nối người mua, người bán và môi giới bất động sản, cung cấp các công cụ để đăng tin, tìm kiếm, quản lý người dùng và xử lý giao dịch.

## Tính Năng Chính

- **Đăng Tin & Tìm Kiếm Bất Động Sản**: Tìm kiếm nâng cao với nhiều bộ lọc (giá, vị trí, loại bất động sản, v.v.)
- **Quản Lý Người Dùng**: Đăng ký, xác thực và kiểm soát truy cập dựa trên vai trò
- **Xử Lý Giao Dịch**: Mua, bán và thuê bất động sản với tích hợp xử lý thanh toán
- **Lưu Tìm Kiếm & Thông Báo**: Lưu tiêu chí tìm kiếm và nhận thông báo cho bất động sản phù hợp
- **Quản Lý Bất Động Sản**: Công cụ cho người bán và môi giới để quản lý tin đăng
- **Quản Lý Hình Ảnh & Tài Liệu**: Tải lên và quản lý ảnh và tài liệu bất động sản

## Kiến Trúc

Dự án này theo kiến trúc microservices với các thành phần sau:

### Dịch Vụ Backend

- **Property Service**: Quản lý danh sách bất động sản, danh mục và chức năng tìm kiếm
- **User Service**: Xử lý tài khoản người dùng, vai trò và tìm kiếm đã lưu
- **Transaction Service**: Xử lý giao dịch mua, bán và thuê
- **Auth Service**: Quản lý xác thực và phân quyền
- **File Service**: Xử lý lưu trữ hình ảnh và tài liệu
- **API Gateway**: Điểm vào cho tất cả các yêu cầu từ client
- **Service Registry**: Quản lý khám phá dịch vụ sử dụng Eureka
- **Config Server**: Tập trung hóa quản lý cấu hình

### Frontend

- Ứng dụng trang đơn dựa trên React.js với Redux để quản lý trạng thái
- Material-UI cho thiết kế nhất quán, đáp ứng
- Tích hợp với dịch vụ bản đồ để hiển thị vị trí bất động sản

## Công Nghệ Sử Dụng

### Backend
- Java 17
- Spring Boot 3.x
- Spring Cloud (Gateway, Eureka, Config Server)
- Spring Data JPA
- Spring Security với JWT
- PostgreSQL
- JUnit & Mockito cho kiểm thử

### Frontend
- React.js
- Redux
- Material-UI
- Axios
- React Router

### Hạ Tầng
- Docker & Docker Compose
- Maven để quản lý build
- Git cho quản lý phiên bản

## Cấu Trúc Dự Án

```
trulia-clone/
├── api-gateway/             # Dịch vụ API Gateway
├── auth-service/            # Dịch vụ xác thực
├── category-service/        # Dịch vụ quản lý danh mục
├── ck-frontend/             # Ứng dụng frontend React
├── config-server/           # Máy chủ cấu hình
├── config-repo/             # Kho lưu trữ cấu hình
├── file-service/            # Dịch vụ quản lý file
├── property-service/        # Dịch vụ quản lý bất động sản
├── service-registry/        # Đăng ký dịch vụ Eureka
├── transaction-service/     # Dịch vụ quản lý giao dịch
├── user-service/            # Dịch vụ quản lý người dùng
├── docker-compose.yml       # Cấu hình Docker
└── parent-pom.xml           # File POM cha của Maven
```

## Bắt Đầu

### Yêu Cầu
- Java 17 trở lên
- Maven 3.6+
- Node.js 14+ và npm/yarn
- Docker và Docker Compose (tùy chọn)
- PostgreSQL (nếu chạy không sử dụng Docker)

### Chạy với Docker

1. Clone repository
   ```bash
   git clone https://github.com/your-username/trulia-clone.git
   cd trulia-clone
   ```

2. Build tất cả microservices
   ```bash
   mvn clean package -DskipTests
   ```

3. Khởi động ứng dụng với Docker Compose
   ```bash
   docker-compose up -d
   ```

4. Truy cập ứng dụng tại http://localhost:3000

### Chạy Cục Bộ (Phát Triển)

1. Khởi động service registry
   ```bash
   cd service-registry
   mvn spring-boot:run
   ```

2. Khởi động config server
   ```bash
   cd config-server
   mvn spring-boot:run
   ```

3. Khởi động API gateway và các dịch vụ khác
   ```bash
   cd api-gateway
   mvn spring-boot:run
   
   # Lặp lại cho các dịch vụ khác
   ```

4. Khởi động frontend
   ```bash
   cd ck-frontend
   npm install
   npm start
   ```

## Tài Liệu API

API được lập tài liệu bằng Swagger UI, có sẵn tại:
- `http://localhost:8080/swagger-ui.html`

Tài liệu dịch vụ riêng lẻ có sẵn tại các điểm cuối tương ứng:
- `/swagger-ui.html`

## Đóng Góp

1. Fork repository
2. Tạo nhánh tính năng: `git checkout -b tên-tính-năng`
3. Commit thay đổi: `git commit -m 'Thêm tính năng'`
4. Push lên nhánh: `git push origin tên-tính-năng`
5. Gửi pull request

## Giấy Phép

Dự án này được cấp phép theo Giấy phép MIT - xem file LICENSE để biết chi tiết.

## Lời Cảm Ơn

- Trulia cho nguồn cảm hứng
- Spring Boot và Spring Cloud cho các framework xuất sắc
- Cộng đồng mã nguồn mở cho các công cụ đã làm dự án này có thể thực hiện được 