# Tài liệu đồ án quản lý cửa hàng sữa

Bản thiết kế cho ứng dụng Java 26 chạy bằng console, lưu dữ liệu trong file text, không dùng database. Đây là tài liệu để triển khai; chưa phải ứng dụng đã hoàn thiện.

## Các tài liệu

| File | Nội dung |
| --- | --- |
| [01 — Mô tả ứng dụng](01-mo-ta-ung-dung.md) | Mục tiêu, phạm vi, sản phẩm và quy cách bán |
| [02 — Tài khoản và phân quyền](02-tai-khoan-va-phan-quyen.md) | Ba vai trò, quyền truy cập, menu |
| [03 — Danh sách chức năng](03-danh-sach-chuc-nang.md) | Chức năng và các luồng thao tác chính |
| [04 — Tổng quan thiết kế lớp](04-thiet-ke-lop.md) | Kiến trúc, minh chứng OOP, mục lục 32 tài liệu riêng cho từng class/interface/enum |
| [05 — Mô hình dữ liệu](05-mo-hinh-du-lieu.md) | Bảng logic, khóa chính/ngoại, từ điển cột và sơ đồ quan hệ |
| [06 — Quy tắc nghiệp vụ](06-quy-tac-nghiep-vu.md) | Vòng đời đơn, công thức tiền/điểm, tồn kho và thu-chi |
| [07 — Lưu trữ file text](07-luu-tru-file-text.md) | Định dạng UTF-8, đọc/ghi, khôi phục khi lưu lỗi và bộ dữ liệu mẫu |
| [08 — Kế hoạch kiểm thử](08-ke-hoach-kiem-thu.md) | 33 tình huống kiểm chứng và thứ tự triển khai |

Thư mục `lop/` có **32 file riêng**, ví dụ [TaiKhoan](lop/TaiKhoan.md), [BienTheSanPham](lop/BienTheSanPham.md), [IQuanLy](lop/IQuanLy.md), [DanhSach](lop/DanhSach.md), [QuanLySanPham](lop/QuanLySanPham.md). Mỗi file mô tả một kiểu Java; xem toàn bộ tại [mục lục lớp](04-thiet-ke-lop.md). Tổng cộng có 41 file Markdown: mục lục này, 8 tài liệu theo chủ đề và 32 tài liệu lớp.

## Những lựa chọn chính

- Ba loại tài khoản: khách hàng, nhân viên bán hàng, quản lý cửa hàng.
- Năm nhóm hàng: sữa, sữa chua, kem, nước giải khát, phô mai; quy cách bán lẻ, lốc, thùng nằm ở biến thể sản phẩm.
- Mỗi biến thể có mã bán hàng, giá và tồn kho riêng. Không tự chuyển tồn giữa hộp, lốc và thùng.
- Đặt hàng là yêu cầu mua và nhận tại cửa hàng. Chỉ thanh toán thành công mới ghi nhận doanh thu, trừ hàng và cập nhật điểm.
- Dùng kế thừa cho tài khoản và khuyến mãi; dùng composition cho đơn hàng và các dòng chi tiết.
- `IQuanLy<T>` khai báo xuất, thêm, sửa, xóa; lớp cha generic `DanhSach<T>` implements interface; tám lớp danh sách kế thừa với kiểu phần tử tương ứng.
- Sáu đối tượng `QuanLy...` được tạo một lần trong `Main`, dùng chung qua constructor của dịch vụ/lưu trữ; chi tiết vẫn thuộc riêng từng chứng từ.
- Lưu các bảng logic trong một file `data/du-lieu.txt`, để một lần ghi chứa đầy đủ thay đổi của đơn hàng, tồn kho và điểm.

Bộ dữ liệu mẫu tại [`data/du-lieu.txt`](../data/du-lieu.txt) có **60 bản ghi**, vượt yêu cầu tối thiểu 50 bản ghi, gồm đủ tám bảng logic. Thành phần và tài khoản thực hành được mô tả trong [mục dữ liệu mẫu](07-luu-tru-file-text.md#4-bộ-dữ-liệu-mẫu-tối-thiểu-50-bản-ghi).

## Cấu trúc quản lý danh sách

Ví dụ `QuanLySanPham extends DanhSach<SanPham>` thực hiện `IQuanLy<SanPham>` thông qua lớp cha. Danh sách và các thao tác là instance để tham gia kế thừa/đa hình. `DanhSach<T>` giữ `List<T>` và thực thi `xuat()`; lớp con override thêm, sửa, xóa theo nghiệp vụ, bảo toàn lịch sử và vòng đời chứng từ. Xem [thiết kế chung](04-thiet-ke-lop.md#4-interface-và-lớp-cha-generic-cho-danh-sách).

## Phạm vi của đợt tài liệu này

Chưa viết các lớp Java, chưa thay đổi `src/Main.java`. Mỗi lớp/interface/enum trong thiết kế dự kiến được đặt trong một file `.java` riêng. Dùng Markdown để có thể đọc, sửa và theo dõi thay đổi ngay trong dự án.

Môi trường terminal được kiểm tra ngày 22/10/2026 đang dùng Java 17; khi triển khai cần cấu hình JDK 26 cho cả IDE và lệnh chạy. Tài liệu không yêu cầu tính năng preview hoặc thư viện ngoài JDK.
