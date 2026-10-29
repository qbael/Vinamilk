# KhuyenMaiPhanTram

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class KhuyenMaiPhanTram extends KhuyenMai`, file `KhuyenMaiPhanTram.java`.

Tính giảm theo tỷ lệ phần trăm trên tổng tiền một dòng hàng. Kế thừa mã, tên, SKU, khoảng ngày và trạng thái từ [KhuyenMai](KhuyenMai.md).

## Thuộc tính riêng

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `tyLePhanTram` | `int` | Phần trăm được giảm, trong khoảng 1–100 |

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public long tinhTienGiam(long donGia, int soLuong)` | Override, trả tiền giảm của cả dòng theo phần trăm |
| `void doiTyLePhanTram(int tyLeMoi)` | Cập nhật tỷ lệ hợp lệ cho các lần thanh toán tiếp theo |

Công thức: `floor((donGia × soLuong) × tyLePhanTram / 100)`. Làm tròn xuống đồng trên cả dòng, không làm tròn từng đơn vị rồi nhân lượng. Ví dụ giá 10.001 đồng, lượng 2, giảm 15% thì tiền giảm là 3.000 đồng.

## Quan hệ, lưu trữ và ràng buộc

- Thực hiện hợp đồng của [ChinhSachGiamGia](ChinhSachGiamGia.md) thông qua lớp cha; không tự kiểm tra tồn hoặc trừ tiền vào đơn.
- Bản ghi `KHUYEN_MAI` có `loaiKhuyenMai = PHAN_TRAM`, `giaTri = tyLePhanTram`. Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md).
- Giá và số lượng phải dương; phải tránh hoặc phát hiện tràn số ở phép nhân trung gian. Kết quả không âm và không vượt tiền hàng của dòng.
- Giảm 100% là hợp lệ. Việc chọn chương trình có lợi nhất, xử lý đơn tổng 0 và chốt giao dịch nằm tại dịch vụ theo [Quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md).
- Thay đổi tỷ lệ không sửa tiền giảm đã chốt trên hóa đơn cũ.

[Mục lục lớp](../04-thiet-ke-lop.md)
