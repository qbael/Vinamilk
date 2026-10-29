# VaiTro

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public enum VaiTro`, file `VaiTro.java`.

| Giá trị | Ý nghĩa | Lớp tài khoản tương ứng |
| --- | --- | --- |
| `KHACH_HANG` | Thành viên mua hàng | [KhachHang](KhachHang.md) |
| `NHAN_VIEN_BAN_HANG` | Nhân viên bán hàng | [NhanVienBanHang](NhanVienBanHang.md) |
| `QUAN_LY_CUA_HANG` | Quản lý cửa hàng | [QuanLyCuaHang](QuanLyCuaHang.md) |

Là kiểu trả về của `TaiKhoan.layVaiTro()`, dùng để hiển thị menu và kiểm tra quyền. Vai trò được quyết định bởi lớp tài khoản cụ thể; không có setter đổi vai trò sau tạo.

Lưu tên enum tại cột `vaiTro` của `TAI_KHOAN`; khi đọc, tạo đúng lớp con. Giá trị ngoài ba tên trên là lỗi dữ liệu. Enum không có bảng riêng, không có giá trị khách vãng lai vì khách vãng lai không phải một loại tài khoản.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md), [Tài khoản và phân quyền](../02-tai-khoan-va-phan-quyen.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
