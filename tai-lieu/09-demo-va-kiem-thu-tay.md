# 09. Hướng dẫn demo và kiểm thử tay

Tài liệu dùng khi trình bày đồ án: hiểu luồng chạy, demo đủ các yêu cầu của đề, trả lời câu hỏi, và tự test tay toàn bộ app trước buổi chấm.

## 0. Chuẩn bị (làm trước buổi demo)

```bash
git restore data/          # về đúng bộ dữ liệu mẫu
rm -rf out hoa-don
javac -d out $(find src -name '*.java')
java -cp out cuahang.Main
```

- Mật khẩu mọi tài khoản mẫu: `VnmDemo!2026`.
- Tài khoản: `demo.quanly` (quản lý), `demo.nhanvien` (nhân viên), `demo.khach01` (2 điểm, SĐT 0900000003), `demo.khach02` (6 điểm, 0900000004), `demo.khach03` (6 điểm, 0900000005).
- Ở mọi ô chọn mã, gõ **8 ký tự đầu của mã** in ở cột "Mã" (hoặc ngắn hơn nếu không trùng). Ô tìm SKU nhận cả tên: gõ đủ cụ thể để chỉ khớp 1 SKU thì app tự chọn.
- Để trống rồi Enter ở ô nhập = quay lại. Câu hỏi xác nhận trả lời `c` hoặc `k`.
- Một số mã mẫu hay dùng:

| Mã | Là gì |
| --- | --- |
| `f9f193ce` | SKU Kem vani – 1 ly 100 ml, 12.000đ, chưa có khuyến mãi |
| `6c910bdf` | SKU Sữa tươi – Lốc 4 hộp 180 ml, 30.000đ |
| `a8f710f9` | Sản phẩm Kem vani |
| `bc4ebe22` | Đơn chờ thanh toán của demo.khach02 (1 thùng sữa, 340.000đ) |
| `64ee1592` | Đơn chờ thanh toán của demo.khach03 (108.000đ) |
| `84736ab2` | Đơn đã thanh toán ngày 02/10/2026 (để xem/xuất lại hóa đơn) |
| `6828eb46` | Khuyến mãi đã dùng trên đơn (để demo "không xóa được") |

Kiểm thử tự động (chạy trước khi demo để chắc mọi thứ còn đúng):

```bash
javac -d /tmp/vnm $(find src test -name '*.java') && java -cp /tmp/vnm cuahang.KiemThu
```

```bash
bash test/e2e.sh
```

## 1. Luồng của app

```text
Main ─ tạo 6 danh sách QuanLy... + LuuTruText + DichVuCuaHang ─ đọc đủ 8 file text trong data/
  └─ GiaoDienConsole.chay()
       ├─ Đăng ký (chỉ tạo khách hàng) / Đăng nhập
       └─ Menu theo vai trò (đa hình: tk.layVaiTro())
            ├─ Khách hàng : xem/tìm hàng → đặt đơn (CHO_THANH_TOAN) → xem đơn, hủy đơn, xem điểm
            ├─ Nhân viên  : lập đơn tại quầy → thanh toán (dùng điểm) → hóa đơn console + file
            └─ Quản lý    : bán hàng + sản phẩm/SKU + khuyến mãi + nhập hàng + tài khoản + báo cáo
```

Vòng đời một đơn: **khách đặt** (giá tạm tính, giữ ngay tồn khả dụng) → **nhân viên thanh toán** (tính lại giá, chọn khuyến mãi tốt nhất, hỏi dùng điểm) → **lưu 1 lần**: đơn `DA_THANH_TOAN` + xuất hàng đã giữ + trừ/cộng điểm → **hóa đơn** console và `hoa-don/HD-…txt` → **báo cáo** thu-chi, thống kê, nhập - xuất - tồn. Đơn chờ có thể hủy (`DA_HUY`) để hoàn tồn khả dụng; thanh toán không trừ khả dụng lần nữa. Đơn đã thanh toán không sửa/xóa.

