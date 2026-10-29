# DanhSachChiTietDonHang

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý các dòng của đúng một [DonHang](DonHang.md), theo quan hệ composition. **Khai báo dự kiến:** `public final class DanhSachChiTietDonHang extends DanhSach<ChiTietDonHang>`. Mỗi đơn tạo một đối tượng danh sách riêng; các phương thức là instance và thực hiện [IQuanLy](IQuanLy.md) qua lớp cha.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `protected final List<ChiTietDonHang> danhSach` (kế thừa) | Chi tiết thuộc riêng đơn đang sở hữu; được khởi tạo ở [DanhSach](DanhSach.md), không khai báo lại |
| `private boolean daChot = false` | Khóa CRUD sau khi đơn nhận quyền sở hữu; trạng thái trong RAM, không thêm cột lưu trữ |

## Phương thức dự kiến

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<ChiTietDonHang> xuat()` | Kế thừa từ lớp cha, trả `List.copyOf(danhSach)` |
| `ChiTietDonHang timTheoBienThe(String maBienThe)` | Trả dòng SKU hoặc `null` nếu chưa có |
| `void them(ChiTietDonHang chiTiet)` | Override: thêm dòng mới; nếu đã có SKU, gộp lượng rồi tính lại tạm tính cho toàn dòng |
| `void sua(String maBienThe, ChiTietDonHang duLieuMoi)` | Override: thay dòng khi đang soạn, giữ cùng SKU; kiểm tra lượng và tính lại giá/ưu đãi cho toàn dòng trước khi thay |
| `void xoa(String maBienThe)` | Override: xóa một dòng khi đơn còn đang soạn; báo lỗi nếu không có dòng |
| `void khoaChinhSua()` | Package-private: đặt `daChot = true`, không có thao tác mở khóa |
| `long tinhTongTienHang()` | Cộng `tinhTienHang()` của từng chi tiết |
| `long tinhTongGiam()` | Tổng tiền giảm khuyến mãi của các dòng, chưa gồm đổi điểm |

## Quan hệ và ràng buộc

- Phần tử là [ChiTietDonHang](ChiTietDonHang.md); mỗi SKU tối đa một dòng. Danh sách đang soạn có thể rỗng, đơn trước khi lưu phải có ít nhất một dòng.
- Lượng phải dương, kiểm tra tràn khi cộng gộp và khi tính tổng tiền. [DichVuCuaHang](DichVuCuaHang.md) kiểm tra tồn theo **tổng lượng đã gộp**, tính lại chương trình tốt nhất cho cả dòng; không cộng cơ học hai khoản giảm đã làm tròn của hai lần chọn.
- `them`/`sua` từ chối null; `sua`/`xoa` kiểm tra mã và báo lỗi nếu không có dòng. `sua` không cộng thêm lượng như `them`; dữ liệu mới phải có `maBienThe` trùng mã cần sửa, lượng dương và không vượt tồn sau kiểm tra của dịch vụ.
- Chỉ cho thêm/sửa/xóa lúc soạn danh sách trước khi tạo đơn. Sau khi kiểm tra đủ dữ liệu, constructor của đơn gọi `khoaChinhSua()` khi nhận quyền sở hữu, kể cả đơn chờ hoặc đơn dựng lại từ file. Cả ba override kiểm tra `daChot` trước khi thay đổi và báo `IllegalStateException` nếu đã khóa, kể cả lời gọi qua `IQuanLy<ChiTietDonHang>` hoặc tham chiếu cũ của nơi soạn.
- Đơn đã lưu không sửa dòng từ menu; muốn đổi hàng phải hủy đơn chờ và lập lại. Thanh toán chỉ được dịch vụ tính lại giá/ưu đãi trên cùng các SKU và số lượng đã đặt, sau xác nhận của khách, qua hành vi chốt của đơn. Hành vi đó tạo và khóa danh sách chi tiết mới rồi thay bên trong đơn, không mở khóa danh sách cũ.
- Đơn sở hữu kiểm soát quyền sửa và không công khai đối tượng danh sách có thể thay đổi cho giao diện. `List.copyOf` là bản sao nông, nên chi tiết đã chốt cũng không có setter tùy ý.
- Các tổng của lớp chưa áp dụng điểm; phần giảm điểm/tổng trả nằm ở giao dịch thanh toán của đơn.
- Lớp không có bảng/file riêng. Khi [LuuTruText](LuuTruText.md) ghi `CT_DON_HANG`, mã đơn được lấy từ chủ sở hữu; khóa dòng là `(maDonHang, maBienThe)`.
