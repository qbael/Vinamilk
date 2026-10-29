# KhuyenMai

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public abstract class KhuyenMai implements ChinhSachGiamGia`, file `KhuyenMai.java`.

Giữ thông tin chung của một chương trình áp dụng cho một SKU. Lớp thực hiện phần kiểm tra hiệu lực chung và để cách tính giảm cho lớp con; không khởi tạo trực tiếp.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `maKhuyenMai` | `String` | Mã chương trình duy nhất, bất biến |
| `tenKhuyenMai` | `String` | Tên chương trình, không rỗng |
| `maBienThe` | `String` | SKU áp dụng, không đổi sau tạo |
| `tuNgay` | `LocalDate` | Ngày bắt đầu, tính cả ngày này |
| `denNgay` | `LocalDate` | Ngày kết thúc, tính cả ngày này |
| `dangApDung` | `boolean` | Cho phép áp dụng chương trình hay đã ngừng |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `boolean coHieuLuc(LocalDate ngay)` | Kiểm tra đang áp dụng và `tuNgay ≤ ngay ≤ denNgay` |
| `public abstract long tinhTienGiam(long donGia, int soLuong)` | Hợp đồng tính tiền giảm được lớp con override |
| `void capNhatThongTin(String tenKhuyenMai, LocalDate tuNgay, LocalDate denNgay)` | Cập nhật thông tin chung sau kiểm tra hợp lệ |
| `void ngungApDung()` | Ngừng dùng chương trình cho các lần thanh toán tiếp theo |

Constructor kiểm tra mã, tên, SKU và khoảng ngày; thuộc tính là `private`. `coHieuLuc` chỉ kiểm tra trạng thái/ngày, bên gọi còn phải kiểm tra đúng SKU.

## Quan hệ, lưu trữ và ràng buộc

- Implements [ChinhSachGiamGia](ChinhSachGiamGia.md); hai lớp con là [KhuyenMaiPhanTram](KhuyenMaiPhanTram.md) và [KhuyenMaiSoTien](KhuyenMaiSoTien.md).
- Tham chiếu một [BienTheSanPham](BienTheSanPham.md). Muốn áp dụng cho nhiều SKU thì tạo từng chương trình; chưa có bảng quan hệ nhiều–nhiều.
- [QuanLyKhuyenMai](QuanLyKhuyenMai.md) giữ `List<KhuyenMai>` chứa cả hai lớp con. Khi tính ưu đãi dùng đa hình, không thay bằng chuỗi điều kiện phân loại.
- Ánh xạ vào `KHUYEN_MAI`; cột `loaiKhuyenMai` xác định lớp con, cột `giaTri` chứa giá trị riêng của lớp con. Không cần thuộc tính loại có setter trong lớp cha.
- Ngày bắt đầu không sau ngày kết thúc. Không đổi mã SKU hoặc kiểu giảm sau tạo; trường hợp đó tạo chương trình mới.
- Không xóa chương trình. Sửa hoặc ngừng chương trình không tính lại giảm giá đã chốt trong [ChiTietDonHang](ChiTietDonHang.md).

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md), [Quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