Mọi thao tác ghi đều đi qua `DichVuCuaHang.giaoDich`: chụp trạng thái → đổi dữ liệu → chuẩn bị tám file tạm, ghi nhật ký rồi thay bộ file (`LuuTruText.ghiAnToan`). Lưu lỗi thì phục hồi, không báo thành công.

## 2. Kịch bản demo cho cô (~12 phút)

Mỗi bước ghi: **gõ gì** → **thấy gì** → **ý cần nói**. Làm theo thứ tự để dữ liệu nối tiếp nhau.

### 2.1. Đọc file và đăng ký (thêm)
1. Chạy app → dòng đầu in `Thư mục dữ liệu: …/data`. Mở các file cho cô xem: mỗi loại có file riêng, mỗi file mẫu 50 bản ghi; mỗi dòng một bản ghi `LOAI|cột|cột…`, cuối có `END|số dòng`. *Ý: đọc/ghi file text, không dùng database.*
2. `2` Đăng ký: `demo.moi` / `matkhau1` / `matkhau1` / `Nguyễn Văn Demo` / `0912345678` → "Đăng ký thành công". *Ý: thêm tài khoản; chỉ tạo được khách hàng, điểm 0.*

### 2.2. Khách hàng: tìm kiếm, thêm/sửa/xóa dòng giỏ, đặt đơn
1. `1` đăng nhập `demo.moi` / `matkhau1`.
2. `1` Xem/tìm: gõ `sữa chua` → chỉ hiện các SKU khớp sữa chua. *Ý: tìm theo tên, nhóm, quy cách, mã.*
3. `2` Đặt hàng:
   - `1` thêm, tìm `lốc 4 hũ` → tự chọn → số lượng `3`.
   - `1` thêm, tìm `1 hộp 180` → `2`.
   - `2` sửa số lượng: STT `2` → `5`. *Ý: sửa dòng thứ hai trong giỏ; Enter để quay lại.*
   - `3` bỏ dòng: STT `1` → dòng biến mất, giỏ đánh số lại từ `1`. *Ý: xóa (dòng trong giỏ).*
   - `4` xác nhận → `c` → "Đặt hàng thành công. Mã đơn: xxxxxxxx". Ghi lại mã đơn.
4. `3` Xem đơn của tôi → thấy "Chờ thanh toán". `6` Đổi mật khẩu (tùy thời gian). `0` đăng xuất.

### 2.3. Nhân viên: thanh toán, dùng điểm, hóa đơn file
1. Đăng nhập `demo.nhanvien`.
2. `4` Thanh toán → nhập mã đơn vừa đặt → xem bảng tính → `c` → in **HÓA ĐƠN** và dòng `Đã lưu hóa đơn: …/hoa-don/HD-….txt`. Mở file cho cô xem.
3. `2` Lập đơn tại quầy → `1` khách thành viên → `0900000005` → `1` thêm, tìm `f9f1` → `2` → `4` → `c` lưu → `c` thanh toán ngay → dùng `6` điểm → `c` đồng ý → `c` thu tiền → "Giảm bằng điểm (6 điểm) -600đ". *Ý: 1 điểm = 100đ, tối đa 20% tiền sau KM; điểm cộng = tiền trả / 10.000.*
4. `3` Xem/tìm đơn → `3` "Đã thanh toán" → nhập mã để xem lại hóa đơn. *Ý: tìm/lọc đơn theo trạng thái.*
5. `5` Hủy đơn chờ → `64ee` → `c`. *Ý: đơn hủy vẫn giữ lịch sử và hoàn đủ lượng hàng đã giữ về tồn khả dụng.*

### 2.4. Quản lý: đủ thêm, sửa, xóa, tìm trên danh mục
1. Đăng nhập `demo.quanly`. Nếu có SKU tồn ≤ 10 sẽ thấy cảnh báo ngay dưới lời chào.
2. `2` Sản phẩm và SKU:
   - `3` thêm sản phẩm: `Sữa demo` / `Vinamilk` / `hộp` / `110 ml` / (Enter) / `1` → "Đã thêm sản phẩm xxxxxxxx". Ghi lại mã này.
   - `2` danh sách sản phẩm → thấy "Sữa demo". *Ý: xem.*
   - `5` thêm SKU: mã sản phẩm vừa ghi / `Lốc 4 hộp 110 ml` / `2` (lốc) / `4` / `24000` → "Đã thêm SKU".
   - `6` sửa giá: tìm `110 ml` → `25000` → `c` → "Đã cập nhật SKU". *Ý: sửa.*
   - `1` danh sách/tìm SKU: gõ `110 ml`. *Ý: tìm.*
