#!/usr/bin/env bash
# Kiểm thử end-to-end qua giao diện console: gõ sẵn phím vào stdin, đối chiếu output.
# Chạy trên bản copy của data/ nên không đụng data/du-lieu.txt. Chạy: bash test/e2e.sh
set -u
cd "$(dirname "$0")/.."
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
javac -d "$TMP/out" $(find src -name '*.java') || exit 1

MK='VnmDemo!2026'
KEM=f9f193ce # Kem vani – 1 ly 100 ml, 12.000đ, tồn 100, không có KM
loi=0

# Mỗi lần gọi là một lần chạy chương trình mới; dữ liệu giữ nguyên giữa các lần cho đến khi gọi moi.
moi() { rm -rf "$TMP/data"; cp -r data "$TMP/data"; }

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

moi
kichBan "Sai mật khẩu bị từ chối" "1
demo.khach01
sai
0" "Sai tên đăng nhập hoặc mật khẩu" "Tạm biệt!"

kichBan "Khách xem điểm" "1
demo.khach01
$MK
5
0
0" "Xin chào Lê Hoàng An" "Điểm hiện có: [0-9]+"

kichBan "Khách đặt hàng nhận tại cửa hàng" "1
demo.khach01
$MK
2
1
$KEM
1
4
c
0
0" "Đã chọn: Kem vani" "Đặt hàng thành công"

kichBan "Nhân viên bán vãng lai và thanh toán" "1
demo.nhanvien
$MK
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
kichBan "Tồn kho sau khi khởi động lại" "1
demo.quanly
$MK
6
2
0
0
0" "Kem vani +1 ly 100 ml +98 "

kichBan "Quản lý xem báo cáo thu - chi" "1
demo.quanly
$MK
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

moi
if command -v expect >/dev/null; then
    expect test/e2e.exp "$TMP" || loi=$((loi + 1))
else
    echo "SKIP: TTY — chưa cài expect"
fi

[ "$loi" -eq 0 ] && echo "OK: e2e đều đạt." || { echo "$loi kịch bản lỗi."; exit 1; }
