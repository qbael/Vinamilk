# LoaiSanPham

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public enum LoaiSanPham`, file `LoaiSanPham.java`.

| Giá trị | Nhóm hàng |
| --- | --- |
| `SUA` | Sữa |
| `SUA_CHUA` | Sữa chua |
| `KEM` | Kem |
| `NUOC_GIAI_KHAT` | Nước giải khát |
| `PHO_MAI` | Phô mai |

Dùng làm kiểu của `SanPham.loai`, hỗ trợ hiển thị và tìm/lọc sản phẩm. Mỗi [SanPham](SanPham.md) phải thuộc đúng một nhóm trong năm giá trị trên; không dùng `null` hoặc nhãn tùy ý thay enum.

Lưu tên enum vào cột `loai` của `SAN_PHAM`, không tạo bảng riêng. Nhóm hàng không quyết định đóng gói, giá hay tồn: các thông tin đó thuộc [BienTheSanPham](BienTheSanPham.md). Chưa cần lớp con riêng cho từng nhóm khi không có hành vi khác nhau.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
