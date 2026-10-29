# ChiTietDonHang — một dòng bán hàng

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường. Lưu một SKU, lượng mua và các giá trị được chụp lại để hóa đơn không đổi khi danh mục được cập nhật.

## Thuộc tính private

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `maBienThe` | `String` | Liên kết SKU được bán |
| `tenSanPhamLucBan`, `tenQuyCachLucBan` | `String` | Tên và quy cách tại giao dịch |
| `soLuong` | `int` | Số SKU mua, nguyên dương |
| `donGia` | `long` | Giá niêm yết của một SKU tại giao dịch |
| `maKhuyenMai` | `String` | Chương trình được chọn, có thể null |
| `tienGiamKhuyenMai` | `long` | Tổng giảm khuyến mãi trên cả dòng |

## Phương thức dự kiến

- `long tinhTienHang()`: `donGia × soLuong`, có kiểm tra tràn số.
- `long tinhTienSauKhuyenMai()`: tiền hàng trừ tiền giảm khuyến mãi.
- Constructor kiểm tra dữ liệu; getter để xem. Trong lúc soạn có thể tạo dòng mới thay dòng cũ khi đổi số lượng, thay vì cho UI sửa trực tiếp đối tượng.

## Quan hệ và ràng buộc

- Nằm trong [DanhSachChiTietDonHang](DanhSachChiTietDonHang.md), tham chiếu [BienTheSanPham](BienTheSanPham.md) và tối đa một [KhuyenMai](KhuyenMai.md).
- Ánh xạ `CT_DON_HANG`, khóa `(maDonHang, maBienThe)`. Trong RAM lấy mã đơn từ [DonHang](DonHang.md) sở hữu, không cần lặp trường `maDonHang` trong mỗi chi tiết.
- Giá dương, giảm không âm và không vượt tiền dòng. Không có mã khuyến mãi thì giảm bằng 0. Không lưu tiền giảm điểm ở dòng: đổi điểm là mức giảm cho toàn đơn.
- Đơn chờ dùng dòng tạm tính; thanh toán thay bằng dòng đã chốt. Hóa đơn cũ dùng dữ liệu của dòng, không tra giá hiện tại để tính lại.

Xem [công thức nghiệp vụ](../06-quy-tac-nghiep-vu.md).
