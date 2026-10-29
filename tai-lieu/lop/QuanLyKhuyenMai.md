# QuanLyKhuyenMai

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý chương trình khuyến mãi và chọn một chương trình đem lại mức giảm lớn nhất cho mỗi dòng SKU. Khai báo `public final class QuanLyKhuyenMai extends DanhSach<KhuyenMai>`, constructor `public QuanLyKhuyenMai(QuanLyBienTheSanPham quanLyBienTheSanPham)`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<KhuyenMai>` | Chứa cả `KhuyenMaiPhanTram` và `KhuyenMaiSoTien`; không khai báo lại trường danh sách |
| `private final QuanLyBienTheSanPham quanLyBienTheSanPham` | Manager SKU được truyền qua constructor, từ chối `null`; dùng kiểm tra SKU áp dụng |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; `quanLyBienTheSanPham` cũng là manager SKU dùng chung đó.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<KhuyenMai> xuat()` | Kế thừa; trả `List.copyOf(danhSach)` để giao diện trình bày |
| `KhuyenMai timTheoMa(String maKhuyenMai)` | Trả chương trình hoặc `null` nếu không tìm thấy |
| `List<KhuyenMai> timTheoBienThe(String maBienThe)` | Tra mọi chương trình của SKU, gồm cả chương trình hết hiệu lực |
| `KhuyenMai timTotNhat(String maBienThe, LocalDate ngay, long donGia, int soLuong)` | Chọn chương trình đúng SKU và đang có hiệu lực; trả `null` nếu không có |
| `void them(KhuyenMai khuyenMai)` | Từ chối `null`, trùng mã, SKU không tồn tại hoặc dữ liệu khuyến mãi sai |
| `void sua(String ma, KhuyenMai duLieuMoi)` | Từ chối `null`, mã không tồn tại hoặc mã không khớp; kiểm tra toàn bộ trường rồi gọi `capNhatThongTin`, phương thức đổi mức giảm đúng lớp con và `ngungApDung` nếu cần; không dùng `list.set` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa khuyến mãi; hãy ngừng áp dụng qua dịch vụ.")` |
| `void napDanhSach(List<KhuyenMai> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô trước khi `clear`/`addAll`, không áp điều kiện chỉ dành cho tạo mới |

## Quan hệ và ràng buộc

- [KhuyenMai](KhuyenMai.md) là abstract class thực thi [ChinhSachGiamGia](ChinhSachGiamGia.md). `timTotNhat` gọi đa hình `tinhTienGiam(donGia, soLuong)` cho từng ứng viên, không phân nhánh công thức theo lớp con.
- Lọc đúng SKU, cờ áp dụng và ngày nằm trong khoảng gồm cả hai đầu. Nếu nhiều chương trình giảm bằng nhau, chọn `maKhuyenMai` nhỏ nhất theo `String.compareTo`; không cộng chồng các chương trình. Chương trình hiệu lực cho kết quả 0 do làm tròn vẫn là ứng viên hợp lệ.
- Đơn giá và số lượng phải dương; kiểm tra tràn số theo hợp đồng tính tiền. Một chương trình chỉ áp dụng cho một SKU.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền quản lý trước khi thêm, sửa điều kiện hoặc ngừng chương trình. Không xóa bản ghi; mã SKU và loại giảm không đổi sau tạo. Sửa chương trình không được tính lại hóa đơn đã thanh toán.
- `sua` giữ nguyên mã, SKU và lớp cụ thể/loại giảm; chỉ sửa tên, thời hạn, giá trị giảm hợp lệ và ngừng áp dụng bằng hành vi hiện có. Chương trình đã ngừng không được mở lại qua `sua`; tạo chương trình mới nếu cần. Kiểm tra đủ trường trước khi thay đổi, không cập nhật dở dang khi giá trị giảm hoặc ngày sai.
- Danh sách trả về dùng `List.copyOf`; phần tử vẫn phải được bảo vệ bằng đóng gói và luồng thay đổi qua dịch vụ. Lớp không có bảng/file riêng; phần tử ánh xạ `KHUYEN_MAI`, do [LuuTruText](LuuTruText.md) đọc/ghi.
