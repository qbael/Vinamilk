# 07. Lưu trữ file text

Tài liệu này mô tả cách lưu toàn hệ thống trong một file text. Thứ tự cột và liên kết giữa các bảng được quy định riêng tại [Mô hình dữ liệu](05-mo-hinh-du-lieu.md).

## 1. Tổ chức file text

```text
data/
    du-lieu.txt       File chính chứa toàn bộ các bảng
    du-lieu.tmp       File tạm trong lúc ghi; không phải nguồn dữ liệu khi khởi động
```

Một file chứa nhiều bảng giúp một lần thay file cập nhật đồng thời đơn, tồn và điểm. Không tạo riêng `diem.txt`, `ton-kho.txt` hoặc `doanh-thu.txt` vì dễ lưu được bảng này nhưng mất bảng kia. Các file tài liệu vẫn tách riêng theo chủ đề.

Quy ước định dạng:

1. UTF-8, mỗi bản ghi một dòng, cột phân cách bằng `|`.
2. Dòng đầu chính xác `VNM|1`. Dòng cuối `END|N`, trong đó N là số bản ghi dữ liệu, không tính hai dòng đầu/cuối.
3. Không có dòng tiêu đề cột trong dữ liệu thật; không có dòng trống ở giữa. Chấp nhận kết thúc dòng cuối bằng newline.
4. `true`/`false` cho boolean; số không có dấu phân cách hàng nghìn; ngày `yyyy-MM-dd`, thời điểm `yyyy-MM-dd'T'HH:mm:ss`.
5. Trường tùy chọn trống biểu diễn null; không ghi chuỗi `null`. Đọc bằng `split("\\|", -1)` để giữ cột trống cuối dòng.
6. Trường văn bản không chứa `|`, `\r`, `\n`; báo lỗi và yêu cầu nhập lại. Phiên bản nhỏ này chưa cần cơ chế escape; tên tiếng Việt, dấu phẩy và dấu nháy được giữ bình thường.
7. Kiểm tra số cột chính xác, enum hợp lệ, số trong miền giá trị, ID duy nhất, đủ dòng kết thúc và đúng số bản ghi. Không bỏ qua dòng lỗi.

Ví dụ hai bản ghi dữ liệu danh mục (chỉ minh họa từng dòng, **không phải file đầy đủ để nạp**):

```text
SAN_PHAM|SP001|Sữa tươi có đường|Vinamilk|hộp|180 ml||SUA|true
BIEN_THE|BT002|SP001|Lốc 4 hộp 180 ml|LOC|4|30000|20|true
```

Khi triển khai, mã thật dùng UUID; cần có tài khoản quản lý, phiếu nhập tạo ra tồn đầu và các bản ghi liên quan thì file mới hợp lệ. Không phát sinh lại mã cho đối tượng cũ khi đọc file.

## 2. Quy trình đọc

1. Xác định cố định đường dẫn dữ liệu của dự án; không lặng lẽ đọc một file khác do thay thư mục chạy.
2. Nếu chưa có file, chỉ khởi tạo sau xác nhận và tạo quản lý đầu tiên. Nếu file có nhưng rỗng/lỗi/không đọc được, dừng và báo nguyên nhân; giữ nguyên file.
3. Đọc và phân tích vào các danh sách **tạm**, chưa thay danh sách instance trong sáu đối tượng quản lý đang sử dụng. `Main` truyền cùng sáu đối tượng này cho `LuuTruText` và `DichVuCuaHang`.
4. Tạo đúng lớp con theo `vaiTro` hoặc `loaiKhuyenMai`. Ghép chi tiết vào đơn/phiếu đúng mã; phát hiện chi tiết mồ côi, trùng dòng hoặc chứng từ rỗng.
5. Kiểm tra khóa ngoại và vai trò của các bên, công thức tiền, điểm, tồn và ít nhất một quản lý đang hoạt động. Tài khoản từng lập chứng từ có thể hiện đã bị khóa; vẫn phải giữ và đọc được lịch sử.
6. Chỉ khi toàn bộ đồ thị dữ liệu hợp lệ mới gọi `napDanhSach(List<T> duLieuDaKiemTra)` có phạm vi package-private trên cả sáu đối tượng quản lý. Dùng `clear`/`addAll` trên `protected final List<T> danhSach` của lớp cha `DanhSach<T>`; giữ nguyên các đối tượng quản lý và tham chiếu danh sách, chỉ thay nội dung. Danh sách chi tiết thuộc từng đơn/phiếu được dựng và ghép trong vùng tạm trước khi công bố.

