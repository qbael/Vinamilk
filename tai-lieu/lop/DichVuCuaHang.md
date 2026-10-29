# DichVuCuaHang — điều phối nghiệp vụ

[Mục lục lớp](../04-thiet-ke-lop.md)

**Loại:** class thông thường với phương thức instance; tạo một đối tượng khi chạy chương trình. Là điểm gọi nghiệp vụ từ console, chịu trách nhiệm kiểm tra quyền và phối hợp nhiều danh sách.

## Thuộc tính

| Thuộc tính | Kiểu | Ý nghĩa |
| --- | --- | --- |
| `quanLyTaiKhoan` | `private final QuanLyTaiKhoan` | Danh sách tài khoản dùng chung |
| `quanLySanPham` | `private final QuanLySanPham` | Danh sách sản phẩm dùng chung |
| `quanLyBienThe` | `private final QuanLyBienTheSanPham` | Danh sách SKU dùng chung |
| `quanLyKhuyenMai` | `private final QuanLyKhuyenMai` | Danh sách khuyến mãi dùng chung |
| `quanLyDonHang` | `private final QuanLyDonHang` | Danh sách đơn hàng dùng chung |
| `quanLyPhieuNhap` | `private final QuanLyPhieuNhap` | Danh sách phiếu nhập dùng chung |
| `luuTru` | `private final LuuTruText` | Đối tượng đọc/ghi, truyền qua constructor |

Constructor nhận sáu đối tượng quản lý và `LuuTruText` do [Main](Main.md) tạo. Đây phải là đúng các đối tượng quản lý đã truyền cho lưu trữ; dịch vụ giữ tham chiếu, không tự tạo lại chúng hoặc sao chép danh sách. Mỗi lớp quản lý kế thừa `DanhSach<T>` thực thi `IQuanLy<T>`; gọi phương thức qua đối tượng, ví dụ `quanLySanPham.xuat()` hoặc `quanLySanPham.them(sanPham)`.

## Phương thức dự kiến theo chức năng

| Nhóm | Phương thức chính | Trách nhiệm |
| --- | --- | --- |
| Tài khoản | `dangKyKhachHang(...)`, `dangNhap(...)`, `taoTaiKhoanNoiBo(...)`, `khoaTaiKhoan(...)`, `moKhoaTaiKhoan(...)` | Kiểm tra xác thực, tên trùng, vai trò; bảo vệ quản lý hoạt động cuối |
| Danh mục | `themSanPham(...)`, `capNhatSanPham(...)`, `themBienThe(...)`, `capNhatBienThe(...)` | Kiểm tra danh mục, giá, quy cách; không sửa tồn trực tiếp |
| Đặt/bán | `datHang(...)`, `lapDonTaiQuay(...)`, `xemDon(...)`, `huyDon(...)` | Kiểm tra chủ đơn, hàng, số lượng và trạng thái |
| Thanh toán | `thanhToan(...)` | Tính lại giá/khuyến mãi, kiểm tra điểm, tồn; chốt đơn và lưu đồng thời |
| Khuyến mãi | `themKhuyenMai(...)`, `capNhatKhuyenMai(...)`, `ngungKhuyenMai(...)` | Chỉ quản lý; kiểm tra SKU, ngày và mức giảm |
| Nhập | `nhapHang(...)`, `xemPhieuNhap(...)` | Chỉ quản lý; tăng tồn cùng ghi nhận phiếu |
| Báo cáo | `tongKetThuChi(...)`, `xemTonKho(...)` | Tổng hợp chứng từ đã chốt và tồn hiện tại |

Các chữ ký là định hướng, chưa phải API hoàn chỉnh. Thao tác cần quyền phải nhận mã người gọi do phiên console cung cấp, tra lại tài khoản đang hoạt động và kiểm tra quyền tại dịch vụ; không tin vai trò do người dùng nhập. Các thao tác chỉ đọc cũng kiểm tra quyền sở hữu khi cần.

`xuat()` của lớp quản lý trả danh sách để dịch vụ lọc theo quyền và giao diện định dạng. Không in trực tiếp đối tượng tài khoản hoặc trả hash/salt ra màn hình. Bản sao cấu trúc từ `List.copyOf` không cho phép bỏ qua đóng gói của từng phần tử.

## Ràng buộc

- Không để lớp quản lý danh sách hoặc menu tự ghép một giao dịch từ nhiều lần lưu rời.
- Thanh toán kiểm tra đơn chờ, lấy giá/khuyến mãi hiện tại, kiểm tra đồng ý của khách rồi chốt đơn + tồn + điểm cùng một lần lưu.
- Lưu thất bại phải phục hồi RAM; sau commit không rollback chỉ vì lỗi hiển thị. Quy trình cụ thể ở [lưu trữ](../07-luu-tru-file-text.md).
- Phục hồi giữ nguyên sáu đối tượng quản lý và tham chiếu danh sách của chúng; phần tử có thể được dựng lại nên dịch vụ tra lại theo mã. Danh sách chi tiết vẫn thuộc từng đơn/phiếu.
- Quản lý có quyền bán hàng qua điều kiện quyền, không cần kế thừa nhân viên.
- Không có bảng dữ liệu cho dịch vụ. Công thức tiền và các tỷ lệ nằm ở [quy tắc nghiệp vụ](../06-quy-tac-nghiep-vu.md).
