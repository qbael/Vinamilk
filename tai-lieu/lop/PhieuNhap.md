# PhieuNhap — chứng từ nhập hàng

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường. Ghi nhận một lần quản lý đã nhận hàng và trả đủ tiền nhập.

## Thuộc tính private

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `maPhieuNhap` | `String` | Khóa chính, không đổi |
| `maNguoiNhap` | `String` | Tài khoản quản lý lập phiếu |
| `tenNguonNhap` | `String` | Tên người/đơn vị cung cấp, không rỗng |
| `thoiDiemNhap` | `LocalDateTime` | Thời điểm ghi nhận nhập và chi tiền |
| `chiTiet` | `DanhSachChiTietPhieuNhap` | Danh sách riêng của phiếu, không static |

## Phương thức dự kiến

- Constructor nhận người nhập, nguồn nhập, thời điểm và chi tiết; kiểm tra toàn bộ dữ liệu rồi gọi `chiTiet.khoaChinhSua()` khi nhận quyền sở hữu.
- `long tinhTongTien()`: tổng thành tiền từ các dòng, kiểm tra tràn số; không lưu thêm trường tổng trùng lặp.
- Getter để tra cứu và hiển thị.

## Quan hệ và ràng buộc

- Được quản lý bởi [QuanLyPhieuNhap](QuanLyPhieuNhap.md), sở hữu [DanhSachChiTietPhieuNhap](DanhSachChiTietPhieuNhap.md).
- Khi nạp file hoặc phục hồi, ghép đủ chi tiết trong vùng tạm rồi khóa danh sách trước khi công bố phiếu. Cờ khóa được dựng lại trong RAM, không đổi định dạng file text.
- Ánh xạ bảng `PHIEU_NHAP`; chi tiết ghi vào `CT_PHIEU_NHAP`.
- Phiếu đã lưu có ít nhất một dòng, không sửa/xóa, không ghi nhận lại cùng mã. Không cần trạng thái nháp hoặc công nợ trong bản đầu.
- [DichVuCuaHang](DichVuCuaHang.md) xác thực người nhập là quản lý đang hoạt động, tăng tồn và lưu phiếu cùng một lần. Lớp phiếu không tự gọi lưu file.
- Chỉ phiếu đã ghi nhận đóng góp vào chi nhập; chi nhập không đồng nghĩa giá vốn hàng bán.
