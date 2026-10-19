# Danh sách chức năng

Phạm vi: một cửa hàng bán sữa và thực phẩm liên quan, chạy bằng Java 24 trên console, không dùng cơ sở dữ liệu. Dữ liệu lâu dài nằm trong một tệp snapshot; giỏ hàng đang thao tác chỉ nằm trong RAM. Các quyền bên dưới tuân theo [Tài khoản và phân quyền](02-tai-khoan-va-phan-quyen.md).

## 1. Tài khoản

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| TK-01 | Khởi tạo quản lý đầu tiên / người cài đặt | Tên đăng nhập, mật khẩu, họ tên | Tạo quản lý khi chưa có tệp dữ liệu; không ghi đè dữ liệu có sẵn hoặc tự khởi tạo lại khi tệp lỗi |
| TK-02 | Đăng ký khách hàng / người chưa đăng nhập | Tên đăng nhập, mật khẩu, họ tên, số điện thoại | Tạo `KhachHang` đang hoạt động, điểm bằng 0; kiểm tra thông tin bắt buộc và trùng tên/số điện thoại |
| TK-03 | Đăng nhập, đăng xuất / cả ba vai trò | Thông tin đăng nhập hoặc lựa chọn đăng xuất | Đăng nhập mở menu đúng vai trò; đăng xuất xóa phiên và giỏ tạm; tài khoản bị khóa không được đăng nhập |
| TK-04 | Đổi mật khẩu / tài khoản đang đăng nhập | Mật khẩu hiện tại, mật khẩu mới, xác nhận | Cập nhật thông tin xác thực sau khi kiểm tra mật khẩu hiện tại; không lưu mật khẩu dạng rõ |
| TK-05 | Quản lý tài khoản / quản lý | Thông tin nhân viên mới hoặc mã tài khoản cần khóa/mở khóa | Tạo nhân viên; khóa/mở khóa nhân viên và khách hàng; không xóa lịch sử, không tự khóa quản lý |

## 2. Danh mục và kho hàng

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| SP-01 | Xem/tìm sản phẩm / cả ba vai trò | Từ khóa, loại sản phẩm hoặc quy cách đóng gói | Trả `SanPhamHienThi` bất biến gồm tên sản phẩm, SKU, quy cách, giá, khuyến mãi và tình trạng còn hàng; không có giá vốn hoặc số tồn; khách chỉ thấy sản phẩm và SKU đang bán |
| SP-02 | Quản lý sản phẩm / quản lý | Tên, loại, mô tả hoặc mã sản phẩm | Thêm/sửa thông tin trình bày, ngừng bán; loại là kem, sữa, sữa chua, nước giải khát hoặc phô mai; không biến sản phẩm đã tạo thành mặt hàng khác; giữ mã và lịch sử giao dịch |
| SP-03 | Quản lý SKU / quản lý | Sản phẩm, mã SKU, loại đóng gói, số đơn vị cơ bản, giá bán, trạng thái | Thêm/sửa/ngừng bán biến thể; giá bán dương, số đơn vị cơ bản hợp lệ; không sửa trực tiếp tồn kho hoặc giá vốn bằng màn hình này |
| SP-04 | Xem tồn kho / nhân viên, quản lý | Mã SKU hoặc bộ lọc | Trả `Map<String, Integer>` chỉ đọc chứa số lượng tồn từng SKU; giá nhập/giá vốn/giá trị tồn chỉ qua phiếu nhập hoặc báo cáo/đối chiếu dành cho quản lý |
| NH-01 | Nhập hàng / quản lý | Tên nhà cung cấp, các SKU, số lượng, đơn giá nhập | Hiển thị phiếu nháp; xác nhận thì tạo phiếu nhập, tăng tồn và cập nhật giá vốn; ghi nhận toàn bộ tiền nhập đã trả |
| NH-02 | Xem phiếu nhập / quản lý | Mã phiếu hoặc khoảng ngày | Xem nhà cung cấp, người nhập, các dòng hàng, tổng tiền; phiếu đã ghi nhận không sửa/xóa |

