# Danh sách chức năng

Ứng dụng phục vụ một cửa hàng, chạy trong một tiến trình Java 26, thao tác bằng console và đọc/ghi file text. Tài liệu này mô tả chức năng để triển khai; quyền chi tiết nằm trong [Tài khoản và phân quyền](02-tai-khoan-va-phan-quyen.md).

## 1. Danh mục chức năng

| Mã | Chức năng | Người thực hiện | Kết quả chính |
| --- | --- | --- | --- |
| HT-01 | Đọc hoặc khởi tạo dữ liệu | Hệ thống, người vận hành khi khởi tạo | Dữ liệu hợp lệ được nạp; có quản lý đầu tiên khi khởi tạo |
| TK-01 | Đăng ký khách hàng | Người chưa đăng nhập | Tài khoản khách hàng, điểm ban đầu bằng 0 |
| TK-02 | Đăng nhập, đăng xuất | Ba loại tài khoản | Phiên sử dụng với đúng quyền |
| TK-03 | Quản lý tài khoản | Quản lý | Tài khoản nội bộ mới hoặc thay đổi trạng thái hoạt động |
| TK-04 | Đổi mật khẩu | Ba loại tài khoản | Mật khẩu mới của chính mình được lưu |
| SP-01 | Xem, tìm sản phẩm và SKU | Ba loại tài khoản | Danh mục, giá, khuyến mãi và tồn có thể bán |
| SP-02 | Quản lý sản phẩm và SKU | Quản lý | Danh mục được cập nhật và lưu |
| DH-01 | Đặt hàng nhận tại cửa hàng | Khách hàng | Đơn `CHO_THANH_TOAN` thuộc khách hàng |
| DH-02 | Lập đơn tại quầy | Nhân viên, quản lý | Đơn `CHO_THANH_TOAN` cho thành viên hoặc khách vãng lai |
| DH-03 | Xem, tìm đơn hàng | Ba loại tài khoản theo quyền | Danh sách, chi tiết và hóa đơn đã chốt nếu đã thanh toán |
| DH-04 | Hủy đơn chưa thanh toán | Ba loại tài khoản theo quyền | Đơn chuyển từ `CHO_THANH_TOAN` sang `DA_HUY` |
| TT-01 | Thanh toán, xuất hóa đơn console và file text | Nhân viên, quản lý | Đơn `DA_THANH_TOAN`, tồn và điểm cập nhật đồng thời; file `hoa-don/HD-…txt` |
| DI-01 | Xem và sử dụng điểm | Khách hàng; nhân viên, quản lý phục vụ bán hàng | Điểm được hiển thị hoặc quy đổi khi thanh toán |
| KM-01 | Quản lý khuyến mãi | Quản lý | Khuyến mãi theo SKU được lưu |
| NH-01 | Nhập hàng, xem phiếu nhập | Quản lý | Phiếu nhập, tồn tăng và chi nhập được ghi nhận |
| BC-01 | Xem tổng kết tài chính và tồn | Quản lý | Báo cáo theo khoảng ngày, tồn hiện tại theo SKU |
| BC-02 | Thống kê bán hàng theo kỳ | Quản lý | Top SKU, theo nhóm hàng, theo người thu, theo ngày, tỉ lệ hủy |
| BC-03 | Nhập - xuất - tồn theo kỳ | Quản lý | Tồn đầu kỳ, nhập, xuất, tồn cuối kỳ từng SKU và đối chiếu lệch |

## 2. Quy ước xuyên suốt

