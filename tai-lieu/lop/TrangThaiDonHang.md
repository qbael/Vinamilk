# TrangThaiDonHang

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public enum TrangThaiDonHang`, file `TrangThaiDonHang.java`.

| Giá trị | Ý nghĩa | Tác động nghiệp vụ |
| --- | --- | --- |
| `CHO_THANH_TOAN` | Đơn đã lưu, chờ trả tiền tại cửa hàng | Giá tạm tính; đã giữ hàng khỏi tồn khả dụng, chưa đổi điểm hay ghi nhận thu |
| `DA_THANH_TOAN` | Đã chốt thanh toán và lưu thành công | Chốt giá/ưu đãi, xuất hàng đã giữ, cập nhật điểm và ghi nhận thu; không trừ khả dụng lần nữa |
| `DA_HUY` | Đơn chờ đã hủy và lưu thành công | Giữ lịch sử; hoàn lượng giữ về tồn khả dụng, điểm và thu không đổi |

Dùng cho `DonHang.trangThai`; lưu tên enum trong cột `trangThai` của `DON_HANG`. Enum không có bảng riêng và không nhận `null`/giá trị lạ.

Chỉ cho chuyển `CHO_THANH_TOAN → DA_THANH_TOAN` hoặc `CHO_THANH_TOAN → DA_HUY`. [DonHang](DonHang.md) bảo vệ chuyển trạng thái; [DichVuCuaHang](DichVuCuaHang.md) kiểm tra quyền và điều kiện, điều phối lưu nhất quán. Không chuyển ngược, không thanh toán lại hoặc hủy đơn đã thanh toán. In lại hóa đơn chỉ đọc dữ liệu.

Xem [Mô hình dữ liệu](../05-mo-hinh-du-lieu.md), [Quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md) và [Mục lục lớp](../04-thiet-ke-lop.md).
