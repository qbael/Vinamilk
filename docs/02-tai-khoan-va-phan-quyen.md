# Tài khoản và phân quyền

Ứng dụng có ba loại tài khoản. Mỗi tài khoản thuộc đúng một loại; giao diện console hiển thị menu tương ứng sau khi đăng nhập. Việc ẩn menu không thay thế kiểm tra quyền tại các lớp dịch vụ.

## 1. Mô hình tài khoản

```text
TaiKhoan (abstract)
├── KhachHang
├── NhanVienBanHang
└── QuanLyCuaHang
```

`TaiKhoan` chứa thông tin chung như mã tài khoản, tên đăng nhập, thông tin xác thực, họ tên và trạng thái hoạt động. Các lớp con kế thừa phần chung và thể hiện vai trò riêng. `KhachHang` bổ sung số điện thoại và số điểm tích lũy. Chi tiết kiểu dữ liệu, thuộc tính và phương thức được trình bày trong tài liệu thiết kế lớp.

| Loại tài khoản | Mục đích | Quyền chính |
| --- | --- | --- |
| `KhachHang` | Mua sản phẩm và sử dụng quyền lợi thành viên | Xem sản phẩm, tạo đơn đặt hàng, xem/hủy đơn chờ của mình, xem và đề nghị sử dụng điểm của mình |
| `NhanVienBanHang` | Thực hiện bán hàng và thu tiền tại cửa hàng | Xem danh mục/tồn kho, lập đơn tại quầy, xử lý đơn khách đã đặt, thanh toán, xem các đơn do mình thu tiền |
| `QuanLyCuaHang` | Vận hành cửa hàng và xem kết quả kinh doanh | Quản lý hàng hóa, nhập hàng, quản lý khuyến mãi/tài khoản, xem đơn và báo cáo tài chính |

Quản lý và nhân viên là hai lớp con ngang hàng. Tài khoản quản lý không tự có quyền bán hàng hay thu tiền; đây là lựa chọn phạm vi để việc phân quyền rõ ràng khi trình bày đồ án.

## 2. Ma trận phân quyền

Ký hiệu: **Có** là được phép thực hiện; **Của mình** chỉ áp dụng cho dữ liệu thuộc tài khoản đang đăng nhập; **—** là không có quyền.

| Chức năng | Khách hàng | Nhân viên bán hàng | Quản lý cửa hàng |
| --- | --- | --- | --- |
| Đăng nhập, đăng xuất, đổi mật khẩu bản thân | Có | Có | Có |
| Xem danh mục, giá bán và khuyến mãi công khai | Có | Có | Có |
| Xem tồn kho chi tiết theo SKU | — | Có | Có |
| Tạo đơn đặt hàng để nhận tại cửa hàng | Có | — | — |
| Xem đơn đặt hàng của khách | Của mình | Các đơn chờ xử lý | Có |
| Hủy đơn đang chờ thanh toán | Của mình | Có, khi khách yêu cầu | — |
| Lập đơn bán tại quầy | — | Có | — |
| Thanh toán đơn tại quầy/đơn khách đã đặt | — | Có | — |
| Xem đơn đã thanh toán | Của mình | Các đơn do mình thu tiền | Có |
| Xem số điểm và điểm biến động trên từng đơn | Của mình | Khách đang được phục vụ | Có, khi tra cứu giao dịch |
| Yêu cầu đổi điểm khi mua hàng | Của mình | Nhập yêu cầu đã được khách xác nhận | — |
| Thêm/sửa/ngừng bán sản phẩm và SKU | — | — | Có |
| Lập, xác nhận và xem phiếu nhập hàng | — | — | Có |
| Tạo/ngừng chương trình khuyến mãi | — | — | Có |
| Tạo/khóa/mở khóa tài khoản nhân viên | — | — | Có |
| Khóa/mở khóa tài khoản khách hàng | — | — | Có |
| Xem doanh thu, chi nhập hàng, giá vốn, lãi gộp và dòng tiền | — | — | Có |

Khách hàng thấy trạng thái **còn hàng/hết hàng** và các quy cách đang bán; không được nhận giá nhập, giá vốn, giá trị tồn hoặc số lượng tồn chi tiết. Nhân viên được nhận số lượng tồn theo SKU nhưng không nhận giá nhập, giá vốn, giá trị tồn hoặc báo cáo tài chính tổng hợp; không được sửa giá, sửa điểm trực tiếp hay sửa giao dịch đã thanh toán.

Phân quyền áp dụng đến các trường trong kết quả service. Danh mục và đơn hàng trả các đối tượng hiển thị bất biến `SanPhamHienThi`, `ChiTietDonHienThi`, `DonHangHienThi`, không chứa thông tin xác thực, giá vốn hoặc số tồn chi tiết; không trả entity trong storage rồi chỉ ẩn cột trên console. Chức năng tồn kho trả `Map<String, Integer>` chỉ đọc cho nhân viên/quản lý. Giá nhập, giá vốn và giá trị tồn chỉ được cung cấp qua phiếu nhập bất biến hoặc kết quả báo cáo/đối chiếu đã kiểm tra quyền quản lý. Danh sách tài khoản và tra cứu khách chỉ trả các trường cần cho thao tác; thông tin băm/salt không được trả ra UI.