- Một `SanPham` mô tả mặt hàng, một `BienTheSanPham` là SKU có quy cách đóng gói, giá và tồn riêng. Nhóm hàng gồm sữa, sữa chua, kem, nước giải khát và phô mai; kiểu đóng gói gồm bán lẻ, lốc và thùng.
- Số lượng trên đơn và phiếu nhập luôn là số đơn vị của SKU. Mua 2 thùng nghĩa là mua 2 đơn vị SKU thùng, không phải 2 hộp lẻ. Hệ thống không tự tách thùng thành lốc hoặc bán lẻ.
- Đơn chờ thanh toán giữ hàng khỏi tồn khả dụng, không trừ điểm và chưa tạo doanh thu. Hủy đơn hoàn lượng đã giữ; thanh toán không trừ khả dụng lần nữa. Giá, khuyến mãi và khả năng dùng điểm được kiểm tra lại tại quầy.
- Chỉ ghi nhận kết quả nghiệp vụ sau khi lưu thành công. Nếu việc lưu thất bại, không thông báo thành công và không giữ lại thay đổi dở dang trong bộ nhớ.
- Dữ liệu được lưu trong tám file text riêng theo loại bản ghi tại `data/`, có nhật ký phục hồi khi ghi theo bộ. Cách ánh xạ đối tượng thành các bảng nằm trong [Mô hình dữ liệu](05-mo-hinh-du-lieu.md); cách ghi an toàn nằm trong [Lưu trữ file text](07-luu-tru-file-text.md).

## 3. Hệ thống và tài khoản

### HT-01 — Đọc hoặc khởi tạo dữ liệu

1. Khi khởi động, phục hồi lần ghi bị gián đoạn nếu có rồi kiểm tra bộ tám file dữ liệu.
2. Nếu đã có dữ liệu, đọc đủ tám file, kiểm tra định dạng, mã định danh, liên kết và giá trị nghiệp vụ trước khi đưa vào danh sách dùng chung. Thiếu một file là lỗi; không nạp một phần.
3. Nếu chưa có file dữ liệu nào, thông báo và yêu cầu người vận hành xác nhận khởi tạo. Khi đồng ý, nhận thông tin quản lý đầu tiên và lưu dữ liệu ban đầu; không dùng mật khẩu mặc định.
4. Chỉ mở menu đăng nhập sau khi nạp hoặc khởi tạo thành công.

**Đầu ra:** Các danh sách quản lý sẵn sàng sử dụng. **Lỗi cần xử lý:** Không đọc được file, file rỗng/hỏng, mã trùng, tham chiếu không tồn tại, không có quản lý hoạt động hoặc không ghi được file khởi tạo. File đã tồn tại bị lỗi không được tự thay bằng dữ liệu rỗng. Nếu không xác nhận khởi tạo, thoát chương trình.

### TK-01 — Đăng ký khách hàng

1. Nhập tên đăng nhập, mật khẩu, họ tên và số điện thoại.
2. Kiểm tra dữ liệu và tính duy nhất của tên đăng nhập; tạo khách hàng đang hoạt động với điểm bằng 0.
3. Lưu thành công rồi thông báo mã tài khoản và hướng dẫn đăng nhập.

**Đầu ra:** Một tài khoản `KhachHang`. **Lỗi cần xử lý:** Trường bắt buộc trống, dữ liệu không hợp lệ, tên đăng nhập đã có hoặc lỗi lưu. Người đăng ký không được chọn loại tài khoản nội bộ.

### TK-02 — Đăng nhập, đăng xuất

1. Nhập tên đăng nhập và mật khẩu; kiểm tra xác thực và trạng thái hoạt động.
2. Hiển thị menu phù hợp với loại tài khoản.
3. Khi đăng xuất, kết thúc phiên và quay về màn hình đầu.

**Đầu ra:** Phiên làm việc có tài khoản hiện tại hoặc không còn phiên. **Lỗi cần xử lý:** Sai thông tin xác thực, tài khoản bị khóa, lựa chọn menu không hợp lệ hoặc gọi chức năng ngoài quyền.

### TK-03 — Quản lý tài khoản

1. Quản lý xem/tìm tài khoản hoặc chọn tạo tài khoản nhân viên hay quản lý.
2. Với tài khoản mới, nhập và kiểm tra thông tin. Với tài khoản đã có, chọn khóa hoặc mở lại.
3. Kiểm tra không khóa quản lý đang hoạt động cuối cùng, xác nhận và lưu thay đổi.

