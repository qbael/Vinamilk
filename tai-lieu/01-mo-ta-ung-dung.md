# 01. Mô tả ứng dụng

## Mục tiêu

Xây dựng ứng dụng quản lý một cửa hàng bán các sản phẩm sữa và thực phẩm liên quan, lấy cảm hứng từ cửa hàng Vinamilk. Khách hàng xem hàng và đặt mua; nhân viên lập đơn, tính tiền tại quầy; quản lý nhập hàng, điều chỉnh danh mục, khuyến mãi và xem thu-chi.

Ứng dụng phục vụ đồ án lập trình hướng đối tượng: thiết kế rõ trách nhiệm, dễ trình bày quan hệ giữa các lớp và có thể triển khai từng phần. Các mức giá và quy tắc khuyến mãi trong tài liệu là dữ liệu minh họa của đồ án, không phải chính sách thực tế của Vinamilk.

## Môi trường và phạm vi

| Hạng mục | Lựa chọn |
| --- | --- |
| Ngôn ngữ | Java 26, không dùng preview |
| Giao diện | Menu console nhập số, nhập mã hàng, xác nhận thao tác |
| Lưu trữ | File text UTF-8; dữ liệu được đọc lại khi mở chương trình |
| Database | Không dùng SQL, JDBC, ORM hoặc hệ quản trị cơ sở dữ liệu |
| Bộ nhớ | `ArrayList<T>` instance trong lớp trừu tượng generic `DanhSach<T>` thực thi `IQuanLy<T>`; các lớp quản lý kế thừa theo kiểu phần tử |
| Hoạt động | Một cửa hàng, một tiến trình JVM, một người đăng nhập tại một thời điểm |
| Thanh toán | Ghi nhận tiền mặt tại cửa hàng; không tích hợp ngân hàng |
| Đặt hàng | Khách đặt trên console, tới cửa hàng nhận và thanh toán |

Đồ án vận hành tuần tự: đăng xuất tài khoản này rồi đăng nhập tài khoản khác để mô phỏng các vai trò. `Main` tạo một bộ sáu đối tượng quản lý chung rồi truyền đúng các tham chiếu đó cho dịch vụ và lưu trữ; danh sách chi tiết thuộc riêng từng chứng từ. Các phương thức xuất, thêm, sửa, xóa tuân theo `IQuanLy<T>` và ràng buộc nghiệp vụ của từng lớp. File text giữ dữ liệu qua các lần chạy.

## Sản phẩm và hình thức đóng gói

`SanPham` mô tả mặt hàng gốc. `BienTheSanPham` là một lựa chọn bán cụ thể, gọi là SKU, gồm mã riêng, quy cách, giá bán và tồn kho. Không tạo ba bản sao sản phẩm chỉ để bán lẻ, lốc và thùng.

| Nhóm (`LoaiSanPham`) | Ví dụ sản phẩm gốc | Đơn vị cơ sở | Quy cách có thể bán |
| --- | --- | --- | --- |
| `SUA` | Sữa tươi có đường 180 ml | Hộp | Một hộp, lốc 4 hộp, thùng 48 hộp |
| `SUA_CHUA` | Sữa chua có đường 100 g | Hũ | Một hũ, lốc 4 hũ, thùng 48 hũ |
| `KEM` | Kem sữa dừa 60 g | Que | Một que, thùng 30 que |
| `NUOC_GIAI_KHAT` | Nước trái cây 250 ml | Chai | Một chai, lốc 6 chai, thùng 24 chai |
| `PHO_MAI` | Phô mai hộp 120 g | Hộp | Một hộp, thùng 24 hộp |

Không bắt buộc sản phẩm nào cũng có đủ ba cách đóng gói. “Đơn vị cơ sở” là một đơn vị sản phẩm được chọn cho danh mục: nếu phô mai bán theo hộp thì số miếng trong hộp chỉ là mô tả, không tự phát sinh SKU bán từng miếng.

Ví dụ một sản phẩm `SP001` có ba biến thể:

| Mã biến thể | Kiểu | Số đơn vị cơ sở | Giá bán/SKU | Tồn kho/SKU |
| --- | --- | ---: | ---: | ---: |
| `BT001` | `BAN_LE` | 1 | 8.000đ | 100 hộp |
| `BT002` | `LOC` | 4 | 30.000đ | 20 lốc |
| `BT003` | `THUNG` | 48 | 340.000đ | 5 thùng |

Bán 2 lốc chỉ trừ 2 ở `BT002`. Không trừ 8 hộp ở `BT001`, không trừ thùng ở `BT003`. Giá lốc/thùng do quản lý đặt, không bắt buộc bằng giá lẻ nhân số đơn vị. Số đơn vị cơ sở dùng để mô tả, không phải lệnh quy đổi tồn kho.

## Ba người dùng chính

- **Khách hàng:** xem và tìm sản phẩm; đặt hàng; xem/hủy đơn đang chờ của mình; xem điểm, đề nghị sử dụng điểm khi mua hàng.
- **Nhân viên bán hàng:** xem danh mục và tồn; lập đơn cho thành viên hoặc khách vãng lai; xử lý đơn đã đặt; xác nhận thu tiền và in hóa đơn ra console.
- **Quản lý cửa hàng:** có chức năng bán hàng, quản lý danh mục/quy cách/giá/khuyến mãi, tạo tài khoản nội bộ, nhập hàng và xem báo cáo thu-chi.

Khách vãng lai không phải loại tài khoản thứ tư. Đó là đơn bán tại quầy không liên kết với `KhachHang`, nên không được tích hoặc đổi điểm.

## Các đầu ra chính

1. Danh sách sản phẩm và biến thể đang bán, có giá niêm yết, khuyến mãi hiện hành và tồn kho.
2. Đơn đặt hàng chờ thanh toán và hóa đơn đã thanh toán, có các dòng hàng và tổng tiền.
3. Số dư điểm cùng lịch sử cộng/trừ điểm suy ra từ các đơn đã thanh toán của khách.
4. Phiếu nhập hàng có số lượng và giá nhập từng biến thể.
5. Báo cáo doanh thu trước giảm giá, giảm khuyến mãi, giảm bằng điểm, tiền thực thu, tiền nhập hàng và chênh lệch thu-chi trong kỳ.

## Giới hạn có chủ đích

Phiên bản đầu chưa có vận chuyển, thanh toán trực tuyến, trả hàng/hoàn tiền, công nợ, nhiều chi nhánh, tách thùng thành lốc/hộp, nhà cung cấp riêng, quản lý lô và hạn sử dụng. Thông tin người bán hàng đầu vào được ghi bằng tên trên phiếu nhập; chưa cần lớp `NhaCungCap`.

Vì chưa có lô/hạn sử dụng, mô hình chỉ phù hợp minh họa đồ án, chưa đủ vận hành cửa hàng thực phẩm thực tế. Báo cáo chỉ là thu-chi cơ bản; chưa tính giá vốn, lương, thuê mặt bằng hoặc thuế nên không công bố lợi nhuận.

Tiếp theo: [Tài khoản và phân quyền](02-tai-khoan-va-phan-quyen.md), [Danh sách chức năng](03-danh-sach-chuc-nang.md).
