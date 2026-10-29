# DonHang — đơn hàng

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường. Đại diện đơn khách tự đặt hoặc nhân viên lập tại quầy; sau thanh toán chính đơn này là dữ liệu để hiển thị hóa đơn.

## Thuộc tính private

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `maDonHang` | `String` | Khóa chính, không đổi |
| `maKhachHang` | `String` | Khách thành viên; null nếu vãng lai |
| `maNguoiLap` | `String` | Khách tự đặt hoặc nhân viên/quản lý lập đơn |
| `maNguoiThuTien` | `String` | Nhân viên/quản lý; null khi chưa thanh toán |
| `thoiDiemTao`, `thoiDiemThanhToan` | `LocalDateTime` | Thời điểm tạo và thu tiền; thời điểm thu có thể null |
| `trangThai` | `TrangThaiDonHang` | Chờ thanh toán, đã thanh toán hoặc đã hủy |
| `diemSuDung`, `diemCong` | `int` | Điểm đã trừ và điểm đã cộng trong giao dịch |
| `tongTienHang`, `tongGiamKhuyenMai` | `long` | Tổng tiền trước giảm và tổng giảm khuyến mãi |
| `tienGiamDiem`, `tongThanhToan` | `long` | Tiền quy đổi điểm và số tiền thực trả |
| `chiTiet` | `DanhSachChiTietDonHang` | Danh sách riêng của đơn này, không static |

## Phương thức dự kiến

- Constructor: nhận người lập, khách nếu có và danh sách chi tiết; kiểm tra toàn bộ dữ liệu rồi gọi `chiTiet.khoaChinhSua()` khi nhận quyền sở hữu, tạo đơn chờ hợp lệ với mã mới.
- `capNhatTamTinh()`: tổng hợp giá/giảm từ các dòng, không dùng điểm.
- `chotThanhToan(...)`: nhận các dòng đã tính lại, người thu, thời điểm và điểm sử dụng; kiểm tra trạng thái chờ và tính nhất quán các tổng trước khi chốt.
- Khi chốt thanh toán, kiểm tra các dòng mới giữ đúng bộ SKU và số lượng đã đặt, tạo danh sách chi tiết mới và khóa trước khi thay bên trong đơn. Không gọi CRUD trên danh sách đã khóa hoặc mở khóa danh sách cũ.
- `huy()`: chỉ cho chuyển từ chờ sang đã hủy.
- Getter để xem dữ liệu; không có setter trạng thái hoặc tổng tiền tự do.

Chữ ký đầy đủ của thao tác chốt có thể quyết định khi triển khai, nhưng các trường tiền phải được tính từ dữ liệu đã xác nhận, không nhận giá trị tùy ý từ console. Lớp này không tự giảm tồn, trừ điểm hoặc ghi file; [DichVuCuaHang](DichVuCuaHang.md) phối hợp các thay đổi đó trong một lần lưu.

## Quan hệ và ràng buộc

- Được quản lý trong [QuanLyDonHang](QuanLyDonHang.md); sở hữu [DanhSachChiTietDonHang](DanhSachChiTietDonHang.md).
- Khi nạp file hoặc phục hồi, ghép đủ chi tiết trong vùng tạm rồi khóa danh sách trước khi công bố đơn. Cờ khóa được dựng lại trong RAM, không đổi định dạng file text.
- Ánh xạ bảng `DON_HANG`; các dòng thuộc `CT_DON_HANG` trong [mô hình dữ liệu](../05-mo-hinh-du-lieu.md).
- Đơn chờ/hủy có điểm dùng, điểm cộng, tiền giảm điểm bằng 0 và không có người/thời điểm thu tiền. Giá trị tiền của đơn chờ là tạm tính.
- Khi thanh toán: tính lại giá và khuyến mãi, hỏi điểm muốn dùng, xác nhận tổng cuối. Đơn đã thanh toán giữ giá trị lịch sử, không sửa/xóa/hủy/thanh toán lần hai.
- Không sửa dòng hoặc người mua của một đơn đã lưu qua menu; hủy đơn chờ và lập lại khi chọn nhầm. Đơn vãng lai không có điểm.
