# QuanLyDonHang

[Mục lục lớp](../04-thiet-ke-lop.md)

## Trách nhiệm và cấu trúc

Quản lý đơn đặt của khách và đơn bán tại quầy trong một danh sách. Đơn đã thanh toán cũng là hóa đơn để tra cứu. Khai báo `public final class QuanLyDonHang extends DanhSach<DonHang>`, constructor `public QuanLyDonHang()`. Kế thừa hợp đồng [IQuanLy](IQuanLy.md) qua [DanhSach](DanhSach.md); mọi thao tác danh sách là instance, không dùng lớp lồng.

| Thuộc tính | Ý nghĩa |
| --- | --- |
| `danhSach` kế thừa từ `DanhSach<DonHang>` | Danh sách đơn dùng chung; không khai báo lại trường danh sách |

[Main](Main.md) tạo một manager dùng chung, truyền cùng tham chiếu cho dịch vụ và lưu trữ; đổi phiên đăng nhập không tạo danh sách mới.

## Phương thức dự kiến

Các phương thức dưới đây là instance; `xuat()` kế thừa `public final` từ lớp cha, còn `them`, `sua`, `xoa` được lớp này override với phạm vi `public`.

| Chữ ký | Kết quả / trách nhiệm |
| --- | --- |
| `List<DonHang> xuat()` | Kế thừa; trả `List.copyOf(danhSach)`; không đưa trực tiếp toàn bộ cho khách hàng |
| `DonHang timTheoMa(String maDonHang)` | Trả đơn hoặc `null` nếu không tìm thấy |
| `List<DonHang> timTheoKhach(String maKhachHang)` | Tra lịch sử của một khách thành viên |
| `List<DonHang> timTheoTrangThai(TrangThaiDonHang trangThai)` | Lọc đơn chờ, đã thanh toán hoặc đã hủy |
| `List<DonHang> timDaThanhToanTrongKhoang(LocalDate tuNgay, LocalDate denNgay)` | Chỉ lấy đơn đã thanh toán theo ngày thanh toán, bao gồm hai đầu |
| `void them(DonHang donHang)` | Từ chối `null`, trùng mã, chứng từ rỗng hoặc không hợp lệ |
| `void sua(String ma, DonHang duLieuMoi)` | Ném `UnsupportedOperationException("Không được sửa trực tiếp đơn hàng; hãy dùng nghiệp vụ thanh toán hoặc hủy qua dịch vụ.")` |
| `void xoa(String ma)` | Ném `UnsupportedOperationException("Không được xóa đơn hàng; hãy hủy đơn chờ qua dịch vụ.")` |
| `void napDanhSach(List<DonHang> duLieuDaKiemTra)` | Package-private, chỉ phục vụ nạp/phục hồi: kiểm tra toàn bộ lô và vòng đời trước khi `clear`/`addAll`; giữ trạng thái và số tiền đã chốt, không xử lý như thêm đơn mới |

## Quan hệ và ràng buộc

- Mỗi [DonHang](DonHang.md) sở hữu một [DanhSachChiTietDonHang](DanhSachChiTietDonHang.md) riêng. Manager không giữ một danh sách chi tiết dùng chung thứ hai.
- [DichVuCuaHang](DichVuCuaHang.md) kiểm tra tài khoản, quyền và sở hữu đơn trước khi cho xem hoặc thay đổi. Manager tìm theo mã không thay thế kiểm tra quyền.
- Thanh toán/hủy là hành vi có kiểm soát của đơn, do dịch vụ điều phối cùng tồn, điểm và lưu file. `sua`/`xoa` có mặt để thực thi hợp đồng chung nhưng luôn từ chối, không thay nguyên đơn hoặc xóa lịch sử; không thanh toán lần hai. `them` không tự đổi tồn, điểm hoặc ghi file.
- Khoảng ngày phải hợp lệ (`tuNgay ≤ denNgay`); đơn chờ/hủy không góp vào thu bán. Báo cáo lấy số tiền đã chốt, không dùng giá danh mục hiện tại.
- Danh sách trả về dùng `List.copyOf`, là bản sao nông; giao diện chỉ đọc, không sửa phần tử để vượt vòng đời đơn.
- Lớp không có bảng/file riêng; các đơn và dòng con ánh xạ `DON_HANG`/`CT_DON_HANG`. [LuuTruText](LuuTruText.md) lưu toàn hệ thống một lần khi giao dịch được xác nhận.
