# KhachHang

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public class KhachHang extends TaiKhoan`, file `KhachHang.java`.

Đại diện thành viên mua hàng, sở hữu điểm tích lũy. Kế thừa toàn bộ thông tin xác thực, liên hệ và trạng thái từ [TaiKhoan](TaiKhoan.md).

## Thuộc tính riêng

| Thuộc tính | Kiểu Java | Ý nghĩa |
| --- | --- | --- |
| `diemTichLuy` | `int` | Số điểm hiện có, không âm; tài khoản mới bắt đầu bằng 0 |

Không giữ một bản sao danh sách đơn hàng trong khách hàng; tra đơn theo mã khách hàng qua [QuanLyDonHang](QuanLyDonHang.md).

## Phương thức quan trọng

| Chữ ký dự kiến | Trách nhiệm |
| --- | --- |
| `public VaiTro layVaiTro()` | Override, trả `VaiTro.KHACH_HANG` |
| `void congDiem(int soDiem)` | Cộng điểm hợp lệ, kiểm tra tràn `int` |
| `void truDiem(int soDiem)` | Trừ điểm hợp lệ, không vượt số điểm đang có |

Hai phương thức điểm không nhận số âm và không cho phép gán số dư tùy ý. Điểm bằng 0 không làm thay đổi số dư. Chỉ luồng thanh toán của [DichVuCuaHang](DichVuCuaHang.md) yêu cầu cộng/trừ điểm, cùng giao dịch cập nhật đơn và tồn.

## Quan hệ, lưu trữ và ràng buộc

- Là một nhánh trực tiếp của `TaiKhoan`, ngang hàng với [NhanVienBanHang](NhanVienBanHang.md) và [QuanLyCuaHang](QuanLyCuaHang.md).
- Lưu ở `TAI_KHOAN` với `vaiTro = KHACH_HANG`; cột `diemTichLuy` bắt buộc có giá trị. [DonHang](DonHang.md) liên kết bằng `maKhachHang`.
- Điểm chỉ được cập nhật sau thanh toán lưu thành công; đơn chờ/hủy không đổi điểm. Không tặng điểm đăng ký, chuyển điểm hoặc sửa thủ công.
- Công thức đổi/tích điểm và yêu cầu khách đồng ý nằm trong [Quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md); lớp này chỉ bảo vệ số dư, không tự tính ưu đãi.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
