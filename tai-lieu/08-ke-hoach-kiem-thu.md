# 08. Kế hoạch kiểm thử và thứ tự triển khai

Bảng dưới ghi kết quả mong đợi. Cả 33 tình huống có kiểm chứng tự động trong `test/cuahang/KiemThu.java` (cách chạy ở [README](README.md#mã-nguồn-và-cách-chạy)); phần chỉ nằm ở giao diện, như menu đăng ký không có lựa chọn nội bộ (KT01) hay nhập chữ thay số (KT26), được kiểm tra khi chạy console, còn phần chặn sai kiểu của KT30 do trình biên dịch đảm nhận. Đối chiếu với [Quy tắc nghiệp vụ](06-quy-tac-nghiep-vu.md) và [Lưu trữ file text](07-luu-tru-file-text.md).

## 1. Tình huống kiểm thử

| Mã | Tình huống | Kết quả mong đợi |
| --- | --- | --- |
| KT01 | Tự đăng ký và nhập lựa chọn quản lý | Không có đường đăng ký nội bộ; tài khoản mới là khách, điểm 0 |
| KT02 | Trùng tên đăng nhập sau trim/đổi chữ thường | Từ chối, không tạo tài khoản thứ hai |
| KT03 | Khách xem/hủy mã đơn của khách khác | Từ chối dù mã đơn tồn tại |
| KT04 | Khóa quản lý hoạt động cuối cùng | Từ chối; hệ thống luôn còn quản lý truy cập được |
| KT05 | Bán 2 lốc từ tồn lốc 20 | Tồn lốc còn 18, tồn hộp/thùng giữ nguyên |
| KT06 | Tồn 5, chọn SKU 3 rồi chọn thêm 3 | Gộp thành 6 rồi từ chối vượt tồn, không lách qua nhiều dòng |
| KT07 | Hai đơn chờ cùng đặt 4 khi tồn 5 | Cả hai có thể chờ; thanh toán đơn thứ nhất còn 1, đơn thứ hai bị thiếu tồn |
| KT08 | Đặt hàng rồi hủy | Không đổi tồn, điểm hoặc thu bán; chỉ đổi trạng thái |
| KT09 | Giá tăng/khuyến mãi hết hạn sau lúc đặt | Thanh toán dùng giá/ưu đãi hiện tại, cần xác nhận bảng cuối |
| KT10 | SKU hoặc sản phẩm cha bị ngừng bán | Không được thanh toán dòng đó; không tự bỏ dòng rồi thu tổng khác |
| KT11 | Bài tính 60.000đ, giảm 10%, dùng 100/250 điểm | Thực trả 44.000đ, cộng 4, còn 154 điểm |
| KT12 | Cùng ví dụ nhưng đề nghị 109 điểm | Từ chối, thông báo tối đa 108 |
| KT13 | Tiền sau KM 499đ; đòi dùng 1 điểm | Tối đa 0 điểm, không làm tròn tăng mức giảm |
| KT14 | KM số tiền vượt giá; hoặc KM 100% | Dòng về 0, không âm; đơn 0 không tích điểm nhưng trừ hàng khi hoàn tất |
| KT15 | Hai KM giảm bằng nhau, ngày đúng đầu/cuối kỳ KM | Chọn ổn định theo mã; hai ngày biên vẫn hợp lệ |
| KT16 | Khách vãng lai trả 100.000đ | Thu 100.000đ theo giá cuối; điểm dùng/cộng bằng 0 |
| KT17 | Thanh toán lại đơn đã thanh toán / in lại hóa đơn | Thanh toán lặp bị chặn; in lại không làm thay đổi dữ liệu |
| KT18 | Sau bán, quản lý đổi tên/giá hoặc ngừng KM | Hóa đơn cũ và tổng thu cũ không đổi |
| KT19 | Nhập 3 thùng với giá nhập 200.000đ/thùng | Tồn thùng +3, chi nhập +600.000đ; tồn hộp/lốc không đổi |
| KT20 | Lưu lại phiếu nhập cùng mã | Từ chối, không tăng tồn/chi lần hai |
| KT21 | Ghi file lỗi trong thanh toán/nhập/sửa giá | File chính và RAM về trạng thái cũ, không báo thành công |
| KT22 | Commit thành công nhưng chưa in thì dừng chương trình | Mở lại thấy giao dịch đã hoàn tất; không được ghi nhận thêm |
| KT23 | File sai số cột, thiếu END, ID trùng, FK sai, điểm/tồn lệch | Báo lỗi rõ dòng hoặc mã liên quan, không nạp một phần/ghi đè |
| KT24 | Lưu rồi mở lại chương trình | Đúng lớp con, đúng toàn bộ đơn/chi tiết, điểm, tồn và số liệu thu-chi |
| KT25 | Đơn tạo ngày A, trả tiền ngày B | Thu tính ngày B, đơn chờ/hủy không góp vào báo cáo |
| KT26 | Số lượng 0/âm, chữ thay số, tràn tiền/tồn/điểm | Báo lỗi, cho nhập lại; không đổi một phần dữ liệu |
| KT27 | Hai đơn được soạn bằng hai đối tượng danh sách chi tiết | Thêm/xóa dòng ở đơn này không thay đổi đơn kia |
| KT28 | Đăng xuất KH rồi đăng nhập NV trong cùng JVM | Dùng lại cùng sáu đối tượng quản lý, dữ liệu giữ nguyên; phiên trước không còn quyền tác động |
| KT29 | Dùng biến `IQuanLy<SanPham>` tham chiếu `QuanLySanPham`, gọi `xuat`/`them`/`sua` | Chạy đúng override; dữ liệu hợp lệ được cập nhật, null/trùng mã/đổi mã bị từ chối; không thay đổi một phần khi có lỗi |
| KT30 | Truyền `TaiKhoan` vào `DanhSach<SanPham>`; thử thêm/xóa qua danh sách do `xuat()` trả về | Sai kiểu bị chặn khi biên dịch; sửa cấu trúc bản xuất báo `UnsupportedOperationException`, danh sách gốc giữ nguyên |
| KT31 | Gọi `xoa` qua interface trên sáu manager chung; gọi `sua` trên manager đơn/phiếu | Báo không hỗ trợ, không xóa lịch sử hoặc sửa chứng từ; khóa/ngừng/hủy vẫn qua hành vi chuyên biệt |
| KT32 | Gọi `them`/`sua`/`xoa` qua interface chi tiết khi đang soạn rồi sau khi đơn/phiếu nhận quyền sở hữu | Lúc soạn: gộp/thay/xóa đúng SKU, giữ kiểm tra tồn/giá/tràn số; sau khóa: cả ba bị chặn qua cả tham chiếu cũ, kể cả đơn chờ và dữ liệu nạp lại |
| KT33 | Kiểm tra bộ mẫu `data/du-lieu.txt` rồi nạp bằng bộ đọc khi triển khai | Có ít nhất 50 bản ghi, không tính `VNM`/`END`; bộ hiện tại có 60 bản ghi và `END\|60`; đủ tám bảng, xác thực được tài khoản mẫu, khóa ngoại/tiền/tồn/điểm khớp lịch sử |

Khi bắt đầu viết code, ưu tiên một self-check nhỏ cho công thức KT11–KT16 và vòng đọc/ghi KT24, thêm kiểm chứng KT21 cho xử lý lỗi lưu. Không chỉ kiểm tra menu chạy được; cần chứng minh tiền, điểm và tồn không bị lệch.

## 2. Thứ tự triển khai đề xuất

1. Tạo các enum, lớp thực thể, abstract class, interface `IQuanLy<T>` và lớp cha generic `DanhSach<T>`; kiểm chứng công thức khuyến mãi/điểm.
2. Tạo sáu lớp quản lý chung và hai lớp danh sách chi tiết kế thừa `DanhSach<T>` với kiểu tương ứng; kiểm tra tra cứu, trùng mã, đa hình qua interface và độc lập chi tiết.
3. Làm `LuuTruText`, tạo sáu manager một lần ở `Main` rồi truyền cùng tham chiếu cho lưu trữ/dịch vụ; kiểm tra lưu–đọc và file lỗi trước khi thêm giao dịch.
4. Thêm đăng nhập, phân quyền, danh mục và nhập hàng để có tồn hợp lệ.
5. Thêm đặt hàng, bán tại quầy, thanh toán, đổi/tích điểm; kiểm tra cập nhật đồng thời và chống ghi nhận lặp.
6. Thêm xem lịch sử, hóa đơn console và báo cáo; chạy các tình huống trong bảng với cả ba vai trò.

Không cần thêm database, GUI, framework hoặc lớp kế thừa riêng cho từng nhãn hàng trong phạm vi này. Generic chỉ dùng ở `IQuanLy<T>`/`DanhSach<T>`, chưa cần thêm tầng repository.