`SanPham` mô tả mặt hàng chung; `BienTheSanPham` là đơn vị tồn kho và bán hàng thực tế. Ví dụ cùng một loại sữa có SKU hộp lẻ, lốc 4 hộp và thùng 48 hộp với ba giá bán và ba số tồn riêng. `soDonViCoBan` mô tả quy cách, không tự quy đổi tồn: bán 1 thùng chỉ trừ 1 tồn SKU thùng. Giai đoạn này không tháo thùng thành lốc hoặc hộp lẻ.

SKU mới có tồn kho bằng 0 và chỉ được tăng tồn qua phiếu nhập. Sản phẩm, loại đóng gói và số đơn vị cơ bản của SKU bất biến ngay từ khi tạo; muốn đổi quy cách thì tạo SKU mới. Màn hình sửa SKU chỉ đổi giá bán hoặc trạng thái bán. Ngừng bán giữ nguyên tồn và lịch sử, đồng thời chặn đặt hàng/thanh toán SKU đó.

Sửa tên/mô tả sản phẩm chỉ hiệu chỉnh cách trình bày của cùng mặt hàng; không đổi mặt hàng thực tế, dung tích, khối lượng, hương vị hoặc đơn vị cơ bản. Muốn thay các đặc tính đó thì tạo `SanPham` và SKU mới, không gán lại tồn kho hiện có cho hàng khác.

Nhà cung cấp chỉ là tên ghi trên phiếu nhập. Không cần tài khoản nhà cung cấp, quản lý công nợ hay đơn đặt hàng với nhà cung cấp.

## 3. Khách đặt hàng và nhân viên bán hàng

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| BH-01 | Quản lý giỏ hàng / khách hàng | SKU, số lượng; thao tác thêm, đổi số lượng, xóa | Giỏ tạm gộp SKU trùng thành một dòng; số lượng phải nguyên dương; hiển thị số tiền tạm tính |
| BH-02 | Đặt hàng nhận tại cửa hàng / khách hàng | Giỏ không rỗng, số điểm muốn dùng, xác nhận đặt | Tạo `DonHang` ở `CHO_THANH_TOAN`; `diemSuDung` lưu số điểm tạm đề nghị, tiền tạm tính dùng số điểm này, `diemNhanDuoc = 0`; chưa thu tiền, trừ tồn, trừ điểm hay cộng điểm |
| BH-03 | Xem/hủy đơn đã đặt / khách hàng | Mã đơn thuộc khách đang đăng nhập | Xem chi tiết/trạng thái; chỉ hủy đơn `CHO_THANH_TOAN`, chuyển sang `DA_HUY`; ghi `nguoiHuyId` từ phiên và thời điểm hủy |
| BH-04 | Lập đơn tại quầy / nhân viên | Các SKU/số lượng, khách hàng đã xác nhận hoặc khách vãng lai | Tạo nội dung mua và số tiền tạm tính; hàng trùng SKU được gộp; đơn có thể chờ thanh toán hoặc bị hủy |
| BH-05 | Tiếp nhận đơn khách đã đặt / nhân viên | Mã đơn hoặc danh sách đơn chờ | Xem đơn chờ, xác nhận người nhận và yêu cầu đổi điểm; giữ nguyên chủ đơn; có thể hủy khi khách yêu cầu, lưu người hủy là nhân viên đang đăng nhập |
| BH-06 | Thanh toán / nhân viên | Đơn chờ, `DonHangHienThi` báo giá khách đã xác nhận, số điểm thực dùng, xác nhận đã nhận tiền | Tính lại giá/khuyến mãi/tồn/điểm tại một thời điểm chốt; chỉ ghi khi ngày và nội dung khớp báo giá, nếu khác trả báo giá mới để xác nhận lại; ghi người thu tiền và thời điểm; trừ tồn, đổi/cộng điểm và chuyển `DA_THANH_TOAN` cùng một lần lưu |
| BH-07 | Xem đơn/hóa đơn console / theo quyền | Mã đơn hoặc khoảng ngày | Trả `DonHangHienThi` với các `ChiTietDonHienThi` bất biến: hàng, giá/giảm giá đã chốt, điểm và tổng thực thu, không có giá vốn; khách xem đơn mình, nhân viên xem đơn mình thu tiền, quản lý xem mọi đơn và tra cứu giá vốn qua báo cáo riêng |
| BH-08 | Xem điểm tích lũy / khách hàng | Tài khoản đang đăng nhập | Hiển thị số điểm hiện có và điểm đã dùng/được cộng trên các đơn của mình |

