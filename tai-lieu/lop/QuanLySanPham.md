# QuanLySanPham

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý danh mục sản phẩm dùng chung toàn ứng dụng. Khai báo `public final class QuanLySanPham extends DanhSach<SanPham>`, constructor `public QuanLySanPham()`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<SanPham>` | Nguồn danh mục sản phẩm chung; không khai báo lại trường danh sách |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; đổi phiên đăng nhập không tạo danh sách mới.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<SanPham> xuat()` | Kế thừa; trả `List.copyOf(danhSach)` để giao diện trình bày |
| `SanPham timTheoMa(String maSanPham)` | Trả sản phẩm hoặc `null` nếu không tìm thấy |
| `List<SanPham> timTheoTen(String tuKhoa)` | Tìm tên chứa từ khóa, không phân biệt chữ hoa/thường |
| `List<SanPham> timTheoLoai(LoaiSanPham loai)` | Lọc theo một trong năm nhóm hàng |
| `void them(SanPham sanPham)` | Từ chối `null`, trùng mã và dữ liệu sản phẩm không hợp lệ |
| `void sua(String ma, SanPham duLieuMoi)` | Từ chối `null`, mã không tồn tại hoặc mã không khớp; kiểm tra toàn bộ trường rồi gọi `capNhatThongTin`, `ngungKinhDoanh`/`moKinhDoanh`, không dùng `list.set` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa sản phẩm; hãy ngừng kinh doanh qua dịch vụ.")` |
| `void napDanhSach(List<SanPham> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô trước khi `clear`/`addAll`, không áp điều kiện chỉ dành cho tạo mới |

## Quan hệ và ràng buộc

- Quản lý các [SanPham](SanPham.md); các quy cách bán được quản lý riêng bởi [QuanLyBienTheSanPham](QuanLyBienTheSanPham.md). Không tạo bản sao danh sách SKU trong mỗi sản phẩm.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền quản lý trước khi thêm hoặc gọi hành vi cập nhật mô tả, ngừng/mở bán trên sản phẩm tìm được. Không dùng một setter chung để thay toàn bộ sản phẩm.
- `sua` chỉ đổi tên, thương hiệu, mô tả và trạng thái bằng các hành vi trên. Mã, lớp cụ thể, đơn vị cơ sở, định lượng và loại sản phẩm phải giữ nguyên; kiểm tra mọi trường trước khi thay đổi để không cập nhật dở dang.
- Không xóa sản phẩm đã có liên kết. Mã bất biến; thay định lượng/đơn vị thành mặt hàng khác phải tạo sản phẩm mới. Ngừng sản phẩm cha làm mọi SKU của nó không bán được dù cờ riêng của SKU vẫn bật.
- Mọi danh sách trả về đều không cho thêm/xóa trực tiếp, dùng `List.copyOf`. Đây là bản sao nông; giao diện chỉ đọc phần tử, thay đổi phải qua dịch vụ kiểm tra quyền.
- Lớp không tương ứng một bảng riêng; các phần tử ánh xạ `SAN_PHAM`. [LuuTruText](LuuTruText.md) đảm nhiệm lưu toàn hệ thống, manager không tự ghi file.
