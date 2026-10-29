# QuanLyCuaHang

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class QuanLyCuaHang extends TaiKhoan`, file `QuanLyCuaHang.java`.

Đại diện tài khoản quản lý cửa hàng. Quản lý được bán hàng, quản lý danh mục, khuyến mãi, nhập hàng, tài khoản và xem báo cáo; các nghiệp vụ do [DichVuCuaHang](DichVuCuaHang.md) điều phối.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| Thuộc tính chung kế thừa | Theo [TaiKhoan](TaiKhoan.md) | Mã, tên đăng nhập, thông tin xác thực, họ tên, số điện thoại và trạng thái |

Không có thuộc tính riêng trong phiên bản này; không lưu báo cáo hay số điểm vào tài khoản quản lý.

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public VaiTro layVaiTro()` | Override, trả `VaiTro.QUAN_LY_CUA_HANG` |

Các hành vi xác thực và khóa/mở được kế thừa từ lớp cha.

## Quan hệ, lưu trữ và ràng buộc

- Kế thừa trực tiếp `TaiKhoan`, không kế thừa [NhanVienBanHang](NhanVienBanHang.md). Quyền bán hàng của quản lý được cấp bằng kiểm tra vai trò.
- Lưu tại `TAI_KHOAN` với `vaiTro = QUAN_LY_CUA_HANG`; cột `diemTichLuy` để trống.
- Có thể là người lập/thu tiền của [DonHang](DonHang.md) và người nhập của [PhieuNhap](PhieuNhap.md).
- Quản lý đầu tiên được tạo khi khởi tạo dữ liệu có xác nhận; tài khoản quản lý tiếp theo do quản lý đang hoạt động tạo. Không có tự đăng ký quản lý.
- Hệ thống luôn phải còn ít nhất một quản lý hoạt động. Dịch vụ kiểm tra toàn danh sách trước khi khóa; nếu tự khóa hợp lệ thì kết thúc phiên sau khi lưu thành công.

Xem [Tài khoản và phân quyền](../02-tai-khoan-va-phan-quyen.md), [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