### Luồng thanh toán thống nhất

1. Nhân viên mở đơn chờ hoặc lập đơn tại quầy. Đơn có thể gắn khách hàng đã xác nhận hoặc để trống với khách vãng lai.
2. Dịch vụ kiểm tra sản phẩm/SKU đang bán, số lượng tồn, trạng thái đơn và tài khoản khách. Đơn chờ **không giữ tồn kho hoặc giữ điểm**.
3. `tinhTamDon` trả báo giá `DonHangHienThi` bất biến có `thoiDiemTinhGia`, các dòng giá/khuyến mãi, điểm và tổng tiền; không chứa giá vốn và không lưu vào file. Hiển thị rõ khác biệt với lúc đặt để khách xác nhận; không tự giảm số lượng hoặc tự dùng thêm điểm.
4. Chỉ đổi điểm khi khách yêu cầu. Nhân viên xác nhận số tiền phải trả và đã nhận đủ tiền tại cửa hàng, rồi gửi đúng báo giá khách đã xác nhận vào `thanhToan`. Không có thanh toán trực tuyến hay giao hàng.
5. Khi bắt đầu `thanhToan`, lấy một `lucChot` theo giờ cửa hàng, kiểm tra lại quyền/trạng thái/tồn/điểm và tính lại báo giá bằng dữ liệu tin cậy. Nếu khác ngày tính giá, đơn/khách/kênh bán, SKU/số lượng, tên/quy cách, đơn giá, mã/mô tả khuyến mãi, khoản giảm, điểm/giới hạn điểm hoặc tổng tiền, từ chối ghi và trả báo giá mới để khách xác nhận lại trước khi chốt/thu thêm tiền. Không so thời điểm đến nano giây; không tin số tiền UI gửi để tính giao dịch. Nếu khớp, dùng chính `lucChot` làm thời điểm thanh toán và lịch sử điểm.
6. Ghi giao dịch và lưu snapshot thành công mới báo hoàn tất. Nếu phải xác nhận lại hoặc lưu thất bại, giữ nguyên dữ liệu cũ; nếu đã nhận tiền, nhân viên phải hoàn lại hoặc đối chiếu khoản đang giữ trước khi thử lại, không tự thu tiền lần hai.
7. In hóa đơn ra console bằng các giá trị đã chốt. Thay đổi giá, tên hàng hoặc khuyến mãi sau này không làm thay đổi hóa đơn cũ.

Một đơn chỉ thanh toán một lần. `DA_THANH_TOAN` và `DA_HUY` là trạng thái kết thúc; không mở lại, sửa, xóa, hoàn tiền hoặc trả hàng trong phạm vi này. Trước khi trả tiền, khách có thể hủy đơn sai và lập đơn mới.

Số điểm đề nghị của đơn chờ không phải số điểm đã được giữ. Khi nạp đơn chờ từ tệp, không đối chiếu đề nghị này với số dư điểm hiện tại; số dư có thể đã đổi do một giao dịch khác. Đến lúc thanh toán, dịch vụ mới kiểm tra lại số dư/trần đổi và yêu cầu khách xác nhận số điểm thực dùng trước khi chốt `diemSuDung`, `diemNhanDuoc` và `tongThanhToan`.

Đơn `DA_HUY` bắt buộc có `nguoiHuyId` và `thoiDiemHuy`; các trạng thái còn lại không có hai trường này. Lịch sử điểm chỉ ghi số điểm trừ/cộng theo từng đơn, không lưu số dư sau từng giao dịch; số điểm hiện tại nằm ở khách hàng và phải khớp tổng điểm cộng trừ trên toàn bộ lịch sử của khách.

