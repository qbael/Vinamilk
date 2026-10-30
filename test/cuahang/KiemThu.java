package cuahang;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Self-check các tình huống trong tài liệu 08. Chạy:
 *   javac -d out $(find src test -name '*.java') && java -cp out cuahang.KiemThu
 */
public class KiemThu {
    private static int soKiemTra = 0;

    public static void main(String[] args) {
        kiemThuCongThuc();
        kiemThuChiTiet();
        System.out.println("OK: " + soKiemTra + " kiểm tra đều đạt.");
    }

    // ---------- KT11–KT16: công thức khuyến mãi và điểm ----------

    static void kiemThuCongThuc() {
        LocalDate tu = LocalDate.of(2026, 10, 1);
        LocalDate den = LocalDate.of(2026, 10, 31);
        KhuyenMai giam10 = new KhuyenMaiPhanTram("Giảm 10%", "BT", tu, den, 10);
        KhuyenMai giam2000 = new KhuyenMaiSoTien("Giảm 2000/lốc", "BT", tu, den, 2000);

        // Đa hình qua List<KhuyenMai>
        List<KhuyenMai> ds = List.of(giam10, giam2000);
        dung(ds.get(0).tinhTienGiam(30_000, 2) == 6_000, "KT11 giảm 10% của 60.000 = 6.000");
        dung(ds.get(1).tinhTienGiam(30_000, 2) == 4_000, "KT11 giảm 2.000 × 2 lốc = 4.000");
        dung(new KhuyenMaiPhanTram("x", "BT", tu, den, 15).tinhTienGiam(10_001, 2) == 3_000,
                "Làm tròn xuống trên cả dòng: 20.002 × 15% = 3.000");
        dung(new KhuyenMaiPhanTram("x", "BT", tu, den, 10).tinhTienGiam(Long.MAX_VALUE / 2, 2)
                == (Long.MAX_VALUE - 1) / 10, "Giảm % không tràn số ở phép nhân trung gian");
        loi(ArithmeticException.class, () -> giam2000.tinhTienGiam(Long.MAX_VALUE, 2), "Tràn tiền hàng bị chặn");
        loi(IllegalArgumentException.class, () -> giam10.tinhTienGiam(0, 1), "Đơn giá 0 bị từ chối");

        // KT14: giảm vượt giá và giảm 100% đều về 0, không âm
        dung(new KhuyenMaiSoTien("x", "BT", tu, den, 50_000).tinhTienGiam(30_000, 2) == 60_000,
                "KT14 KM số tiền vượt giá bị chặn ở đơn giá");
        dung(new KhuyenMaiPhanTram("x", "BT", tu, den, 100).tinhTienGiam(30_000, 2) == 60_000,
                "KT14 KM 100% giảm đúng tiền hàng");
        loi(IllegalArgumentException.class, () -> new KhuyenMaiPhanTram("x", "BT", tu, den, 101), "Tỷ lệ > 100 bị từ chối");
        loi(IllegalArgumentException.class, () -> new KhuyenMaiSoTien("x", "BT", den, tu, 1), "Ngày kết thúc trước bắt đầu bị từ chối");

        // KT15: hai đầu kỳ khuyến mãi đều hợp lệ
        dung(giam10.coHieuLuc(tu) && giam10.coHieuLuc(den), "KT15 ngày đầu/cuối kỳ KM vẫn hợp lệ");
        dung(!giam10.coHieuLuc(tu.minusDays(1)) && !giam10.coHieuLuc(den.plusDays(1)), "KT15 ngoài kỳ KM không hợp lệ");
        giam10.ngungApDung();
        dung(!giam10.coHieuLuc(tu), "KM đã ngừng không còn hiệu lực");

        // KT11: 2 lốc × 30.000, chọn giảm 6.000, khách 250 điểm dùng 100
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 10, 9, 0);
        DonHang don = new DonHang("KH1", "KH1", gio(dong(2, 30_000, null, 0)), t0);
        dung(don.getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN && don.getTongThanhToan() == 60_000,
                "Đơn mới chờ thanh toán, tạm tính 60.000");
        dung(DonHang.diemToiDaTheoTien(54_000) == 108, "KT11 điểm tối đa theo tiền = 108");

        // KT12: đề nghị 109 điểm bị từ chối, đơn giữ nguyên
        loi(IllegalArgumentException.class,
                () -> don.chotThanhToan(gio(dong(2, 30_000, "KM10", 6_000)), "NV1", t0.plusMinutes(5), 109),
                "KT12 dùng 109 điểm vượt mức 108");
        dung(don.getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN, "KT12 đơn vẫn chờ sau lỗi");

