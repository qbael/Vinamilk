#!/usr/bin/env bash
# Kiểm thử end-to-end qua giao diện console: gõ sẵn phím vào stdin, đối chiếu output.
# Chạy trên bản copy của data/ nên không đụng các file dữ liệu gốc. Chạy: bash test/e2e.sh
set -u
cd "$(dirname "$0")/.."
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
javac -d "$TMP/out" $(find src -name '*.java') || exit 1

MK='VnmDemo!2026'
KEM=f9f193ce      # Kem vani – 1 ly 100 ml, 12.000đ, tồn 100, không có KM
SP_KEM=a8f710f9   # sản phẩm Kem vani
NV=eee4acf9       # tài khoản demo.nhanvien
QL=fb5e2d83       # tài khoản demo.quanly, quản lý duy nhất
DON_KH02=bc4ebe22 # đơn chờ thanh toán của demo.khach02
DON_KH03=64ee1592 # đơn chờ thanh toán của demo.khach03 (nhân viên lập)
KM_SUA_CHUA=6828eb46
loi=0

# Mỗi lần gọi kichBan là một lần chạy chương trình mới; dữ liệu giữ nguyên giữa các lần cho đến khi gọi moi.
moi() { rm -rf "$TMP/data"; cp -r data "$TMP/data"; }

# Phím đăng nhập: dn <tên> [mật khẩu]
dn() { printf '1\n%s\n%s' "$1" "${2:-$MK}"; }

# kichBan "tên" "phím, mỗi dòng một lần Enter" "regex phải có"...
kichBan() {
    local ten=$1 phim=$2 out; shift 2
    out=$(cd "$TMP" && printf '%s\n' "$phim" | java -cp out cuahang.Main 2>&1)
    for mau in "$@"; do
        if ! grep -qE -- "$mau" <<<"$out"; then
            echo "FAIL: $ten — thiếu: $mau"
            echo "$out" | tail -25 | sed 's/^/    /'
            loi=$((loi + 1))
            return
        fi
    done
    echo "PASS: $ten"
}

# ---------- đăng nhập, menu, luồng bán hàng chính

moi
kichBan "Sai mật khẩu bị từ chối" "$(dn demo.khach01 sai)
0" "Sai tên đăng nhập hoặc mật khẩu" "Tạm biệt!"

kichBan "Khách xem điểm" "$(dn demo.khach01)
5
0
0" "Xin chào Lê Hoàng An" "Điểm hiện có: [0-9]+"

kichBan "Khách đặt hàng nhận tại cửa hàng" "$(dn demo.khach01)
2
1
$KEM
1
4
c
0
0" "Đã chọn: Kem vani" "Đặt hàng thành công"

kichBan "Nhân viên bán vãng lai và thanh toán" "$(dn demo.nhanvien)
2
2
1
$KEM
2
4
c
c
c
0
0" "Đã lưu đơn" "Thanh toán thành công" "TỔNG THANH TOÁN +24.000đ" "Đã lưu hóa đơn: .*hoa-don/HD-[0-9]{8}-[0-9a-f]{8}\.txt"

# File hóa đơn tự lưu sau khi thanh toán
if grep -qE "^TỔNG THANH TOÁN +24.000đ$" "$TMP"/hoa-don/HD-*.txt 2>/dev/null; then
    echo "PASS: File hóa đơn có tổng tiền"
else
    echo "FAIL: File hóa đơn có tổng tiền"; loi=$((loi + 1))
fi

# Chạy lại: 100 − 1 đơn khách đang chờ − 2 vừa bán = 97 khả dụng.
kichBan "Tồn kho sau khi khởi động lại" "$(dn demo.quanly)
6
2
0
0
0" "Kem vani +1 ly 100 ml +97 "

kichBan "Quản lý xem báo cáo thu - chi" "$(dn demo.quanly)
6
1
01/10/2026
31/10/2026
0
0
0" "BÁO CÁO THU - CHI từ 01/10/2026 đến 31/10/2026"

kichBan "Quản lý xem thống kê bán hàng" "$(dn demo.quanly)
6
3
01/10/2026
31/10/2026
0
0
0" "THỐNG KÊ BÁN HÀNG từ 01/10/2026" "Kem vani – Lốc 6 ly 100 ml +1 +66.000đ" "Trần Thị Ngân +4 đơn +173.500đ +█+"

kichBan "Quản lý xem nhập - xuất - tồn" "$(dn demo.quanly)
6
4
03/10/2026
05/10/2026
0
0
0" "NHẬP - XUẤT - TỒN từ 03/10/2026" "1 hũ 100 g +100 +0 +4 +96" "tồn đang lưu khớp"

kichBan "Nhân viên xuất lại hóa đơn ra file" "$(dn demo.nhanvien)
7
84736ab2
0
0" "Đã lưu hóa đơn: .*HD-20261002-84736ab2\.txt"