## 3. Tạo tài khoản và xác thực

1. **Lần chạy đầu:** khi chưa có tệp dữ liệu, chương trình hướng dẫn tạo tài khoản quản lý đầu tiên bằng cách nhập tên đăng nhập và mật khẩu. Không có mật khẩu quản lý mặc định được ghi cứng trong chương trình. Nếu tệp tồn tại nhưng hỏng, phải báo lỗi để khôi phục; không coi đó là lần chạy đầu.
2. **Khách hàng tự đăng ký:** nhập tên đăng nhập, mật khẩu, họ tên và số điện thoại. Tên đăng nhập là duy nhất trong toàn hệ thống; số điện thoại khách hàng được chuẩn hóa và không trùng nhau để hỗ trợ tra cứu tại quầy. Điểm ban đầu bằng 0.
3. **Nhân viên:** quản lý tạo tài khoản mới và cấp thông tin đăng nhập. Nhân viên có thể đổi mật khẩu của mình sau khi đăng nhập.
4. **Đăng nhập:** kiểm tra thông tin xác thực và trạng thái tài khoản. Không cho phép tài khoản bị khóa đăng nhập; thông báo thất bại không tiết lộ tài khoản có tồn tại hay không.
5. **Đổi mật khẩu:** yêu cầu mật khẩu hiện tại, mật khẩu mới và xác nhận mật khẩu mới. Chỉ đổi mật khẩu của tài khoản đang đăng nhập.
6. **Đăng xuất:** kết thúc phiên, xóa giỏ hàng tạm của phiên và trở về màn hình đăng nhập. Những đơn đã đặt và giao dịch đã lưu vẫn còn.

Số điện thoại được chuẩn hóa bằng cách `trim`, bỏ dấu cách và dấu `-`, rồi đổi tiền tố `+84` thành `0`. Kết quả phải gồm đúng 10 chữ số và bắt đầu bằng `0`; kiểm tra duy nhất sau khi chuẩn hóa. Ví dụ `+84 912-345-678` trở thành `0912345678`. Đây là giới hạn số điện thoại Việt Nam của đồ án; cùng quy tắc được dùng khi đăng ký và tra cứu tại quầy.

Mật khẩu không lưu dạng rõ trong tệp và không xuất hiện trong màn hình danh sách tài khoản hay báo cáo. Ứng dụng chạy một tiến trình với một phiên đăng nhập tại một thời điểm; người dùng đăng xuất trước khi chuyển sang vai trò khác.

## 4. Khóa tài khoản và giữ lịch sử

- Quản lý được khóa/mở khóa khách hàng hoặc nhân viên; không có chức năng tự khóa tài khoản quản lý hoặc tạo thêm quản lý trong phạm vi ban đầu.
- Không xóa tài khoản đã tham gia giao dịch. Khóa tài khoản chỉ thay đổi quyền sử dụng; mã tham chiếu trong đơn hàng và phiếu nhập vẫn giữ nguyên.
- Khi hủy đơn, lưu `nguoiHuyId` lấy từ phiên đăng nhập cùng `thoiDiemHuy`. Người hủy chỉ có thể là khách sở hữu đơn hoặc nhân viên đang xử lý yêu cầu khách; hai trường này chỉ có khi đơn `DA_HUY`. Không nhận mã người hủy do người dùng tự khai.
- Khóa khách hàng không tự hủy các đơn chờ. Nhân viên có thể hủy đơn theo yêu cầu; muốn thanh toán đơn gắn với tài khoản bị khóa thì quản lý cần mở khóa trước.
- Mỗi thao tác dịch vụ kiểm tra lại tài khoản còn hoạt động và có đúng quyền. Không chấp nhận mã khách hàng hoặc nhân viên do người dùng nhập để thay thế danh tính đang đăng nhập.
- Quản lý không được sửa mật khẩu, điểm tích lũy hoặc nội dung đơn đã thanh toán qua chức năng sửa hồ sơ.

## 5. Gắn khách hàng vào giao dịch tại quầy

Nhân viên tìm khách theo mã khách hàng hoặc số điện thoại; màn hình chỉ hiển thị thông tin cần thiết để đối chiếu, chẳng hạn họ tên, số điện thoại và số điểm hiện có. Nhân viên xác nhận đúng khách đang mua trước khi gắn vào đơn. Chỉ khách gắn với đơn mới được tích điểm hoặc sử dụng điểm trên đơn đó.

Khách vãng lai được mua hàng mà không cần tạo tài khoản, nhưng không tích điểm và không đổi điểm. Với đơn khách đã đặt, người mua đã gắn với đơn nên nhân viên không được thay sang khách khác để chuyển điểm.

Đây là quy trình xác nhận trực tiếp phục vụ đồ án console. Không triển khai OTP, cấp lại mật khẩu hay xác thực nhiều yếu tố ở giai đoạn này.

Xem các thao tác và menu cụ thể tại [Danh sách chức năng](03-danh-sach-chuc-nang.md).
