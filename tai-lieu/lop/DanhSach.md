# DanhSach — lớp cha generic của các danh sách

[Mục lục lớp](../04-thiet-ke-lop.md)

**Khai báo dự kiến:** `public abstract class DanhSach<T> implements IQuanLy<T>`, file `DanhSach.java`.

Lớp cha giữ danh sách có kiểu và thực thi phần xuất dữ liệu dùng chung. Không khởi tạo trực tiếp; lớp con xác định `T` và triển khai thêm, sửa, xóa theo ràng buộc nghiệp vụ.

## Khung lớp dự kiến

```java
import java.util.ArrayList;
import java.util.List;

public abstract class DanhSach<T> implements IQuanLy<T> {
    protected final List<T> danhSach = new ArrayList<>();

    @Override
    public final List<T> xuat() {
        return List.copyOf(danhSach);
    }

    @Override
    public abstract void them(T doiTuong);

    @Override
    public abstract void sua(String ma, T duLieuMoi);

    @Override
    public abstract void xoa(String ma);
}
```

`danhSach` là thuộc tính instance, chỉ lớp cha và các lớp con thao tác trực tiếp. Mỗi đối tượng danh sách có vùng dữ liệu riêng; lớp con không khai báo lại một trường danh sách khác. Không dùng `static List<T>` cho kiểu `T` của lớp.

## Phần dùng chung và phần chuyên biệt

- `xuat()` được kế thừa nguyên trạng, trả `List.copyOf(danhSach)`. Đây là bản sao nông: bảo vệ cấu trúc, còn phần tử phải giữ đóng gói và chỉ thay đổi qua nghiệp vụ được cho phép.
- `them`, `sua`, `xoa` là phương thức instance abstract. Lớp con bắt buộc override; lớp cha không cung cấp thao tác thay/xóa phần tử vô điều kiện làm mất lịch sử, đổi tồn hoặc điểm tùy ý.
- Với sáu danh sách chung, `ma` là mã thực thể; với chi tiết, `ma` là `maBienThe` duy nhất trong chứng từ đang sở hữu. Không cần thêm interface cho tất cả thực thể chỉ để lấy mã.
- Các phương thức tìm kiếm, tổng hợp, nạp dữ liệu và kiểm tra nghiệp vụ ở lớp con. Không đưa menu, phân quyền hoặc đọc/ghi file vào lớp generic.

## Các lớp kế thừa

| Lớp cụ thể | Kiểu của lớp cha |
| --- | --- |
| [QuanLyTaiKhoan](QuanLyTaiKhoan.md) | `DanhSach<TaiKhoan>` |
| [QuanLySanPham](QuanLySanPham.md) | `DanhSach<SanPham>` |
| [QuanLyBienTheSanPham](QuanLyBienTheSanPham.md) | `DanhSach<BienTheSanPham>` |
| [QuanLyKhuyenMai](QuanLyKhuyenMai.md) | `DanhSach<KhuyenMai>` |
| [QuanLyDonHang](QuanLyDonHang.md) | `DanhSach<DonHang>` |
| [QuanLyPhieuNhap](QuanLyPhieuNhap.md) | `DanhSach<PhieuNhap>` |
| [DanhSachChiTietDonHang](DanhSachChiTietDonHang.md) | `DanhSach<ChiTietDonHang>` |
| [DanhSachChiTietPhieuNhap](DanhSachChiTietPhieuNhap.md) | `DanhSach<ChiTietPhieuNhap>` |

Ví dụ biến có kiểu `IQuanLy<SanPham>` có thể tham chiếu một `QuanLySanPham`; lời gọi `them`, `sua`, `xoa` vẫn chạy đúng override của lớp đó. Trình biên dịch chặn việc truyền `TaiKhoan` vào danh sách sản phẩm, không cần ép kiểu hoặc dùng raw type.