## 4. Khuyến mãi và điểm tích lũy

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| KM-01 | Tạo khuyến mãi / quản lý | Mã, tên, thời gian áp dụng, một SKU, loại và mức giảm | Tạo khuyến mãi phần trăm hoặc số tiền cố định cho đúng một SKU; kiểm tra thời gian và mức giảm hợp lệ; muốn áp dụng cho nhiều SKU thì tạo các quy tắc riêng |
| KM-02 | Xem/ngừng khuyến mãi / quản lý | Mã chương trình | Xem phạm vi áp dụng hoặc ngừng cho giao dịch tiếp theo; hóa đơn đã thanh toán giữ giá trị cũ |
| KM-03 | Tính ưu đãi tự động / hệ thống khi xem giỏ và thanh toán | SKU, số lượng, giá hiện tại, thời gian | Chọn tối đa một khuyến mãi cho mỗi SKU theo số tiền giảm tốt nhất; khi bằng nhau chọn mã khuyến mãi theo thứ tự tăng |
| KM-04 | Đổi và cộng điểm / khách yêu cầu, nhân viên xác nhận | Khách hàng, số điểm muốn đổi, tiền sau khuyến mãi | Giảm tiền bằng điểm trước khi tính điểm thưởng; kiểm tra số dư và trần đổi; chỉ cập nhật điểm khi đơn thanh toán thành công |

Quy tắc chung dùng cho cả mua tại quầy và đơn đặt trước:

- Giảm phần trăm tính trên toàn dòng SKU: `floor(số lượng × đơn giá × tỷ lệ / 100)`. Giảm số tiền cố định áp dụng **cho từng đơn vị SKU**, tổng giảm của dòng là `min(đơn giá, số tiền giảm) × số lượng`. Ví dụ 3 lốc giá 30.000 đ/lốc, giảm 5.000 đ/lốc thì giảm tổng 15.000 đ, gộp hay tách đơn đều cùng mức giảm cố định. Gộp SKU trùng trước khi xét khuyến mãi.
- Khoản giảm của một dòng không vượt quá tiền gốc dòng đó. Không cộng dồn hai khuyến mãi trên cùng SKU. Các SKU khác nhau được hưởng chương trình riêng.
- Sau khuyến mãi, khách có thể đổi **1 điểm = 1.000 đồng**. Số điểm đổi tối đa là giá trị nhỏ nhất giữa số dư trước đơn và `floor(tiền sau khuyến mãi × 50% / 1.000)`.
- Số điểm được cộng là `floor(tiền thực trả / 10.000)`. Không cộng điểm từ khoản đã giảm; không dùng số điểm vừa được cộng để thanh toán chính đơn đó.
- Khách vãng lai không đổi hoặc nhận điểm. Hủy đơn chờ không tác động điểm. Hệ thống không tự động dùng điểm thay khách.
- Tiền được lưu bằng `long`, đơn vị đồng Việt Nam nguyên, không dùng `float`/`double`. Dùng `BigInteger` cho phép nhân/chia trung gian có nguy cơ tràn và kiểm tra miền `long` trước khi lưu kết quả. Phần giảm theo phần trăm được làm tròn xuống đến đồng trước khi cộng các dòng.

Ví dụ: tiền gốc 200.000 đồng, khuyến mãi giảm 20.000 đồng, còn 180.000 đồng. Khách có 120 điểm và chọn dùng 50 điểm: giảm tiếp 50.000 đồng, trả 130.000 đồng, được cộng 13 điểm; số dư mới là `120 − 50 + 13 = 83` điểm. Trần đổi cho đơn này là 90 điểm.

## 5. Tổng kết tài chính

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| BC-01 | Báo cáo tài chính / quản lý | Ngày bắt đầu, ngày kết thúc | Tổng doanh thu thực thu, tiền nhập đã trả, giá vốn hàng đã bán, lãi gộp và dòng tiền; chỉ lấy đơn đã thanh toán và phiếu nhập đã ghi nhận trong kỳ |
| BC-02 | Xem chi tiết phục vụ đối chiếu / quản lý | Mã đơn, mã phiếu hoặc SKU trong kỳ | Đi từ tổng báo cáo về các giao dịch tạo nên tổng đó; đọc giá bán/giảm giá/giá vốn đã lưu tại giao dịch |

