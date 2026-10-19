# 01. Mô tả ứng dụng

## 1. Mục tiêu

Xây dựng ứng dụng **quản lý cửa hàng bán sữa** bằng Java 24, thao tác qua menu console. Bối cảnh tham khảo là cửa hàng có danh mục hàng hóa tương tự Vinamilk; tên, giá và chính sách minh họa trong đồ án là giả lập, không phải chính sách chính thức của Vinamilk.

Ứng dụng giúp khách hàng xem hàng, đặt mua và sử dụng điểm; giúp nhân viên tính tiền tại quầy; giúp quản lý nhập hàng, thiết lập khuyến mãi và xem tình hình kinh doanh. Thiết kế phải thể hiện đóng gói, kế thừa, đa hình và trừu tượng bằng hành vi nghiệp vụ cụ thể.

## 2. Phạm vi phiên bản đầu

| Hạng mục | Phạm vi đã chọn |
| --- | --- |
| Công nghệ | Java 24, thư viện chuẩn, không cần framework hoặc tính năng preview |
| Giao diện | Menu console tiếng Việt, nhập số để chọn, nhập `0` để quay lại |
| Quy mô | Một cửa hàng, một chương trình chạy tại một thời điểm |
| Tài khoản | Khách hàng, nhân viên bán hàng, quản lý cửa hàng |
| Bán hàng | Khách đặt trước để đến nhận; nhân viên bán trực tiếp tại quầy |
| Thanh toán | Nhân viên xác nhận đã nhận tiền tại cửa hàng, không kết nối ngân hàng |
| Dữ liệu | Collection trong RAM; lưu toàn bộ dữ liệu thành một file Properties UTF-8 |
| Tiền tệ | Đồng Việt Nam, số nguyên `long`, không dùng `float`/`double` để tính tiền |
| Thời gian | Giờ cửa hàng `Asia/Ho_Chi_Minh`; giao dịch dùng `LocalDateTime`, chương trình khuyến mãi dùng `LocalDate` |
| Lưu trữ nghiệp vụ | Lưu sau mỗi thao tác làm thay đổi dữ liệu, không chỉ lưu khi thoát |

Đặt trước nghĩa là tạo yêu cầu mua và đến cửa hàng thanh toán. Không có thanh toán trực tuyến hoặc giao hàng trong phiên bản này.

## 3. Người sử dụng

| Loại tài khoản | Mục đích chính |
| --- | --- |
| Khách hàng | Xem danh mục và giá, đặt hàng, xem đơn của mình, tích và đổi điểm |
| Nhân viên bán hàng | Chọn hàng cho khách tại quầy, xử lý đơn đặt trước, xác nhận tiền và in nội dung hóa đơn ra console |
| Quản lý cửa hàng | Quản lý danh mục, nhân viên, nhập hàng, khuyến mãi và báo cáo tài chính |

Mỗi tài khoản có đúng một vai trò. Quản lý không mặc nhiên có quyền thu ngân; phân quyền chi tiết ở [tài liệu 02](02-tai-khoan-va-phan-quyen.md).

## 4. Mô hình hàng hóa

### 4.1. Nhóm sản phẩm

| Giá trị `LoaiSanPham` | Ví dụ sản phẩm | Đơn vị cơ bản minh họa |
| --- | --- | --- |
| `KEM` | Kem vani 60 g | Que |
| `SUA` | Sữa tươi có đường 180 ml | Hộp |
| `SUA_CHUA` | Sữa chua có đường 100 g | Hũ |
| `NUOC_GIAI_KHAT` | Nước trái cây 250 ml | Chai |
| `PHO_MAI` | Phô mai hộp 120 g | Hộp |

Dung tích, khối lượng và hương vị được thể hiện trong tên/mô tả sản phẩm. Sữa 180 ml và sữa 1 lít là hai `SanPham` khác nhau. Sửa tên/mô tả chỉ để hiệu chỉnh cách trình bày của cùng mặt hàng; không đổi mặt hàng thực tế, dung tích, khối lượng, hương vị hoặc đơn vị cơ bản của sản phẩm đã tạo. Những thay đổi đó phải tạo `SanPham` mới để không gán lại ý nghĩa cho tồn kho và các SKU hiện có. “Đơn vị cơ bản” do cửa hàng xác định cho sản phẩm đó: một hộp phô mai có thể chứa nhiều miếng nhưng vẫn là một đơn vị bán lẻ.

### 4.2. Sản phẩm và mã hàng bán

`SanPham` mô tả hàng hóa chung. `BienTheSanPham` là một mã hàng bán, gọi là SKU, xác định quy cách đóng gói, số đơn vị cơ bản, giá và tồn kho.

Ví dụ cho cùng sản phẩm “Sữa tươi có đường 180 ml”:

