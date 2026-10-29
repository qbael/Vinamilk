# QuanLyBienTheSanPham

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý SKU theo từng hình thức bán lẻ, lốc, thùng; mỗi SKU có giá và tồn riêng. Khai báo `public final class QuanLyBienTheSanPham extends DanhSach<BienTheSanPham>`, constructor `public QuanLyBienTheSanPham(QuanLySanPham quanLySanPham)`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<BienTheSanPham>` | Danh sách SKU dùng chung; không khai báo lại trường danh sách |
| `private final QuanLySanPham quanLySanPham` | Manager sản phẩm được truyền qua constructor, từ chối `null`; dùng kiểm tra sản phẩm cha |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; `quanLySanPham` cũng là manager sản phẩm dùng chung đó.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<BienTheSanPham> xuat()` | Kế thừa; trả `List.copyOf(danhSach)` để giao diện trình bày |
| `BienTheSanPham timTheoMa(String maBienThe)` | Trả SKU hoặc `null` nếu không tìm thấy |
| `List<BienTheSanPham> timTheoSanPham(String maSanPham)` | Trả các quy cách của cùng sản phẩm |
| `List<BienTheSanPham> layHangDangBan()` | SKU và sản phẩm cha đều đang kinh doanh; giao diện hiển thị riêng tình trạng hết hàng |
| `void them(BienTheSanPham bienThe)` | Từ chối `null`, kiểm tra sản phẩm cha tồn tại, mã không trùng, quy cách không trùng; SKU mới có tồn 0 |
| `void sua(String ma, BienTheSanPham duLieuMoi)` | Từ chối `null`, mã không tồn tại hoặc mã không khớp; kiểm tra toàn bộ trường rồi gọi `doiGia`, `ngungKinhDoanh`/`moKinhDoanh`, không dùng `list.set` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa SKU; hãy ngừng kinh doanh qua dịch vụ.")` |
| `void napDanhSach(List<BienTheSanPham> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô trước khi `clear`/`addAll`; giữ tồn đã đối chiếu từ chứng từ, không yêu cầu tồn 0 như SKU mới |

## Quan hệ và ràng buộc

- Phần tử [BienTheSanPham](BienTheSanPham.md) liên kết tới [SanPham](SanPham.md) bằng `maSanPham`. Không trùng bộ `(maSanPham, kieuDongGoi, soDonViCoSo)`.
- Mã và cấu tạo quy cách bất biến; giá bán dương, tồn không âm. Không xóa SKU có lịch sử; dùng cờ ngừng kinh doanh.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền rồi mới gọi `doiGia`, `tangTon`, `giamTon` của SKU. Tăng tồn chỉ qua ghi nhận nhập hàng, giảm tồn chỉ qua thanh toán; không có chức năng đặt tồn tùy ý.
- `sua` chỉ đổi giá và trạng thái bằng các hành vi trên. Mã, lớp cụ thể, sản phẩm cha, tên/cấu tạo quy cách, kiểu đóng gói, số đơn vị cơ sở và tồn phải giữ nguyên; từ chối dữ liệu đổi tồn hoặc chuyển SKU sang hàng khác.
- Tồn lẻ/lốc/thùng độc lập. Mua một lốc giảm một đơn vị SKU lốc; không tự tháo thùng hay đổi sang tồn hộp lẻ.
- Danh sách trả về dùng `List.copyOf`, chỉ bảo vệ cấu trúc. Giao diện không được sửa phần tử trực tiếp.
- Lớp không có bảng/file riêng; phần tử ánh xạ `BIEN_THE`. [LuuTruText](LuuTruText.md) đọc/ghi cùng các chứng từ để giữ tồn nhất quán.