| Chỉ tiêu | Cách hiểu |
| --- | --- |
| Doanh thu thực thu | Tổng `DonHang.tongThanhToan` sau khuyến mãi và đổi điểm, lọc theo `thoiDiemThanhToan` |
| Chi nhập hàng | Tổng tiền các phiếu nhập đã trả, lọc theo `thoiDiemNhap` |
| Giá vốn hàng bán | Tổng `ChiTietDonHang.giaVon` đã chốt tại lúc thanh toán; mỗi dòng phân bổ `floor(giaTriTonTruoc × soLuong / tonKhoTruoc)`, không nhân số lượng với giá vốn đơn vị đã làm tròn và không tính lại bằng giá nhập hiện tại |
| Lãi gộp | Doanh thu thực thu trừ giá vốn hàng bán |
| Dòng tiền từ bán và nhập hàng | Doanh thu thực thu trừ chi nhập hàng |

Nhập 100 sản phẩm nhưng chỉ bán 20 sản phẩm sẽ làm **chi nhập hàng** và **giá vốn hàng bán** khác nhau. Báo cáo không gọi dòng tiền là lợi nhuận và chưa tính tiền thuê cửa hàng, lương, điện, vận chuyển, thuế hay công nợ. Khoảng ngày lọc bao gồm cả ngày bắt đầu và kết thúc theo giờ Việt Nam.

## 6. Dữ liệu và xử lý lỗi

| Mã | Chức năng / người thực hiện | Đầu vào | Kết quả và điều kiện chính |
| --- | --- | --- | --- |
| DL-01 | Nạp dữ liệu / hệ thống lúc khởi động | Một tệp snapshot `Properties` UTF-8 | Nạp tài khoản, danh mục, tồn, khuyến mãi, đơn và phiếu nhập; kiểm tra tham chiếu, giá trị và đối soát tồn/giá trị tồn/điểm với toàn bộ chứng từ; tệp hỏng phải báo lỗi, không ghi đè bằng dữ liệu rỗng |
| DL-02 | Lưu dữ liệu / hệ thống sau mỗi thay đổi lâu dài | Toàn bộ trạng thái hợp lệ | Kiểm tra bất biến và đối soát bộ dữ liệu ứng viên, ghi tệp tạm rồi thay snapshot nguyên tử; thao tác lỗi không được báo thành công hoặc làm mất bản dữ liệu trước đó |

Đăng ký, đổi mật khẩu, khóa tài khoản, thay đổi danh mục/khuyến mãi, đặt/hủy đơn, nhập hàng và thanh toán đều cần lưu. Tìm kiếm, xem báo cáo và sửa giỏ hàng tạm không cần ghi tệp. Không dùng cơ sở dữ liệu, thư viện ORM hoặc máy chủ bên ngoài.

Console kiểm tra lựa chọn menu, số nguyên, khoảng ngày, giá và số lượng nhập vào. Nhập sai phải thông báo dễ hiểu và cho nhập lại; không làm chương trình thoát hoặc để tồn/điểm âm.

## 7. Menu console dự kiến

```text
CHƯA ĐĂNG NHẬP
1. Đăng nhập
2. Đăng ký khách hàng
0. Thoát

KHÁCH HÀNG
1. Xem và tìm sản phẩm
2. Giỏ hàng và đặt hàng
3. Đơn hàng của tôi: xem / hủy đơn chờ
4. Điểm tích lũy của tôi
5. Đổi mật khẩu
0. Đăng xuất

NHÂN VIÊN BÁN HÀNG
1. Danh sách sản phẩm và tồn kho
2. Lập đơn tại quầy
3. Đơn chờ: xem / thanh toán / hủy theo yêu cầu
4. Các đơn tôi đã thu tiền
5. Đổi mật khẩu
0. Đăng xuất

QUẢN LÝ CỬA HÀNG
1. Sản phẩm và SKU
2. Tồn kho và nhập hàng
3. Khuyến mãi
4. Tài khoản nhân viên và khách hàng
5. Tổng kết tài chính và tra cứu giao dịch
6. Đổi mật khẩu
0. Đăng xuất
```

Menu chỉ tổ chức thao tác nhập/xuất. Các quy tắc về quyền, giá, tồn, khuyến mãi và điểm phải nằm trong lớp dịch vụ hoặc lớp nghiệp vụ tương ứng để cả hai luồng bán hàng dùng chung.