3. `0`, rồi `3` Khuyến mãi: `2` thêm → tìm `110 ml` → `1` (phần trăm) → `KM demo` → `10` → `01/01/2026` → `31/12/2099`. Sau đó `5` xóa → mã `6828` → `c` → **bị chặn**: "Khuyến mãi đã được áp dụng trên đơn hàng…". *Ý: dữ liệu đã có giao dịch không xóa để giữ lịch sử; chỉ ngừng.*
4. Xóa thật theo thứ tự KM → SKU → sản phẩm:
   - Khuyến mãi `5` → mã của "KM demo" → `c` → "Đã xóa khuyến mãi".
   - `0`, `2` Sản phẩm: `7` xóa SKU → tìm `110 ml` → `c` → "Đã xóa SKU".
   - `8` xóa sản phẩm → mã "Sữa demo" → `c` → "Đã xóa sản phẩm".
   - Thử `7` với `f9f1` → `c` → "SKU đã có trên đơn hàng; không xóa để giữ lịch sử, hãy ngừng bán" (kem vừa bán ở 2.3). *Ý: xóa có điều kiện.*
5. `4` Nhập hàng → `1` lập phiếu → `1` thêm dòng: tìm `f9f1` → `20` → `9000` → `4` ghi nhận → `NPP Demo` → `c`. *Ý: phiếu nhập cũng có thêm/sửa/bỏ dòng; tồn chỉ tăng qua phiếu.*
6. `5` Tài khoản: `2` tạo nhân viên, `4` khóa, `5` mở lại; thử khóa `demo.quanly` → bị chặn "quản lý cuối cùng".
7. `6` Báo cáo: `1` thu-chi, `3` thống kê (01/10/2026 – 31/12/2026), `4` nhập - xuất - tồn, `2` tồn hiện tại.

### 2.5. Chỉ code theo từng yêu cầu của đề

| Yêu cầu | Mở file nào, nói gì |
| --- | --- |
| ≥ 15 lớp | 32 file trong `src/cuahang/`, chia 6 package theo nhóm: 26 class, 2 interface, 4 enum ([thiết kế lớp](04-thiet-ke-lop.md)) |
| Constructor | Mọi lớp có constructor tường minh; lớp thực thể có 2 constructor: tạo mới (sinh mã, kiểm tra) và dựng lại từ file, ví dụ `SanPham`, `DonHang`, `TaiKhoan` |
| Kế thừa | `TaiKhoan` → `KhachHang`, `NhanVienBanHang`, `QuanLyCuaHang`; `KhuyenMai` → `KhuyenMaiPhanTram`, `KhuyenMaiSoTien`; `DanhSach<T>` → 8 lớp danh sách |
| Đa hình | `danhsach/QuanLyKhuyenMai.java:36` gọi `km.tinhTienGiam(...)` không cần biết lớp con; `GiaoDienConsole.java:122` chọn menu theo `tk.layVaiTro()`; biến `IQuanLy<SanPham>` gọi đúng `them/sua/xoa` của `QuanLySanPham` |
| Lớp danh sách | `DanhSach<T>` giữ `List<T>`; `QuanLySanPham`, `QuanLyDonHang`… có `them/sua/xoa/xuat` và các hàm tìm `timTheoMa`, `timTheoTen`… |
| File text | `LuuTruText.docDuLieu()` đọc đủ tám file và `ghiAnToan()` dùng file tạm, nhật ký phục hồi và `ATOMIC_MOVE`; `GiaoDienConsole.xuatHoaDon` ghi hóa đơn |
| static | Thuộc tính: `DonHang.TIEN_MOI_DIEM`, `TaiKhoan.SO_VONG_BAM`, `DichVuCuaHang.NGUONG_TON_THAP`; phương thức: `TaiKhoan.chuanHoaTenDangNhap`, `DonHang.diemToiDaTheoTien`, `GiaoDienConsole.tien` |
| abstract | `TaiKhoan.layVaiTro()`, `KhuyenMai.tinhTienGiam()`, `DanhSach.them/sua/xoa()` |
| interface | `IQuanLy<T>` (xuat, them, sua, xoa) do `DanhSach<T>` implements; `ChinhSachGiamGia` do `KhuyenMai` implements |

