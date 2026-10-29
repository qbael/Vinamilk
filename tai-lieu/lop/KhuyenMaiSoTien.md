# KhuyenMaiSoTien

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class KhuyenMaiSoTien extends KhuyenMai`, file `KhuyenMaiSoTien.java`.

Tính giảm một số tiền cố định trên mỗi đơn vị SKU mua. Kế thừa thông tin chung từ [KhuyenMai](KhuyenMai.md); một SKU có thể là hộp lẻ, lốc hoặc thùng.

## Thuộc tính riêng

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `soTienGiamMoiDonVi` | `long` | Số đồng giảm cho mỗi đơn vị SKU, phải dương |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public long tinhTienGiam(long donGia, int soLuong)` | Override, tính tiền giảm trên cả dòng, chặn giảm vượt đơn giá |
| `void doiSoTienGiamMoiDonVi(long soTienMoi)` | Cập nhật mức giảm dương cho các lần thanh toán tiếp theo |

Công thức: `min(soTienGiamMoiDonVi, donGia) × soLuong`. Ví dụ giảm 2.000 đồng/lốc, mua 3 lốc thì giảm 6.000 đồng; không nhân theo số hộp bên trong lốc.

## Quan hệ, lưu trữ và ràng buộc

- Thực hiện hợp đồng [ChinhSachGiamGia](ChinhSachGiamGia.md) thông qua lớp cha; không tự sửa đơn hoặc tồn.
- Bản ghi `KHUYEN_MAI` có `loaiKhuyenMai = SO_TIEN`, `giaTri = soTienGiamMoiDonVi`. Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md).
- Giá và số lượng phải dương, phép nhân tiền phải kiểm tra tràn số. Mức giảm khai báo được phép lớn hơn đơn giá nhưng kết quả chỉ giảm tối đa tiền hàng.
- Thay đổi mức giảm không viết lại hóa đơn cũ. Việc kiểm tra hiệu lực và chọn chương trình tốt nhất thực hiện bên ngoài phương thức tính theo [Quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md).

[Mục lục lớp](../04-thiet-ke-lop.md)
