# ChinhSachGiamGia

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public interface ChinhSachGiamGia`, file `ChinhSachGiamGia.java`.

Quy định hợp đồng tính tiền giảm cho một dòng hàng. Interface không có thuộc tính trạng thái instance và không cần bảng dữ liệu riêng.

## Phương thức

```java
long tinhTienGiam(long donGia, int soLuong);
```

| Thành phần | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `donGia` | `long` | Giá bán một đơn vị SKU, đơn vị đồng, phải dương |
| `soLuong` | `int` | Số đơn vị SKU của dòng, phải dương |
| Kết quả | `long` | Tiền giảm trên cả dòng, từ 0 đến tiền hàng của dòng |

Phương thức không thay đổi giá, tồn, điểm hoặc dữ liệu đơn. Dữ liệu đầu vào không hợp lệ hoặc phép tính tràn số phải bị từ chối; không trả một số giảm sai để tiếp tục giao dịch.

## Quan hệ và đa hình

[KhuyenMai](KhuyenMai.md) implements interface này; [KhuyenMaiPhanTram](KhuyenMaiPhanTram.md) và [KhuyenMaiSoTien](KhuyenMaiSoTien.md) cung cấp hai cách tính. Nơi tính giá gọi qua `ChinhSachGiamGia` hoặc `KhuyenMai`, Java chọn đúng override tại runtime.

Việc kiểm tra SKU, thời hạn và chọn chương trình tốt nhất thuộc [QuanLyKhuyenMai](QuanLyKhuyenMai.md)/[DichVuCuaHang](DichVuCuaHang.md). Interface chỉ nhận giá và lượng để tính giảm, không tự xác định chương trình có đủ điều kiện áp dụng hay không.

Dữ liệu của đối tượng triển khai được lưu qua bảng `KHUYEN_MAI` tại [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md).

[Mục lục lớp](../04-thiet-ke-lop.md)
