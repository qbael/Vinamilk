# GiaoDienConsole — menu nhập xuất

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường với phương thức instance. Hiển thị menu và chứng từ, nhận dữ liệu rồi gọi dịch vụ.

## Thuộc tính

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `dichVu` | `private final DichVuCuaHang` | Dịch vụ được truyền qua constructor |
| `boDoc` | `private final Scanner` | Một bộ đọc console dùng xuyên suốt phiên |
| `maTaiKhoanDangNhap` | `private String` | null khi chưa đăng nhập; tra lại tài khoản theo mã khi cần |

Giữ mã phiên thay vì giữ tham chiếu tài khoản lâu dài giúp tránh dùng đối tượng cũ sau khi phục hồi RAM từ text.

## Phương thức dự kiến

- `chay()`: vòng lặp menu đầu, đăng nhập/đăng ký/thoát.
- `hienThiMenuKhachHang()`, `hienThiMenuNhanVien()`, `hienThiMenuQuanLy()`: lựa chọn theo vai trò.
- Các phương thức nhập số nguyên, tiền, ngày; báo rõ lỗi và cho nhập lại/quay lại trước khi xác nhận.
- Hiển thị danh mục, báo giá, đơn, hóa đơn, phiếu nhập và báo cáo; phân biệt giá tạm tính với giá đã chốt.
- `dangXuat()`: xóa mã phiên và quay lại menu đầu.

## Ràng buộc

- Dùng một cơ chế đọc dòng rồi parse để tránh lỗi trộn `nextInt()` và `nextLine()`. Không đóng bộ đọc giữa các menu.
- Không gọi setter tồn/điểm, không tính lại hóa đơn cũ bằng giá hiện tại, không tự ghi file.
- Nhận dữ liệu xuất qua dịch vụ đã kiểm tra quyền rồi định dạng/in; `IQuanLy<T>.xuat()` chỉ trả danh sách, không tự hiển thị. Không in hash/salt hoặc dữ liệu tài khoản ngoài phạm vi được phép.
- Menu đúng vai trò không thay thế kiểm tra quyền của [DichVuCuaHang](DichVuCuaHang.md).
- Khách chọn số điểm; nhân viên xác nhận khách đồng ý và thu tiền. Giao diện hiển thị mức tối đa và số tiền cuối trước khi lưu.
- Không có bảng dữ liệu cho UI. Mẫu menu chi tiết ở [tài khoản và phân quyền](../02-tai-khoan-va-phan-quyen.md).
