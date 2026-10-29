# LuuTruText — đọc và ghi dữ liệu text

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường với phương thức instance. Chuyển dữ liệu giữa đối tượng Java và một file text UTF-8 có nhiều bảng logic.

## Thuộc tính

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `duongDan` | `private final Path` | Đường dẫn file chính `data/du-lieu.txt`, truyền qua constructor |
| `quanLyTaiKhoan` | `private final QuanLyTaiKhoan` | Danh sách tài khoản dùng chung |
| `quanLySanPham` | `private final QuanLySanPham` | Danh sách sản phẩm dùng chung |
| `quanLyBienThe` | `private final QuanLyBienTheSanPham` | Danh sách SKU dùng chung |
| `quanLyKhuyenMai` | `private final QuanLyKhuyenMai` | Danh sách khuyến mãi dùng chung |
| `quanLyDonHang` | `private final QuanLyDonHang` | Danh sách đơn hàng dùng chung |
| `quanLyPhieuNhap` | `private final QuanLyPhieuNhap` | Danh sách phiếu nhập dùng chung |

Constructor nhận `Path` cùng sáu đối tượng quản lý do [Main](Main.md) tạo; [DichVuCuaHang](DichVuCuaHang.md) nhận đúng sáu tham chiếu này. Đường dẫn file tạm được suy ra cùng thư mục; chưa cần lớp cấu hình. Không giữ một bộ danh sách lâu dài khác hoặc tự tạo các đối tượng quản lý mới.

## Phương thức dự kiến

| Phương thức | Trách nhiệm |
| --- | --- |
| `void docDuLieu()` | Đọc file, gọi nạp/kiểm tra, công bố dữ liệu chỉ khi hợp lệ |
| `String xuatThanhChuoi()` | Đọc `xuat()` trên sáu đối tượng quản lý, kèm chi tiết từng chứng từ, rồi định dạng trạng thái để ghi và phục hồi RAM |
| `void napTuChuoi(String noiDung)` | Parse vào danh sách tạm; kiểm tra toàn bộ cấu trúc, ID, tham chiếu và bất biến trước khi nạp vào các đối tượng quản lý đang dùng |
| `void ghiAnToan()` | Ghi file tạm, đóng thành công và thay thế nguyên tử file chính; báo lỗi cho dịch vụ nếu không lưu được |

Các thao tác file truyền lỗi `IOException` lên nơi điều phối; lỗi dữ liệu báo rõ dòng/mã liên quan. Không nuốt lỗi hoặc tự biến file lỗi thành dữ liệu rỗng.

## Quan hệ và ràng buộc

- [Main](Main.md) tạo đối tượng và yêu cầu nạp trước khi mở menu; [DichVuCuaHang](DichVuCuaHang.md) gọi ghi sau mỗi thay đổi nghiệp vụ.
- Chỉ định dạng lưu trữ biết cách ánh xạ `vaiTro` sang lớp tài khoản và `loaiKhuyenMai` sang lớp khuyến mãi. Không dùng Java object serialization hoặc database.
- Các danh sách tạm chỉ là vùng đệm khi nạp, không có lớp quản lý nghiệp vụ riêng. Sau khi kiểm tra toàn bộ đồ thị dữ liệu, gọi `napDanhSach(List<T> duLieuDaKiemTra)` nội bộ ở cả sáu đối tượng quản lý. Phương thức có phạm vi package-private, dùng `clear`/`addAll` trên `protected final List<T> danhSach` kế thừa từ `DanhSach<T>`; giữ nguyên danh tính đối tượng quản lý và tham chiếu danh sách. Menu không gọi thao tác này.
- Nạp file và phục hồi đều giữ tồn, điểm và trạng thái lịch sử đã đối chiếu hợp lệ; không gọi `them(...)` dành cho tạo mới vì tài khoản/SKU mới phải bắt đầu với điểm/tồn bằng 0. Danh sách chi tiết được ghép lại và thuộc đúng từng đơn/phiếu.
- Thực thể được giữ nguyên ID khi nạp; nối lại chi tiết đúng đơn/phiếu. Không xóa lịch sử vì tài khoản hiện đã khóa hoặc SKU hiện ngừng bán.
- Dữ liệu chứa hash/salt và thông tin cá nhân; không in toàn bộ nội dung file hoặc bản phục hồi ra log.

Định dạng cột nằm ở [mô hình dữ liệu](../05-mo-hinh-du-lieu.md); quy trình đọc/ghi nằm ở [lưu trữ file text](../07-luu-tru-file-text.md).