kichBan "Menu chặn lựa chọn sai" "9
abc
0" "Lựa chọn không hợp lệ" "Tạm biệt!"

kichBan "Hết input thì thoát gọn" "1
demo.khach01" "Hết dữ liệu nhập, kết thúc chương trình."

# ---------- sửa/bỏ dòng giỏ theo số thứ tự

moi
kichBan "Sửa/bỏ dòng theo STT, kiểm tra nhập và đánh số lại" "$(dn demo.nhanvien)
2
2
2
3
1
$KEM
1
1
83e324f1
1
2

3

2
2

2
0
3
abc
2
3
3
1
2
1
2
4
c
c
c
0
0" "Giỏ hàng đang trống" "STT +Sản phẩm" "Số thứ tự trong giỏ \\(Enter = quay lại\\)" \
    "Hãy nhập số từ 1 đến 2" "Hãy nhập một số nguyên hợp lệ" \
    "^2 +Kem vani +Lốc 6 ly 100 ml +3 +66.000đ" "^1 +Kem vani +Lốc 6 ly 100 ml +2 +66.000đ" \
    "Thanh toán thành công" "TỔNG THANH TOÁN +132.000đ"

# ---------- đăng ký, hủy đơn, thanh toán thành viên

moi
kichBan "Đăng ký khách hàng rồi đăng nhập" "2
khach.moi
matkhau1
matkhau1
Đỗ Văn Mới
0900000099
$(dn khach.moi matkhau1)
0
0" "Đăng ký thành công" "Xin chào Đỗ Văn Mới \(Khách hàng\)"

kichBan "Đăng ký với mật khẩu nhập lại không khớp" "2
khach.khac
matkhau1
matkhau2
0" "Hai lần nhập mật khẩu không khớp"

kichBan "Khách hủy đơn đang chờ thanh toán" "$(dn demo.khach02)
4
$DON_KH02
c
4
0
0" "Đã hủy đơn\." "Không có đơn đang chờ thanh toán"

kichBan "Nhân viên bán cho thành viên, trừ điểm" "$(dn demo.nhanvien)
2
1
0900000005
1
$KEM
2
4
c
c
6
c
c
0
0" "Khách Võ Minh Khoa có 6 điểm" "Giảm bằng điểm \(6 điểm\) +-600đ" "TỔNG THANH TOÁN +23.400đ" "Điểm trừ: 6"

kichBan "Nhân viên thanh toán đơn khách đặt trước" "$(dn demo.nhanvien)
4
$DON_KH03
0
c
0
0" "Thanh toán thành công" "HÓA ĐƠN"

# ---------- quản lý: sản phẩm, SKU, nhập hàng

moi
kichBan "Quản lý thêm sản phẩm, SKU rồi nhập hàng" "$(dn demo.quanly)
2
3
Sữa hạt óc chó
Vinamilk
hộp
180 ml

1
2
5
$SP_KEM
Lốc 4 ly 100 ml
2
4
44000
0
4
1
1
Lốc 4 ly
10
30000
4
NPP Thử
c
0
6
2
0
0
0" "Đã thêm sản phẩm" "Sữa hạt óc chó" "Đã thêm SKU" "Đã ghi nhận phiếu nhập" \
    "Kem vani +Lốc 4 ly 100 ml +10 +Đang bán"

kichBan "Quản lý sửa giá SKU" "$(dn demo.quanly)
2
6
$KEM
15000
c
1
$KEM
0
0
0" "Đã cập nhật SKU" "1 ly 100 ml +15.000đ"

kichBan "Ngừng bán SKU thì khách không đặt được" "$(dn demo.quanly)
2
6
$KEM

k
0
0
$(dn demo.khach01)
2
1
$KEM
0
0
0" "Đã cập nhật SKU" "Không tìm thấy SKU phù hợp"

# ---------- quản lý: khuyến mãi

moi
kichBan "Quản lý thêm và ngừng khuyến mãi" "$(dn demo.quanly)
3
2
$KEM
1
Giảm nửa giá kem
50
01/01/2026
31/12/2099
4
$KM_SUA_CHUA
c
1
0
0
0" "Đã thêm khuyến mãi" "Đã ngừng khuyến mãi" "Giảm nửa giá kem.*-50%.*Đang áp dụng" "Giảm 1000 đồng mỗi.*Đã ngừng"

kichBan "Khách thấy giá sau khuyến mãi" "$(dn demo.khach01)
1
$KEM
0
0" "1 ly 100 ml +12.000đ +-50% +6.000đ"

# ---------- quản lý: xóa hẳn mục chưa có giao dịch

