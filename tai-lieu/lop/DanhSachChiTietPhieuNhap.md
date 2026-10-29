# DanhSachChiTietPhieuNhap

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý chi tiết thuộc đúng một [PhieuNhap](PhieuNhap.md), theo quan hệ composition. **Khai báo dự kiến:** `public final class DanhSachChiTietPhieuNhap extends DanhSach<ChiTietPhieuNhap>`. Mỗi phiếu tạo một đối tượng danh sách riêng; các phương thức là instance và thực hiện [IQuanLy](IQuanLy.md) qua lớp cha.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `protected final List<ChiTietPhieuNhap> danhSach` (kế thừa) | Các dòng thuộc riêng phiếu đang sở hữu; được khởi tạo ở [DanhSach](DanhSach.md), không khai báo lại |
| `private boolean daChot = false` | Khóa CRUD sau khi phiếu nhận quyền sở hữu; trạng thái trong RAM, không thêm cột lưu trữ |

## Phương thức dự kiến

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<ChiTietPhieuNhap> xuat()` | Kế thừa từ lớp cha, trả `List.copyOf(danhSach)` |
| `ChiTietPhieuNhap timTheoBienThe(String maBienThe)` | Trả dòng SKU hoặc `null` nếu chưa có |
| `void them(ChiTietPhieuNhap chiTiet)` | Override: thêm mới hoặc cộng lượng vào dòng cùng SKU và cùng đơn giá nhập |
| `void sua(String maBienThe, ChiTietPhieuNhap duLieuMoi)` | Override: thay dòng khi đang soạn, giữ cùng SKU; kiểm tra lượng, giá nhập và thành tiền trước khi thay |
| `void xoa(String maBienThe)` | Override: xóa dòng khi đang soạn; báo lỗi nếu không tìm thấy |
| `void khoaChinhSua()` | Package-private: đặt `daChot = true`, không có thao tác mở khóa |
| `long tinhTongTien()` | Tổng `tinhThanhTien()` của các dòng |

## Quan hệ và ràng buộc

- Phần tử là [ChiTietPhieuNhap](ChiTietPhieuNhap.md); mỗi SKU tối đa một dòng. Danh sách đang soạn có thể rỗng, nhưng phiếu phải có ít nhất một dòng để ghi nhận.
- Số lượng và giá nhập dương. Khi gộp cùng SKU, nếu đơn giá khác thì từ chối gộp; người dùng thống nhất một giá cho toàn dòng trước khi thêm lại hoặc lập phiếu khác. Không lấy trung bình giá ngầm.
- Giá nhập tính cho một SKU đúng quy cách: nhập 3 thùng nghĩa là lượng 3 và giá một thùng. Kiểm tra tràn số khi gộp lượng, nhân giá và cộng tổng.
- `them`/`sua` từ chối null; `sua`/`xoa` kiểm tra mã và báo lỗi nếu không có dòng. `sua` thay toàn dòng bằng lượng/giá mới đã kiểm tra, không cộng lượng và không đổi `maBienThe`.
- Chỉ chủ sở hữu và [DichVuCuaHang](DichVuCuaHang.md) được điều phối thay đổi lúc soạn. Phiếu đã ghi nhận bất biến; giao diện không được lấy danh sách này rồi thêm/xóa dòng.
- Sau khi kiểm tra đủ dữ liệu, constructor của phiếu gọi `khoaChinhSua()` khi nhận quyền sở hữu, kể cả phiếu dựng lại từ file. Cả ba override `them`/`sua`/`xoa` kiểm tra `daChot` trước khi thay đổi và báo `IllegalStateException` nếu đã khóa, kể cả lời gọi qua `IQuanLy<ChiTietPhieuNhap>` hoặc tham chiếu cũ của nơi soạn.
- `List.copyOf` chỉ bảo vệ cấu trúc; phần tử phải giữ đóng gói và không cho sửa chi tiết của phiếu đã chốt. Hai phiếu không được dùng chung đối tượng danh sách hoặc chi tiết có thể thay đổi.
- Lớp không tăng tồn, không ghi file và không có bảng riêng. [LuuTruText](LuuTruText.md) ghi các phần tử vào `CT_PHIEU_NHAP`, lấy mã phiếu từ chủ sở hữu; khóa dòng là `(maPhieuNhap, maBienThe)`.
