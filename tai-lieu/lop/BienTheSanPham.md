# BienTheSanPham

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class BienTheSanPham`, file `BienTheSanPham.java`.

Đại diện một SKU có quy cách đóng gói, giá bán và tồn độc lập. Ví dụ hộp lẻ, lốc 4 hộp và thùng 48 hộp của cùng một loại sữa là ba đối tượng khác nhau.

## Thuộc tính

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `maBienThe` | `String` | Mã SKU duy nhất, bất biến |
| `maSanPham` | `String` | Mã sản phẩm cha, không đổi sau tạo |
| `tenQuyCach` | `String` | Tên quy cách, ví dụ `Lốc 4 hộp 180 ml`; không rỗng |
| `kieuDongGoi` | `KieuDongGoi` | Bán lẻ, lốc hoặc thùng |
| `soDonViCoSo` | `int` | Số đơn vị cơ sở trong một SKU |
| `soLuongTon` | `int` | Số đơn vị SKU đang tồn; không phải số hộp quy đổi |
| `giaBan` | `long` | Giá bán một SKU, đơn vị đồng |
| `dangKinhDoanh` | `boolean` | Trạng thái mở/ngừng bán của riêng SKU |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `void doiGia(long giaMoi)` | Đổi sang giá bán dương |
| `void tangTon(int soLuong)` | Tăng lượng hợp lệ, kiểm tra tràn `int` |
| `void giamTon(int soLuong)` | Giảm lượng hợp lệ, không để tồn âm |
| `void ngungKinhDoanh()` | Ngừng bán SKU |
| `void moKinhDoanh()` | Mở lại SKU; sản phẩm cha vẫn phải đang kinh doanh |

Thuộc tính là `private`. Không có setter gán tồn tùy ý. [DichVuCuaHang](DichVuCuaHang.md) chỉ yêu cầu tăng tồn khi nhập, giảm tồn khi thanh toán và lưu đồng thời với chứng từ.

## Quan hệ, lưu trữ và ràng buộc

- Thuộc một [SanPham](SanPham.md) tồn tại. [QuanLyBienTheSanPham](QuanLyBienTheSanPham.md) quản lý danh sách và kiểm tra không trùng bộ `(maSanPham, kieuDongGoi, soDonViCoSo)`.
- Lưu tại bảng `BIEN_THE`, khóa chính `maBienThe`, khóa ngoại `maSanPham`. SKU được tham chiếu bởi [KhuyenMai](KhuyenMai.md), [ChiTietDonHang](ChiTietDonHang.md) và [ChiTietPhieuNhap](ChiTietPhieuNhap.md).
- `giaBan > 0`, `soLuongTon ≥ 0`. SKU mới có tồn 0. `BAN_LE` có 1 đơn vị cơ sở; `LOC`/`THUNG` có hơn 1.
- Hai thùng 24 và 48 đơn vị là hai SKU hợp lệ. Không đổi cấu tạo quy cách thành SKU khác; tạo SKU mới khi thay đổi hàng hóa/quy cách.
- Không tự tách thùng thành lốc/lẻ. Chỉ bán khi cả sản phẩm cha và SKU đang kinh doanh; đặt đơn không giữ tồn.
- Không xóa SKU để bảo toàn tham chiếu lịch sử. Giá mới không thay đơn giá đã chốt của đơn cũ.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
