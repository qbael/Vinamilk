# Đồ án quản lý cửa hàng sữa — Java 24 Console

Bộ tài liệu thiết kế cho ứng dụng quản lý một cửa hàng bán kem, sữa, sữa chua, nước giải khát và phô mai. Có ba loại tài khoản: khách hàng, nhân viên bán hàng, quản lý cửa hàng. Dữ liệu lưu bằng file, không dùng cơ sở dữ liệu.

## Đọc tài liệu

| File | Nội dung |
| --- | --- |
| [01 — Mô tả ứng dụng](docs/01-mo-ta-ung-dung.md) | Mục tiêu, phạm vi, hàng hóa, quyết định thiết kế |
| [02 — Tài khoản và phân quyền](docs/02-tai-khoan-va-phan-quyen.md) | Ba loại tài khoản, bảng quyền, quy tắc truy cập |
| [03 — Danh sách chức năng](docs/03-danh-sach-chuc-nang.md) | Chức năng có mã, điều kiện thực hiện, menu console |
| [04 — Thiết kế các lớp](docs/04-thiet-ke-cac-lop.md) | Từng lớp/file Java, thuộc tính, phương thức, sơ đồ UML, bốn tính chất OOP và thống kê `static` |
| [05 — Mô hình dữ liệu và lưu trữ](docs/05-mo-hinh-du-lieu-va-luu-tru.md) | Bảng logic, khóa, quan hệ ER, từ điển dữ liệu và lưu file |
| [06 — Quy tắc nghiệp vụ và kiểm thử](docs/06-quy-tac-nghiep-vu-va-kiem-thu.md) | Tính tiền, khuyến mãi, điểm, tồn kho, tài chính, kịch bản nghiệm thu |

Đây là tài liệu để bắt đầu lập trình, chưa phải ứng dụng đã triển khai. `src/Main.java` hiện là mã mẫu ban đầu; chưa chuyển thành chương trình theo thiết kế. Các tên file Java trong tài liệu là cấu trúc dự kiến, mỗi lớp và enum nằm trong file riêng.

## Lưu ý về yêu cầu “class static”

Java 24 không cho phép một lớp độc lập được khai báo `static`. Phương án đề xuất là lớp tiện ích `QuyTacDiemTichLuy` độc lập, có constructor `private` và phương thức `static`; **đây không phải lớp có modifier `static`**. Nếu tiêu chí chấm bắt buộc modifier này trên khai báo lớp thì tiêu chí đó mâu thuẫn với yêu cầu không dùng lớp lồng nhau. Xem giải thích và nguồn Java chính thức trong [tài liệu thiết kế lớp](docs/04-thiet-ke-cac-lop.md).

## Thống kê thành viên `static`

Trong **thiết kế dự kiến**, có **2 lớp chủ động khai báo thành viên `static`**:

| Lớp | Thành viên `static` dự kiến | Mục đích |
| --- | --- | --- |
| `Main` | `main(String[] args)` | Điểm vào chương trình |
| `QuyTacDiemTichLuy` | Hai hằng `TIEN_DE_NHAN_MOT_DIEM`, `GIA_TRI_MOT_DIEM`; ba phương thức `tinhDiemNhan`, `tinhDiemToiDa`, `tinhTienGiam` | Tính toán thuần, không giữ trạng thái tài khoản hoặc kho |

Tổng cộng **4 phương thức và 2 hằng được khai báo tường minh**. Năm enum có các hằng enum và phương thức `values()`/`valueOf(String)` tĩnh do Java cung cấp; thống kê riêng, không tính vào hai lớp trên. Không có lớp khai báo `static class`.

Trong **mã nguồn hiện có**, `src/Main.java` vẫn là mẫu `void main()` không khai báo `static`. Mẫu này có gọi API tĩnh của thư viện (`String.format`, `IO.println`); gọi API tĩnh không đồng nghĩa lớp gọi khai báo thành viên tĩnh. `QuyTacDiemTichLuy.java` chưa được triển khai.

## Các điểm đã chốt sau review

- Đối soát lượng tồn và giá trị tồn với toàn bộ phiếu nhập/đơn đã thanh toán khi nạp và trước lưu.
- Chi tiết giao dịch bất biến; service trả dữ liệu hiển thị theo quyền, không trả entity chứa giá vốn cho khách/nhân viên.
- Thanh toán đối chiếu lại toàn bộ báo giá đã xác nhận, kể cả ngày tính giá; đổi nội dung thì phải xác nhận lại.
- Đơn hủy lưu `nguoiHuyId`; lịch sử điểm chỉ lưu cộng/trừ, không lưu số dư sau từng giao dịch.
- Giảm tiền cố định áp dụng cho từng đơn vị SKU; các ví dụ và ca nghiệm thu đã cập nhật theo quy tắc này.