Nạp file và phục hồi RAM đều dùng luồng nạp nội bộ trên, không gọi `them(...)` dành cho tạo mới. Tồn, điểm và trạng thái lịch sử đã đối chiếu hợp lệ phải được khôi phục nguyên giá trị; quy tắc tồn/điểm ban đầu bằng 0 chỉ áp dụng khi tạo tài khoản/SKU mới.

Kiểm tra đối chiếu bắt buộc trong phạm vi chưa có trả hàng/điều chỉnh thủ công:

```text
tonMoiSKU = tổng lượng nhập SKU − tổng lượng SKU trong các đơn DA_THANH_TOAN
diemKhach = tổng diemCong − tổng diemSuDung trên các đơn DA_THANH_TOAN của khách
```

SKU và tài khoản mới đều bắt đầu ở 0; tồn đầu kỳ phải được tạo bằng phiếu nhập, điểm ban đầu không tặng thêm. Nếu tổng đối chiếu lệch giá trị lưu, báo lỗi dữ liệu thay vì tự sửa âm thầm. Chưa cần bảng sổ kho/sổ điểm riêng vì đã có chứng từ gốc.

## 3. Quy trình ghi và nhất quán RAM–file

Áp dụng cho mọi thao tác thay đổi dữ liệu, kể cả đăng ký, sửa giá, hủy đơn, nhập hàng và thanh toán; không đợi thoát chương trình mới lưu.

1. Dịch vụ kiểm tra quyền và điều kiện, hiển thị nội dung cuối cho người dùng xác nhận.
2. Giữ bản text của trạng thái hiện tại trong RAM để có thể phục hồi. `LuuTruText` đọc `xuat()` từ các đối tượng quản lý chung và các chi tiết thuộc từng chứng từ để định dạng bản text này. Không in hoặc ghi bản này ra log vì có dữ liệu tài khoản.
3. Cập nhật các đối tượng trong RAM theo nghiệp vụ; kiểm tra tính hợp lệ của trạng thái dự kiến. Trong giai đoạn này không nhận thao tác khác.
4. Ghi toàn bộ trạng thái mới vào `du-lieu.tmp` cùng thư mục/file system với file chính; đóng writer thành công. Không ghi đè trực tiếp file chính.
5. Thay file chính bằng `Files.move` với yêu cầu `ATOMIC_MOVE`. **Chỉ khi move thành công mới coi giao dịch được lưu.** Việc thay thế đích đang tồn tại phụ thuộc file system; cần kiểm chứng trên môi trường chạy. Nếu không hỗ trợ thay thế nguyên tử, báo lỗi và giữ file cũ, không tự hạ xuống cách ghi thiếu an toàn. [Java 26 — Files.move](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)).
6. Nếu thất bại trước bước commit, phân tích và kiểm tra toàn bộ bản cũ rồi phục hồi nội dung vào đúng sáu đối tượng quản lý qua `napDanhSach`, giữ nguyên danh tính của chúng; bỏ tham chiếu phần tử đang thao tác và tra lại theo mã, báo chưa hoàn tất. Nếu phục hồi cũng lỗi, dừng ứng dụng, không tiếp tục giao dịch với RAM chưa chắc đúng.
7. Sau commit, giữ RAM mới và mới hiển thị thành công. Lỗi in hóa đơn sau commit không được rollback hoặc thu tiền lần nữa; tra mã đơn và hiển thị lại.

Phiên đăng nhập nên giữ mã tài khoản hoặc tra lại tài khoản theo mã sau phục hồi, vì đối tượng cũ có thể đã được thay bằng đối tượng mới. Nếu tiến trình dừng sau commit nhưng trước khi in, lần mở lại file đã có trạng thái hoàn tất; thao tác thanh toán lặp bị chặn theo trạng thái đơn.

File tạm còn sót không được tự nạp thay file chính. Chỉ chạy một tiến trình với thư mục dữ liệu này; mô hình không giải quyết ghi đồng thời giữa nhiều JVM. Thay file nguyên tử hạn chế file bị ghi dở, không phải cam kết chống mọi lỗi ổ đĩa/mất điện. Sao lưu file đã đóng sau buổi thực hành; không sửa dữ liệu bằng tay khi chương trình đang chạy.
