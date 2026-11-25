# Giao diện Sân Nhà

Giao diện Android native Kotlin/XML được triển khai từ bản thiết kế `Untitled.pdf` (30 trang). Ảnh các sân và hình minh họa đăng nhập lấy từ PDF, được lưu sẵn trong tài nguyên ứng dụng.

## Mở và chạy

Mở thư mục `AndroidStudioProjects/QuanLySanBong` bằng Android Studio, dùng Gradle JDK 21, Sync rồi Run. Activity khởi động mới là `SanNhaActivity`.

Ở màn hình đăng nhập, chọn vai trò **Người đặt sân** hoặc **Chủ sân**. Có thể đăng ký tài khoản SQLite hoặc chọn **Xem thử giao diện** để dùng dữ liệu mẫu. Chế độ xem thử được tách riêng với dữ liệu tài khoản thật.

## Các màn hình

| Trang PDF | Màn hình trong mã |
| --- | --- |
| 1–3 | Đăng nhập hai vai trò, đăng ký (`auth`) |
| 4, 9–10 | Trang chủ, tìm kiếm, bộ lọc (`home`, `filters`) |
| 5–8 | Lịch sân chủ sân, chi tiết đơn, tạo lịch thủ công, sân của tôi |
| 11–14 | Chỉnh sửa sân, bảng giá, doanh thu, tài khoản chủ sân |
| 15–19 | Chi tiết sân, chọn lịch, kiểm tra lịch đặt, thanh toán, thành công |
| 20–23 | Lịch đặt, chi tiết, yêu cầu đổi lịch, hủy lịch |
| 24–30 | Yêu thích, ưu đãi, thông báo, đánh giá, tài khoản, hồ sơ, trợ giúp |

`SanNhaUi.kt` chứa các thành phần dùng chung và icon nét; `activity_san_nha.xml` chứa khung màn hình, vùng cuộn, nút hành động và điều hướng đáy. `SanNhaCustomer.kt`, `SanNhaAccount.kt` và `SanNhaOwner.kt` chứa từng nhóm màn hình. Đăng nhập/đăng ký dùng `DatabaseHelper` hiện có; lịch và cấu hình mới được lưu bằng `SanNhaStore` trên thiết bị.

## Phạm vi hoạt động

- Tìm kiếm tên/khu vực, chọn loại sân, ngày, giờ, khoảng cách và mức giá; thêm/bỏ sân yêu thích.
- Chọn lịch, tính tiền theo thời lượng và từng khung giá, kiểm tra trùng lịch, tạo lịch thử, xem lịch, đổi/hủy và đánh giá.
- Chủ sân tạo lịch thủ công, ghi nhận khoản tiền đã nhận, khóa giờ, duyệt/từ chối đổi lịch, quản lý sân/giá và xem/chia sẻ doanh thu.
- Hồ sơ, tùy chọn và dữ liệu mẫu được lưu trên thiết bị; ngày lịch dùng ngày hiện tại, không cố định theo ngày trong bản vẽ.

## Chưa kết nối dịch vụ bên ngoài

Dữ liệu chạy thử cho tài khoản đăng ký bằng số điện thoại được tạo tự động: cụm Gia Định có Sân 1, Sân 2, Sân 3 và lịch mẫu từ hôm nay đến 7 ngày tới. Khách vào Trang chủ → Sân bóng Gia Định → Chọn lịch ra sân, chọn ngày và ô Trống, tiếp tục đến nút Xem thử thanh toán thành công. Chủ sân đăng nhập trên cùng thiết bị, chọn đúng ngày và sân để xem đơn. Lịch vừa đặt chặn các khoảng giờ giao nhau; hủy lịch sẽ mở lại giờ. Dữ liệu mẫu được bổ sung mỗi ngày, không thay thế đơn đã có và không phát sinh thu tiền. Trang chủ luôn cho phép duyệt danh sách địa điểm; nút Tìm sân trống mới áp dụng bộ lọc đầy đủ.

MoMo, VNPAY, VietQR, xác thực OTP, thông báo đẩy, chat hỗ trợ, đồng bộ giữa các máy và tải ảnh chưa được tích hợp. Màn thanh toán chỉ tạo lịch thử, không thu tiền hoặc xác nhận với sân thật. Thông tin hỗ trợ và chính sách trong PDF là dữ liệu thiết kế cần được xác nhận trước khi vận hành.

Không có thay đổi vào các Activity cũ hoặc `DatabaseHelper` ngoài phần mã đã có trước yêu cầu này. Có thể tiếp tục nối API/backend vào các màn hình hiện tại.
