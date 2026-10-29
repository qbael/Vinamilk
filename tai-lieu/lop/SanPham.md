# SanPham

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class SanPham`, file `SanPham.java`.

Mô tả hàng hóa ở mức sản phẩm chung, ví dụ sữa tươi có đường hộp 180 ml. Giá, cách đóng gói và tồn nằm ở [BienTheSanPham](BienTheSanPham.md), không lưu lặp tại đây.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `maSanPham` | `String` | Mã duy nhất, bất biến |
| `tenSanPham` | `String` | Tên sản phẩm, bắt buộc |
| `thuongHieu` | `String` | Thương hiệu, bắt buộc |
| `donViCoSo` | `String` | Đơn vị cơ sở như hộp, chai, hũ; bắt buộc |
| `dinhLuong` | `String` | Định lượng một đơn vị cơ sở, ví dụ `180 ml`; bắt buộc |
| `moTa` | `String` | Mô tả bổ sung, có thể trống |
| `loai` | `LoaiSanPham` | Một trong năm nhóm hàng |
| `dangKinhDoanh` | `boolean` | Sản phẩm đang mở bán hay ngừng bán |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `void capNhatThongTin(String tenSanPham, String thuongHieu, String moTa)` | Cập nhật nội dung mô tả hợp lệ, không biến sản phẩm thành hàng khác |
| `void ngungKinhDoanh()` | Ngừng bán sản phẩm |
| `void moKinhDoanh()` | Cho phép bán lại, vẫn cần kiểm tra trạng thái từng SKU |

Constructor kiểm tra đầy đủ các trường bắt buộc và loại sản phẩm. Thuộc tính là `private`; không có setter đổi mã tùy ý.

## Quan hệ, lưu trữ và ràng buộc

- Một sản phẩm có không hoặc nhiều SKU, liên kết bằng `BienTheSanPham.maSanPham`. Tra qua [QuanLyBienTheSanPham](QuanLyBienTheSanPham.md), không giữ thêm danh sách SKU trong sản phẩm.
- [QuanLySanPham](QuanLySanPham.md) giữ danh mục sản phẩm dùng chung. Bảng logic `SAN_PHAM` có khóa chính `maSanPham`; xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md).
- Không xóa sản phẩm. Ngừng sản phẩm chặn việc bán tất cả SKU của nó mà không cần thay đổi trạng thái riêng từng SKU.
- Đổi đơn vị cơ sở hoặc định lượng làm thành hàng khác thì tạo sản phẩm mới. Không dùng sửa tên để biến sữa 180 ml có lịch sử thành sữa 1 lít.
- Năm nhóm hàng dùng [LoaiSanPham](LoaiSanPham.md); không tạo lớp `Sua`, `Kem`… khi chưa có hành vi riêng. Đổi mô tả không viết lại dữ liệu đã chụp trong chứng từ cũ.

[Mục lục lớp](../04-thiet-ke-lop.md)
