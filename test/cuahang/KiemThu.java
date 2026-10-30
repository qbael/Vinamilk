package cuahang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Self-check các tình huống trong tài liệu 08. Chạy:
 *   javac -d out $(find src test -name '*.java') && java -cp out cuahang.KiemThu
 */
public class KiemThu {
    private static int soKiemTra = 0;

    public static void main(String[] args) {
        kiemThuCongThuc();
        kiemThuChiTiet();
        kiemThuQuanLy();
        kiemThuLuuTru();
        kiemThuDichVuTaiKhoanVaNhapHang();
        kiemThuDatHangVaThanhToan();
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

    // ---------- KT02, KT29, KT31: sáu manager chung ----------

    static void kiemThuQuanLy() {
        // Tài khoản: trùng tên sau chuẩn hóa, khách mới điểm 0, sửa chỉ đổi trạng thái
        QuanLyTaiKhoan qlTk = new QuanLyTaiKhoan();
        qlTk.them(taiKhoan(VaiTro.QUAN_LY_CUA_HANG, "QL1", "quanly"));
        qlTk.them(taiKhoan(VaiTro.KHACH_HANG, "KH1", "khach"));
        loi(IllegalArgumentException.class, () -> qlTk.them(taiKhoan(VaiTro.KHACH_HANG, "KH2", "  KHACH ")),
                "KT02 trùng tên đăng nhập sau trim/đổi chữ thường");
        dung(qlTk.timTheoTenDangNhap(" QuanLy ") != null, "Tìm tên đăng nhập không phân biệt hoa/thường");
        KhachHang coDiem = new KhachHang("KH3", "khach3", SALT, 1, BAM, "Khách", "0911111111", true, 5);
        loi(IllegalArgumentException.class, () -> qlTk.them(coDiem), "Khách mới phải có điểm 0");
        TaiKhoan ql = qlTk.timTheoMa("QL1");
        qlTk.sua("QL1", ql.banSaoVoiTrangThai(false));
        dung(!ql.isDangHoatDong() && qlTk.demQuanLyDangHoatDong() == 0, "Sửa tài khoản chỉ đổi trạng thái");
        loi(IllegalArgumentException.class,
                () -> qlTk.sua("KH1", taiKhoan(VaiTro.KHACH_HANG, "KH1", "tenkhac")), "Không đổi tên đăng nhập qua sửa");
        loi(IllegalArgumentException.class,
                () -> qlTk.sua("KH1", taiKhoan(VaiTro.NHAN_VIEN_BAN_HANG, "KH1", "khach")), "Không đổi loại tài khoản qua sửa");
        dung(qlTk.timKhachTheoSoDienThoai("0900000000").size() == 1, "Tra khách theo số điện thoại");

        // KT29: IQuanLy<SanPham> tham chiếu QuanLySanPham
        QuanLySanPham qlSpThat = new QuanLySanPham();
        IQuanLy<SanPham> qlSp = qlSpThat;
        SanPham sua = new SanPham("SP1", "Sữa tươi", "Vinamilk", "hộp", "180 ml", null, LoaiSanPham.SUA, true);
        qlSp.them(sua);
        loi(IllegalArgumentException.class, () -> qlSp.them(null), "KT29 thêm null bị từ chối");
        loi(IllegalArgumentException.class,
                () -> qlSp.them(new SanPham("SP1", "Khác", "X", "hộp", "1 l", null, LoaiSanPham.SUA, true)), "KT29 trùng mã bị từ chối");
        qlSp.sua("SP1", new SanPham("SP1", "Sữa tươi có đường", "Vinamilk", "hộp", "180 ml", "Mới", LoaiSanPham.SUA, true));
        dung(qlSpThat.timTheoMa("SP1").getTenSanPham().equals("Sữa tươi có đường"), "KT29 sửa chạy đúng override");
        loi(IllegalArgumentException.class,
                () -> qlSp.sua("SP1", new SanPham("SP1", "Tên lạ", "Vinamilk", "hộp", "1 lít", null, LoaiSanPham.SUA, true)),
                "KT29 đổi định lượng (thành hàng khác) bị từ chối");
        dung(sua.getTenSanPham().equals("Sữa tươi có đường"), "KT29 lỗi sửa không đổi một phần dữ liệu");
        loi(IllegalArgumentException.class, () -> qlSp.sua("SP9", sua), "KT29 sửa mã không tồn tại bị từ chối");
        dung(qlSp.xuat().size() == 1 && qlSpThat.timTheoTen("TƯƠI").size() == 1, "Tìm sản phẩm theo tên");

        // SKU: sản phẩm cha phải có, không trùng quy cách, mới tồn 0, sửa không đổi tồn
        QuanLyBienTheSanPham qlBt = new QuanLyBienTheSanPham(qlSpThat);
        qlBt.them(new BienTheSanPham("BT1", "SP1", "Lốc 4 hộp", KieuDongGoi.LOC, 4, 30_000, 0, true));
        qlBt.them(new BienTheSanPham("BT2", "SP1", "Thùng 24 hộp", KieuDongGoi.THUNG, 24, 170_000, 0, true));
        qlBt.them(new BienTheSanPham("BT3", "SP1", "Thùng 48 hộp", KieuDongGoi.THUNG, 48, 340_000, 0, true));
        loi(IllegalArgumentException.class,
                () -> qlBt.them(new BienTheSanPham("BT4", "SP1", "Thùng 48 khác", KieuDongGoi.THUNG, 48, 1, 0, true)),
                "Không trùng bộ (sản phẩm, kiểu, số đơn vị)");
        loi(IllegalArgumentException.class,
                () -> qlBt.them(new BienTheSanPham("BT5", "SP9", "Lốc", KieuDongGoi.LOC, 4, 1, 0, true)), "Sản phẩm cha phải tồn tại");
        loi(IllegalArgumentException.class,
                () -> qlBt.them(new BienTheSanPham("BT6", "SP1", "Lẻ", KieuDongGoi.BAN_LE, 1, 8_000, 50, true)), "SKU mới phải tồn 0");
        loi(IllegalArgumentException.class,
                () -> new BienTheSanPham("SP1", "Lẻ", KieuDongGoi.BAN_LE, 2, 8_000), "Bán lẻ phải đúng 1 đơn vị");
        qlBt.sua("BT1", new BienTheSanPham("BT1", "SP1", "Lốc 4 hộp", KieuDongGoi.LOC, 4, 32_000, 0, true));
        dung(qlBt.timTheoMa("BT1").getGiaBan() == 32_000, "Sửa giá SKU");
        loi(IllegalArgumentException.class,
                () -> qlBt.sua("BT1", new BienTheSanPham("BT1", "SP1", "Lốc 4 hộp", KieuDongGoi.LOC, 4, 32_000, 99, true)),
                "Không sửa tồn trực tiếp");
        qlSpThat.sua("SP1", new SanPham("SP1", "Sữa tươi có đường", "Vinamilk", "hộp", "180 ml", "Mới", LoaiSanPham.SUA, false));
        dung(qlBt.layHangDangBan().isEmpty(), "Ngừng sản phẩm cha thì mọi SKU không bán được");
        qlBt.sua("BT2", new BienTheSanPham("BT2", "SP1", "Thùng 24 hộp", KieuDongGoi.THUNG, 24, 170_000, 0, false));
        loi(IllegalStateException.class,
                () -> qlBt.sua("BT2", new BienTheSanPham("BT2", "SP1", "Thùng 24 hộp", KieuDongGoi.THUNG, 24, 170_000, 0, true)),
                "Không mở SKU khi sản phẩm cha đang ngừng");

        // Khuyến mãi: chọn tốt nhất (KT11), bằng nhau chọn mã nhỏ (KT15), không mở lại khi đã ngừng
        QuanLyKhuyenMai qlKm = new QuanLyKhuyenMai(qlBt);
        LocalDate tu = LocalDate.of(2026, 10, 1);
        LocalDate den = LocalDate.of(2026, 10, 31);
        qlKm.them(new KhuyenMaiSoTien("KM-C", "Giảm 2000", "BT1", tu, den, true, 2_000));
        qlKm.them(new KhuyenMaiPhanTram("KM-B", "Giảm 10%", "BT1", tu, den, true, 10));
        qlKm.them(new KhuyenMaiSoTien("KM-D", "Hết hạn", "BT1", tu, tu, true, 9_000));
        dung(qlKm.timTotNhat("BT1", den, 30_000, 2).getMaKhuyenMai().equals("KM-B"), "KT11 chọn giảm 6.000 thay vì 4.000");
        qlKm.them(new KhuyenMaiSoTien("KM-A", "Giảm 3000", "BT1", tu, den, true, 3_000));
        dung(qlKm.timTotNhat("BT1", den, 30_000, 2).getMaKhuyenMai().equals("KM-A"), "KT15 hai KM bằng nhau chọn mã nhỏ nhất");
        dung(qlKm.timTotNhat("BT1", tu, 30_000, 2).getMaKhuyenMai().equals("KM-D"), "KM chỉ hiệu lực ngày đầu vẫn được chọn");
        dung(qlKm.timTotNhat("BT2", den, 30_000, 2) == null, "SKU không có KM trả null");
        loi(IllegalArgumentException.class,
                () -> qlKm.them(new KhuyenMaiSoTien("KM-E", "x", "BT9", tu, den, true, 1)), "KM cho SKU không tồn tại bị từ chối");
        qlKm.sua("KM-A", new KhuyenMaiSoTien("KM-A", "Giảm 1000", "BT1", tu, den, true, 1_000));
        dung(((KhuyenMaiSoTien) qlKm.timTheoMa("KM-A")).getSoTienGiamMoiDonVi() == 1_000, "Sửa mức giảm đúng lớp con");
        loi(IllegalArgumentException.class,
                () -> qlKm.sua("KM-A", new KhuyenMaiPhanTram("KM-A", "x", "BT1", tu, den, true, 5)), "Không đổi loại giảm");
        qlKm.sua("KM-A", new KhuyenMaiSoTien("KM-A", "Giảm 1000", "BT1", tu, den, false, 1_000));
        loi(IllegalStateException.class,
                () -> qlKm.sua("KM-A", new KhuyenMaiSoTien("KM-A", "Giảm 1000", "BT1", tu, den, true, 1_000)),
                "KM đã ngừng không mở lại được");

        // KT31: xóa qua interface trên sáu manager, sửa đơn/phiếu đều bị từ chối
        QuanLyDonHang qlDh = new QuanLyDonHang();
        QuanLyPhieuNhap qlPn = new QuanLyPhieuNhap();
        List<IQuanLy<?>> sauManager = List.of(qlTk, qlSpThat, qlBt, qlKm, qlDh, qlPn);
        for (IQuanLy<?> manager : sauManager) {
            loi(UnsupportedOperationException.class, () -> manager.xoa("X"),
                    "KT31 xóa trên " + manager.getClass().getSimpleName());
        }
        loi(UnsupportedOperationException.class, () -> qlDh.sua("X", null), "KT31 sửa đơn hàng bị từ chối");
        loi(UnsupportedOperationException.class, () -> qlPn.sua("X", null), "KT31 sửa phiếu nhập bị từ chối");

        // Đơn/phiếu: thêm đơn mới, chặn trùng; lọc theo ngày thanh toán
        LocalDateTime t0 = LocalDateTime.of(2026, 10, 10, 9, 0);
        DonHang don = new DonHang("KH1", "KH1", gio(dong(1, 30_000, null, 0)), t0);
        qlDh.them(don);
        loi(IllegalArgumentException.class, () -> qlDh.them(don), "Đơn trùng mã bị từ chối");
        don.chotThanhToan(gio(dong(1, 30_000, null, 0)), "NV1", t0.plusDays(2), 0);
        dung(qlDh.timDaThanhToanTrongKhoang(t0.toLocalDate(), t0.toLocalDate()).isEmpty()
                        && qlDh.timDaThanhToanTrongKhoang(t0.toLocalDate().plusDays(2), t0.toLocalDate().plusDays(2)).size() == 1,
                "KT25 đơn tính theo ngày thanh toán");
        loi(IllegalArgumentException.class, () -> qlDh.timDaThanhToanTrongKhoang(den, tu), "Khoảng ngày ngược bị từ chối");
        DanhSachChiTietPhieuNhap ctPn = new DanhSachChiTietPhieuNhap();
        ctPn.them(new ChiTietPhieuNhap("BT2", "Sữa", "Thùng 24", 3, 200_000));
        PhieuNhap pn = new PhieuNhap("QL1", "NCC", t0, ctPn);
        qlPn.them(pn);
        loi(IllegalArgumentException.class, () -> qlPn.them(pn), "KT20 ghi nhận lại phiếu cùng mã bị từ chối");

        // napDanhSach: kiểm tra cả lô, giữ nguyên danh tính manager và danh sách cũ khi lỗi
        loi(IllegalArgumentException.class,
                () -> qlSpThat.napDanhSach(List.of(sua, sua)), "Nạp lô trùng mã bị từ chối");
        dung(qlSpThat.xuat().size() == 1, "Nạp lỗi không làm đổi danh sách");
    }

    // ---------- KT21, KT23, KT24, KT33: lưu trữ file text ----------

    static final Path FILE_MAU = Path.of("data", "du-lieu.txt");
    static final String MAT_KHAU_MAU = "VnmDemo!2026";

    static void kiemThuLuuTru() {
        try {
            Path thuMuc = Files.createTempDirectory("vnm-kiemthu");
            Path file = Files.copy(FILE_MAU, thuMuc.resolve("du-lieu.txt"));
            String noiDungMau = Files.readString(file);

            // KT33: bộ mẫu ≥ 50 bản ghi, nạp được, ghi lại y hệt
            BoDuLieu bo = new BoDuLieu(file);
            dung(!new BoDuLieu(thuMuc.resolve("chua-co.txt")).luuTru.tonTai(), "Nhận biết file chưa tồn tại");
            bo.luuTru.docDuLieu();
            dung(noiDungMau.lines().count() - 2 >= 50 && noiDungMau.endsWith("END|60\n"), "KT33 bộ mẫu có 60 bản ghi");
            dung(bo.taiKhoan.xuat().size() == 5 && bo.sanPham.xuat().size() == 5 && bo.bienThe.xuat().size() == 15
                    && bo.khuyenMai.xuat().size() == 3 && bo.donHang.xuat().size() == 8 && bo.phieuNhap.xuat().size() == 1,
                    "KT33 nạp đủ tám bảng");
            dung(bo.luuTru.xuatThanhChuoi().equals(noiDungMau), "KT33/KT24 ghi lại đúng từng byte bộ mẫu");
            for (String ten : List.of("demo.quanly", "demo.nhanvien", "demo.khach01", "demo.khach02", "demo.khach03")) {
                dung(bo.taiKhoan.timTheoTenDangNhap(ten).kiemTraMatKhau(MAT_KHAU_MAU.toCharArray()),
                        "KT33 xác thực được tài khoản mẫu " + ten);
            }
            dung(!bo.taiKhoan.timTheoTenDangNhap("demo.quanly").kiemTraMatKhau("sai".toCharArray()), "Sai mật khẩu bị từ chối");
            dung(bo.taiKhoan.timTheoTenDangNhap("demo.quanly") instanceof QuanLyCuaHang
                    && bo.taiKhoan.timTheoTenDangNhap("demo.nhanvien") instanceof NhanVienBanHang
                    && ((KhachHang) bo.taiKhoan.timTheoTenDangNhap("demo.khach01")).getDiemTichLuy() == 2,
                    "KT24 dựng đúng lớp con tài khoản và điểm");
            dung(bo.khuyenMai.xuat().stream().filter(km -> km instanceof KhuyenMaiPhanTram).count() == 1,
                    "KT24 dựng đúng lớp con khuyến mãi");

            // KT24: đổi trong RAM, lưu, mở lại bằng bộ manager mới → giống hệt
            BienTheSanPham bt = bo.bienThe.xuat().getFirst();
            bo.bienThe.sua(bt.getMaBienThe(), new BienTheSanPham(bt.getMaBienThe(), bt.getMaSanPham(), bt.getTenQuyCach(),
                    bt.getKieuDongGoi(), bt.getSoDonViCoSo(), 9_000, bt.getSoLuongTon(), true));
            bo.luuTru.ghiAnToan();
            BoDuLieu moLai = new BoDuLieu(file);
            moLai.luuTru.docDuLieu();
            dung(moLai.bienThe.timTheoMa(bt.getMaBienThe()).getGiaBan() == 9_000
                    && moLai.luuTru.xuatThanhChuoi().equals(bo.luuTru.xuatThanhChuoi()), "KT24 lưu rồi mở lại khớp hoàn toàn");
            dung(!Files.exists(thuMuc.resolve("du-lieu.tmp")), "Không còn file tạm sau khi lưu");

            // KT23: file hỏng → báo lỗi, không nạp một phần
            String hopLe = moLai.luuTru.xuatThanhChuoi();
            Map<String, String> fileHong = new LinkedHashMap<>();
            fileHong.put("rỗng", "");
            fileHong.put("sai dòng đầu", hopLe.replace("VNM|1", "VNM|2"));
            fileHong.put("thiếu END", hopLe.replace("END|60\n", ""));
            fileHong.put("END sai số", hopLe.replace("END|60", "END|59"));
            fileHong.put("dòng trống ở giữa", hopLe.replaceFirst("\nSAN_PHAM", "\n\nSAN_PHAM"));
            fileHong.put("thừa cột", suaDong(hopLe, "SAN_PHAM|", d -> d + "|x"));
            fileHong.put("ID trùng", suaDong(hopLe, "SAN_PHAM|", d -> d + "\n" + d).replace("END|60", "END|61"));
            fileHong.put("vai trò lạ", hopLe.replace("|NHAN_VIEN_BAN_HANG|", "|BAO_VE|"));
            fileHong.put("FK sản phẩm sai", suaDong(hopLe, "BIEN_THE|", d -> d.replace("|94329236", "|X4329236")));
            fileHong.put("tồn lệch", hopLe.replace("|30000|98|", "|30000|97|"));
            fileHong.put("điểm lệch", suaDong(hopLe, "TAI_KHOAN|8b2a1983", d -> d.substring(0, d.length() - 1) + "3"));
            fileHong.put("chi tiết mồ côi", suaDong(hopLe, "CT_DON_HANG|", d -> d.replaceFirst("CT_DON_HANG\\|.", "CT_DON_HANG|Z")));
            fileHong.put("tổng đơn sai", hopLe.replace("|60000|6000|0|54000", "|60000|6000|0|50000"));
            fileHong.put("không còn quản lý", suaDong(hopLe, "TAI_KHOAN|fb5e2d83", d -> d.replace("|true|", "|false|")));
            fileHong.put("có ký tự null", hopLe.replace("|true|\nTAI_KHOAN", "|null|\nTAI_KHOAN"));
            for (Map.Entry<String, String> hong : fileHong.entrySet()) {
                loi(IllegalArgumentException.class, () -> moLai.luuTru.napTuChuoi(hong.getValue()), "KT23 " + hong.getKey());
                dung(moLai.luuTru.xuatThanhChuoi().equals(hopLe), "KT23 " + hong.getKey() + ": RAM giữ nguyên");
            }
            try {
                moLai.luuTru.napTuChuoi(fileHong.get("thừa cột"));
            } catch (IllegalArgumentException e) {
                dung(e.getMessage().startsWith("Dòng 7:"), "KT23 thông báo đúng số dòng lỗi: " + e.getMessage());
            }

            // File rỗng trên đĩa: báo lỗi, không ghi đè
            Path rong = Files.writeString(thuMuc.resolve("rong.txt"), "");
            loi(IllegalArgumentException.class, () -> chay(() -> new BoDuLieu(rong).luuTru.docDuLieu()), "File rỗng báo lỗi");
            dung(Files.readString(rong).isEmpty(), "File rỗng không bị ghi đè");

            // KT21 (phần file): thư mục không ghi được → lỗi IO, file chính giữ nguyên
            String truocKhiLoi = Files.readString(file);
            Set<PosixFilePermission> quyenCu = Files.getPosixFilePermissions(thuMuc);
            Files.setPosixFilePermissions(thuMuc, PosixFilePermissions.fromString("r-x------"));
            try {
                loi(java.io.UncheckedIOException.class, () -> chay(moLai.luuTru::ghiAnToan), "KT21 lỗi ghi file được báo");
            } finally {
                Files.setPosixFilePermissions(thuMuc, quyenCu);
            }
            dung(Files.readString(file).equals(truocKhiLoi), "KT21 file chính giữ nguyên khi ghi lỗi");
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    // ---------- KT01–KT04, KT19–KT21, KT28: dịch vụ tài khoản, danh mục, nhập hàng ----------

    static void kiemThuDichVuTaiKhoanVaNhapHang() {
        Path thuMuc = chay(() -> Files.createTempDirectory("vnm-dichvu"));

        // Khởi tạo hệ thống khi chưa có file
        BoDuLieu moi = new BoDuLieu(thuMuc.resolve("moi").resolve("du-lieu.txt"));
        dung(moi.dichVu.canKhoiTao(), "Chưa có file thì được khởi tạo");
        String maQlDau = moi.dichVu.khoiTaoHeThong("admin", "matkhau1".toCharArray(), "Quản lý đầu", "0900000009");
        dung(!moi.dichVu.canKhoiTao(), "Đã có dữ liệu thì không khởi tạo lại");
        loi(IllegalStateException.class,
                () -> moi.dichVu.khoiTaoHeThong("admin2", "matkhau1".toCharArray(), "X", "0900000009"), "Không khởi tạo lần hai");
        BoDuLieu moiMoLai = new BoDuLieu(moi.luuTru.getDuongDan());
        chay(moiMoLai.luuTru::docDuLieu);
        dung(moiMoLai.taiKhoan.timTheoMa(maQlDau) instanceof QuanLyCuaHang, "File khởi tạo có quản lý đầu tiên");

        // Làm việc trên bản sao bộ mẫu
        Path file = chay(() -> Files.copy(FILE_MAU, thuMuc.resolve("du-lieu.txt")));
        BoDuLieu bo = new BoDuLieu(file);
        chay(bo.luuTru::docDuLieu);
        DichVuCuaHang dv = bo.dichVu;
        char[] mk = MAT_KHAU_MAU.toCharArray();
        String maQl = dv.dangNhap("demo.quanly", mk);
        String maNv = dv.dangNhap(" DEMO.NhanVien ", mk);
        loi(IllegalArgumentException.class, () -> dv.dangNhap("demo.quanly", "sai".toCharArray()), "Sai mật khẩu");
        loi(IllegalArgumentException.class, () -> dv.dangNhap("khong.co", mk), "Tên không tồn tại");

        // KT01: tự đăng ký chỉ tạo khách hàng điểm 0, lưu xuống file
        String maKh = dv.dangKyKhachHang("khach.moi", "matkhau1".toCharArray(), "Khách Mới", "0911222333");
        dung(dv.xemKhachHang(maKh, maKh).getDiemTichLuy() == 0, "KT01 khách mới điểm 0");
        dung(chay(() -> Files.readString(file)).contains("|KHACH_HANG|khach.moi|"), "KT01 đăng ký đã lưu file");
        // KT02
        int soTk = bo.taiKhoan.xuat().size();
        loi(IllegalArgumentException.class,
                () -> dv.dangKyKhachHang("  KHACH.MOI ", "matkhau1".toCharArray(), "X", "0911222333"), "KT02 trùng tên sau chuẩn hóa");
        loi(IllegalArgumentException.class,
                () -> dv.dangKyKhachHang("ab", "123".toCharArray(), "X", "0911222333"), "Mật khẩu quá ngắn");
        loi(IllegalArgumentException.class,
                () -> dv.dangKyKhachHang("abc", "matkhau1".toCharArray(), "X", "12345"), "Số điện thoại sai");
        dung(bo.taiKhoan.xuat().size() == soTk, "KT02 không tạo thêm tài khoản");

        // Phân quyền tại dịch vụ
        loi(SecurityException.class,
                () -> dv.taoTaiKhoanNoiBo(maKh, VaiTro.QUAN_LY_CUA_HANG, "x", mk, "X", "0900000000"), "Khách không tạo tài khoản nội bộ");
        loi(SecurityException.class, () -> dv.themSanPham(maNv, "Sữa", "V", "hộp", "1 l", null, LoaiSanPham.SUA),
                "Nhân viên không quản lý danh mục");
        loi(SecurityException.class, () -> dv.xemKhachHang(maKh, bo.taiKhoan.timTheoTenDangNhap("demo.khach01").getMaTaiKhoan()),
                "Khách không xem điểm người khác");
        dung(dv.traThanhVien(maNv, "0911222333").size() == 1, "Nhân viên tra thành viên theo số điện thoại");
        loi(SecurityException.class, () -> dv.traThanhVien(maKh, "0911222333"), "Khách không tra thành viên");
        loi(IllegalStateException.class, () -> dv.layTaiKhoanDangNhap("ma-khong-co"), "Mã phiên lạ bị từ chối");

        // KT04: không khóa quản lý hoạt động cuối cùng
        loi(IllegalStateException.class, () -> dv.khoaTaiKhoan(maQl, maQl), "KT04 không khóa quản lý cuối cùng");
        String maQl2 = dv.taoTaiKhoanNoiBo(maQl, VaiTro.QUAN_LY_CUA_HANG, "quanly2", "matkhau2".toCharArray(),
                "Quản lý hai", "0900000010");
        loi(IllegalArgumentException.class,
                () -> dv.taoTaiKhoanNoiBo(maQl, VaiTro.KHACH_HANG, "kh", mk, "X", "0900000000"), "Không tạo khách từ màn nội bộ");
        dung(dv.khoaTaiKhoan(maQl2, maQl2), "Quản lý tự khóa khi còn quản lý khác: kết thúc phiên");
        loi(IllegalStateException.class, () -> dv.danhSachTaiKhoan(maQl2), "Tài khoản đã khóa không thao tác được");
        loi(IllegalStateException.class, () -> dv.dangNhap("quanly2", "matkhau2".toCharArray()), "Tài khoản khóa không đăng nhập được");
        loi(IllegalStateException.class, () -> dv.khoaTaiKhoan(maQl, maQl), "KT04 vẫn còn đúng một quản lý thì không khóa");
        dv.moKhoaTaiKhoan(maQl, maQl2);
        dung(dv.layTaiKhoanDangNhap(maQl2) instanceof QuanLyCuaHang, "Mở lại tài khoản");

        // KT28: khóa khách đang có phiên → mọi thao tác bằng phiên đó bị chặn ngay
        dv.khoaTaiKhoan(maQl, maKh);
        loi(IllegalStateException.class, () -> dv.xemKhachHang(maKh, maKh), "KT28 phiên của tài khoản đã khóa mất quyền");
        dv.moKhoaTaiKhoan(maQl, maKh);

        // Danh mục: SKU mới tồn 0; nhân viên không thấy SKU ngừng bán, quản lý thấy
        String maSp = dv.themSanPham(maQl, "Sữa đặc", "Ông Thọ", "lon", "380 g", null, LoaiSanPham.SUA);
        String maLon = dv.themBienThe(maQl, maSp, "1 lon 380 g", KieuDongGoi.BAN_LE, 1, 25_000);
        String maThung = dv.themBienThe(maQl, maSp, "Thùng 24 lon", KieuDongGoi.THUNG, 24, 560_000);
        dung(bo.bienThe.timTheoMa(maLon).getSoLuongTon() == 0, "SKU mới tồn 0");
        dv.capNhatBienThe(maQl, maLon, 26_000, false);
        dung(dv.timBienThe(maNv, "sữa đặc").size() == 1 && dv.timBienThe(maQl, "sữa đặc").size() == 2,
                "Nhân viên không thấy SKU ngừng bán, quản lý thấy");
        dung(dv.timBienThe(maNv, maThung.substring(0, 8)).size() == 1, "Tìm SKU theo phần đầu mã");
        dv.capNhatSanPham(maQl, maSp, "Sữa đặc có đường", "Ông Thọ", "Lon thiếc", false);
        dung(dv.timBienThe(maNv, "sữa đặc").isEmpty(), "Ngừng sản phẩm cha ẩn mọi SKU với nhân viên");
        dv.capNhatSanPham(maQl, maSp, "Sữa đặc có đường", "Ông Thọ", "Lon thiếc", true);

        // KT19: nhập 3 thùng 200.000đ → tồn thùng +3, các quy cách khác không đổi
        BienTheSanPham thungSuaTuoi = bo.bienThe.timTheoMa("5155fb99-487d-4bab-869c-2ed7810b0716");
        int tonThung = thungSuaTuoi.getSoLuongTon();
        int tonLoc = bo.bienThe.timTheoMa("6c910bdf-d7bc-4493-b9ca-766d7b8bf293").getSoLuongTon();
        DanhSachChiTietPhieuNhap phieu = new DanhSachChiTietPhieuNhap();
        dv.themVaoPhieuNhap(maQl, phieu, thungSuaTuoi.getMaBienThe(), 2, 200_000);
        dv.themVaoPhieuNhap(maQl, phieu, thungSuaTuoi.getMaBienThe(), 1, 200_000);
        loi(IllegalArgumentException.class,
                () -> dv.themVaoPhieuNhap(maQl, phieu, thungSuaTuoi.getMaBienThe(), 1, 210_000), "Cùng SKU khác giá trên phiếu");
        loi(SecurityException.class, () -> dv.nhapHang(maNv, "NCC", phieu), "Nhân viên không nhập hàng");
        String maPhieu = dv.nhapHang(maQl, "Nhà phân phối A", phieu);
        dung(bo.bienThe.timTheoMa(thungSuaTuoi.getMaBienThe()).getSoLuongTon() == tonThung + 3
                        && bo.bienThe.timTheoMa("6c910bdf-d7bc-4493-b9ca-766d7b8bf293").getSoLuongTon() == tonLoc,
                "KT19 tồn thùng +3, tồn lốc không đổi");
        dung(dv.xemPhieuNhap(maQl, maPhieu).tinhTongTien() == 600_000, "KT19 chi nhập 600.000");
        DanhSachChiTietPhieuNhap phieuRong = new DanhSachChiTietPhieuNhap();
        loi(IllegalArgumentException.class, () -> dv.nhapHang(maQl, "NCC", phieuRong), "Phiếu rỗng bị từ chối");
        loi(IllegalArgumentException.class, () -> dv.nhapHang(maQl, " ", phieu), "Nguồn nhập trống bị từ chối");

        // KT21: lưu lỗi khi sửa giá / nhập hàng → RAM và file về trạng thái cũ
        String fileTruoc = chay(() -> Files.readString(file));
        String ramTruoc = bo.luuTru.xuatThanhChuoi();
        Set<PosixFilePermission> quyenCu = chay(() -> Files.getPosixFilePermissions(thuMuc));
        chay(() -> Files.setPosixFilePermissions(thuMuc, PosixFilePermissions.fromString("r-x------")));
        try {
            loi(IllegalStateException.class, () -> dv.capNhatBienThe(maQl, maThung, 999_000, true), "KT21 sửa giá lỗi lưu");
            DanhSachChiTietPhieuNhap phieu2 = new DanhSachChiTietPhieuNhap();
            dv.themVaoPhieuNhap(maQl, phieu2, maThung, 5, 500_000);
            loi(IllegalStateException.class, () -> dv.nhapHang(maQl, "NCC", phieu2), "KT21 nhập hàng lỗi lưu");
            dung(!phieu2.isDaChot(), "KT21 phiếu đang soạn vẫn sửa được sau lỗi lưu");
        } finally {
            chay(() -> Files.setPosixFilePermissions(thuMuc, quyenCu));
        }
        dung(bo.luuTru.xuatThanhChuoi().equals(ramTruoc), "KT21 RAM về trạng thái cũ");
        dung(bo.bienThe.timTheoMa(maThung).getGiaBan() == 560_000 && bo.bienThe.timTheoMa(maThung).getSoLuongTon() == 0,
                "KT21 giá và tồn không đổi");
        dung(chay(() -> Files.readString(file)).equals(fileTruoc), "KT21 file chính không đổi");

        // Toàn bộ thay đổi vẫn đọc lại hợp lệ (đối chiếu tồn/điểm qua chứng từ)
        BoDuLieu moLai = new BoDuLieu(file);
        chay(moLai.luuTru::docDuLieu);
        dung(moLai.luuTru.xuatThanhChuoi().equals(bo.luuTru.xuatThanhChuoi()), "Mở lại sau các giao dịch khớp RAM");
    }

    // ---------- KT03, KT05–KT10, KT14, KT16–KT18, KT21, KT22: đặt hàng và thanh toán ----------

    static final String LOC_SUA_TUOI = "6c910bdf-d7bc-4493-b9ca-766d7b8bf293";
    static final String HOP_SUA_TUOI = "0582a2b4-b1b0-4286-a394-47927cf144b7";
    static final String THUNG_SUA_TUOI = "5155fb99-487d-4bab-869c-2ed7810b0716";
    static final String LOC_SUA_CHUA = "e22b5090-40d5-44a9-8cdc-096f954c2103";

    static void kiemThuDatHangVaThanhToan() {
        Path thuMuc = chay(() -> Files.createTempDirectory("vnm-donhang"));
        Path file = chay(() -> Files.copy(FILE_MAU, thuMuc.resolve("du-lieu.txt")));
        BoDuLieu bo = new BoDuLieu(file);
        chay(bo.luuTru::docDuLieu);
        DichVuCuaHang dv = bo.dichVu;
        char[] mk = MAT_KHAU_MAU.toCharArray();
        String ql = dv.dangNhap("demo.quanly", mk);
        String nv = dv.dangNhap("demo.nhanvien", mk);
        String kh1 = dv.dangNhap("demo.khach01", mk);
        String kh2 = dv.dangNhap("demo.khach02", mk);
        LocalDate homNay = DichVuCuaHang.homNay();

        // KT05: bán 2 lốc → chỉ tồn lốc giảm 2
        int tonLoc = ton(bo, LOC_SUA_TUOI);
        int tonHop = ton(bo, HOP_SUA_TUOI);
        int tonThung = ton(bo, THUNG_SUA_TUOI);
        DanhSachChiTietDonHang gio = new DanhSachChiTietDonHang();
        dv.themVaoGio(nv, gio, LOC_SUA_TUOI, 2);
        String donVangLai = dv.lapDonTaiQuay(nv, null, gio);
        dung(ton(bo, LOC_SUA_TUOI) == tonLoc, "Đơn chờ không giữ/trừ tồn");
        // KT16: vãng lai không dùng điểm, cộng 0
        loi(IllegalArgumentException.class, () -> dv.xemTruocThanhToan(nv, donVangLai, 1), "KT16 vãng lai không dùng điểm");
        DonHang daTra = thanhToan(dv, nv, donVangLai, 0);
        dung(daTra.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN && daTra.getDiemCong() == 0
                && nv.equals(daTra.getMaNguoiThuTien()) && daTra.getThoiDiemThanhToan() != null,
                "KT16 vãng lai đã thanh toán, 0 điểm, có người và thời điểm thu");
        dung(ton(bo, LOC_SUA_TUOI) == tonLoc - 2 && ton(bo, HOP_SUA_TUOI) == tonHop && ton(bo, THUNG_SUA_TUOI) == tonThung,
                "KT05 tồn lốc −2, tồn hộp/thùng giữ nguyên");
        // KT17: thanh toán lặp bị chặn, in lại chỉ đọc
        loi(IllegalStateException.class, () -> dv.thanhToan(nv, donVangLai, 0, daTra.getTongThanhToan()), "KT17 thanh toán lặp");
        dung(ton(bo, LOC_SUA_TUOI) == tonLoc - 2 && dv.xemDon(nv, donVangLai) == daTra, "KT17 tồn không đổi, xem lại hóa đơn");

        // Có khuyến mãi riêng của bài kiểm thử, không phụ thuộc hạn của bộ mẫu
        bo.khuyenMai.them(new KhuyenMaiPhanTram("Giảm 10% lốc sữa chua", LOC_SUA_CHUA, homNay.minusDays(1),
                homNay.plusDays(1), 10));

        // Dùng điểm: khách02 có 6 điểm, mua 2 lốc sữa chua 26.000 giảm 10%
        int diemTruoc = diem(bo, kh2);
        DanhSachChiTietDonHang gio2 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh2, gio2, LOC_SUA_CHUA, 2);
        DanhSachChiTietDonHang baoGia = dv.baoGia(kh2, gio2);
        dung(baoGia.tinhTongTienHang() == 52_000 && baoGia.tinhTongGiam() == 5_200, "Báo giá tạm tính có khuyến mãi");
        String donKh2 = dv.datHang(kh2, gio2);
        dung(dv.diemToiDaDuocDung(nv, donKh2) == Math.min(diemTruoc, 93), "Điểm tối đa = min(điểm có, floor(46.800/500))");
        loi(IllegalArgumentException.class, () -> dv.xemTruocThanhToan(nv, donKh2, diemTruoc + 1), "Không dùng quá điểm đang có");
        loi(SecurityException.class, () -> dv.thanhToan(kh2, donKh2, 0, 0), "Khách không tự thanh toán");
        loi(IllegalStateException.class, () -> dv.thanhToan(nv, donKh2, 6, 1), "Tổng khác bảng tính đã xác nhận bị từ chối");
        dung(dv.xemDon(nv, donKh2).getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN && diem(bo, kh2) == diemTruoc,
                "Từ chối tổng lệch: đơn vẫn chờ, điểm không đổi");
        DonHang donDiem = thanhToan(dv, nv, donKh2, 6);
        dung(donDiem.getTienGiamDiem() == 600 && donDiem.getTongThanhToan() == 46_200 && donDiem.getDiemCong() == 4,
                "Dùng 6 điểm: trả 46.200, cộng 4");
        dung(diem(bo, kh2) == diemTruoc - 6 + 4, "Điểm sau giao dịch = có − dùng + cộng");
        dung(dv.lichSuDiem(kh2, kh2).contains(donDiem), "Lịch sử điểm có đơn vừa thanh toán");

        // KT03: khách không xem/hủy đơn của khách khác
        loi(SecurityException.class, () -> dv.xemDon(kh1, donKh2), "KT03 khách xem đơn người khác");
        loi(SecurityException.class, () -> dv.huyDon(kh1, donKh2), "KT03 khách hủy đơn người khác");
        dung(dv.danhSachDon(kh1, null).stream().allMatch(dh -> kh1.equals(dh.getMaKhachHang())), "KT03 khách chỉ thấy đơn mình");

        // KT08: đặt rồi hủy → chỉ đổi trạng thái
        int tonSuaChua = ton(bo, LOC_SUA_CHUA);
        DanhSachChiTietDonHang gio3 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh1, gio3, LOC_SUA_CHUA, 1);
        String donHuy = dv.datHang(kh1, gio3);
        int diemKh1 = diem(bo, kh1);
        dv.huyDon(kh1, donHuy);
        dung(dv.xemDon(kh1, donHuy).getTrangThai() == TrangThaiDonHang.DA_HUY && ton(bo, LOC_SUA_CHUA) == tonSuaChua
                && diem(bo, kh1) == diemKh1, "KT08 hủy không đổi tồn/điểm");
        loi(IllegalStateException.class, () -> dv.huyDon(kh1, donHuy), "Không hủy lại đơn đã hủy");
        loi(IllegalStateException.class, () -> dv.xemTruocThanhToan(nv, donHuy, 0), "Không thanh toán đơn đã hủy");

        // KT09: giá tăng sau khi đặt → thanh toán theo giá hiện tại
        DanhSachChiTietDonHang gio4 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh1, gio4, HOP_SUA_TUOI, 3);
        String donGiaTang = dv.datHang(kh1, gio4);
        dv.capNhatBienThe(ql, HOP_SUA_TUOI, 9_000, true);
        DonHang xemTruoc = dv.xemTruocThanhToan(nv, donGiaTang, 0);
        dung(dv.xemDon(nv, donGiaTang).getTongTienHang() == 24_000 && xemTruoc.getTongTienHang() == 27_000,
                "KT09 tạm tính 24.000, bảng cuối theo giá mới 27.000");
        dung(dv.xemDon(nv, donGiaTang).getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN, "Xem trước không đổi đơn thật");

        // KT10: SKU ngừng bán → không thanh toán, không tự bỏ dòng
        dv.capNhatBienThe(ql, HOP_SUA_TUOI, 9_000, false);
        loi(IllegalArgumentException.class, () -> dv.xemTruocThanhToan(nv, donGiaTang, 0), "KT10 SKU ngừng bán chặn thanh toán");
        loi(IllegalArgumentException.class, () -> dv.themVaoGio(kh1, new DanhSachChiTietDonHang(), HOP_SUA_TUOI, 1),
                "Không chọn SKU ngừng bán vào giỏ");
        dv.capNhatBienThe(ql, HOP_SUA_TUOI, 9_000, true);

        // KT06, KT07: SKU tồn 5
        String maSp = dv.themSanPham(ql, "Sữa hạt", "Vinamilk", "hộp", "180 ml", null, LoaiSanPham.SUA);
        String sku5 = dv.themBienThe(ql, maSp, "Lốc 4 hộp 180 ml", KieuDongGoi.LOC, 4, 40_000);
        DanhSachChiTietPhieuNhap phieu = new DanhSachChiTietPhieuNhap();
        dv.themVaoPhieuNhap(ql, phieu, sku5, 5, 30_000);
        dv.nhapHang(ql, "NCC", phieu);
        DanhSachChiTietDonHang gio5 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh1, gio5, sku5, 3);
        loi(IllegalArgumentException.class, () -> dv.themVaoGio(kh1, gio5, sku5, 3), "KT06 gộp thành 6 vượt tồn 5");
        dung(gio5.timTheoBienThe(sku5).getSoLuong() == 3, "KT06 giỏ giữ nguyên sau lỗi");
        loi(IllegalArgumentException.class, () -> dv.themVaoGio(kh1, gio5, sku5, 0), "KT26 số lượng 0 bị từ chối");
        dv.suaSoLuongTrongGio(kh1, gio5, sku5, 4);
        String donA = dv.datHang(kh1, gio5);
        DanhSachChiTietDonHang gio6 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh2, gio6, sku5, 4);
        String donB = dv.datHang(kh2, gio6);
        thanhToan(dv, nv, donA, 0);
        dung(ton(bo, sku5) == 1, "KT07 thanh toán đơn thứ nhất còn 1");
        loi(IllegalArgumentException.class, () -> dv.xemTruocThanhToan(nv, donB, 0), "KT07 đơn thứ hai thiếu tồn");
        dung(dv.xemDon(nv, donB).getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN, "KT07 đơn thứ hai vẫn chờ");

        // KT14: KM 100% → đơn 0đ, không tích điểm nhưng vẫn trừ tồn
        bo.khuyenMai.them(new KhuyenMaiPhanTram("Tặng 100%", sku5, homNay, homNay, 100));
        DanhSachChiTietDonHang gio7 = new DanhSachChiTietDonHang();
        dv.themVaoGio(nv, gio7, sku5, 1);
        String don0 = dv.lapDonTaiQuay(nv, kh1, gio7);
        DonHang da0 = thanhToan(dv, nv, don0, 0);
        dung(da0.getTongThanhToan() == 0 && da0.getDiemCong() == 0 && ton(bo, sku5) == 0, "KT14 đơn 0đ trừ tồn, không tích điểm");

        // KT18: đổi giá/ngừng KM sau khi bán → hóa đơn cũ không đổi
        long tongCu = donDiem.getTongThanhToan();
        dv.capNhatBienThe(ql, LOC_SUA_CHUA, 99_000, true);
        for (KhuyenMai km : bo.khuyenMai.timTheoBienThe(LOC_SUA_CHUA)) {
            bo.khuyenMai.sua(km.getMaKhuyenMai(), km instanceof KhuyenMaiPhanTram pt
                    ? new KhuyenMaiPhanTram(km.getMaKhuyenMai(), km.getTenKhuyenMai(), km.getMaBienThe(), km.getTuNgay(),
                    km.getDenNgay(), false, pt.getTyLePhanTram())
                    : new KhuyenMaiSoTien(km.getMaKhuyenMai(), km.getTenKhuyenMai(), km.getMaBienThe(), km.getTuNgay(),
                    km.getDenNgay(), false, ((KhuyenMaiSoTien) km).getSoTienGiamMoiDonVi()));
        }
        DonHang hoaDonCu = dv.xemDon(nv, donKh2);
        dung(hoaDonCu.getTongThanhToan() == tongCu && hoaDonCu.getChiTiet().getFirst().getDonGia() == 26_000
                && hoaDonCu.getChiTiet().getFirst().getMaKhuyenMai() != null, "KT18 hóa đơn cũ giữ giá và KM đã chốt");

        // KT21 khi thanh toán: lưu lỗi → đơn, tồn, điểm về như cũ
        DanhSachChiTietDonHang gio8 = new DanhSachChiTietDonHang();
        dv.themVaoGio(kh1, gio8, LOC_SUA_TUOI, 1);
        String donLoi = dv.datHang(kh1, gio8);
        String ramTruoc = bo.luuTru.xuatThanhChuoi();
        long tongLoi = dv.xemTruocThanhToan(nv, donLoi, 0).getTongThanhToan();
        Set<PosixFilePermission> quyenCu = chay(() -> Files.getPosixFilePermissions(thuMuc));
        chay(() -> Files.setPosixFilePermissions(thuMuc, PosixFilePermissions.fromString("r-x------")));
        try {
            loi(IllegalStateException.class, () -> dv.thanhToan(nv, donLoi, 0, tongLoi), "KT21 thanh toán lỗi lưu");
        } finally {
            chay(() -> Files.setPosixFilePermissions(thuMuc, quyenCu));
        }
        dung(bo.luuTru.xuatThanhChuoi().equals(ramTruoc)
                && dv.xemDon(nv, donLoi).getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN, "KT21 thanh toán lỗi: RAM như cũ");

        // Khách bị khóa không thanh toán được đơn của mình
        dv.khoaTaiKhoan(ql, kh1);
        loi(IllegalStateException.class, () -> dv.xemTruocThanhToan(nv, donLoi, 0), "Khách bị khóa chặn thanh toán");
        dv.moKhoaTaiKhoan(ql, kh1);
        loi(IllegalArgumentException.class, () -> dv.datHang(kh1, new DanhSachChiTietDonHang()), "Giỏ trống không đặt được");

        // KT22: mở lại sau commit → giao dịch đã hoàn tất, không thanh toán lại được
        BoDuLieu moLai = new BoDuLieu(file);
        chay(moLai.luuTru::docDuLieu);
        String nvMoi = moLai.dichVu.dangNhap("demo.nhanvien", mk);
        dung(moLai.dichVu.xemDon(nvMoi, donKh2).getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN
                && moLai.luuTru.xuatThanhChuoi().equals(bo.luuTru.xuatThanhChuoi()), "KT22/KT24 mở lại đúng trạng thái");
        loi(IllegalStateException.class, () -> moLai.dichVu.thanhToan(nvMoi, donKh2, 0, 0), "KT22 không ghi nhận thêm");
    }

    static DonHang thanhToan(DichVuCuaHang dv, String nguoiThu, String maDon, int diem) {
        return dv.thanhToan(nguoiThu, maDon, diem, dv.xemTruocThanhToan(nguoiThu, maDon, diem).getTongThanhToan());
    }

    static int ton(BoDuLieu bo, String maBienThe) {
        return bo.bienThe.timTheoMa(maBienThe).getSoLuongTon();
    }

    static int diem(BoDuLieu bo, String maKhach) {
        return ((KhachHang) bo.taiKhoan.timTheoMa(maKhach)).getDiemTichLuy();
    }

    /** Bộ sáu manager + lưu trữ + dịch vụ như Main tạo. */
    static final class BoDuLieu {
        final QuanLyTaiKhoan taiKhoan = new QuanLyTaiKhoan();
        final QuanLySanPham sanPham = new QuanLySanPham();
        final QuanLyBienTheSanPham bienThe = new QuanLyBienTheSanPham(sanPham);
        final QuanLyKhuyenMai khuyenMai = new QuanLyKhuyenMai(bienThe);
        final QuanLyDonHang donHang = new QuanLyDonHang();
        final QuanLyPhieuNhap phieuNhap = new QuanLyPhieuNhap();
        final LuuTruText luuTru;
        final DichVuCuaHang dichVu;

        BoDuLieu(Path file) {
            luuTru = new LuuTruText(file, taiKhoan, sanPham, bienThe, khuyenMai, donHang, phieuNhap);
            dichVu = new DichVuCuaHang(taiKhoan, sanPham, bienThe, khuyenMai, donHang, phieuNhap, luuTru);
        }
    }

    interface ThaoTacIO {
        void chay() throws IOException;
    }

    interface GiaTriIO<V> {
        V lay() throws IOException;
    }

    static void chay(ThaoTacIO thaoTac) {
        try {
            thaoTac.chay();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    static <V> V chay(GiaTriIO<V> thaoTac) {
        try {
            return thaoTac.lay();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /** Sửa dòng đầu tiên bắt đầu bằng tiền tố. */
    static String suaDong(String noiDung, String tienTo, UnaryOperator<String> sua) {
        List<String> dong = new ArrayList<>(noiDung.lines().toList());
        for (int i = 0; i < dong.size(); i++) {
            if (dong.get(i).startsWith(tienTo)) {
                dong.set(i, sua.apply(dong.get(i)));
                break;
            }
        }
        return String.join("\n", dong) + "\n";
    }

    // ---------- tiện ích ----------

    static final String SALT = "AAAAAAAAAAAAAAAAAAAAAA==";
    static final String BAM = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    /** Tài khoản giả với hash giả: kiểm tra danh sách mà không tốn thời gian băm PBKDF2. */
    static TaiKhoan taiKhoan(VaiTro vaiTro, String ma, String ten) {
        return switch (vaiTro) {
            case KHACH_HANG -> new KhachHang(ma, ten, SALT, 1, BAM, "Khách", "0900000000", true, 0);
            case NHAN_VIEN_BAN_HANG -> new NhanVienBanHang(ma, ten, SALT, 1, BAM, "Nhân viên", "0900000000", true);
            case QUAN_LY_CUA_HANG -> new QuanLyCuaHang(ma, ten, SALT, 1, BAM, "Quản lý", "0900000000", true);
        };
    }

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
