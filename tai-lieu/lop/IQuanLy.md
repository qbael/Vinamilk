# IQuanLy — interface quản lý danh sách

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public interface IQuanLy<T>`, file `IQuanLy.java`. `T` là kiểu phần tử được quản lý, ví dụ `SanPham` hoặc `ChiTietDonHang`.

## Hợp đồng chung

```java
import java.util.List;

public interface IQuanLy<T> {
    List<T> xuat();
    void them(T doiTuong);
    void sua(String ma, T duLieuMoi);
    void xoa(String ma);
}
```

| Phương thức | Ý nghĩa |
| --- | --- |
| `xuat()` | Xuất danh sách dưới dạng bản sao chỉ đọc cấu trúc để nơi gọi duyệt; không tự in console hoặc ghi file |
| `them(T doiTuong)` | Thêm phần tử hợp lệ; danh sách chi tiết được gộp lượng của cùng SKU theo quy tắc riêng |
| `sua(String ma, T duLieuMoi)` | Sửa các trường được phép của phần tử theo mã; dữ liệu mới phải giữ cùng mã |
| `xoa(String ma)` | Xóa phần tử khi nghiệp vụ cho phép; lớp giữ lịch sử phải từ chối xóa vật lý |

Đây là bốn phương thức bắt buộc trong hợp đồng, không phải các phương thức Java `default` có sẵn thân hàm. Phần thực thi dùng chung nằm tại [DanhSach](DanhSach.md); các thao tác thay đổi dữ liệu được lớp quản lý cụ thể override theo nghiệp vụ.

## Ràng buộc

- Với thao tác được hỗ trợ và đang được phép thực hiện, `them` và `sua` từ chối đối tượng `null`; mã dùng để sửa/xóa phải khác null, không rỗng. Không tìm thấy mã hoặc dữ liệu sai thì báo `IllegalArgumentException`, không âm thầm bỏ qua.
- Kiểm tra toàn bộ dữ liệu trước khi thay đổi, không để danh sách bị cập nhật một phần khi có lỗi.
- Thao tác bị cấm ở trạng thái hiện tại báo `IllegalStateException`. Thao tác không hỗ trợ cho cả loại danh sách báo `UnsupportedOperationException` kèm lý do, như sửa/xóa chứng từ đã ghi nhận. Không dùng thân hàm rỗng để giả vờ thành công.
- Interface không cấp quyền cho người gọi. Giao diện luôn đi qua [DichVuCuaHang](DichVuCuaHang.md); chỉ hiển thị các trường được phép xem, không in hash/salt hoặc toàn bộ dữ liệu tài khoản.

## Quan hệ

[DanhSach](DanhSach.md) implements interface này. Sáu lớp `QuanLy...` và hai lớp `DanhSachChiTiet...` thực hiện hợp đồng thông qua lớp cha generic, không cần lặp lại `implements IQuanLy<...>` ở từng lớp con.
