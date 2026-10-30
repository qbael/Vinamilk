# Main — điểm bắt đầu chương trình

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** top-level class thông thường; phương thức khởi động là `public static void main(String[] args)`. Không phải `static nested class`.

## Trách nhiệm

1. Xác định đường dẫn file dữ liệu của dự án và tạo một lần sáu đối tượng quản lý theo thứ tự dưới đây.
2. Tạo [LuuTruText](LuuTruText.md), truyền `Path` và đúng sáu đối tượng quản lý đó qua constructor.
3. Nếu file tồn tại, nạp và kiểm tra; nếu chưa tồn tại, yêu cầu xác nhận khởi tạo và nhập quản lý đầu tiên. File lỗi không được tự khởi tạo lại.
4. Tạo [DichVuCuaHang](DichVuCuaHang.md), truyền đúng sáu đối tượng quản lý trên cùng đối tượng lưu trữ qua constructor.
5. Tạo [GiaoDienConsole](GiaoDienConsole.md), truyền dịch vụ và bộ đọc console; chạy menu sau khi có dữ liệu hợp lệ.
6. Báo lỗi khởi động hoặc kết thúc chương trình rõ ràng; đóng tài nguyên khi kết thúc.

| Biến giữ đối tượng | Khởi tạo một lần trong `main` |
| --- | --- |
| `quanLyTaiKhoan` | `new QuanLyTaiKhoan()` |
| `quanLySanPham` | `new QuanLySanPham()` |
| `quanLyBienThe` | `new QuanLyBienTheSanPham(quanLySanPham)` |
| `quanLyKhuyenMai` | `new QuanLyKhuyenMai(quanLyBienThe)` |
| `quanLyDonHang` | `new QuanLyDonHang()` |
| `quanLyPhieuNhap` | `new QuanLyPhieuNhap()` |

Các danh sách là dữ liệu instance kế thừa từ `DanhSach<T>`, không phải danh sách `static`. Chia sẻ chính các đối tượng trên qua constructor đủ để dịch vụ và lưu trữ cùng thao tác một bộ dữ liệu; không cần singleton hoặc lớp chứa trung gian. Danh sách chi tiết thuộc từng `DonHang`/`PhieuNhap`, không tạo làm danh sách chung tại `Main`.

Không cần thuộc tính lưu danh mục hoặc logic tính tiền trong `Main`. Đồ án lưu sau mỗi thay đổi, không chờ thoát mới lưu. Khi triển khai dùng một điểm vào duy nhất ở `src/cuahang/Main.java` và thay thế điểm vào mẫu hiện có, không để hai `Main` gây nhầm cấu hình chạy.

Đã triển khai tại `src/cuahang/Main.java`, thay điểm vào mẫu cũ. Không yêu cầu tính năng preview hay tính năng riêng của JDK 26; đã chạy với JDK 25.
