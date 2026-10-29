# Tài khoản và phân quyền

Tài liệu này xác định ai được làm gì trong ứng dụng quản lý một cửa hàng sữa. Phạm vi chung nằm trong [Mô tả ứng dụng](01-mo-ta-ung-dung.md); luồng thao tác nằm trong [Danh sách chức năng](03-danh-sach-chuc-nang.md).

## 1. Ba loại tài khoản

`TaiKhoan` là lớp trừu tượng chứa thông tin và hành vi chung của tài khoản. Ba lớp `KhachHang`, `NhanVienBanHang` và `QuanLyCuaHang` cùng kế thừa trực tiếp từ `TaiKhoan`. Mỗi tài khoản thuộc đúng một loại; không đổi loại tài khoản sau khi tạo.

| Loại tài khoản | Mục đích | Thông tin riêng cần quản lý |
| --- | --- | --- |
| `KhachHang` | Xem hàng, đặt đơn nhận tại cửa hàng và tham gia tích điểm | Số điện thoại, số điểm hiện có; các đơn hàng được tra bằng mã khách hàng |
| `NhanVienBanHang` | Lập đơn tại quầy, tiếp nhận đơn đặt và thực hiện thanh toán | Không cần thêm thuộc tính riêng cho phiên bản này |
| `QuanLyCuaHang` | Quản lý danh mục, nhập hàng, khuyến mãi, tài khoản và tài chính; được bán hàng khi cần | Không cần thêm thuộc tính riêng cho phiên bản này |

Các thông tin chung gồm mã tài khoản, tên đăng nhập, thông tin xác thực mật khẩu, họ tên và trạng thái hoạt động. Mã tài khoản không đổi, tên đăng nhập không trùng. Mật khẩu phải được lưu dưới dạng băm kèm salt, không ghi mật khẩu gốc vào file hoặc hiển thị lại trên màn hình.

Khách vãng lai là khách mua trực tiếp không gắn tài khoản thành viên vào đơn. Đây không phải loại tài khoản thứ tư và không có quyền đăng nhập.

Quản lý có quyền thao tác bán hàng do quy tắc phân quyền, không phải do kế thừa `NhanVienBanHang`. Cách thể hiện abstract class, interface và các lớp quản lý danh sách được trình bày trong [Thiết kế lớp](04-thiet-ke-lop.md).

## 2. Tạo và sử dụng tài khoản

### 2.1. Khách hàng tự đăng ký

Người dùng nhập tên đăng nhập, mật khẩu, họ tên và số điện thoại. Chương trình kiểm tra dữ liệu, tạo `KhachHang` với điểm ban đầu bằng 0 và lưu dữ liệu thành công trước khi thông báo đăng ký thành công. Màn hình đăng ký không có lựa chọn trở thành nhân viên hoặc quản lý.

### 2.2. Tài khoản nội bộ

Quản lý đang hoạt động được tạo tài khoản nhân viên bán hàng hoặc quản lý mới. Tài khoản nội bộ không được tạo từ chức năng tự đăng ký của khách hàng.

Khi file dữ liệu chưa tồn tại, ứng dụng thông báo đây là lần khởi tạo hệ thống và yêu cầu xác nhận trước khi tạo dữ liệu ban đầu. Người dùng nhập thông tin quản lý đầu tiên tại console; không dùng mật khẩu mặc định. Nếu file đã tồn tại nhưng rỗng hoặc hỏng, ứng dụng báo lỗi đọc dữ liệu, không tự khởi tạo và không ghi đè file đó.

### 2.3. Đăng nhập, đăng xuất và khóa tài khoản

- Chỉ tài khoản có thông tin xác thực đúng và đang hoạt động được đăng nhập.
- Một phiên console chỉ có một tài khoản đăng nhập. Đăng xuất phải xóa trạng thái phiên hiện tại trước khi quay lại màn hình đầu.
- Quản lý được khóa hoặc mở lại tài khoản. Tài khoản bị khóa không được thực hiện nghiệp vụ; cần kiểm tra lại trạng thái và quyền tại thời điểm gọi chức năng.
- Không được khóa tài khoản quản lý đang hoạt động cuối cùng. Nếu quản lý tự khóa mình khi còn quản lý khác hoạt động, phiên của người đó kết thúc sau khi lưu thành công.
- Không xóa tài khoản đã tạo, để mã tài khoản trên đơn hàng và phiếu nhập luôn tra được. Khóa tài khoản không xóa lịch sử mua bán, phiếu nhập hoặc điểm đang có.
- Không sửa điểm bằng màn hình quản lý tài khoản. Điểm chỉ thay đổi theo giao dịch thanh toán hợp lệ.

## 3. Bảng phân quyền

“Được” nghĩa là được gọi chức năng khi đã đăng nhập và tài khoản đang hoạt động. Các thao tác còn phải thỏa điều kiện nghiệp vụ, ví dụ đủ tồn kho và đơn chưa thanh toán.

