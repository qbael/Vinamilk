# 07. Lưu trữ file text

Dữ liệu được tách thành tám file text UTF-8, mỗi loại bản ghi một file. Thứ tự cột và liên kết giữa các bảng được quy định tại [Mô hình dữ liệu](05-mo-hinh-du-lieu.md).

## 1. Tổ chức file text

| File trong `data/` | Loại bản ghi | Số bản ghi mẫu |
| --- | --- | ---: |
| [tai-khoan.txt](../data/tai-khoan.txt) | `TAI_KHOAN` | 50 |
| [san-pham.txt](../data/san-pham.txt) | `SAN_PHAM` | 50 |
| [bien-the.txt](../data/bien-the.txt) | `BIEN_THE` | 50 |
| [khuyen-mai.txt](../data/khuyen-mai.txt) | `KHUYEN_MAI` | 50 |
| [phieu-nhap.txt](../data/phieu-nhap.txt) | `PHIEU_NHAP` | 50 |
| [chi-tiet-phieu-nhap.txt](../data/chi-tiet-phieu-nhap.txt) | `CT_PHIEU_NHAP` | 50 |
| [don-hang.txt](../data/don-hang.txt) | `DON_HANG` | 50 |
| [chi-tiet-don-hang.txt](../data/chi-tiet-don-hang.txt) | `CT_DON_HANG` | 50 |
| **Tổng** | **8 loại** | **400** |

Các lớp con tài khoản dùng chung file `tai-khoan.txt`, phân biệt bằng `vaiTro`; hai lớp khuyến mãi dùng chung file `khuyen-mai.txt`, phân biệt bằng `loaiKhuyenMai`. Enum, interface và lớp quản lý không có file dữ liệu riêng. Tồn và điểm vẫn là thuộc tính trong bản ghi SKU/tài khoản, được đối chiếu với chứng từ; không tạo thêm bảng tồn/điểm/doanh thu.

Quy ước định dạng cho **từng file**:

1. UTF-8, mỗi bản ghi một dòng, cột phân cách bằng `|`.
2. Dòng đầu chính xác `VNM|1`. Dòng cuối `END|N`, trong đó N là số bản ghi trong chính file đó, không tính hai dòng đầu/cuối. Mỗi file mẫu có 52 dòng và kết thúc bằng `END|50`.
3. File chỉ chứa đúng loại bản ghi đã quy định; không có dòng tiêu đề cột hoặc dòng trống ở giữa. Chấp nhận newline cuối file.
4. `true`/`false` cho boolean; số không có dấu phân cách hàng nghìn; ngày `yyyy-MM-dd`, thời điểm `yyyy-MM-dd'T'HH:mm:ss`.
5. Trường tùy chọn trống biểu diễn null; không ghi chuỗi `null`. Đọc bằng `split("\\|", -1)` để giữ cột trống cuối dòng.
6. Trường văn bản không chứa `|`, `\r`, `\n`; báo lỗi và yêu cầu nhập lại. Tên tiếng Việt, dấu phẩy và dấu nháy được giữ bình thường.
7. Kiểm tra số cột, enum, miền giá trị, ID duy nhất, đủ dòng kết thúc và đúng số bản ghi; không bỏ qua dòng lỗi.

Ví dụ một dòng trong `san-pham.txt` và một dòng trong `bien-the.txt` (chỉ minh họa, chưa đủ để nạp hệ thống):

```text
SAN_PHAM|SP001|Sữa tươi có đường|Vinamilk|hộp|180 ml||SUA|true
BIEN_THE|BT002|SP001|Lốc 4 hộp 180 ml|LOC|4|30000|20|true
```

Bộ mẫu dùng UUID. Mã đối tượng được giữ nguyên khi đọc; tồn đầu phải có phiếu nhập hỗ trợ và điểm phải khớp các đơn đã thanh toán.

## 2. Quy trình đọc

1. `Main` truyền đường dẫn **thư mục** `data/` cùng sáu đối tượng quản lý cho `LuuTruText`. Nếu có nhật ký phục hồi `.phuc-hoi.txt`, khôi phục bộ dữ liệu trước lần ghi bị gián đoạn trước khi kiểm tra/nạp.
2. Chỉ cho khởi tạo khi chưa có file dữ liệu nào. Nếu đã có một phần trong tám file, thiếu file còn lại là lỗi, không được khởi tạo đè. File rỗng/hỏng/không đọc được cũng phải báo lỗi và giữ nguyên.
3. Đọc đủ tám file, kiểm tra phần đầu/cuối, số dòng và loại bản ghi từng file. Lỗi định dạng chỉ rõ tên file và dòng tương ứng.
4. Phân tích vào vùng **tạm**: tạo đúng lớp con, ghép chi tiết theo mã đơn/phiếu; phát hiện chi tiết mồ côi, trùng dòng hoặc chứng từ rỗng.
5. Kiểm tra khóa ngoại, vai trò, công thức tiền, điểm, tồn và ít nhất một quản lý đang hoạt động. Tài khoản đã khóa vẫn được giữ để đọc lịch sử.
6. Chỉ khi toàn bộ dữ liệu hợp lệ mới gọi `napDanhSach(...)` (để `public` vì các lớp quản lý nằm ở package `cuahang.danhsach`) trên cả sáu đối tượng quản lý. Giữ nguyên các đối tượng quản lý dùng chung, chỉ thay nội dung danh sách; không nạp dở nếu một file lỗi.

