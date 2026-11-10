# KhuyenMaiMuaTang

[Mục lục lớp](../04-thiet-ke-lop.md)

`public class KhuyenMaiMuaTang extends KhuyenMai`, file `src/cuahang/khuyenmai/KhuyenMaiMuaTang.java`.

Mua X tặng Y cùng SKU. Hai thuộc tính `int soLuongMua` và `int soLuongTang` phải dương, tổng không vượt `Integer.MAX_VALUE`. Kế thừa mã, tên, SKU, thời hạn và trạng thái từ `KhuyenMai`.

`tinhTienGiam(g, q) = g × floor(q / (X + Y)) × Y`, trong đó `q` là tổng hàng nhận, gồm cả hàng tặng. Chỉ nhóm đủ X + Y mới được giảm; hàng dư tính bình thường. Mua 3 tặng 2: nhận 5 trả 3, nhận 9 trả 7, nhận 10 trả 6. Mua 1 tặng 1: nhận 2 trả 1, nhận 3 trả 2. Giá và số lượng phải dương, tổng tiền hàng không được tràn `long`.

Lưu `loaiKhuyenMai = MUA_TANG`, cột `giaTri` dạng `X,Y` (ví dụ `3,2`). Bản ghi `MUA_2_TANG_1` cũ với `giaTri` trống được đọc thành X = 2, Y = 1 và giữ nguyên mã. Quản lý được tạo, sửa tên/thời hạn/hai số lượng, ngừng và xóa khi chưa được dùng; không đổi SKU hoặc loại giảm sau tạo. `doiSoLuongMuaTang` kiểm tra đủ cả hai giá trị trước khi gán.

Giỏ giữ và xuất đủ `q` đơn vị; hủy hoàn đủ `q`. Lớp khuyến mãi chỉ tính tiền giảm, không tự thêm dòng hay đổi tồn. Khi có nhiều khuyến mãi, dịch vụ chọn chương trình giảm nhiều nhất cho cả dòng, không cộng chồng.