        don.chotThanhToan(gio(dong(2, 30_000, "KM10", 6_000)), "NV1", t0.plusMinutes(5), 100);
        dung(don.getTienGiamDiem() == 10_000 && don.getTongThanhToan() == 44_000 && don.getDiemCong() == 4,
                "KT11 thực trả 44.000, cộng 4 điểm");
        KhachHang kh = new KhachHang("kt11", "matkhau1".toCharArray(), "Khách KT11", "0912345678");
        kh.congDiem(250);
        kh.truDiem(don.getDiemSuDung());
        kh.congDiem(don.getDiemCong());
        dung(kh.getDiemTichLuy() == 154, "KT11 còn 154 điểm");
        loi(IllegalArgumentException.class, () -> kh.truDiem(155), "Không trừ quá số điểm đang có");

        // KT17: thanh toán lặp / hủy đơn đã thanh toán bị chặn
        loi(IllegalStateException.class,
                () -> don.chotThanhToan(gio(dong(2, 30_000, null, 0)), "NV1", t0.plusMinutes(9), 0),
                "KT17 thanh toán lần hai bị chặn");
        loi(IllegalStateException.class, don::huy, "Không hủy đơn đã thanh toán");

        // Dòng thanh toán phải giữ đúng SKU và số lượng đã đặt
        DonHang don2 = new DonHang("KH1", "KH1", gio(dong(2, 30_000, null, 0)), t0);
        loi(IllegalArgumentException.class,
                () -> don2.chotThanhToan(gio(dong(3, 30_000, null, 0)), "NV1", t0, 0),
                "Không đổi số lượng khi thanh toán");

        // KT13: sau KM 499đ thì tối đa 0 điểm
        DonHang don499 = new DonHang("KH1", "KH1", gio(dong(1, 499, null, 0)), t0);
        loi(IllegalArgumentException.class,
                () -> don499.chotThanhToan(gio(dong(1, 499, null, 0)), "NV1", t0, 1), "KT13 499đ không đổi được 1 điểm");

        // KT14: đơn tổng 0 không tích điểm
        DonHang don0 = new DonHang("KH1", "KH1", gio(dong(2, 30_000, null, 0)), t0);
        don0.chotThanhToan(gio(dong(2, 30_000, "KM100", 60_000)), "NV1", t0, 0);
        dung(don0.getTongThanhToan() == 0 && don0.getDiemCong() == 0, "KT14 đơn 0đ không tích điểm");

        // KT16: khách vãng lai không tích/dùng điểm
        DonHang vangLai = new DonHang(null, "NV1", gio(dong(1, 100_000, null, 0)), t0);
        loi(IllegalArgumentException.class,
                () -> vangLai.chotThanhToan(gio(dong(1, 100_000, null, 0)), "NV1", t0, 1), "KT16 vãng lai không dùng điểm");
        vangLai.chotThanhToan(gio(dong(1, 100_000, null, 0)), "NV1", t0, 0);
        dung(vangLai.getTongThanhToan() == 100_000 && vangLai.getDiemCong() == 0, "KT16 vãng lai thu 100.000, 0 điểm");

