# QuanLyTaiKhoan

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý một danh sách tài khoản dùng chung cho cả ba vai trò trong cùng JVM. Khai báo `public final class QuanLyTaiKhoan extends DanhSach<TaiKhoan>`, constructor `public QuanLyTaiKhoan()`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<TaiKhoan>` | Chứa đối tượng `KhachHang`, `NhanVienBanHang`, `QuanLyCuaHang`; không khai báo lại trường danh sách |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; đổi phiên đăng nhập không tạo danh sách mới.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<TaiKhoan> xuat()` | Kế thừa; trả `List.copyOf(danhSach)`, không tự in thông tin tài khoản |
| `TaiKhoan timTheoMa(String maTaiKhoan)` | Trả tài khoản hoặc `null` nếu không tìm thấy |
| `TaiKhoan timTheoTenDangNhap(String tenDangNhap)` | Tìm sau khi trim và chuyển chữ thường bằng `Locale.ROOT`; không chuẩn hóa mật khẩu |
| `List<KhachHang> timKhachTheoSoDienThoai(String soDienThoai)` | Tra thành viên; trả danh sách vì số điện thoại không được quy định là duy nhất |
| `int demQuanLyDangHoatDong()` | Đếm tài khoản quản lý chưa bị khóa |
| `void them(TaiKhoan taiKhoan)` | Từ chối `null`, trùng mã hoặc trùng tên đăng nhập đã chuẩn hóa |
| `void sua(String ma, TaiKhoan duLieuMoi)` | Từ chối `null`, mã không tồn tại hoặc mã không khớp; kiểm tra toàn bộ trường trước khi gọi `khoa()`/`moKhoa()`, không thay phần tử bằng `list.set` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa tài khoản; hãy khóa tài khoản qua dịch vụ.")` |
| `void napDanhSach(List<TaiKhoan> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô trước khi `clear`/`addAll`; giữ điểm và thông tin xác thực đã lưu, không áp điều kiện tài khoản mới có điểm 0 |

## Quan hệ và ràng buộc

- Phần tử có kiểu cha [TaiKhoan](TaiKhoan.md); vai trò lấy qua phương thức đa hình `layVaiTro()`, không tự sửa trường vai trò.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền trước khi thêm, khóa/mở tài khoản hoặc yêu cầu đổi điểm. Không được khóa quản lý hoạt động cuối cùng; đăng ký công khai chỉ tạo khách hàng, điểm ban đầu 0.
- `sua` chỉ đổi trạng thái bằng hành vi của tài khoản sau khi dịch vụ kiểm tra quyền và bảo vệ quản lý hoạt động cuối cùng. Mã, lớp cụ thể/vai trò, tên đăng nhập, họ tên, số điện thoại, hash/salt/tham số băm và điểm phải giữ nguyên; không dùng `sua` để đổi loại, thông tin xác thực hoặc gán điểm tùy ý.
- Không xóa tài khoản đã có lịch sử; dùng trạng thái hoạt động. Tra số điện thoại không đồng nghĩa được phép dùng điểm của người đó.
- `List.copyOf` chỉ bảo vệ cấu trúc danh sách, không làm phần tử bất biến. Giao diện chỉ đọc; không cho sửa trực tiếp dữ liệu xác thực, điểm hoặc trạng thái tài khoản.
- Lớp này không có bảng/file riêng. Phần tử ánh xạ vào bảng logic `TAI_KHOAN`; việc đọc/ghi do [LuuTruText](LuuTruText.md) đảm nhiệm.