moi
kichBan "Thêm sản phẩm để thử xóa" "$(dn demo.quanly)
2
3
Sữa thử xóa
Vinamilk
hộp
110 ml

1
0
0
0" "Đã thêm sản phẩm"
SP_MOI=$(grep '|Sữa thử xóa|' "$TMP/data/san-pham.txt" | cut -d'|' -f2 | cut -c1-8)

kichBan "Không xóa được mục còn SKU, còn khuyến mãi hoặc đã có giao dịch" "$(dn demo.quanly)
2
5
$SP_MOI
Lốc 4 hộp 110 ml
2
4
24000
0
3
2
110 ml
1
KM thử xóa
10
01/01/2026
31/12/2099
5
$KM_SUA_CHUA
c
0
2
8
$SP_MOI
c
7
110 ml
c
7
$KEM
c
0
0
0" "Đã thêm SKU" "Đã thêm khuyến mãi" "Khuyến mãi đã được áp dụng trên đơn hàng" "Sản phẩm còn 1 SKU" \
    "SKU còn 1 khuyến mãi" "SKU đã có trên phiếu nhập"
KM_MOI=$(grep '|KM thử xóa|' "$TMP/data/khuyen-mai.txt" | cut -d'|' -f2 | cut -c1-8)

kichBan "Xóa khuyến mãi, SKU rồi sản phẩm chưa có giao dịch" "$(dn demo.quanly)
3
5
$KM_MOI
c
0
2
7
110 ml
c
8
$SP_MOI
c
0
0
0" "Đã xóa khuyến mãi" "Đã xóa SKU" "Đã xóa sản phẩm"
if grep -qE "Sữa thử xóa|Lốc 4 hộp 110 ml|KM thử xóa" "$TMP"/data/*.txt; then
    echo "FAIL: Mục đã xóa vẫn còn trong file dữ liệu"; loi=$((loi + 1))
else
    echo "PASS: Mục đã xóa không còn trong file dữ liệu"
fi

# ---------- quản lý: tài khoản

moi
kichBan "Tạo nhân viên mới, khóa nhân viên cũ" "$(dn demo.quanly)
5
2
nv.moi
matkhau1
matkhau1
Lý Văn Mới
0900000088
4
$NV
c
0
0
$(dn demo.nhanvien)
$(dn nv.moi matkhau1)
0
0" "Đã tạo tài khoản" "Đã khóa tài khoản" "Tài khoản đã bị khóa" "Xin chào Lý Văn Mới \(Nhân viên bán hàng\)"

kichBan "Mở khóa thì đăng nhập lại được" "$(dn demo.quanly)
5
5
$NV
c
0
0
$(dn demo.nhanvien)
0
0" "Đã mở khóa" "Xin chào Trần Thị Ngân"

kichBan "Không khóa được quản lý cuối cùng" "$(dn demo.quanly)
5
4
$QL
c
0
0
0" "Không được khóa quản lý đang hoạt động cuối cùng"

# ---------- cảnh báo tồn thấp

moi
kichBan "Bán gần hết kem rồi quản lý thấy cảnh báo" "$(dn demo.nhanvien)
2
2
1
$KEM
95
4
c
c
c
0
$(dn demo.quanly)
6
2
0
0
0" "Thanh toán thành công" "[0-9]+ SKU đang bán sắp hết hàng \\(tồn ≤ 10\\)" "Kem vani – 1 ly 100 ml: còn 5" "Kem vani +1 ly 100 ml +5 +Đang bán  ⚠ Sắp hết"

# ---------- đổi mật khẩu

kichBan "Khách nhập sai mật khẩu hiện tại thì không đổi được" "$(dn demo.khach01)
6
saimatkhau
matkhau9
matkhau9
0
$(dn demo.khach01)
0
0" "Mật khẩu hiện tại không đúng" "Xin chào Lê Hoàng An"

kichBan "Nhân viên đổi mật khẩu rồi đăng nhập lại" "$(dn demo.nhanvien)
8
$MK
MoiDoi!2026
MoiDoi!2026
0
$(dn demo.nhanvien)
$(dn demo.nhanvien MoiDoi!2026)
0
0" "Đã đổi mật khẩu" "Sai tên đăng nhập hoặc mật khẩu" "Xin chào Trần Thị Ngân"

kichBan "Mật khẩu mới vẫn dùng được sau khi khởi động lại" "$(dn demo.nhanvien MoiDoi!2026)
0
0" "Xin chào Trần Thị Ngân"

# ---------- terminal thật

moi
if command -v expect >/dev/null; then
    expect test/e2e.exp "$TMP" || loi=$((loi + 1))
else
    echo "SKIP: TTY — chưa cài expect"
fi

[ "$loi" -eq 0 ] && echo "OK: e2e đều đạt." || { echo "$loi kịch bản lỗi."; exit 1; }
