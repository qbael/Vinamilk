#!/usr/bin/env bash
# Kiểm thử end-to-end qua giao diện console: gõ sẵn phím vào stdin, đối chiếu output.
# Chạy trên bản copy của data/ nên không đụng data/du-lieu.txt. Chạy: bash test/e2e.sh
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
0" "Đã lưu đơn" "Thanh toán thành công" "TỔNG THANH TOÁN +24.000đ"

# Chạy lại chương trình: tồn kho phải đọc từ file đã lưu (100 − 2 vừa bán; đơn đặt chưa giữ hàng)
kichBan "Tồn kho sau khi khởi động lại" "$(dn demo.quanly)
6
2
0
0
0" "Kem vani +1 ly 100 ml +98 "

kichBan "Quản lý xem báo cáo thu - chi" "$(dn demo.quanly)
6
1
01/10/2026
31/10/2026
0
0
0" "BÁO CÁO THU - CHI từ 01/10/2026 đến 31/10/2026"

kichBan "Menu chặn lựa chọn sai" "9
abc
0" "Lựa chọn không hợp lệ" "Tạm biệt!"

kichBan "Hết input thì thoát gọn" "1
demo.khach01" "Hết dữ liệu nhập, kết thúc chương trình."

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

# ---------- terminal thật

moi
if command -v expect >/dev/null; then
    expect test/e2e.exp "$TMP" || loi=$((loi + 1))
else
    echo "SKIP: TTY — chưa cài expect"
fi

[ "$loi" -eq 0 ] && echo "OK: e2e đều đạt." || { echo "$loi kịch bản lỗi."; exit 1; }
