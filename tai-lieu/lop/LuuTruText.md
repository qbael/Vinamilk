# LuuTruText — đọc và ghi dữ liệu text

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường với phương thức instance. Chuyển dữ liệu giữa đối tượng Java và tám file text UTF-8, mỗi file một bảng logic.

## Thuộc tính

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `duongDan` | `private final Path` | Đường dẫn thư mục dữ liệu `data/`, truyền qua constructor |
| `quanLyTaiKhoan` | `private final QuanLyTaiKhoan` | Danh sách tài khoản dùng chung |
| `quanLySanPham` | `private final QuanLySanPham` | Danh sách sản phẩm dùng chung |
| `quanLyBienThe` | `private final QuanLyBienTheSanPham` | Danh sách SKU dùng chung |
| `quanLyKhuyenMai` | `private final QuanLyKhuyenMai` | Danh sách khuyến mãi dùng chung |
| `quanLyDonHang` | `private final QuanLyDonHang` | Danh sách đơn hàng dùng chung |
| `quanLyPhieuNhap` | `private final QuanLyPhieuNhap` | Danh sách phiếu nhập dùng chung |

Constructor nhận `Path` cùng sáu đối tượng quản lý do [Main](Main.md) tạo; [DichVuCuaHang](DichVuCuaHang.md) nhận đúng sáu tham chiếu này. Các file dữ liệu, file tạm và nhật ký phục hồi được xác định trong thư mục đó; chưa cần lớp cấu hình. Không giữ một bộ danh sách lâu dài khác hoặc tự tạo các đối tượng quản lý mới.

## Phương thức dự kiến

| Phương thức | Trách nhiệm |
| --- | --- |
| `void docDuLieu()` | Phục hồi lần ghi bị gián đoạn nếu có, đọc đủ tám file, kiểm tra rồi mới công bố dữ liệu |
| `String xuatThanhChuoi()` | Đọc `xuat()` trên sáu đối tượng quản lý cùng chi tiết chứng từ, tạo bản text gộp trong RAM để kiểm tra/phục hồi giao dịch |
| `void napTuChuoi(String noiDung)` | Parse vào danh sách tạm; kiểm tra toàn bộ cấu trúc, ID, tham chiếu và bất biến trước khi nạp vào các đối tượng quản lý đang dùng |
| `void ghiAnToan()` | Chuẩn bị tám file tạm, lưu nhật ký trạng thái cũ rồi thay các file; phục hồi cả bộ nếu lỗi, báo dịch vụ khi không lưu được |

Các thao tác file truyền lỗi `IOException` lên nơi điều phối; lỗi dữ liệu báo rõ file/dòng/mã liên quan. Không nuốt lỗi hoặc tự biến file lỗi thành dữ liệu rỗng.

## Quan hệ và ràng buộc

- [Main](Main.md) tạo đối tượng và yêu cầu nạp trước khi mở menu; [DichVuCuaHang](DichVuCuaHang.md) gọi ghi sau mỗi thay đổi nghiệp vụ.
- Chỉ định dạng lưu trữ biết cách ánh xạ `vaiTro` sang lớp tài khoản và `loaiKhuyenMai` sang lớp khuyến mãi. Không dùng Java object serialization hoặc database.
- Các danh sách tạm chỉ là vùng đệm khi nạp, không có lớp quản lý nghiệp vụ riêng. Sau khi kiểm tra toàn bộ đồ thị dữ liệu, gọi `napDanhSach(List<T> duLieuDaKiemTra)` nội bộ ở cả sáu đối tượng quản lý. Phương thức để `public` vì các lớp quản lý nằm ở package `cuahang.danhsach`, dùng `clear`/`addAll` trên `protected final List<T> danhSach` kế thừa từ `DanhSach<T>`; giữ nguyên danh tính đối tượng quản lý và tham chiếu danh sách. Menu không gọi thao tác này.
- Nạp file và phục hồi đều giữ tồn, điểm và trạng thái lịch sử đã đối chiếu hợp lệ; không gọi `them(...)` dành cho tạo mới vì tài khoản/SKU mới phải bắt đầu với điểm/tồn bằng 0. Danh sách chi tiết được ghép lại và thuộc đúng từng đơn/phiếu.
- File lưu tồn thực tế. Khi nạp hoặc phục hồi, dựng lại lượng giữ từ đơn chờ để tồn khả dụng giảm đúng một lần; từ chối nếu lượng cần giữ vượt tồn thực tế.
- Thực thể được giữ nguyên ID khi nạp; nối lại chi tiết đúng đơn/phiếu. Không xóa lịch sử vì tài khoản hiện đã khóa hoặc SKU hiện ngừng bán.
- Dữ liệu chứa hash/salt và thông tin cá nhân; không in toàn bộ nội dung file hoặc bản phục hồi ra log.

Định dạng cột nằm ở [mô hình dữ liệu](../05-mo-hinh-du-lieu.md); quy trình đọc/ghi nằm ở [lưu trữ file text](../07-luu-tru-file-text.md).