**Đầu ra:** Tài khoản mới hoặc trạng thái mới. **Lỗi cần xử lý:** Tên đăng nhập trùng, dữ liệu không hợp lệ, mã không tồn tại, thao tác làm mất quản lý hoạt động cuối cùng hoặc lỗi lưu. Không xóa tài khoản, không đổi loại sau khi tạo, không sửa trực tiếp điểm.

### TK-04 — Đổi mật khẩu

1. Người đang đăng nhập chọn đổi mật khẩu, nhập mật khẩu hiện tại (để trống = quay lại) rồi nhập mật khẩu mới hai lần.
2. Kiểm tra mật khẩu hiện tại đúng, mật khẩu mới đủ 6 ký tự và khác mật khẩu hiện tại.
3. Sinh salt mới, băm lại và lưu; phiên hiện tại tiếp tục, lần đăng nhập sau dùng mật khẩu mới.

**Đầu ra:** Thông tin xác thực mới của chính tài khoản đó; điểm, trạng thái và lịch sử giữ nguyên. **Lỗi cần xử lý:** Sai mật khẩu hiện tại, nhập lại không khớp, mật khẩu mới quá ngắn hoặc trùng mật khẩu cũ, lỗi lưu (mật khẩu cũ vẫn còn hiệu lực). Không ai được đổi mật khẩu của tài khoản khác, kể cả quản lý; chưa có chức năng cấp lại mật khẩu khi quên.

## 4. Sản phẩm và đóng gói

### SP-01 — Xem, tìm sản phẩm và SKU

1. Hiển thị hoặc tìm theo tên, mã sản phẩm, mã SKU, nhóm hàng và kiểu đóng gói.
2. Với mỗi SKU đang bán, hiển thị tên sản phẩm, quy cách, giá niêm yết, tồn, khuyến mãi hiện hành và giá sau khuyến mãi nếu có.
3. Đánh dấu SKU hết hàng; không cho chọn mua vượt tồn hiện tại.

**Đầu ra:** Danh mục dễ chọn đúng loại hàng và đúng cách đóng gói. Ví dụ cùng một loại sữa có SKU hộp lẻ 180 ml, SKU lốc 4 hộp và SKU thùng 48 hộp, mỗi SKU có giá và tồn độc lập. **Lỗi cần xử lý:** Không có kết quả phù hợp hoặc mã được nhập không tồn tại. Quản lý được xem thêm các mục ngừng bán để quản lý danh mục.

### SP-02 — Quản lý sản phẩm và SKU

1. Tạo hoặc sửa thông tin sản phẩm, chọn một trong năm nhóm hàng.
2. Tạo SKU thuộc sản phẩm với mã tự sinh, tên/quy cách, kiểu đóng gói, số đơn vị cơ sở trong gói, giá bán và trạng thái đang bán. Khi sửa, giữ nguyên mã và liên kết sản phẩm; không đổi cấu tạo quy cách thành hàng khác.
3. Xác nhận và lưu thay đổi. SKU mới có tồn bằng 0; muốn tăng tồn phải lập phiếu nhập.
4. Với hàng đã có lịch sử, sử dụng ngừng bán hoặc mở bán lại; không xóa dữ liệu làm đứt tham chiếu. Mục tạo nhầm, chưa có giao dịch thì xóa hẳn: SKU chưa nằm trên đơn/phiếu nhập và không còn khuyến mãi; sản phẩm không còn SKU nào. Thứ tự xóa: khuyến mãi → SKU → sản phẩm; vi phạm thì báo lý do và gợi ý ngừng bán. Không đổi một SKU đã có giao dịch thành hàng hóa hoặc quy cách khác; tạo SKU mới cho quy cách mới.