`xuatThanhChuoi()` và `napTuChuoi(...)` vẫn dùng biểu diễn gộp trong RAM để phục hồi giao dịch. Đây không phải file dữ liệu chính. Nạp và phục hồi không gọi `them(...)` dành cho tạo mới, vì tồn/điểm lịch sử phải được giữ nguyên sau khi kiểm tra.

Đối chiếu bắt buộc:

```text
tonThucTeMoiSKU = tổng lượng nhập SKU − tổng lượng SKU trong các đơn DA_THANH_TOAN
tonKhaDungMoiSKU = tonThucTeMoiSKU − tổng lượng SKU trong các đơn CHO_THANH_TOAN
diemKhach = tổng diemCong − tổng diemSuDung trên các đơn DA_THANH_TOAN của khách
```

SKU và khách hàng mới bắt đầu ở 0. File vẫn lưu tồn thực tế; sau khi kiểm tra chứng từ, nạp lại lượng giữ từ đơn chờ trước khi công bố dữ liệu. Dữ liệu cũ không cần chuyển định dạng; khởi động lại hoặc phục hồi giao dịch không giữ trùng. Nếu đối chiếu lệch hoặc đơn chờ giữ vượt tồn thực tế, báo lỗi thay vì tự sửa dữ liệu.

## 3. Quy trình ghi và nhất quán RAM–file

Áp dụng sau mọi thay đổi nghiệp vụ, không đợi thoát chương trình:

1. Dịch vụ kiểm tra quyền/điều kiện và giữ bản text trạng thái cũ trong RAM.
2. Cập nhật RAM, xuất và kiểm tra toàn bộ trạng thái mới.
3. Chuẩn bị đủ tám file `<tên-file>.tmp` trong `data/`; chưa thay file dữ liệu chính.
4. Ghi trạng thái cũ vào `.phuc-hoi.tmp`, rồi đổi tên nguyên tử thành `.phuc-hoi.txt`. Khi khởi tạo lần đầu, nhật ký dùng dấu `MOI` để biết cần xóa bộ file đang tạo nếu lưu thất bại.
5. Thay lần lượt tám file bằng `Files.move(..., ATOMIC_MOVE)`. Chỉ xóa `.phuc-hoi.txt` sau khi thay đủ cả bộ; lúc đó mới coi giao dịch lưu thành công.
6. Nếu ghi thất bại, dùng nhật ký khôi phục bộ file cũ và dịch vụ phục hồi RAM từ bản chụp. Nếu không phục hồi được, dừng ứng dụng và giữ nhật ký để xử lý; không tiếp tục với dữ liệu chưa nhất quán.
7. Nếu chương trình bị dừng trong khi nhật ký còn tồn tại, lần khởi động sau khôi phục bộ cũ trước khi nạp. File `.tmp` đơn lẻ không được dùng thay dữ liệu chính.

Lỗi xuất hóa đơn sau khi lưu thành công không làm hoàn tác giao dịch hoặc thu tiền lần nữa. Phiên đăng nhập giữ mã tài khoản và tra lại sau phục hồi vì phần tử trong danh sách có thể đã được dựng lại.

Chỉ chạy một tiến trình với thư mục dữ liệu này. Cơ chế phục hồi xử lý lần ghi bị gián đoạn; chưa có `fsync` nên không cam kết chống mất điện hoặc lỗi ổ đĩa. Không sửa tay các file khi ứng dụng đang chạy; sao lưu cả tám file cùng lúc khi ứng dụng đã đóng.

## 4. Bộ dữ liệu mẫu 50 bản ghi mỗi loại

Bộ mẫu có đúng **50 bản ghi mỗi file, tổng 400 bản ghi**, không tính dòng `VNM|1` và `END|50`. Con số 50 là kích thước bộ mẫu; ứng dụng vẫn cho thêm dữ liệu và lưu đúng `END|N` theo số lượng hiện tại.

Bộ mẫu gồm 1 quản lý, 1 nhân viên và 48 khách hàng; đủ năm nhóm hàng, ba kiểu đóng gói, hai loại khuyến mãi và ba trạng thái đơn. Tám đơn tháng 10 giữ nguyên lịch sử và kết quả tiền/điểm/tồn. Mười lăm dòng nhập ngày 01/10 được chia thành 15 phiếu, mỗi phiếu một dòng; thêm 35 phiếu và 42 đơn trong tháng 11. Vì vậy mỗi phiếu/đơn đều có một dòng chi tiết và báo cáo tháng 10 vẫn giữ kết quả cũ. Ngày và giá chỉ dùng minh họa đồ án.

| Tên đăng nhập mẫu | Vai trò | Điểm hiện có |
| --- | --- | ---: |
| `demo.quanly` | Quản lý cửa hàng | Không áp dụng |
| `demo.nhanvien` | Nhân viên bán hàng | Không áp dụng |
| `demo.khach01` | Khách hàng | 2 |
| `demo.khach02` | Khách hàng | 6 |
| `demo.khach03` | Khách hàng | 6 |
| `demo.khach04`…`demo.khach48` | Khách hàng | Theo đơn đã thanh toán |

Mật khẩu thực hành là `VnmDemo!2026`. File lưu PBKDF2-HMAC-SHA256 với 600000 vòng, salt riêng 16 byte và hash 32 byte, mã hóa Base64. Khi khởi tạo hệ thống mới, người vận hành tự nhập tài khoản quản lý đầu tiên.

Khi đổi bộ mẫu, giữ đủ 50 bản ghi từng loại, cập nhật `END|N` từng file và kiểm tra khóa ngoại, tiền, tồn, điểm. `KiemThu` kiểm tra số lượng và nạp bộ mẫu bằng chính `LuuTruText`.