### 2.6. Câu cô hay hỏi

- **Sao đơn hàng, tài khoản, phiếu nhập không xóa được?** Chúng là chứng từ/lịch sử: xóa làm sai báo cáo, điểm, tồn. Thay vào đó có hủy đơn chờ, khóa tài khoản. Sản phẩm/SKU/khuyến mãi xóa được khi chưa có giao dịch.
- **Đa hình nằm ở đâu?** Xem bảng trên; ví dụ hay nhất là chọn khuyến mãi tốt nhất: một vòng lặp gọi `tinhTienGiam` trên cả hai loại.
- **Interface khác lớp trừu tượng thế nào?** `IQuanLy` chỉ là hợp đồng (không có dữ liệu); `DanhSach` là lớp trừu tượng có thuộc tính `danhSach` và code dùng chung `xuat`, `timDauTien`, `loc`.
- **Vì sao SKU tách khỏi sản phẩm?** Cùng một sữa có hộp lẻ, lốc, thùng với giá và tồn riêng; bán 2 lốc chỉ trừ tồn SKU lốc.
- **Mất điện giữa lúc lưu thì sao?** Chuẩn bị tám file tạm, lưu nhật ký trạng thái cũ rồi mới thay từng file. Nếu lần ghi bị gián đoạn, lần chạy sau dùng nhật ký phục hồi cả bộ cũ trước khi đọc; chưa có `fsync` nên không cam kết chống mất điện hay hỏng ổ đĩa. App kiểm tra liên kết, tồn và điểm khi đọc; dữ liệu lỗi được báo, không tự khởi tạo đè.
- **Sao chia package mà nhiều phương thức đổi dữ liệu lại `public`?** `DichVuCuaHang` (package `cuahang`) cần gọi `tangTon`, `congDiem`… của lớp ở package con, nên Java bắt phải `public`. Quy ước: chỉ `DichVuCuaHang` và `LuuTruText` gọi chúng, giao diện luôn đi qua `DichVuCuaHang.giaoDich` để có lưu file và phục hồi khi lỗi.
- **Mật khẩu lưu thế nào?** Không lưu rõ: PBKDF2 + salt ngẫu nhiên riêng từng tài khoản; đổi mật khẩu sinh salt mới.
- **Giá đổi thì hóa đơn cũ có đổi không?** Không: dòng đơn chụp tên, giá, khuyến mãi lúc thanh toán.

## 3. Checklist test tay toàn bộ app

Bắt đầu từ dữ liệu mẫu (`git restore data/`). Đánh dấu ☐ → ☑ khi đúng. Làm xong nhớ `git restore data/` và `rm -rf hoa-don`.

### 3.1. Khởi động và tài khoản

| # | Thao tác | Kết quả mong đợi |
| --- | --- | --- |
| ☐ 1 | Chạy app | In đường dẫn thư mục dữ liệu, hiện menu CỬA HÀNG SỮA |
| ☐ 2 | Menu gõ `9`, rồi `abc` | "Lựa chọn không hợp lệ", hỏi lại |
| ☐ 3 | Đăng nhập `demo.khach01` mật khẩu sai | "Sai tên đăng nhập hoặc mật khẩu" |
| ☐ 4 | Đăng ký trùng tên `DEMO.KHACH01` | "Tên đăng nhập đã được sử dụng" |
| ☐ 5 | Đăng ký mật khẩu `123` | Báo cần ít nhất 6 ký tự |
| ☐ 6 | Đăng ký nhập lại mật khẩu khác | "Hai lần nhập mật khẩu không khớp" |
| ☐ 7 | Đăng ký SĐT `12345` | "Số điện thoại phải bắt đầu bằng 0…" |
| ☐ 8 | Đăng ký hợp lệ rồi đăng nhập | "Xin chào … (Khách hàng)" |
| ☐ 9 | Khách: Đổi mật khẩu, nhập sai mật khẩu hiện tại | "Mật khẩu hiện tại không đúng" |
| ☐ 10 | Đổi mật khẩu đúng, đăng xuất, đăng nhập mật khẩu cũ rồi mới | Cũ bị từ chối, mới vào được |