**Đầu ra:** Danh mục có thể dùng ngay cho bán hàng và nhập hàng. **Lỗi cần xử lý:** Mã trùng, sản phẩm cha không tồn tại, quy cách/giá không hợp lệ, dữ liệu bắt buộc thiếu hoặc lỗi lưu. Ngừng bán sản phẩm khiến các SKU của sản phẩm đó không được bán; lịch sử giao dịch vẫn giữ nguyên.

## 5. Đơn hàng và thanh toán

### DH-01 — Khách hàng đặt hàng

1. Khách hàng chọn SKU đang bán, nhập số lượng nguyên dương; có thể sửa số lượng hoặc bỏ dòng trước khi đặt.
2. Kiểm tra danh sách không rỗng và số lượng không vượt tồn hiện tại. SKU chọn nhiều lần được gộp vào cùng dòng.
3. Hiển thị giỏ và tổng tạm tính, kèm thông báo nhận và trả tiền tại cửa hàng; giỏ đang soạn chưa giữ hàng.
4. Khách hàng xác nhận, chương trình gắn mã khách hàng đang đăng nhập, giữ hàng khỏi tồn khả dụng và lưu đơn `CHO_THANH_TOAN` cùng một giao dịch.

**Đầu ra:** Mã đơn để tra cứu và thanh toán tại cửa hàng. **Lỗi cần xử lý:** SKU ngừng bán, hết/thiếu hàng, số lượng không hợp lệ, danh sách rỗng hoặc lỗi lưu. Không tạo đơn cho khách hàng khác bằng cách nhập mã tài khoản của họ.

### DH-02 — Lập đơn bán tại quầy

1. Nhân viên hoặc quản lý chọn khách thành viên bằng mã tài khoản, hoặc chọn khách vãng lai.
2. Chọn SKU, nhập số lượng và điều chỉnh các dòng trước khi xác nhận.
3. Kiểm tra tương tự DH-01 rồi giữ hàng và lưu đơn `CHO_THANH_TOAN` cùng một giao dịch. Với khách vãng lai, đơn không có mã khách hàng.
4. Chuyển sang TT-01 khi khách hàng muốn thanh toán.

**Đầu ra:** Đơn tại quầy được lưu và sẵn sàng thanh toán. **Lỗi cần xử lý:** Tài khoản được gắn không phải khách hàng đang hoạt động, SKU/số lượng không hợp lệ, thiếu tồn hoặc lỗi lưu. Một đơn chỉ gắn tối đa một khách hàng thành viên.

### DH-03 — Xem, tìm đơn và hóa đơn

1. Tìm theo mã đơn hoặc lọc trạng thái; nhân viên và quản lý có thể tra các đơn của cửa hàng.
2. Khách hàng chỉ nhận các đơn gắn mã tài khoản của mình.
3. Hiển thị dòng hàng, giá, khuyến mãi, điểm quy đổi, tổng tiền và trạng thái. Đơn chờ phải ghi rõ giá là tạm tính.
4. Với đơn đã thanh toán, hiển thị lại hóa đơn từ dữ liệu đã chốt, gồm người thu tiền và thời điểm thanh toán.

**Đầu ra:** Chi tiết đơn hoặc nội dung hóa đơn trên console. **Lỗi cần xử lý:** Mã đơn không tồn tại hoặc người gọi không có quyền. Đổi giá/tên sản phẩm hay khuyến mãi sau này không làm thay đổi hóa đơn cũ.

### DH-04 — Hủy đơn chờ thanh toán

1. Chọn đơn, kiểm tra quyền và trạng thái `CHO_THANH_TOAN`.
2. Xác nhận hủy, chuyển trạng thái thành `DA_HUY`, hoàn đủ lượng đã giữ về tồn khả dụng và lưu cùng một giao dịch.

