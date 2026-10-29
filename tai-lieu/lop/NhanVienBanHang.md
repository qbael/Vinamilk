# NhanVienBanHang

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class NhanVienBanHang extends TaiKhoan`, file `NhanVienBanHang.java`.

Đại diện tài khoản nội bộ bán hàng tại quầy và xử lý đơn khách đặt. Đây là đối tượng tài khoản; nghiệp vụ thanh toán nằm trong [DichVuCuaHang](DichVuCuaHang.md), không đặt trong lớp nhân viên.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| Thuộc tính chung kế thừa | Theo [TaiKhoan](TaiKhoan.md) | Mã, tên đăng nhập, thông tin xác thực, họ tên, số điện thoại và trạng thái |

Không có thuộc tính riêng trong phiên bản này; không tạo số điểm cho nhân viên.

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public VaiTro layVaiTro()` | Override, trả `VaiTro.NHAN_VIEN_BAN_HANG` |

Các hành vi xác thực và khóa/mở được kế thừa từ lớp cha.

## Quan hệ, lưu trữ và ràng buộc

- Kế thừa trực tiếp `TaiKhoan`, ngang hàng với [KhachHang](KhachHang.md) và [QuanLyCuaHang](QuanLyCuaHang.md).
- Lưu tại `TAI_KHOAN` với `vaiTro = NHAN_VIEN_BAN_HANG`; cột `diemTichLuy` để trống.
- Có thể được tham chiếu bởi `maNguoiLap`, `maNguoiThuTien` của [DonHang](DonHang.md); không được dùng làm `maKhachHang` thành viên.
- Quản lý tạo tài khoản này; không có tự đăng ký nội bộ. Vai trò không thay đổi sau tạo.
- Quyền bán hàng và phạm vi tra cứu được kiểm tra tại dịch vụ theo [Tài khoản và phân quyền](../02-tai-khoan-va-phan-quyen.md), không chỉ dựa vào menu.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
