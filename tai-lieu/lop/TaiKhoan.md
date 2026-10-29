# TaiKhoan

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public abstract class TaiKhoan`, file `TaiKhoan.java`.

Lưu thông tin chung, xác thực và trạng thái hoạt động của tài khoản. Không khởi tạo trực tiếp lớp này. Mọi thuộc tính là `private`; lớp không đọc bàn phím, hiển thị menu hoặc tự ghi file.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `maTaiKhoan` | `String` | Mã duy nhất, bất biến sau tạo |
| `tenDangNhap` | `String` | Tên đăng nhập đã chuẩn hóa |
| `hoTen` | `String` | Họ tên, bắt buộc |
| `soDienThoai` | `String` | Số điện thoại, bắt buộc; giữ số 0 đầu |
| `salt` | `String` | Salt ngẫu nhiên riêng, lưu Base64 |
| `matKhauBam` | `String` | Kết quả băm mật khẩu, lưu Base64 |
| `soVongBam` | `int` | Số vòng băm dùng khi tạo thông tin xác thực |
| `dangHoatDong` | `boolean` | Cho biết tài khoản đang hoạt động hay bị khóa |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public abstract VaiTro layVaiTro()` | Lớp con trả đúng vai trò của mình |
| `boolean kiemTraMatKhau(char[] matKhau)` | Xác thực mật khẩu nhập bằng salt và tham số đã lưu |
| `void khoa()` | Đánh dấu tài khoản ngừng hoạt động sau khi dịch vụ cho phép |
| `void moKhoa()` | Mở lại tài khoản sau khi dịch vụ cho phép |

Constructor kiểm tra dữ liệu và tạo thông tin xác thực khi đăng ký/tạo tài khoản. Khi nạp file, khôi phục salt, số vòng và hash đã lưu, không băm lại hash. Chi tiết băm có thể nằm trong phương thức `private` của lớp: dùng `PBKDF2WithHmacSHA256` với API JDK `SecretKeyFactory`, `PBEKeySpec`, `SecureRandom` và so sánh bằng `MessageDigest.isEqual`.

## Quan hệ và lưu trữ

Ba lớp con trực tiếp là [KhachHang](KhachHang.md), [NhanVienBanHang](NhanVienBanHang.md), [QuanLyCuaHang](QuanLyCuaHang.md). [QuanLyTaiKhoan](QuanLyTaiKhoan.md) giữ chung `List<TaiKhoan>` và kiểm tra trùng tên đăng nhập. [VaiTro](VaiTro.md) được suy ra từ lớp cụ thể, không lưu thêm một thuộc tính vai trò có setter trong đối tượng.

Cả ba lớp con ánh xạ vào bảng logic `TAI_KHOAN`; cột `vaiTro` giúp đọc lại đúng lớp. Xem thứ tự cột tại [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md).

## Ràng buộc riêng

- Tên đăng nhập được trim, chuyển chữ thường bằng `Locale.ROOT`, không rỗng và duy nhất sau chuẩn hóa. Mật khẩu không bị trim hoặc đổi chữ.
- Không lưu mật khẩu rõ, không in hash/salt trong menu; số vòng băm phải dương và được kiểm tra giới hạn khi đọc file.
- Không đổi loại tài khoản sau tạo. Không xóa tài khoản để giữ tham chiếu lịch sử.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền và bảo vệ quản lý hoạt động cuối cùng trước khi khóa. `khoa()` không tự quyết định quy tắc cần đọc cả danh sách.

[Mục lục lớp](../04-thiet-ke-lop.md)
