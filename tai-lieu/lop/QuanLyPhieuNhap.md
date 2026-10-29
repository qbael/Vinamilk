# QuanLyPhieuNhap

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý lịch sử các lần đã nhận hàng và trả đủ tiền. Khai báo `public final class QuanLyPhieuNhap extends DanhSach<PhieuNhap>`, constructor `public QuanLyPhieuNhap()`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<PhieuNhap>` | Danh sách phiếu nhập dùng chung; không khai báo lại trường danh sách |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; đổi phiên đăng nhập không tạo danh sách mới.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<PhieuNhap> xuat()` | Kế thừa; trả `List.copyOf(danhSach)` để giao diện trình bày |
| `PhieuNhap timTheoMa(String maPhieuNhap)` | Trả phiếu hoặc `null` nếu không tìm thấy |
| `List<PhieuNhap> timTrongKhoang(LocalDate tuNgay, LocalDate denNgay)` | Lọc theo ngày nhập, bao gồm hai đầu; yêu cầu ngày bắt đầu không sau ngày kết thúc |
| `void them(PhieuNhap phieuNhap)` | Từ chối `null`, trùng mã hoặc phiếu không hợp lệ; không cho ghi nhận lần hai |
| `void sua(String ma, PhieuNhap duLieuMoi)` | Ném `UnsupportedOperationException("Không được sửa phiếu nhập đã ghi nhận.")` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa phiếu nhập đã ghi nhận.")` |
| `void napDanhSach(List<PhieuNhap> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô trước khi `clear`/`addAll`; giữ dữ liệu đã chốt, không tăng tồn hoặc ghi nhận lại phiếu |

## Quan hệ và ràng buộc

- Mỗi [PhieuNhap](PhieuNhap.md) sở hữu một [DanhSachChiTietPhieuNhap](DanhSachChiTietPhieuNhap.md) riêng và ít nhất một dòng trước khi ghi nhận.
- [DichVuCuaHang](DichVuCuaHang.md) chỉ cho quản lý đang hoạt động ghi nhận nhập, kiểm tra SKU/nguồn/giá/lượng, rồi điều phối thêm phiếu và tăng tồn trong cùng giao dịch lưu. `them` của manager không tự tăng tồn hoặc ghi file.
- Phiếu đã ghi nhận bất biến, không sửa/xóa; nhập lại cùng mã bị từ chối. SKU ngừng bán vẫn có thể nhập nhưng không tự được mở bán lại.
- Chi nhập trong kỳ là tổng `tinhTongTien()` của các phiếu phù hợp. Phiếu không có công nợ hoặc trạng thái chờ trong phiên bản đầu.
- Danh sách trả về dùng `List.copyOf`, chỉ ngăn sửa cấu trúc; giao diện chỉ đọc và không được thay đổi phiếu đã ghi nhận.
- Lớp không có bảng/file riêng; phiếu và dòng con ánh xạ `PHIEU_NHAP`/`CT_PHIEU_NHAP`. [LuuTruText](LuuTruText.md) đảm nhiệm lưu cùng tồn và dữ liệu khác.