**Đầu ra:** Đơn đã hủy vẫn được giữ trong lịch sử; hàng được đặt tiếp. **Lỗi cần xử lý:** Đơn đã thanh toán, đã hủy, không thuộc quyền hoặc lỗi lưu. Hủy lặp không hoàn tồn lần hai; điểm không đổi.

### TT-01 — Thanh toán tại cửa hàng

1. Nhân viên hoặc quản lý mở đơn `CHO_THANH_TOAN`.
2. Kiểm tra lại quyền, khách hàng gắn với đơn nếu có và tình trạng đang bán của từng SKU. Đơn dùng hàng đã giữ, vẫn thanh toán được khi tồn khả dụng bằng 0.
3. Tính lại giá hiện tại và chọn tối đa một khuyến mãi tốt nhất cho mỗi dòng. Nếu khách là thành viên, hỏi khách có dùng điểm không và dùng bao nhiêu; kiểm tra số điểm hiện có và giới hạn đổi điểm.
4. Hiển thị bảng tính cuối cùng: tiền hàng trước giảm, giảm khuyến mãi, giảm bằng điểm, số tiền cần trả, điểm sẽ trừ và điểm sẽ cộng. Yêu cầu khách xác nhận nếu giá hoặc ưu đãi khác đơn tạm tính.
5. Khi xác nhận thu tiền, ghi nhận người thu tiền, thời điểm thanh toán, các giá trị đã chốt; chuyển đơn thành `DA_THANH_TOAN`, xuất hàng đã giữ, trừ điểm dùng và cộng điểm mới trong cùng một lần lưu an toàn. Tồn khả dụng không bị trừ thêm.
6. Chỉ sau khi lưu thành công mới thông báo hoàn tất, hiển thị hóa đơn và tự lưu bản text vào `hoa-don/HD-<yyyyMMdd>-<mã ngắn>.txt` (UTF-8, rộng 50 ký tự, có đầu/chân hóa đơn). Lỗi ghi file hóa đơn chỉ được báo, không làm hỏng giao dịch đã lưu; nhân viên xuất lại bằng mục "Xuất hóa đơn ra file" trong menu bán hàng. Nếu lưu thất bại sau khi nhân viên đã nhận tiền thực tế, phải báo giao dịch chưa hoàn tất, đối chiếu khoản đã nhận và xử lý lại đúng đơn; không yêu cầu khách trả lần nữa.

**Đầu ra:** Hóa đơn console và trạng thái nhất quán giữa đơn, tồn, điểm. **Lỗi cần xử lý:** Đơn không còn chờ, hàng ngừng bán/thiếu tồn, khách hàng bị khóa, điểm không đủ hoặc vượt giới hạn, khách không đồng ý tổng mới, dữ liệu tính tiền không hợp lệ hoặc lỗi lưu.

Đơn `DA_THANH_TOAN` không được sửa, xóa, hủy hoặc thanh toán lần hai. Hóa đơn là cách trình bày đơn đã thanh toán, không cần tạo thêm một loại giao dịch độc lập. Quy tắc chống ghi nhận lặp và công thức tính tiền nằm trong [Quy tắc nghiệp vụ](06-quy-tac-nghiep-vu.md).

### DI-01 — Xem và sử dụng điểm

1. Khách hàng xem số điểm hiện có của mình. Nhân viên hoặc quản lý tra đúng thành viên để phục vụ bán hàng.
2. Khi thanh toán, khách hàng có thể chọn không dùng điểm hoặc đề nghị dùng một số điểm nguyên không âm.
3. Quy đổi 1 điểm thành 100 đồng; số điểm dùng không vượt số điểm đang có và tiền giảm bằng điểm không vượt 20% tiền hàng sau khuyến mãi.
4. Sau khi thanh toán được lưu thành công, cộng `floor(số tiền thực trả / 10.000)` điểm. Điểm mới của đơn này chỉ dùng được cho lần mua sau.