| Chức năng | Khách hàng | Nhân viên bán hàng | Quản lý cửa hàng |
| --- | --- | --- | --- |
| Xem, tìm sản phẩm, SKU, giá bán và tồn có thể bán | Được | Được | Được |
| Xem khuyến mãi đang có hiệu lực | Được | Được | Được |
| Đặt đơn để nhận và trả tiền tại cửa hàng | Được, cho bản thân | Qua chức năng lập đơn tại quầy | Qua chức năng lập đơn tại quầy |
| Xem danh sách và chi tiết đơn hàng | Chỉ đơn của mình | Các đơn của cửa hàng | Các đơn của cửa hàng |
| Hủy đơn `CHO_THANH_TOAN` | Chỉ đơn của mình | Được | Được |
| Lập đơn tại quầy cho thành viên hoặc khách vãng lai | Không | Được | Được |
| Xác nhận thanh toán, thu tiền và xuất nội dung hóa đơn | Không | Được | Được |
| Xem điểm tích lũy | Chỉ điểm của mình | Tra điểm thành viên phục vụ thanh toán | Tra điểm thành viên phục vụ thanh toán |
| Đề nghị sử dụng điểm của khách hàng | Cho điểm của mình khi thanh toán | Thực hiện khi chủ tài khoản đồng ý | Thực hiện khi chủ tài khoản đồng ý |
| Thêm, sửa thông tin hoặc ngừng bán sản phẩm/SKU | Không | Không | Được |
| Tạo, cập nhật hoặc ngừng khuyến mãi | Không | Không | Được |
| Lập và lưu phiếu nhập, xem lịch sử nhập | Không | Không | Được |
| Xem báo cáo thu, chi nhập hàng và tồn kho | Không | Không | Được |
| Tạo tài khoản nhân viên hoặc quản lý | Không | Không | Được |
| Xem danh sách, khóa hoặc mở lại tài khoản | Không | Không | Được, bảo vệ quản lý hoạt động cuối cùng |
| Đăng xuất | Được | Được | Được |

Khách hàng được đưa ra lựa chọn dùng điểm; nhân viên hoặc quản lý thực hiện thanh toán tại quầy. Khách hàng không tự đánh dấu đơn đã trả tiền và nhân viên không được đổi điểm nếu khách hàng chưa đồng ý. Khách vãng lai không được tích hoặc sử dụng điểm.

## 4. Menu console dự kiến

Các nhóm menu dưới đây là cách tổ chức màn hình, không phải các loại tài khoản bổ sung. Tại màn hình nhập liệu, người dùng có lựa chọn quay lại trước khi xác nhận giao dịch.

### 4.1. Màn hình chưa đăng nhập

```text
1. Đăng nhập
2. Đăng ký khách hàng
0. Thoát chương trình
```

### 4.2. Khách hàng

```text
1. Xem / tìm sản phẩm và khuyến mãi
2. Đặt hàng nhận tại cửa hàng
3. Xem đơn hàng của tôi
4. Hủy đơn đang chờ thanh toán
5. Xem điểm tích lũy
0. Đăng xuất
```

Menu đặt hàng cho phép chọn SKU, nhập số lượng, sửa hoặc bỏ dòng trước khi xác nhận đặt. Giá và điểm được trình bày rõ là tạm tính; khách hàng quyết định số điểm muốn dùng khi thanh toán tại cửa hàng.

### 4.3. Nhân viên bán hàng

```text
1. Xem / tìm sản phẩm và khuyến mãi
2. Lập đơn bán tại quầy
3. Xem / tìm đơn hàng
4. Thanh toán đơn hàng
5. Hủy đơn đang chờ thanh toán
6. Tra thành viên và điểm để bán hàng
0. Đăng xuất
```

Trong chi tiết đơn đã thanh toán, nhân viên có thể hiển thị lại hóa đơn. Đây chỉ là đọc lịch sử, không thanh toán lại và không tạo thêm doanh thu hoặc điểm.

### 4.4. Quản lý cửa hàng

```text
1. Bán hàng                    -> Các thao tác của nhân viên bán hàng
2. Quản lý sản phẩm và SKU     -> Danh sách, thêm, sửa, ngừng / mở bán
3. Quản lý khuyến mãi          -> Danh sách, thêm, sửa, ngừng áp dụng
4. Nhập hàng                  -> Lập phiếu nhập, xem lịch sử nhập
5. Quản lý tài khoản          -> Danh sách, tạo nội bộ, khóa / mở lại
6. Báo cáo                    -> Thu bán hàng, chi nhập, chênh lệch, tồn
0. Đăng xuất
```

## 5. Kiểm tra quyền ở đâu?

Menu chỉ giúp hiển thị lựa chọn phù hợp. `DichVuCuaHang` phải kiểm tra tài khoản, trạng thái hoạt động và quyền trước khi thực hiện nghiệp vụ. Với đơn của khách hàng, lớp này kiểm tra cả mã khách hàng trên đơn; biết mã đơn của người khác không làm phát sinh quyền xem hoặc hủy đơn đó.

Các lớp `QuanLy...` kế thừa `DanhSach<T>` thực thi `IQuanLy<T>`, giữ danh sách instance và được `Main` tạo một lần rồi truyền chung cho dịch vụ và lưu trữ. Các phương thức xuất, thêm, sửa, xóa không được trở thành đường đi để bỏ qua phân quyền hoặc sửa tồn, điểm và doanh thu tùy ý. `xuat()` trả danh sách cho dịch vụ lọc theo quyền và giao diện định dạng; không hiển thị dữ liệu băm/salt hoặc đối tượng tài khoản nguyên bản. Luồng thao tác từ console đi qua lớp điều phối trước khi cập nhật và lưu dữ liệu. Thiết kế cụ thể nằm trong [Thiết kế lớp](04-thiet-ke-lop.md) và [Quy tắc nghiệp vụ](06-quy-tac-nghiep-vu.md).