### 3.2. Khách hàng

| # | Thao tác | Kết quả mong đợi |
| --- | --- | --- |
| ☐ 11 | Xem/tìm, Enter để trống | Hiện mọi SKU đang bán, có cột KM và giá sau KM |
| ☐ 12 | Tìm `kem`, `thùng`, `xyz` | Lọc đúng; `xyz` báo không có kết quả |
| ☐ 13 | Đặt hàng, số lượng `0`, `-1`, `abc` | Báo lỗi, cho nhập lại |
| ☐ 14 | Đặt số lượng lớn hơn tồn (ví dụ `999`) | "Không đủ hàng … còn …, cần 999" |
| ☐ 15 | Thêm cùng SKU 2 lần | Gộp thành 1 dòng, cộng số lượng |
| ☐ 16 | Sửa số lượng, bỏ dòng, rồi xác nhận | Tổng tạm tính cập nhật đúng; "Đặt hàng thành công" |
| ☐ 17 | Xác nhận khi giỏ trống | Báo giỏ hàng trống |
| ☐ 18 | Xem đơn của tôi | Chỉ thấy đơn của mình, ghi "GIÁ TẠM TÍNH" |
| ☐ 19 | Hủy đơn chờ | "Đã hủy đơn"; đơn chuyển "Đã hủy" |
| ☐ 20 | Xem điểm | Số điểm và lịch sử các đơn đã thanh toán |

### 3.3. Nhân viên

| # | Thao tác | Kết quả mong đợi |
| --- | --- | --- |
| ☐ 21 | Thanh toán đơn `bc4ebe22`, dùng điểm vượt mức tối đa | Báo chỉ được nhập từ 0 đến mức tối đa |
| ☐ 22 | Thanh toán, trả lời `k` ở câu "đã nhận đủ tiền?" | "Đơn vẫn chờ thanh toán", tồn và điểm không đổi |
| ☐ 23 | Thanh toán đầy đủ | Hóa đơn console + "Đã lưu hóa đơn: …HD-….txt"; file mở được, căn thẳng cột |
| ☐ 24 | Lập đơn cho khách vãng lai rồi thanh toán | Hóa đơn ghi "Khách vãng lai", không có dòng điểm |
| ☐ 25 | Lập đơn cho SĐT không phải thành viên | Báo không tìm thấy |
| ☐ 26 | Xem/tìm đơn, lọc từng trạng thái | Đúng danh sách; đơn đã thanh toán hiện lại đúng hóa đơn |
| ☐ 27 | Xuất hóa đơn ra file với `84736ab2` | Tạo `HD-20261002-84736ab2.txt` |
| ☐ 28 | Tra thành viên `0900000004` | Họ tên và số điểm hiện tại |
| ☐ 29 | Thử hủy đơn đã thanh toán (gõ mã ở mục hủy) | Không có trong danh sách / báo không hủy được |

### 3.4. Quản lý