**Đầu ra:** Số điểm trước/sau và số tiền giảm được thể hiện trên hóa đơn. **Lỗi cần xử lý:** Điểm âm, số lẻ, không đủ điểm, vượt mức quy đổi, không có khách thành viên hoặc khách chưa đồng ý. Không tự dùng toàn bộ điểm của khách và không tích điểm cho đơn đang chờ hoặc đã hủy.

## 6. Khuyến mãi và nhập hàng

### KM-01 — Quản lý khuyến mãi

1. Quản lý chọn SKU và loại khuyến mãi: giảm theo phần trăm hoặc giảm số tiền trên một đơn vị SKU.
2. Nhập giá trị giảm, thời gian bắt đầu/kết thúc và trạng thái áp dụng.
3. Kiểm tra dữ liệu, xác nhận và lưu. Có thể cập nhật hoặc ngừng chương trình cho các lần thanh toán sau.
4. Chương trình tạo nhầm, chưa áp dụng cho đơn nào (kể cả đơn chờ, đơn hủy) thì xóa hẳn được; đã áp dụng thì chỉ ngừng.

**Đầu ra:** Chương trình khuyến mãi được áp dụng tự động khi đủ điều kiện. **Lỗi cần xử lý:** SKU không tồn tại, giá trị giảm không hợp lệ, thời gian kết thúc trước bắt đầu hoặc lỗi lưu. Nếu nhiều chương trình cùng hợp lệ, mỗi dòng chỉ nhận chương trình giảm nhiều nhất; giảm bằng điểm được tính sau bước này. Dữ liệu khuyến mãi trên đơn đã thanh toán không bị tính lại.

### NH-01 — Nhập hàng và xem phiếu nhập

1. Quản lý lập phiếu nhập, chọn SKU đã có, số lượng nguyên dương và đơn giá nhập cho từng dòng.
2. Kiểm tra phiếu không rỗng, các mã hợp lệ và giá trị tiền hợp lệ. Mỗi SKU chỉ xuất hiện một dòng; khi chọn lại SKU, cộng lượng vào dòng đã có. Nếu giá nhập khác, yêu cầu chọn một đơn giá cho toàn dòng hoặc lập phiếu khác.
3. Hiển thị chi tiết và tổng tiền nhập; người quản lý xác nhận việc nhận hàng và chi trả.
4. Lưu phiếu nhập và tăng tồn đúng SKU trong cùng một lần lưu an toàn. Mọi phiếu nhập trong phạm vi đồ án được xem là đã trả đủ tiền khi ghi nhận.
5. Tra cứu lịch sử hoặc chi tiết phiếu đã lưu khi cần. Phiếu đã ghi nhận không được lưu lần hai để tăng tồn lặp, sửa hoặc xóa trực tiếp.

**Đầu ra:** Tồn tăng, phiếu nhập được lưu và chi nhập hàng được ghi nhận. **Lỗi cần xử lý:** SKU không tồn tại, số lượng/đơn giá không hợp lệ, phiếu rỗng, tràn giới hạn số hoặc lỗi lưu. Chức năng này không quản lý công nợ nhà cung cấp, lô hàng hay hạn sử dụng.

## 7. Báo cáo

### BC-01 — Tổng kết tài chính và tồn kho

1. Quản lý nhập khoảng ngày cần xem; ngày bắt đầu và kết thúc đều được tính trong kỳ.
2. Lấy đơn `DA_THANH_TOAN` có thời điểm thanh toán trong kỳ và phiếu nhập đã ghi nhận trong kỳ. Không dùng ngày đặt đơn để ghi nhận doanh thu.
3. Tổng hợp các chỉ tiêu từ giá trị đã lưu của giao dịch:

| Chỉ tiêu | Cách tính |
| --- | --- |
| Doanh thu gộp | Tổng tiền hàng trước giảm trên đơn đã thanh toán |
| Giảm do khuyến mãi | Tổng tiền giảm khuyến mãi của các đơn trong kỳ |
| Giảm do điểm | Tổng tiền quy đổi điểm của các đơn trong kỳ |
| Thu bán hàng ròng | Doanh thu gộp − giảm khuyến mãi − giảm bằng điểm |
| Chi nhập hàng | Tổng thành tiền các phiếu nhập đã ghi nhận trong kỳ |
| Chênh lệch thu − chi nhập | Thu bán hàng ròng − chi nhập hàng |
| Số đơn đã thanh toán | Số đơn hợp lệ trong kỳ |

4. Có lựa chọn xem tồn hiện tại theo SKU, tách khỏi báo cáo kỳ để tránh hiểu nhầm đó là tồn tại ngày cuối kỳ. SKU đang bán có tồn ≤ 10 (ngưỡng cố định của đồ án) được đánh dấu "Sắp hết", tồn 0 là "HẾT HÀNG"; SKU ngừng bán không bị đánh dấu.
5. Mỗi lần quản lý đăng nhập, nếu có SKU đang bán tồn ≤ 10 thì in danh sách cảnh báo, ít tồn nhất trước, để lập phiếu nhập (NH-01).

**Đầu ra:** Báo cáo console có khoảng ngày, đơn vị tiền và các tổng số. **Lỗi cần xử lý:** Ngày không hợp lệ, ngày kết thúc trước ngày bắt đầu; kỳ không có giao dịch hiển thị các tổng bằng 0.

Chênh lệch thu và chi nhập không phải lợi nhuận: hàng nhập trong kỳ có thể chưa bán, hàng bán có thể được nhập từ kỳ trước. Phiên bản này chưa tính giá vốn, chi phí vận hành, thuế, hoàn trả hoặc vận chuyển. Các giới hạn chung được ghi trong [Mô tả ứng dụng](01-mo-ta-ung-dung.md).

### BC-02 — Thống kê bán hàng theo kỳ

1. Quản lý nhập khoảng ngày như BC-01; chỉ dùng đơn `DA_THANH_TOAN` theo ngày thanh toán và giá trị đã chốt trên đơn.
2. Hiển thị số đơn, thu ròng, giá trị trung bình mỗi đơn; số đơn tạo trong kỳ (theo ngày tạo) và số đơn trong đó đã hủy.
3. Top 5 SKU theo doanh thu, doanh thu theo nhóm hàng: tiền sau khuyến mãi, **trước** giảm điểm, vì điểm giảm trên cả đơn và không chia về từng dòng. Tổng theo nhóm hàng bằng doanh thu gộp − giảm khuyến mãi của BC-01.
4. Theo người thu tiền và theo ngày thanh toán: số tiền khách thực trả; tổng bằng thu bán hàng ròng của BC-01. Mỗi bảng kèm thanh ngang tỉ lệ với số tiền.

**Lỗi cần xử lý:** như BC-01. Kỳ không có đơn đã thanh toán chỉ hiển thị các số đếm.

### BC-03 — Nhập - xuất - tồn theo kỳ

1. Quản lý nhập khoảng ngày. Với mỗi SKU: nhập = tổng số lượng trên phiếu nhập trong kỳ (theo ngày nhập); xuất = tổng số lượng trên đơn `DA_THANH_TOAN` trong kỳ (theo ngày thanh toán).
2. Tồn cuối kỳ = tồn thực tế hiện tại − nhập sau kỳ + xuất sau kỳ; tồn đầu kỳ = tồn cuối kỳ − nhập trong kỳ + xuất trong kỳ. Tồn thực tế bao gồm hàng đang giữ cho đơn chờ; màn hình danh mục/tồn hiện tại hiển thị tồn khả dụng.
3. Đối chiếu: nếu tồn đang lưu khác tổng nhập − tổng bán mọi thời điểm, đánh dấu SKU đó là lệch và in cảnh báo.

**Lỗi cần xử lý:** như BC-01. Đơn chờ và đơn hủy không làm thay đổi xuất bán; chỉ thay đổi lượng hàng đang giữ.