        // Bản sao xem trước không ảnh hưởng đơn thật
        DonHang don3 = new DonHang("KH1", "KH1", gio(dong(2, 30_000, null, 0)), t0);
        DonHang xemTruoc = don3.banSao();
        xemTruoc.chotThanhToan(gio(dong(2, 30_000, "KM10", 6_000)), "NV1", t0, 0);
        dung(don3.getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN && xemTruoc.getTongThanhToan() == 54_000,
                "Xem trước thanh toán không đổi đơn thật");
    }

    // ---------- KT27, KT30, KT32: danh sách chi tiết ----------

    static void kiemThuChiTiet() {
        // KT32 lúc soạn, gọi qua interface
        DanhSachChiTietDonHang soan = new DanhSachChiTietDonHang();
        IQuanLy<ChiTietDonHang> quaInterface = soan;
        quaInterface.them(dong(3, 30_000, null, 0));
        quaInterface.them(dong(3, 30_000, null, 0));
        dung(soan.xuat().size() == 1 && soan.timTheoBienThe("BT").getSoLuong() == 6, "KT06/KT32 chọn lại SKU được gộp thành 6");
        quaInterface.sua("BT", dong(2, 30_000, null, 0));
        dung(soan.timTheoBienThe("BT").getSoLuong() == 2, "KT32 sửa thay nguyên dòng, không cộng lượng");
        loi(IllegalArgumentException.class, () -> quaInterface.sua("KHAC", dong(1, 1, null, 0)), "Sửa dòng không có bị từ chối");
        loi(IllegalArgumentException.class, () -> quaInterface.xoa("KHAC"), "Xóa dòng không có bị từ chối");
        loi(IllegalArgumentException.class, () -> quaInterface.them(null), "Thêm null bị từ chối");

        // KT30: bản xuất không sửa được cấu trúc
        loi(UnsupportedOperationException.class, () -> soan.xuat().add(dong(1, 1, null, 0)), "KT30 bản xuat() chỉ đọc");
        loi(UnsupportedOperationException.class, () -> soan.xuat().clear(), "KT30 bản xuat() không xóa được");
        dung(soan.xuat().size() == 1, "KT30 danh sách gốc giữ nguyên");

        // KT27: hai đơn soạn độc lập
        DanhSachChiTietDonHang soanKhac = new DanhSachChiTietDonHang();
        soanKhac.them(new ChiTietDonHang("BT2", "Kem", "1 ly", 1, 12_000, null, 0));
        soan.xoa("BT");
        dung(soan.isRong() && soanKhac.xuat().size() == 1, "KT27 xóa dòng đơn này không đổi đơn kia");

        // KT32 sau khi đơn nhận quyền sở hữu: chặn qua cả tham chiếu cũ
        DanhSachChiTietDonHang soHuu = gio(dong(1, 8_000, null, 0));
        IQuanLy<ChiTietDonHang> thamChieuCu = soHuu;
        new DonHang(null, "NV1", soHuu, LocalDateTime.now());
        loi(IllegalStateException.class, () -> thamChieuCu.them(dong(1, 8_000, null, 0)), "KT32 thêm sau khi chốt bị chặn");
        loi(IllegalStateException.class, () -> thamChieuCu.sua("BT", dong(2, 8_000, null, 0)), "KT32 sửa sau khi chốt bị chặn");
        loi(IllegalStateException.class, () -> thamChieuCu.xoa("BT"), "KT32 xóa sau khi chốt bị chặn");
        loi(IllegalArgumentException.class, () -> new DonHang(null, "NV1", soHuu, LocalDateTime.now()),
                "Hai đơn không dùng chung danh sách chi tiết");
        loi(IllegalArgumentException.class, () -> new DonHang(null, "NV1", new DanhSachChiTietDonHang(), LocalDateTime.now()),
                "Đơn rỗng bị từ chối");

        // Phiếu nhập: cùng SKU khác giá bị từ chối gộp; sau ghi nhận bất biến
        DanhSachChiTietPhieuNhap phieu = new DanhSachChiTietPhieuNhap();
        phieu.them(new ChiTietPhieuNhap("BT", "Sữa", "Thùng 48", 3, 200_000));
        phieu.them(new ChiTietPhieuNhap("BT", "Sữa", "Thùng 48", 2, 200_000));
        dung(phieu.timTheoBienThe("BT").getSoLuong() == 5, "Phiếu nhập gộp cùng SKU cùng giá");
        loi(IllegalArgumentException.class, () -> phieu.them(new ChiTietPhieuNhap("BT", "Sữa", "Thùng 48", 1, 190_000)),
                "Phiếu nhập cùng SKU khác giá bị từ chối");
        PhieuNhap pn = new PhieuNhap("QL1", "Nhà cung cấp", LocalDateTime.now(), phieu);
        dung(pn.tinhTongTien() == 1_000_000, "Tổng phiếu = 5 × 200.000");
        loi(IllegalStateException.class, () -> phieu.xoa("BT"), "Phiếu đã ghi nhận không xóa dòng được");
        loi(ArithmeticException.class, () -> new ChiTietPhieuNhap("BT", "Sữa", "Thùng", Integer.MAX_VALUE, Long.MAX_VALUE),
                "KT26 tràn thành tiền nhập bị chặn");
    }

    // ---------- tiện ích ----------

    static ChiTietDonHang dong(int soLuong, long donGia, String maKhuyenMai, long tienGiam) {
        return new ChiTietDonHang("BT", "Sữa tươi", "Lốc 4 hộp", soLuong, donGia, maKhuyenMai, tienGiam);
    }

    static DanhSachChiTietDonHang gio(ChiTietDonHang... dong) {
        DanhSachChiTietDonHang ds = new DanhSachChiTietDonHang();
        for (ChiTietDonHang ct : dong) {
            ds.them(ct);
        }
        return ds;
    }

    static void dung(boolean dieuKien, String moTa) {
        soKiemTra++;
        if (!dieuKien) {
            throw new AssertionError("SAI: " + moTa);
        }
    }

    static void loi(Class<? extends Throwable> loaiLoi, Runnable thaoTac, String moTa) {
        soKiemTra++;
        try {
            thaoTac.run();
        } catch (Throwable t) {
            if (loaiLoi.isInstance(t)) {
                return;
            }
            throw new AssertionError("SAI: " + moTa + " — gặp " + t, t);
        }
        throw new AssertionError("SAI: " + moTa + " — không báo lỗi");
    }
}
