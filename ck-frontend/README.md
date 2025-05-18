# Frontend TruliaClone

Giao diện hiện đại dựa trên React cho nền tảng thương mại điện tử bất động sản TruliaClone.

## Tổng Quan

Đây là ứng dụng frontend cho TruliaClone, một nền tảng bất động sản toàn diện cho phép người dùng tìm kiếm, đăng tin và giao dịch bất động sản. Frontend cung cấp giao diện người dùng trực quan và đáp ứng, giao tiếp với các microservices backend.

## Tính Năng

### Quản Lý Bất Động Sản
- Duyệt danh sách bất động sản với các tùy chọn lọc nâng cao
- Xem thông tin chi tiết bất động sản với thư viện hình ảnh
- Tìm kiếm bất động sản theo vị trí, khoảng giá, số phòng và các tiêu chí khác
- Lưu bất động sản yêu thích và xem lịch sử duyệt

### Tính Năng Người Dùng
- Đăng ký và xác thực người dùng
- Quản lý hồ sơ người dùng
- Bảng điều khiển cho người bán và môi giới
- Lưu tìm kiếm bất động sản để sử dụng sau
- Nhận thông báo về bất động sản mới phù hợp với tìm kiếm đã lưu

### Giao Dịch
- Yêu cầu xem bất động sản
- Gửi đề nghị mua/thuê
- Theo dõi trạng thái giao dịch
- Quản lý danh sách bất động sản (cho người bán và môi giới)

## Công Nghệ Sử Dụng

- **React.js**: Framework frontend
- **Redux**: Quản lý trạng thái
- **React Router**: Điều hướng
- **Material-UI**: Thư viện component cho thiết kế nhất quán
- **Axios**: Giao tiếp API
- **React Query**: Tìm nạp và lưu trữ dữ liệu
- **Formik & Yup**: Xử lý và xác thực form
- **React Image Gallery**: Hiển thị hình ảnh bất động sản
- **Google Maps API**: Hiển thị vị trí bất động sản
- **Recharts**: Trực quan hóa dữ liệu cho xu hướng thị trường

## Cấu Trúc Dự Án

```
ck-frontend/
├── public/              # Tệp tĩnh
├── src/
│   ├── api/             # Lớp tích hợp API
│   ├── assets/          # Hình ảnh, biểu tượng và tài sản tĩnh khác
│   ├── components/      # Các component UI có thể tái sử dụng
│   ├── context/         # Các context provider của React
│   ├── hooks/           # Custom React hooks
│   ├── layouts/         # Các component bố cục trang
│   ├── pages/           # Các trang chính của ứng dụng
│   ├── redux/           # Redux store, actions, và reducers
│   ├── routes/          # Định tuyến ứng dụng
│   ├── services/        # Các dịch vụ logic nghiệp vụ
│   ├── styles/          # Kiểu dáng và chủ đề toàn cục
│   ├── utils/           # Các hàm tiện ích
│   ├── App.js           # Component ứng dụng chính
│   └── index.js         # Điểm vào ứng dụng
├── .env                 # Biến môi trường
└── package.json         # Các phụ thuộc dự án
```

## Bắt Đầu

### Yêu Cầu
- Node.js (v14.x trở lên)
- npm hoặc yarn
- Các dịch vụ backend đang chạy (xem README chính của dự án)

### Cài Đặt

1. Clone repository
   ```bash
   git clone https://github.com/your-username/trulia-clone.git
   cd trulia-clone/ck-frontend
   ```

2. Cài đặt các phụ thuộc
   ```bash
   npm install
   # hoặc
   yarn install
   ```

3. Tạo file `.env` với các biến sau
   ```
   REACT_APP_API_BASE_URL=http://localhost:8080
   REACT_APP_GOOGLE_MAPS_API_KEY=your_google_maps_api_key
   ```

4. Khởi động máy chủ phát triển
   ```bash
   npm start
   # hoặc
   yarn start
   ```

5. Mở trình duyệt và điều hướng đến `http://localhost:3000`

### Xây Dựng cho Sản Xuất

```bash
npm run build
# hoặc
yarn build
```

## Hỗ Trợ Docker

Frontend có thể được đóng gói bằng Docker:

```bash
# Xây dựng Docker image
docker build -t trulia-clone-frontend .

# Chạy container
docker run -p 3000:80 trulia-clone-frontend
```

## Giao Tiếp với Backend

Frontend giao tiếp với các microservices backend thông qua API Gateway. Các endpoint chính bao gồm:

- `/api/auth/*` - Xác thực và phân quyền
- `/api/users/*` - Quản lý người dùng
- `/api/properties/*` - Danh sách và tìm kiếm bất động sản
- `/api/transactions/*` - Xử lý giao dịch
- `/api/files/*` - Tải lên và quản lý tệp

## Kiểm Thử

```bash
# Chạy kiểm thử
npm test
# hoặc
yarn test

# Tạo báo cáo test coverage
npm test -- --coverage
```

## Đóng Góp

1. Fork repository
2. Tạo nhánh tính năng: `git checkout -b ten-tinh-nang`
3. Commit thay đổi: `git commit -m 'Thêm tính năng'`
4. Push lên nhánh: `git push origin ten-tinh-nang`
5. Gửi pull request

## Giấy Phép

Dự án này được cấp phép theo Giấy phép MIT - xem file LICENSE để biết chi tiết.
