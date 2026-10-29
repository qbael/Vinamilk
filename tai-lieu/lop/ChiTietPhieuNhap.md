# ChiTietPhieuNhap — một dòng nhập hàng

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường. Ghi số lượng và giá nhập một SKU, cùng tên/quy cách tại thời điểm nhập.

## Thuộc tính private

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `maBienThe` | `String` | SKU được nhập |
| `tenSanPhamLucNhap`, `tenQuyCachLucNhap` | `String` | Giá trị chụp để xem lịch sử |
| `soLuong` | `int` | Số đơn vị SKU nhập, dương |
| `donGiaNhap` | `long` | Giá nhập một SKU, số đồng dương |

## Phương thức dự kiến

- Constructor kiểm tra số lượng, giá và các trường bắt buộc.
- `long tinhThanhTien()`: lượng nhập × giá nhập, kiểm tra tràn số.
- Getter để đọc; không sửa chi tiết sau khi phiếu đã ghi nhận.

## Quan hệ và ràng buộc

- Thuộc [DanhSachChiTietPhieuNhap](DanhSachChiTietPhieuNhap.md); tham chiếu [BienTheSanPham](BienTheSanPham.md).
- Ánh xạ `CT_PHIEU_NHAP`, khóa `(maPhieuNhap, maBienThe)`; mã phiếu lấy từ [PhieuNhap](PhieuNhap.md) sở hữu khi ghi file.
- Giá nhập một thùng là giá cả thùng, không phải giá hộp lẻ bên trong. Không tự đổi đơn vị hoặc cập nhật giá bán từ giá nhập.
- Một SKU có một đơn giá trên một phiếu. Nếu cần giữ hai giá nhập khác nhau, lập hai phiếu.