| # | Thao tác | Kết quả mong đợi |
| --- | --- | --- |
| ☐ 30 | Thêm sản phẩm, để trống tên | Quay lại menu, không tạo gì |
| ☐ 31 | Thêm SKU trùng quy cách đã có (ví dụ lốc 4 cho Sữa tươi) | "Sản phẩm đã có quy cách …" |
| ☐ 32 | Sửa giá SKU; khách xem lại | Giá mới; hóa đơn cũ vẫn giá cũ |
| ☐ 33 | Ngừng bán SKU; khách tìm SKU đó | Khách không thấy; quản lý vẫn thấy "Ngừng bán" |
| ☐ 34 | Thêm KM ngày kết thúc trước ngày bắt đầu, tỷ lệ `101` | Bị từ chối |
| ☐ 35 | Thêm KM 50% cho kem; khách xem | Cột KM `-50%`, giá sau KM giảm nửa |
| ☐ 36 | Ngừng KM rồi thử sửa mở lại | Không mở lại được |
| ☐ 37 | Xóa KM `6828eb46`; xóa SKU `f9f193ce`; xóa sản phẩm Kem vani | Cả ba bị chặn kèm lý do |
| ☐ 38 | Tạo sản phẩm → SKU → KM mới rồi xóa sản phẩm, xóa SKU | Bị chặn ("còn SKU", "còn khuyến mãi") |
| ☐ 39 | Xóa KM → SKU → sản phẩm mới tạo; mở `data/khuyen-mai.txt`, `data/bien-the.txt`, `data/san-pham.txt` | Ba lần "Đã xóa"; không còn trong file |
| ☐ 40 | Nhập hàng: thêm 2 dòng, sửa 1 dòng, bỏ 1 dòng, ghi nhận | Tồn tăng đúng; lịch sử phiếu nhập có phiếu mới |
| ☐ 41 | Nhập hàng cùng SKU với giá khác | Báo phải chọn một đơn giá |
| ☐ 42 | Tạo nhân viên trùng tên đăng nhập | Bị từ chối |
| ☐ 43 | Khóa nhân viên rồi đăng nhập bằng nhân viên đó | "Tài khoản đã bị khóa"; mở lại thì vào được |
| ☐ 44 | Khóa `demo.quanly` | "Không được khóa quản lý đang hoạt động cuối cùng" |
| ☐ 45 | Báo cáo thu-chi 01/10/2026 – 31/10/2026 | Doanh thu gộp 248.000đ, giảm KM 10.000đ, giảm điểm 500đ, thu ròng 237.500đ, 5 đơn |
| ☐ 46 | Báo cáo ngày `31/02/2026`, hoặc ngày kết thúc trước ngày bắt đầu | "Ngày không hợp lệ" / báo khoảng ngày không hợp lệ |
| ☐ 47 | Thống kê tháng 10/2026 | Top 1 Kem vani lốc 6 ly 66.000đ; Trần Thị Ngân 4 đơn 173.500đ |
| ☐ 48 | Nhập - xuất - tồn 03/10/2026 – 05/10/2026 | Hũ sữa chua: đầu 100, xuất 4, cuối 96; dòng "Đối chiếu: … khớp" |
| ☐ 49 | Bán 1 ly kem số lượng 95 rồi đăng nhập quản lý | Cảnh báo "sắp hết hàng … còn 5"; báo cáo tồn ghi "⚠ Sắp hết" |
| ☐ 50 | Nhân viên/khách thử vào menu quản lý | Không có trong menu (dịch vụ cũng kiểm tra quyền) |

### 3.5. Lưu file và khởi động lại

| # | Thao tác | Kết quả mong đợi |
| --- | --- | --- |
| ☐ 51 | Sau các bước trên, thoát và chạy lại | Mọi đơn, tồn, điểm, tài khoản giữ đúng như trước khi thoát |
| ☐ 52 | Sửa tay một dòng trong `data/san-pham.txt` cho sai (xóa một cột) rồi chạy | App báo lỗi rõ dòng, **không** ghi đè file; `git restore` để sửa |
| ☐ 53 | Đổi tên cả thư mục `data/` rồi chạy | Hỏi "Khởi tạo hệ thống mới?"; `k` thì thoát, `c` thì tạo quản lý đầu tiên. Xóa bộ mới tạo nếu có rồi đổi tên thư mục cũ lại sau khi thử |
| ☐ 54 | Gõ Ctrl+D ở bất kỳ ô nhập nào | "Hết dữ liệu nhập, kết thúc chương trình." |