| Mã minh họa | Đóng gói | Số đơn vị cơ bản | Giá bán giả lập | Tồn kho theo quy cách |
| --- | --- | ---: | ---: | ---: |
| SKU-SUA-LE | `LE` | 1 hộp | 8.000 đ | 30 hộp lẻ |
| SKU-SUA-LOC4 | `LOC` | 4 hộp | 30.000 đ | 10 lốc |
| SKU-SUA-THUNG48 | `THUNG` | 48 hộp | 340.000 đ | 2 thùng |

Các mã ngắn trong ví dụ chỉ để dễ đọc; khi lập trình khóa định danh dùng chuỗi UUID.

- Mua 2 lốc trừ 2 đơn vị tồn của SKU lốc, không trừ 8 đơn vị tồn của SKU lẻ.
- Giá lốc/thùng do quản lý đặt; không bắt buộc bằng số lượng nhân giá lẻ.
- Có thể có lốc 4 và lốc 6 của cùng sản phẩm, nếu cửa hàng thực sự bán cả hai.
- Không bắt buộc mỗi sản phẩm có đủ ba hình thức đóng gói.
- Chưa có chức năng xé thùng thành lốc, ghép lốc hoặc tự động chuyển tồn giữa các SKU.

## 5. Chu trình hoạt động

1. Khởi tạo tài khoản quản lý đầu tiên bằng thông tin nhập tại console.
2. Quản lý tạo nhân viên, danh mục sản phẩm và các SKU; SKU mới có tồn bằng 0.
3. Quản lý lập phiếu nhập để có hàng và giá vốn, kể cả khi tạo tồn đầu kỳ.
4. Quản lý tạo chương trình giảm theo phần trăm hoặc theo số tiền cho từng đơn vị SKU.
5. Khách đăng ký, đăng nhập, xem hàng và đặt đơn; hoặc nhân viên lập đơn cho khách tại quầy.
6. Nhân viên kiểm tra lại hàng, giá, khuyến mãi và điểm; khách xác nhận số tiền cuối cùng.
7. Thanh toán thành công cập nhật cùng lúc đơn, tồn kho và điểm; lưu file rồi mới thông báo hoàn tất.
8. Quản lý xem doanh thu thực thu, giá vốn hàng đã bán, lãi gộp, chi nhập hàng và chênh lệch thu–chi.

Giá của đơn chờ là **tạm tính**. Đơn chờ chưa giữ hàng, chưa giữ điểm và chưa được tính vào doanh thu. Khi thanh toán, chương trình trả báo giá bất biến có thời điểm tính để khách xác nhận. Lúc chốt, dịch vụ dùng một thời điểm hiện tại để tính lại; nếu ngày tính giá hoặc nội dung báo giá thay đổi thì không ghi giao dịch và phải xin xác nhận lại, kể cả khi khách để màn hình chờ qua nửa đêm.

## 6. Các quyết định để giữ đồ án vừa sức

| Quyết định | Lý do |
| --- | --- |
| Năm loại hàng là một enum | Hiện tại chúng dùng cùng cách bán, nhập và tính tiền; không tạo năm lớp con chỉ khác tên loại |
| Kế thừa ở tài khoản và khuyến mãi | Tài khoản giữ cấu trúc ba vai trò; khuyến mãi là ví dụ chính về đa hình hành vi vì các lớp con tính giảm giá khác nhau |
| Đơn đã thanh toán đồng thời là hóa đơn | Không tạo thêm lớp hóa đơn chứa lại toàn bộ thông tin của đơn |
| Nhà cung cấp là tên trên phiếu nhập | Chưa cần quản lý hồ sơ hoặc công nợ nhà cung cấp |
| Giỏ tạm là `Map<String, Integer>` | Chỉ cần mã SKU và số lượng; chưa cần lớp giỏ hàng riêng |
| Mỗi chương trình khuyến mãi gắn một SKU | Dễ giải thích, dễ kiểm tra điều kiện và tránh thêm quan hệ nhiều–nhiều |
| Dữ liệu tổ chức giống bảng quan hệ | Dễ trình bày khóa và quan hệ, nhưng thực thi bằng đối tượng và file |

Lớp tiện ích có thành viên `static` được mô tả trong [tài liệu 04](04-thiet-ke-cac-lop.md). Không có lớp `static` lồng nhau.

## 7. Giới hạn công khai

Chưa làm quản lý nhiều chi nhánh, giao hàng, cổng thanh toán, công nợ, thuế, chi phí vận hành, hoàn tiền/đổi trả, lô hàng/hạn sử dụng, chuyển đổi quy cách tồn kho hoặc nhiều người cùng ghi dữ liệu. Đây là phạm vi mô phỏng phục vụ học OOP; chưa phù hợp vận hành cửa hàng thực tế có yêu cầu kiểm soát hạn dùng.

Mức tích điểm và giảm giá ở [tài liệu 06](06-quy-tac-nghiep-vu-va-kiem-thu.md) là quy tắc của đồ án. Lãi gộp chưa phải lợi nhuận ròng.
