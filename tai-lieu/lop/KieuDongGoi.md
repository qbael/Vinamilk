# KieuDongGoi

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public enum KieuDongGoi`, file `KieuDongGoi.java`.

| Giá trị | Ý nghĩa | Ràng buộc `soDonViCoSo` |
| --- | --- | --- |
| `BAN_LE` | Một đơn vị cơ sở như hộp/chai/hũ | Bằng 1 |
| `LOC` | Một lốc gồm nhiều đơn vị cơ sở | Lớn hơn 1 |
| `THUNG` | Một thùng gồm nhiều đơn vị cơ sở | Lớn hơn 1 |

Dùng cho `BienTheSanPham.kieuDongGoi`; lưu tên enum tại cột `kieuDongGoi` của `BIEN_THE`, không có bảng riêng. Không để `null` và không chấp nhận tên ngoài ba giá trị trên.

[BienTheSanPham](BienTheSanPham.md) còn có `soDonViCoSo` nên cùng một sản phẩm có thể có thùng 24 và thùng 48 đơn vị. Bộ `(maSanPham, kieuDongGoi, soDonViCoSo)` không được trùng. Tồn của mỗi SKU độc lập; enum không tạo cơ chế tự quy đổi hoặc tách thùng.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
