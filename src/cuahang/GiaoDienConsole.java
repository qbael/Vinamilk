package cuahang;

import java.io.Console;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public class GiaoDienConsole {
    private static final Locale TIENG_VIET = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DINH_DANG_NGAY =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DINH_DANG_THOI_DIEM = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");
    private static final int DO_DAI_MA_NGAN = 8;
    private static final int RONG_HOA_DON = 50;

    private final DichVuCuaHang dichVu;
    private final Scanner boDoc;
    private String maTaiKhoanDangNhap;
    private boolean hetNhap;

    public GiaoDienConsole(DichVuCuaHang dichVu, Scanner boDoc) {
        this.dichVu = dichVu;
        this.boDoc = boDoc;
    }

    // ================================================================ vòng lặp chính

    public void chay() {
        try {
            while (true) {
                int chon = chonMenu("CỬA HÀNG SỮA", "Thoát chương trình", "Đăng nhập", "Đăng ký khách hàng");
                switch (chon) {
                    case 1 -> thucHien(this::dangNhap);
                    case 2 -> thucHien(this::dangKy);
                    default -> {
                        inDong("Tạm biệt!");
                        return;
                    }
                }
                menuSauDangNhap();
            }
        } catch (NoSuchElementException e) {
            if (!hetNhap) {
                throw e;
            }
            inDong("");
            inDong("Hết dữ liệu nhập, kết thúc chương trình.");
        }
    }

    /** Lần chạy đầu chưa có file dữ liệu: xác nhận rồi nhập quản lý đầu tiên. */
    public boolean khoiTaoHeThong(Path duongDan) {
        inDong("Chưa có file dữ liệu tại: " + duongDan);
        inDong("Đây là lần khởi tạo hệ thống: bạn sẽ tạo tài khoản quản lý đầu tiên (không có mật khẩu mặc định).");
        if (!xacNhan("Khởi tạo hệ thống mới?")) {
            return false;
        }
        while (true) {
            String ten = nhap("Tên đăng nhập quản lý (Enter = hủy)");
            if (ten.isEmpty()) {
                return false;
            }
            char[] matKhau = nhapMatKhauHaiLan();
            if (matKhau == null) {
                continue;
            }
            String hoTen = nhap("Họ tên");
            String soDienThoai = nhap("Số điện thoại");
            try {
                dichVu.khoiTaoHeThong(ten, matKhau, hoTen, soDienThoai);
                inDong("Đã khởi tạo hệ thống. Hãy đăng nhập bằng tài khoản vừa tạo.");
                return true;
            } catch (IllegalArgumentException e) {
                inDong("Lỗi: " + e.getMessage() + " Hãy nhập lại.");
            } finally {
                Arrays.fill(matKhau, '\0');
            }
        }
    }

    private void menuSauDangNhap() {
        while (phienConHieuLuc()) {
            TaiKhoan tk = dichVu.layTaiKhoanDangNhap(maTaiKhoanDangNhap);
            String tieuDe = tk.layVaiTro().getTenHienThi().toUpperCase(TIENG_VIET) + " — " + tk.getHoTen();
            switch (tk.layVaiTro()) {
                case KHACH_HANG -> hienThiMenuKhachHang(tieuDe);
                case NHAN_VIEN_BAN_HANG -> hienThiMenuNhanVien(tieuDe);
                case QUAN_LY_CUA_HANG -> hienThiMenuQuanLy(tieuDe);
            }
        }
    }

    private boolean phienConHieuLuc() {
        if (maTaiKhoanDangNhap == null) {
            return false;
        }
        try {
            dichVu.layTaiKhoanDangNhap(maTaiKhoanDangNhap);
            return true;
        } catch (RuntimeException e) {
            inDong(e.getMessage());
            dangXuat();
            return false;
        }
    }

    /** Chạy một chức năng; lỗi nghiệp vụ được báo rồi quay lại menu. */
    private void thucHien(Runnable thaoTac) {
        try {
            thaoTac.run();
        } catch (ArithmeticException e) {
            inDong("Lỗi: số vượt quá giới hạn cho phép.");
        } catch (RuntimeException e) {
            if (hetNhap) {
                throw e;
            }
            inDong("Lỗi: " + e.getMessage());
        }
    }

    // ================================================================ tài khoản

    private void dangNhap() {
        String ten = nhap("Tên đăng nhập (Enter = quay lại)");
        if (ten.isEmpty()) {
            return;
        }
        char[] matKhau = nhapMatKhau("Mật khẩu");
        try {
            maTaiKhoanDangNhap = dichVu.dangNhap(ten, matKhau);
        } finally {
            Arrays.fill(matKhau, '\0');
        }
        TaiKhoan tk = dichVu.layTaiKhoanDangNhap(maTaiKhoanDangNhap);
        inDong("Xin chào " + tk.getHoTen() + " (" + tk.layVaiTro().getTenHienThi() + ").");
        inDong("Mẹo: tại ô nhập, để trống rồi Enter để quay lại.");
    }

    private void dangXuat() {
        maTaiKhoanDangNhap = null;
        inDong("Đã đăng xuất.");
    }

    private void dangKy() {
        inDong("Đăng ký tài khoản khách hàng.");
        String ten = nhap("Tên đăng nhập (Enter = quay lại)");
        if (ten.isEmpty()) {
            return;
        }
        char[] matKhau = nhapMatKhauHaiLan();
        if (matKhau == null) {
            return;
        }
        try {
            String ma = dichVu.dangKyKhachHang(ten, matKhau, nhap("Họ tên"), nhap("Số điện thoại"));
            inDong("Đăng ký thành công. Mã tài khoản: " + ma);
            inDong("Hãy đăng nhập bằng tên " + TaiKhoan.chuanHoaTenDangNhap(ten) + ".");
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    private char[] nhapMatKhauHaiLan() {
        char[] matKhau = nhapMatKhau("Mật khẩu (ít nhất 6 ký tự)");
        char[] nhapLai = nhapMatKhau("Nhập lại mật khẩu");
        boolean khop = Arrays.equals(matKhau, nhapLai);
        Arrays.fill(nhapLai, '\0');
        if (!khop) {
            Arrays.fill(matKhau, '\0');
            inDong("Hai lần nhập mật khẩu không khớp.");
            return null;
        }
        return matKhau;
    }

    // ================================================================ menu theo vai trò

    private void hienThiMenuKhachHang(String tieuDe) {
        int chon = chonMenu(tieuDe, "Đăng xuất", "Xem / tìm sản phẩm và khuyến mãi", "Đặt hàng nhận tại cửa hàng",
                "Xem đơn hàng của tôi", "Hủy đơn đang chờ thanh toán", "Xem điểm tích lũy");
        switch (chon) {
            case 1 -> thucHien(this::xemSanPham);
            case 2 -> thucHien(this::khachDatHang);
            case 3 -> thucHien(() -> xemDanhSachDon(null));
            case 4 -> thucHien(this::huyDonCho);
            case 5 -> thucHien(this::xemDiem);
            default -> dangXuat();
        }
    }

    private void hienThiMenuNhanVien(String tieuDe) {
        if (!menuBanHang(tieuDe, "Đăng xuất")) {
            dangXuat();
        }
    }

    /** Một lượt menu bán hàng; trả false khi người dùng chọn 0. */
    private boolean menuBanHang(String tieuDe, String nhanThoat) {
        int chon = chonMenu(tieuDe, nhanThoat, "Xem / tìm sản phẩm và khuyến mãi", "Lập đơn bán tại quầy",
                "Xem / tìm đơn hàng", "Thanh toán đơn hàng", "Hủy đơn đang chờ thanh toán", "Tra thành viên và điểm");
        switch (chon) {
            case 1 -> thucHien(this::xemSanPham);
            case 2 -> thucHien(this::lapDonTaiQuay);
            case 3 -> thucHien(this::xemTimDon);
            case 4 -> thucHien(this::thanhToanDonCho);
            case 5 -> thucHien(this::huyDonCho);
            case 6 -> thucHien(this::traThanhVien);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void hienThiMenuQuanLy(String tieuDe) {
        int chon = chonMenu(tieuDe, "Đăng xuất", "Bán hàng", "Quản lý sản phẩm và SKU", "Quản lý khuyến mãi",
                "Nhập hàng", "Quản lý tài khoản", "Báo cáo");
        switch (chon) {
            case 1 -> {
                while (phienConHieuLuc() && menuBanHang("BÁN HÀNG", "Quay lại")) {
                    // lặp đến khi chọn quay lại
                }
            }
            case 2 -> menuSanPham();
            case 3 -> menuKhuyenMai();
            case 4 -> menuNhapHang();
            case 5 -> menuTaiKhoan();
            case 6 -> menuBaoCao();
            default -> dangXuat();
        }
    }

    // ================================================================ danh mục

    private void xemSanPham() {
        String tuKhoa = nhap("Từ khóa (tên, nhóm, quy cách hoặc mã; Enter = tất cả)");
        List<BienTheSanPham> ds = dichVu.timBienThe(maTaiKhoanDangNhap, tuKhoa);
        if (ds.isEmpty()) {
            inDong("Không có sản phẩm phù hợp.");
        } else {
            inBangBienThe(ds);
        }
        LocalDate homNay = DichVuCuaHang.homNay();
        List<KhuyenMai> dangApDung = dichVu.danhSachKhuyenMai(maTaiKhoanDangNhap).stream()
                .filter(km -> km.coHieuLuc(homNay)).toList();
        if (!dangApDung.isEmpty()) {
            inDong("Khuyến mãi đang áp dụng:");
            for (KhuyenMai km : dangApDung) {
                inDong("- " + km.getTenKhuyenMai() + " | " + tenSku(km.getMaBienThe()) + " | " + mucGiam(km)
                        + " | " + ngay(km.getTuNgay()) + " – " + ngay(km.getDenNgay()));
            }
        }
    }

    private void inBangBienThe(List<BienTheSanPham> ds) {
        Map<String, String> ma = maNgan(ds.stream().map(BienTheSanPham::getMaBienThe).toList());
        inDong(cot("Mã", 10) + cot("Sản phẩm", 22) + cot("Quy cách", 22) + cotPhai("Giá bán", 11) + "  "
                + cot("KM", 9) + cotPhai("Sau KM", 11) + cotPhai("Tồn", 6) + "  Ghi chú");
        for (BienTheSanPham bt : ds) {
            boolean dangBan = dichVu.dangBan(bt);
            KhuyenMai km = dangBan ? dichVu.khuyenMaiHienHanh(bt) : null;
            String ghiChu = !dangBan ? "Ngừng bán" : bt.getSoLuongTon() == 0 ? "Hết hàng" : "";
            inDong(cot(ma.get(bt.getMaBienThe()), 10) + cot(dichVu.timSanPham(bt.getMaSanPham()).getTenSanPham(), 22)
                    + cot(bt.getTenQuyCach(), 22) + cotPhai(tien(bt.getGiaBan()), 11) + "  "
                    + cot(km == null ? "" : mucGiam(km), 9)
                    + cotPhai(km == null ? "" : tien(bt.getGiaBan() - km.tinhTienGiam(bt.getGiaBan(), 1)), 11)
                    + cotPhai(String.valueOf(bt.getSoLuongTon()), 6) + "  " + ghiChu);
        }
    }

    /** Tìm SKU theo từ khóa rồi chọn bằng mã; trả null nếu quay lại. */
    private BienTheSanPham chonBienThe(boolean chiHangDangBan) {
        String tuKhoa = nhap("Tìm SKU theo tên hoặc mã (Enter = quay lại)");
        if (tuKhoa.isEmpty()) {
            return null;
        }
        List<BienTheSanPham> ds = dichVu.timBienThe(maTaiKhoanDangNhap, tuKhoa).stream()
                .filter(bt -> !chiHangDangBan || dichVu.dangBan(bt)).toList();
        if (ds.isEmpty()) {
            inDong("Không tìm thấy SKU phù hợp.");
            return null;
        }
        inBangBienThe(ds);
        if (ds.size() == 1) {
            inDong("Đã chọn: " + tenSku(ds.getFirst().getMaBienThe()));
            return ds.getFirst();
        }
        return chonTheoMa(ds, BienTheSanPham::getMaBienThe, "Mã SKU");
    }

    private String tenSku(String maBienThe) {
        BienTheSanPham bt = dichVu.timBienTheTheoMa(maBienThe);
        return bt == null ? maBienThe : dichVu.timSanPham(bt.getMaSanPham()).getTenSanPham() + " – " + bt.getTenQuyCach();
    }

    // ================================================================ giỏ hàng và đơn

    /** Soạn giỏ: thêm/sửa/bỏ dòng trước khi xác nhận. Trả null nếu hủy. */
    private DanhSachChiTietDonHang soanGio() {
        DanhSachChiTietDonHang gio = new DanhSachChiTietDonHang();
        while (true) {
            inGio(gio);
            int chon = chonMenu("SOẠN ĐƠN", "Hủy đơn đang soạn", "Thêm SKU", "Sửa số lượng", "Bỏ dòng", "Xác nhận");
            switch (chon) {
                case 1 -> thucHien(() -> {
                    BienTheSanPham bt = chonBienThe(true);
                    Integer soLuong = bt == null ? null : nhapSo("Số lượng (" + bt.getTenQuyCach() + ")", 1, Integer.MAX_VALUE);
                    if (soLuong != null) {
                        dichVu.themVaoGio(maTaiKhoanDangNhap, gio, bt.getMaBienThe(), soLuong);
                    }
                });
                case 2 -> thucHien(() -> {
                    ChiTietDonHang ct = chonTheoMa(gio.xuat(), ChiTietDonHang::getMaBienThe, "Mã SKU trong giỏ");
                    Integer soLuong = ct == null ? null : nhapSo("Số lượng mới", 1, Integer.MAX_VALUE);
                    if (soLuong != null) {
                        dichVu.suaSoLuongTrongGio(maTaiKhoanDangNhap, gio, ct.getMaBienThe(), soLuong);
                    }
                });
                case 3 -> thucHien(() -> {
                    ChiTietDonHang ct = chonTheoMa(gio.xuat(), ChiTietDonHang::getMaBienThe, "Mã SKU cần bỏ");
                    if (ct != null) {
                        dichVu.xoaKhoiGio(maTaiKhoanDangNhap, gio, ct.getMaBienThe());
                    }
                });
                case 4 -> {
                    if (!gio.isRong()) {
                        return gio;
                    }
                    inDong("Giỏ hàng đang trống.");
                }
                default -> {
                    inDong("Đã hủy đơn đang soạn.");
                    return null;
                }
            }
        }
    }

    private void inGio(DanhSachChiTietDonHang gio) {
        inDong("");
        if (gio.isRong()) {
            inDong("(Giỏ hàng trống)");
            return;
        }
        try {
            DanhSachChiTietDonHang baoGia = dichVu.baoGia(maTaiKhoanDangNhap, gio);
            inBangDong(baoGia.xuat());
            inDong(cot("TẠM TÍNH", 70) + cotPhai(tien(baoGia.tinhTongTienHang() - baoGia.tinhTongGiam()), 12));
        } catch (IllegalArgumentException e) {
            inBangDong(gio.xuat());
            inDong("Lưu ý: " + e.getMessage());
        }
    }

    private void inBangDong(List<ChiTietDonHang> dong) {
        Map<String, String> ma = maNgan(dong.stream().map(ChiTietDonHang::getMaBienThe).toList());
        inDong(cot("Mã", 10) + cot("Sản phẩm", 20) + cot("Quy cách", 20) + cotPhai("SL", 5) + cotPhai("Đơn giá", 11)
                + cotPhai("Giảm KM", 10) + cotPhai("Thành tiền", 12));
        for (ChiTietDonHang ct : dong) {
            inDong(cot(ma.get(ct.getMaBienThe()), 10) + cot(ct.getTenSanPhamLucBan(), 20) + cot(ct.getTenQuyCachLucBan(), 20)
                    + cotPhai(String.valueOf(ct.getSoLuong()), 5) + cotPhai(tien(ct.getDonGia()), 11)
                    + cotPhai(ct.getTienGiamKhuyenMai() == 0 ? "" : "-" + tien(ct.getTienGiamKhuyenMai()), 10)
                    + cotPhai(tien(ct.tinhTienSauKhuyenMai()), 12));
        }
    }

    private void khachDatHang() {
        DanhSachChiTietDonHang gio = soanGio();
        if (gio == null) {
            return;
        }
        inDong("Nhận hàng và trả tiền tại cửa hàng. Hàng và điểm chưa được giữ; giá được tính lại khi thanh toán.");
        if (!xacNhan("Xác nhận đặt hàng?")) {
            return;
        }
        String maDon = dichVu.datHang(maTaiKhoanDangNhap, gio);
        inDong("Đặt hàng thành công. Mã đơn: " + maDon.substring(0, DO_DAI_MA_NGAN) + " (đầy đủ: " + maDon + ").");
    }

    private void lapDonTaiQuay() {
        int chon = chonMenu("KHÁCH CỦA ĐƠN", "Quay lại", "Khách thành viên", "Khách vãng lai");
        if (chon == 0) {
            return;
        }
        String maKhach = null;
        if (chon == 1) {
            KhachHang khach = chonThanhVien();
            if (khach == null) {
                return;
            }
            maKhach = khach.getMaTaiKhoan();
        }
        DanhSachChiTietDonHang gio = soanGio();
        if (gio == null || !xacNhan("Lưu đơn chờ thanh toán?")) {
            return;
        }
        String maDon = dichVu.lapDonTaiQuay(maTaiKhoanDangNhap, maKhach, gio);
        inDong("Đã lưu đơn " + maDon.substring(0, DO_DAI_MA_NGAN) + " (chờ thanh toán).");
        if (xacNhan("Thanh toán ngay?")) {
            thanhToanDon(maDon);
        }
    }

    private KhachHang chonThanhVien() {
        String soDienThoai = nhap("Số điện thoại thành viên (Enter = quay lại)");
        if (soDienThoai.isEmpty()) {
            return null;
        }
        List<KhachHang> ds = dichVu.traThanhVien(maTaiKhoanDangNhap, soDienThoai);
        if (ds.isEmpty()) {
            inDong("Không có thành viên với số điện thoại này.");
            return null;
        }
        inBangThanhVien(ds);
        return ds.size() == 1 ? ds.getFirst() : chonTheoMa(ds, KhachHang::getMaTaiKhoan, "Mã thành viên");
    }

    private void traThanhVien() {
        String soDienThoai = nhap("Số điện thoại thành viên (Enter = quay lại)");
        if (soDienThoai.isEmpty()) {
            return;
        }
        List<KhachHang> ds = dichVu.traThanhVien(maTaiKhoanDangNhap, soDienThoai);
        if (ds.isEmpty()) {
            inDong("Không có thành viên với số điện thoại này.");
        } else {
            inBangThanhVien(ds);
        }
    }

    private void inBangThanhVien(List<KhachHang> ds) {
        Map<String, String> ma = maNgan(ds.stream().map(KhachHang::getMaTaiKhoan).toList());
        inDong(cot("Mã", 10) + cot("Họ tên", 24) + cot("Số điện thoại", 14) + cotPhai("Điểm", 7) + "  Trạng thái");
        for (KhachHang kh : ds) {
            inDong(cot(ma.get(kh.getMaTaiKhoan()), 10) + cot(kh.getHoTen(), 24) + cot(kh.getSoDienThoai(), 14)
                    + cotPhai(String.valueOf(kh.getDiemTichLuy()), 7) + "  " + (kh.isDangHoatDong() ? "Hoạt động" : "Đã khóa"));
        }
    }

    private void xemTimDon() {
        int chon = chonMenu("LỌC ĐƠN", "Quay lại", "Tất cả", "Chờ thanh toán", "Đã thanh toán", "Đã hủy");
        if (chon != 0) {
            xemDanhSachDon(chon == 1 ? null : TrangThaiDonHang.values()[chon - 2]);
        }
    }

    private void xemDanhSachDon(TrangThaiDonHang trangThai) {
        List<DonHang> ds = dichVu.danhSachDon(maTaiKhoanDangNhap, trangThai);
        if (ds.isEmpty()) {
            inDong("Không có đơn hàng.");
            return;
        }
        inBangDon(ds);
        DonHang don = chonTheoMa(ds, DonHang::getMaDonHang, "Mã đơn để xem chi tiết (Enter = quay lại)");
        if (don != null) {
            inDon(dichVu.xemDon(maTaiKhoanDangNhap, don.getMaDonHang()), false);
        }
    }

    private void inBangDon(List<DonHang> ds) {
        Map<String, String> ma = maNgan(ds.stream().map(DonHang::getMaDonHang).toList());
        inDong(cot("Mã", 10) + cot("Tạo lúc", 18) + cot("Khách", 22) + cot("Trạng thái", 16) + cotPhai("Tổng", 12));
        for (DonHang dh : ds) {
            inDong(cot(ma.get(dh.getMaDonHang()), 10) + cot(thoiDiem(dh.getThoiDiemTao()), 18) + cot(tenKhach(dh), 22)
                    + cot(dh.getTrangThai().getTenHienThi(), 16) + cotPhai(tien(dh.getTongThanhToan()), 12));
        }
    }

    private String tenKhach(DonHang dh) {
        return dh.getMaKhachHang() == null ? "Khách vãng lai" : dichVu.hoTen(dh.getMaKhachHang());
    }

    /** In đơn, hóa đơn (đã thanh toán) hoặc bảng tính xem trước; chỉ đọc dữ liệu đã có. */
    private void inDon(DonHang dh, boolean laXemTruoc) {
        boolean daChot = dh.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN;
        String tieuDe = laXemTruoc ? " BẢNG TÍNH THANH TOÁN " : daChot ? " HÓA ĐƠN " : " ĐƠN HÀNG ";
        int le = (RONG_HOA_DON - tieuDe.length()) / 2;
        inDong("");
        inDong("=".repeat(le) + tieuDe + "=".repeat(RONG_HOA_DON - le - tieuDe.length()));
        inDong("Mã đơn    : " + dh.getMaDonHang());
        if (!laXemTruoc) {
            inDong("Trạng thái: " + dh.getTrangThai().getTenHienThi()
                    + (dh.getTrangThai() == TrangThaiDonHang.CHO_THANH_TOAN ? " (GIÁ TẠM TÍNH, chưa giữ hàng)" : ""));
        }
        inDong("Khách     : " + tenKhach(dh));
        inDong("Người lập : " + dichVu.hoTen(dh.getMaNguoiLap()) + " — " + thoiDiem(dh.getThoiDiemTao()));
        if (daChot && !laXemTruoc) {
            inDong("Thu tiền  : " + dichVu.hoTen(dh.getMaNguoiThuTien()) + " — " + thoiDiem(dh.getThoiDiemThanhToan()));
        }
        inDong("-".repeat(RONG_HOA_DON));
        for (ChiTietDonHang ct : dh.getChiTiet()) {
            inDong(cot(ct.getTenSanPhamLucBan() + " – " + ct.getTenQuyCachLucBan(), RONG_HOA_DON));
            dongTien("  " + ct.getSoLuong() + " x " + tien(ct.getDonGia()), ct.tinhTienHang());
            if (ct.getMaKhuyenMai() != null) {
                dongTien("  KM: " + dichVu.tenKhuyenMai(ct.getMaKhuyenMai()), -ct.getTienGiamKhuyenMai());
            }
        }
        inDong("-".repeat(RONG_HOA_DON));
        dongTien("Tiền hàng", dh.getTongTienHang());
        dongTien("Giảm khuyến mãi", -dh.getTongGiamKhuyenMai());
        if (daChot) {
            dongTien("Giảm bằng điểm (" + dh.getDiemSuDung() + " điểm)", -dh.getTienGiamDiem());
            dongTien(laXemTruoc ? "SỐ TIỀN CẦN TRẢ" : "TỔNG THANH TOÁN", dh.getTongThanhToan());
            if (dh.getMaKhachHang() != null) {
                inDong("Điểm trừ: " + dh.getDiemSuDung() + " | Điểm cộng: +" + dh.getDiemCong());
            }
        } else {
            dongTien("TẠM TÍNH", dh.getTongThanhToan());
        }
        inDong("=".repeat(RONG_HOA_DON));
    }

    private void dongTien(String nhan, long soTien) {
        inDong(cot(nhan, RONG_HOA_DON - 14) + cotPhai(tien(soTien), 14));
    }

    private void huyDonCho() {
        List<DonHang> ds = dichVu.danhSachDon(maTaiKhoanDangNhap, TrangThaiDonHang.CHO_THANH_TOAN);
        if (ds.isEmpty()) {
            inDong("Không có đơn đang chờ thanh toán.");
            return;
        }
        inBangDon(ds);
        DonHang don = chonTheoMa(ds, DonHang::getMaDonHang, "Mã đơn cần hủy (Enter = quay lại)");
        if (don != null && xacNhan("Hủy đơn " + don.getMaDonHang().substring(0, DO_DAI_MA_NGAN) + "?")) {
            dichVu.huyDon(maTaiKhoanDangNhap, don.getMaDonHang());
            inDong("Đã hủy đơn.");
        }
    }

    private void xemDiem() {
        KhachHang khach = dichVu.xemKhachHang(maTaiKhoanDangNhap, maTaiKhoanDangNhap);
        inDong("Điểm hiện có: " + khach.getDiemTichLuy() + " (1 điểm = 100đ, dùng tối đa 20% tiền hàng sau khuyến mãi).");
        List<DonHang> lichSu = dichVu.lichSuDiem(maTaiKhoanDangNhap, maTaiKhoanDangNhap);
        if (lichSu.isEmpty()) {
            inDong("Chưa có giao dịch tích/đổi điểm.");
            return;
        }
        Map<String, String> ma = maNgan(lichSu.stream().map(DonHang::getMaDonHang).toList());
        inDong(cot("Mã đơn", 10) + cot("Thanh toán lúc", 18) + cotPhai("Điểm dùng", 10) + cotPhai("Điểm cộng", 10)
                + cotPhai("Thực trả", 12));
        for (DonHang dh : lichSu) {
            inDong(cot(ma.get(dh.getMaDonHang()), 10) + cot(thoiDiem(dh.getThoiDiemThanhToan()), 18)
                    + cotPhai("-" + dh.getDiemSuDung(), 10) + cotPhai("+" + dh.getDiemCong(), 10)
                    + cotPhai(tien(dh.getTongThanhToan()), 12));
        }
    }

    // ================================================================ thanh toán

    private void thanhToanDonCho() {
        List<DonHang> ds = dichVu.danhSachDon(maTaiKhoanDangNhap, TrangThaiDonHang.CHO_THANH_TOAN);
        if (ds.isEmpty()) {
            inDong("Không có đơn đang chờ thanh toán.");
            return;
        }
        inBangDon(ds);
        DonHang don = chonTheoMa(ds, DonHang::getMaDonHang, "Mã đơn cần thanh toán (Enter = quay lại)");
        if (don != null) {
            thanhToanDon(don.getMaDonHang());
        }
    }

    private void thanhToanDon(String maDon) {
        DonHang don = dichVu.xemDon(maTaiKhoanDangNhap, maDon);
        inDon(don, false);
        int diem = 0;
        if (don.getMaKhachHang() != null) {
            KhachHang khach = dichVu.xemKhachHang(maTaiKhoanDangNhap, don.getMaKhachHang());
            int toiDa = dichVu.diemToiDaDuocDung(maTaiKhoanDangNhap, maDon);
            inDong("Khách " + khach.getHoTen() + " có " + khach.getDiemTichLuy() + " điểm; đơn này dùng tối đa "
                    + toiDa + " điểm (1 điểm = 100đ).");
            if (toiDa > 0) {
                Integer chon = nhapSo("Số điểm khách muốn dùng (0 = không dùng)", 0, toiDa);
                if (chon == null) {
                    inDong("Đơn vẫn chờ thanh toán.");
                    return;
                }
                diem = chon > 0 && !xacNhan("Chủ tài khoản đã đồng ý dùng " + chon + " điểm?") ? 0 : chon;
            }
        }
        DonHang xemTruoc = dichVu.xemTruocThanhToan(maTaiKhoanDangNhap, maDon, diem);
        inDon(xemTruoc, true);
        if (xemTruoc.getTongTienHang() != don.getTongTienHang()
                || xemTruoc.getTongGiamKhuyenMai() != don.getTongGiamKhuyenMai()) {
            inDong("LƯU Ý: giá hoặc ưu đãi hiện tại khác lúc đặt (tạm tính " + tien(don.getTongThanhToan())
                    + "). Hãy báo khách trước khi thu tiền.");
        }
        if (!xacNhan("Khách đồng ý trả " + tien(xemTruoc.getTongThanhToan()) + " và đã nhận đủ tiền?")) {
            inDong("Đơn vẫn chờ thanh toán.");
            return;
        }
        DonHang daThanhToan;
        try {
            daThanhToan = dichVu.thanhToan(maTaiKhoanDangNhap, maDon, diem, xemTruoc.getTongThanhToan());
        } catch (IllegalStateException | IllegalArgumentException e) {
            inDong("GIAO DỊCH CHƯA HOÀN TẤT: " + e.getMessage());
            inDong("Đơn vẫn chờ thanh toán. Nếu đã nhận tiền, hãy đối chiếu và xử lý lại đúng đơn này; không thu thêm.");
            return;
        }
        inDong("Thanh toán thành công.");
        inDon(daThanhToan, false);
    }

    // ================================================================ quản lý: sản phẩm, SKU

    private void menuSanPham() {
        while (phienConHieuLuc()) {
            int chon = chonMenu("SẢN PHẨM VÀ SKU", "Quay lại", "Danh sách / tìm SKU", "Danh sách sản phẩm",
                    "Thêm sản phẩm", "Sửa sản phẩm / ngừng, mở bán", "Thêm SKU (quy cách bán)",
                    "Sửa giá / ngừng, mở bán SKU");
            switch (chon) {
                case 1 -> thucHien(this::xemSanPham);
                case 2 -> thucHien(() -> inBangSanPham(dichVu.danhSachSanPham(maTaiKhoanDangNhap)));
                case 3 -> thucHien(this::themSanPham);
                case 4 -> thucHien(this::suaSanPham);
                case 5 -> thucHien(this::themBienThe);
                case 6 -> thucHien(this::suaBienThe);
                default -> {
                    return;
                }
            }
        }
    }

    private void inBangSanPham(List<SanPham> ds) {
        Map<String, String> ma = maNgan(ds.stream().map(SanPham::getMaSanPham).toList());
        inDong(cot("Mã", 10) + cot("Tên", 24) + cot("Thương hiệu", 14) + cot("Đơn vị", 16) + cot("Nhóm", 16) + "Trạng thái");
        for (SanPham sp : ds) {
            inDong(cot(ma.get(sp.getMaSanPham()), 10) + cot(sp.getTenSanPham(), 24) + cot(sp.getThuongHieu(), 14)
                    + cot(sp.getDonViCoSo() + " " + sp.getDinhLuong(), 16) + cot(sp.getLoai().getTenHienThi(), 16)
                    + (sp.isDangKinhDoanh() ? "Đang bán" : "Ngừng bán"));
        }
    }

    private void themSanPham() {
        String ten = nhap("Tên sản phẩm (Enter = quay lại)");
        if (ten.isEmpty()) {
            return;
        }
        String thuongHieu = nhap("Thương hiệu");
        String donVi = nhap("Đơn vị cơ sở (hộp, chai, hũ…)");
        String dinhLuong = nhap("Định lượng một đơn vị (vd: 180 ml)");
        String moTa = nhap("Mô tả (Enter = để trống)");
        LoaiSanPham loai = chonEnum("NHÓM HÀNG", LoaiSanPham.values(), LoaiSanPham::getTenHienThi);
        if (loai == null) {
            return;
        }
        String ma = dichVu.themSanPham(maTaiKhoanDangNhap, ten, thuongHieu, donVi, dinhLuong, moTa, loai);
        inDong("Đã thêm sản phẩm " + ma.substring(0, DO_DAI_MA_NGAN) + ". Hãy thêm SKU để bán.");
    }

    private void suaSanPham() {
        List<SanPham> ds = dichVu.danhSachSanPham(maTaiKhoanDangNhap);
        inBangSanPham(ds);
        SanPham sp = chonTheoMa(ds, SanPham::getMaSanPham, "Mã sản phẩm (Enter = quay lại)");
        if (sp == null) {
            return;
        }
        inDong("Enter để giữ nguyên giá trị trong [ ]. Đơn vị, định lượng và nhóm hàng không đổi được.");
        String ten = giuNguyen(nhap("Tên [" + sp.getTenSanPham() + "]"), sp.getTenSanPham());
        String thuongHieu = giuNguyen(nhap("Thương hiệu [" + sp.getThuongHieu() + "]"), sp.getThuongHieu());
        String moTaNhap = nhap("Mô tả [" + (sp.getMoTa() == null ? "" : sp.getMoTa()) + "] (gõ - để xóa)");
        String moTa = moTaNhap.equals("-") ? null : giuNguyen(moTaNhap, sp.getMoTa());
        boolean dangBan = xacNhan("Sản phẩm đang kinh doanh?");
        dichVu.capNhatSanPham(maTaiKhoanDangNhap, sp.getMaSanPham(), ten, thuongHieu, moTa, dangBan);
        inDong("Đã cập nhật sản phẩm.");
    }

    private void themBienThe() {
        List<SanPham> ds = dichVu.danhSachSanPham(maTaiKhoanDangNhap);
        inBangSanPham(ds);
        SanPham sp = chonTheoMa(ds, SanPham::getMaSanPham, "Mã sản phẩm của SKU (Enter = quay lại)");
        if (sp == null) {
            return;
        }
        String tenQuyCach = nhap("Tên quy cách (vd: Lốc 4 " + sp.getDonViCoSo() + " " + sp.getDinhLuong() + ")");
        KieuDongGoi kieu = chonEnum("KIỂU ĐÓNG GÓI", KieuDongGoi.values(), KieuDongGoi::getTenHienThi);
        if (kieu == null) {
            return;
        }
        Integer soDonVi = kieu == KieuDongGoi.BAN_LE ? Integer.valueOf(1)
                : nhapSo("Số " + sp.getDonViCoSo() + " trong một " + kieu.getTenHienThi().toLowerCase(TIENG_VIET), 2, Integer.MAX_VALUE);
        Long gia = soDonVi == null ? null : nhapTien("Giá bán một SKU", 1);
        if (gia == null) {
            return;
        }
        String ma = dichVu.themBienThe(maTaiKhoanDangNhap, sp.getMaSanPham(), tenQuyCach, kieu, soDonVi, gia);
        inDong("Đã thêm SKU " + ma.substring(0, DO_DAI_MA_NGAN) + " với tồn 0; hãy lập phiếu nhập để có hàng.");
    }

    private void suaBienThe() {
        BienTheSanPham bt = chonBienThe(false);
        if (bt == null) {
            return;
        }
        Long gia = nhapTien("Giá bán mới [" + tien(bt.getGiaBan()) + "] (Enter = giữ nguyên)", 1);
        boolean dangBan = xacNhan("SKU đang bán?");
        dichVu.capNhatBienThe(maTaiKhoanDangNhap, bt.getMaBienThe(), gia == null ? bt.getGiaBan() : gia, dangBan);
        inDong("Đã cập nhật SKU.");
    }

    // ================================================================ quản lý: khuyến mãi

    private void menuKhuyenMai() {
        while (phienConHieuLuc()) {
            int chon = chonMenu("KHUYẾN MÃI", "Quay lại", "Danh sách", "Thêm", "Sửa", "Ngừng áp dụng");
            switch (chon) {
                case 1 -> thucHien(() -> inBangKhuyenMai(dichVu.danhSachKhuyenMai(maTaiKhoanDangNhap)));
                case 2 -> thucHien(this::themKhuyenMai);
                case 3 -> thucHien(this::suaKhuyenMai);
                case 4 -> thucHien(this::ngungKhuyenMai);
                default -> {
                    return;
                }
            }
        }
    }

    private void inBangKhuyenMai(List<KhuyenMai> ds) {
        if (ds.isEmpty()) {
            inDong("Chưa có khuyến mãi.");
            return;
        }
        LocalDate homNay = DichVuCuaHang.homNay();
        Map<String, String> ma = maNgan(ds.stream().map(KhuyenMai::getMaKhuyenMai).toList());
        inDong(cot("Mã", 10) + cot("Tên", 26) + cot("SKU", 30) + cot("Mức", 10) + cot("Thời gian", 25) + "Trạng thái");
        for (KhuyenMai km : ds) {
            String trangThai = !km.isDangApDung() ? "Đã ngừng" : km.coHieuLuc(homNay) ? "Đang áp dụng"
                    : homNay.isBefore(km.getTuNgay()) ? "Chưa tới ngày" : "Hết hạn";
            inDong(cot(ma.get(km.getMaKhuyenMai()), 10) + cot(km.getTenKhuyenMai(), 26) + cot(tenSku(km.getMaBienThe()), 30)
                    + cot(mucGiam(km), 10) + cot(ngay(km.getTuNgay()) + " – " + ngay(km.getDenNgay()), 25) + trangThai);
        }
    }

    private void themKhuyenMai() {
        BienTheSanPham bt = chonBienThe(false);
        if (bt == null) {
            return;
        }
        int loai = chonMenu("LOẠI GIẢM", "Quay lại", "Giảm theo phần trăm", "Giảm số tiền mỗi đơn vị SKU");
        if (loai == 0) {
            return;
        }
        String ten = nhap("Tên chương trình");
        Long giaTri = loai == 1 ? toLong(nhapSo("Tỷ lệ giảm (%)", 1, 100)) : nhapTien("Số tiền giảm mỗi SKU", 1);
        LocalDate tuNgay = giaTri == null ? null : nhapNgay("Từ ngày");
        LocalDate denNgay = tuNgay == null ? null : nhapNgay("Đến ngày");
        if (denNgay == null) {
            return;
        }
        dichVu.themKhuyenMai(maTaiKhoanDangNhap, loai == 1, ten, bt.getMaBienThe(), tuNgay, denNgay, giaTri);
        inDong("Đã thêm khuyến mãi.");
    }

    private void suaKhuyenMai() {
        List<KhuyenMai> ds = dichVu.danhSachKhuyenMai(maTaiKhoanDangNhap);
        inBangKhuyenMai(ds);
        KhuyenMai km = chonTheoMa(ds, KhuyenMai::getMaKhuyenMai, "Mã khuyến mãi (Enter = quay lại)");
        if (km == null) {
            return;
        }
        inDong("Enter để giữ nguyên giá trị trong [ ]. SKU và loại giảm không đổi được.");
        boolean laPhanTram = km instanceof KhuyenMaiPhanTram;
        long giaTriCu = km instanceof KhuyenMaiPhanTram pt ? pt.getTyLePhanTram() : ((KhuyenMaiSoTien) km).getSoTienGiamMoiDonVi();
        String ten = giuNguyen(nhap("Tên [" + km.getTenKhuyenMai() + "]"), km.getTenKhuyenMai());
        Long giaTri = laPhanTram ? toLong(nhapSo("Tỷ lệ % [" + giaTriCu + "]", 1, 100))
                : nhapTien("Số tiền giảm mỗi SKU [" + tien(giaTriCu) + "]", 1);
        LocalDate tuNgay = nhapNgay("Từ ngày [" + ngay(km.getTuNgay()) + "]");
        LocalDate denNgay = nhapNgay("Đến ngày [" + ngay(km.getDenNgay()) + "]");
        dichVu.capNhatKhuyenMai(maTaiKhoanDangNhap, km.getMaKhuyenMai(), ten,
                tuNgay == null ? km.getTuNgay() : tuNgay, denNgay == null ? km.getDenNgay() : denNgay,
                giaTri == null ? giaTriCu : giaTri);
        inDong("Đã cập nhật khuyến mãi cho các lần thanh toán sau.");
    }

    private void ngungKhuyenMai() {
        List<KhuyenMai> ds = dichVu.danhSachKhuyenMai(maTaiKhoanDangNhap).stream().filter(KhuyenMai::isDangApDung).toList();
        inBangKhuyenMai(ds);
        KhuyenMai km = chonTheoMa(ds, KhuyenMai::getMaKhuyenMai, "Mã khuyến mãi cần ngừng (Enter = quay lại)");
        if (km != null && xacNhan("Ngừng \"" + km.getTenKhuyenMai() + "\"? Không mở lại được.")) {
            dichVu.ngungKhuyenMai(maTaiKhoanDangNhap, km.getMaKhuyenMai());
            inDong("Đã ngừng khuyến mãi.");
        }
    }

    // ================================================================ quản lý: nhập hàng

    private void menuNhapHang() {
        while (phienConHieuLuc()) {
            int chon = chonMenu("NHẬP HÀNG", "Quay lại", "Lập phiếu nhập", "Lịch sử phiếu nhập");
            switch (chon) {
                case 1 -> thucHien(this::lapPhieuNhap);
                case 2 -> thucHien(this::lichSuPhieuNhap);
                default -> {
                    return;
                }
            }
        }
    }

    private void lapPhieuNhap() {
        DanhSachChiTietPhieuNhap phieu = new DanhSachChiTietPhieuNhap();
        while (true) {
            inDong("");
            if (phieu.isRong()) {
                inDong("(Phiếu nhập chưa có dòng)");
            } else {
                inBangDongNhap(phieu.xuat());
                inDong(cot("TỔNG TIỀN NHẬP", 70) + cotPhai(tien(phieu.tinhTongTien()), 14));
            }
            int chon = chonMenu("SOẠN PHIẾU NHẬP", "Hủy phiếu", "Thêm dòng", "Sửa dòng", "Bỏ dòng", "Ghi nhận phiếu");
            switch (chon) {
                case 1 -> thucHien(() -> {
                    BienTheSanPham bt = chonBienThe(false);
                    Integer soLuong = bt == null ? null : nhapSo("Số lượng (đơn vị: " + bt.getTenQuyCach() + ")", 1, Integer.MAX_VALUE);
                    Long gia = soLuong == null ? null : nhapTien("Giá nhập một " + bt.getTenQuyCach(), 1);
                    if (gia != null) {
                        dichVu.themVaoPhieuNhap(maTaiKhoanDangNhap, phieu, bt.getMaBienThe(), soLuong, gia);
                    }
                });
                case 2 -> thucHien(() -> {
                    ChiTietPhieuNhap ct = chonTheoMa(phieu.xuat(), ChiTietPhieuNhap::getMaBienThe, "Mã SKU trên phiếu");
                    Integer soLuong = ct == null ? null : nhapSo("Số lượng mới", 1, Integer.MAX_VALUE);
                    Long gia = soLuong == null ? null : nhapTien("Giá nhập mới", 1);
                    if (gia != null) {
                        dichVu.suaDongPhieuNhap(maTaiKhoanDangNhap, phieu, ct.getMaBienThe(), soLuong, gia);
                    }
                });
                case 3 -> thucHien(() -> {
                    ChiTietPhieuNhap ct = chonTheoMa(phieu.xuat(), ChiTietPhieuNhap::getMaBienThe, "Mã SKU cần bỏ");
                    if (ct != null) {
                        dichVu.xoaDongPhieuNhap(maTaiKhoanDangNhap, phieu, ct.getMaBienThe());
                    }
                });
                case 4 -> {
                    if (phieu.isRong()) {
                        inDong("Phiếu nhập chưa có dòng.");
                        continue;
                    }
                    String nguon = nhap("Tên nguồn nhập (người/đơn vị cung cấp)");
                    if (nguon.isEmpty() || !xacNhan("Tổng " + tien(phieu.tinhTongTien())
                            + ". Xác nhận đã nhận hàng và trả đủ tiền?")) {
                        continue;
                    }
                    try {
                        String ma = dichVu.nhapHang(maTaiKhoanDangNhap, nguon, phieu);
                        inDong("Đã ghi nhận phiếu nhập " + ma.substring(0, DO_DAI_MA_NGAN) + "; tồn kho đã tăng.");
                        return;
                    } catch (RuntimeException e) {
                        if (hetNhap) {
                            throw e;
                        }
                        inDong("Lỗi: " + e.getMessage());
                    }
                }
                default -> {
                    inDong("Đã hủy phiếu đang soạn.");
                    return;
                }
            }
        }
    }

    private void inBangDongNhap(List<ChiTietPhieuNhap> dong) {
        Map<String, String> ma = maNgan(dong.stream().map(ChiTietPhieuNhap::getMaBienThe).toList());
        inDong(cot("Mã", 10) + cot("Sản phẩm", 20) + cot("Quy cách", 20) + cotPhai("SL", 6) + cotPhai("Giá nhập", 14)
                + cotPhai("Thành tiền", 14));
        for (ChiTietPhieuNhap ct : dong) {
            inDong(cot(ma.get(ct.getMaBienThe()), 10) + cot(ct.getTenSanPhamLucNhap(), 20) + cot(ct.getTenQuyCachLucNhap(), 20)
                    + cotPhai(String.valueOf(ct.getSoLuong()), 6) + cotPhai(tien(ct.getDonGiaNhap()), 14)
                    + cotPhai(tien(ct.tinhThanhTien()), 14));
        }
    }

    private void lichSuPhieuNhap() {
        List<PhieuNhap> ds = dichVu.danhSachPhieuNhap(maTaiKhoanDangNhap);
        if (ds.isEmpty()) {
            inDong("Chưa có phiếu nhập.");
            return;
        }
        Map<String, String> ma = maNgan(ds.stream().map(PhieuNhap::getMaPhieuNhap).toList());
        inDong(cot("Mã", 10) + cot("Thời điểm", 18) + cot("Nguồn nhập", 24) + cot("Người nhập", 20) + cotPhai("Tổng", 14));
        for (PhieuNhap pn : ds) {
            inDong(cot(ma.get(pn.getMaPhieuNhap()), 10) + cot(thoiDiem(pn.getThoiDiemNhap()), 18)
                    + cot(pn.getTenNguonNhap(), 24) + cot(dichVu.hoTen(pn.getMaNguoiNhap()), 20)
                    + cotPhai(tien(pn.tinhTongTien()), 14));
        }
        PhieuNhap pn = chonTheoMa(ds, PhieuNhap::getMaPhieuNhap, "Mã phiếu để xem chi tiết (Enter = quay lại)");
        if (pn != null) {
            inDong("Phiếu nhập: " + pn.getMaPhieuNhap() + " — " + thoiDiem(pn.getThoiDiemNhap()) + " — " + pn.getTenNguonNhap());
            inBangDongNhap(pn.getChiTiet());
            inDong(cot("TỔNG TIỀN NHẬP", 70) + cotPhai(tien(pn.tinhTongTien()), 14));
        }
    }

    // ================================================================ quản lý: tài khoản

    private void menuTaiKhoan() {
        while (phienConHieuLuc()) {
            int chon = chonMenu("TÀI KHOẢN", "Quay lại", "Danh sách", "Tạo tài khoản nhân viên",
                    "Tạo tài khoản quản lý", "Khóa tài khoản", "Mở khóa tài khoản");
            switch (chon) {
                case 1 -> thucHien(() -> inBangTaiKhoan(dichVu.danhSachTaiKhoan(maTaiKhoanDangNhap)));
                case 2 -> thucHien(() -> taoTaiKhoanNoiBo(VaiTro.NHAN_VIEN_BAN_HANG));
                case 3 -> thucHien(() -> taoTaiKhoanNoiBo(VaiTro.QUAN_LY_CUA_HANG));
                case 4 -> thucHien(() -> doiTrangThaiTaiKhoan(false));
                case 5 -> thucHien(() -> doiTrangThaiTaiKhoan(true));
                default -> {
                    return;
                }
            }
        }
    }

    private void inBangTaiKhoan(List<TaiKhoan> ds) {
        Map<String, String> ma = maNgan(ds.stream().map(TaiKhoan::getMaTaiKhoan).toList());
        inDong(cot("Mã", 10) + cot("Tên đăng nhập", 18) + cot("Họ tên", 22) + cot("Vai trò", 20) + cot("Điện thoại", 13)
                + cot("Trạng thái", 11) + cotPhai("Điểm", 6));
        for (TaiKhoan tk : ds) {
            inDong(cot(ma.get(tk.getMaTaiKhoan()), 10) + cot(tk.getTenDangNhap(), 18) + cot(tk.getHoTen(), 22)
                    + cot(tk.layVaiTro().getTenHienThi(), 20) + cot(tk.getSoDienThoai(), 13)
                    + cot(tk.isDangHoatDong() ? "Hoạt động" : "Đã khóa", 11)
                    + cotPhai(tk instanceof KhachHang kh ? String.valueOf(kh.getDiemTichLuy()) : "", 6));
        }
    }

    private void taoTaiKhoanNoiBo(VaiTro vaiTro) {
        inDong("Tạo tài khoản " + vaiTro.getTenHienThi().toLowerCase(TIENG_VIET) + ".");
        String ten = nhap("Tên đăng nhập (Enter = quay lại)");
        if (ten.isEmpty()) {
            return;
        }
        char[] matKhau = nhapMatKhauHaiLan();
        if (matKhau == null) {
            return;
        }
        try {
            String ma = dichVu.taoTaiKhoanNoiBo(maTaiKhoanDangNhap, vaiTro, ten, matKhau, nhap("Họ tên"), nhap("Số điện thoại"));
            inDong("Đã tạo tài khoản " + ma.substring(0, DO_DAI_MA_NGAN) + ".");
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    private void doiTrangThaiTaiKhoan(boolean moKhoa) {
        List<TaiKhoan> ds = dichVu.danhSachTaiKhoan(maTaiKhoanDangNhap).stream()
                .filter(tk -> tk.isDangHoatDong() != moKhoa).toList();
        if (ds.isEmpty()) {
            inDong(moKhoa ? "Không có tài khoản bị khóa." : "Không có tài khoản đang hoạt động.");
            return;
        }
        inBangTaiKhoan(ds);
        TaiKhoan tk = chonTheoMa(ds, TaiKhoan::getMaTaiKhoan, "Mã tài khoản (Enter = quay lại)");
        if (tk == null || !xacNhan((moKhoa ? "Mở khóa " : "Khóa ") + tk.getTenDangNhap() + "?")) {
            return;
        }
        if (moKhoa) {
            dichVu.moKhoaTaiKhoan(maTaiKhoanDangNhap, tk.getMaTaiKhoan());
            inDong("Đã mở khóa.");
        } else if (dichVu.khoaTaiKhoan(maTaiKhoanDangNhap, tk.getMaTaiKhoan())) {
            inDong("Bạn đã tự khóa tài khoản của mình; phiên làm việc kết thúc.");
            dangXuat();
        } else {
            inDong("Đã khóa tài khoản.");
        }
    }

    // ================================================================ quản lý: báo cáo

    private void menuBaoCao() {
        while (phienConHieuLuc()) {
            int chon = chonMenu("BÁO CÁO", "Quay lại", "Thu - chi theo kỳ", "Tồn kho hiện tại theo SKU");
            switch (chon) {
                case 1 -> thucHien(this::baoCaoThuChi);
                case 2 -> thucHien(this::baoCaoTonKho);
                default -> {
                    return;
                }
            }
        }
    }

    private void baoCaoThuChi() {
        LocalDate tuNgay = nhapNgay("Từ ngày");
        LocalDate denNgay = tuNgay == null ? null : nhapNgay("Đến ngày");
        if (denNgay == null) {
            return;
        }
        Map<String, Long> baoCao = dichVu.tongKetThuChi(maTaiKhoanDangNhap, tuNgay, denNgay);
        inDong("");
        inDong("BÁO CÁO THU - CHI từ " + ngay(tuNgay) + " đến " + ngay(denNgay) + " (đơn vị: đồng)");
        inDong("-".repeat(RONG_HOA_DON));
        for (Map.Entry<String, Long> dong : baoCao.entrySet()) {
            boolean laSoDon = dong.getKey().startsWith("Số đơn");
            inDong(cot(dong.getKey(), RONG_HOA_DON - 16)
                    + cotPhai(laSoDon ? String.valueOf(dong.getValue()) : tien(dong.getValue()), 16));
        }
        inDong("-".repeat(RONG_HOA_DON));
        inDong("Thu tính theo ngày thanh toán; chi nhập theo ngày nhập.");
        inDong("Chênh lệch thu − chi nhập không phải lợi nhuận (chưa tính giá vốn, chi phí, thuế).");
    }

    private void baoCaoTonKho() {
        List<BienTheSanPham> ds = dichVu.xemTonKho(maTaiKhoanDangNhap);
        inDong("TỒN KHO HIỆN TẠI (không phải tồn cuối kỳ của báo cáo thu - chi)");
        Map<String, String> ma = maNgan(ds.stream().map(BienTheSanPham::getMaBienThe).toList());
        inDong(cot("Mã", 10) + cot("Sản phẩm", 22) + cot("Quy cách", 22) + cotPhai("Tồn", 7) + "  Trạng thái");
        for (BienTheSanPham bt : ds) {
            inDong(cot(ma.get(bt.getMaBienThe()), 10) + cot(dichVu.timSanPham(bt.getMaSanPham()).getTenSanPham(), 22)
                    + cot(bt.getTenQuyCach(), 22) + cotPhai(String.valueOf(bt.getSoLuongTon()), 7) + "  "
                    + (dichVu.dangBan(bt) ? "Đang bán" : "Ngừng bán"));
        }
    }

    // ================================================================ nhập liệu

    /** Đọc một dòng (chuẩn hóa NFC); hết dữ liệu nhập thì đánh dấu và ném lỗi để thoát gọn. */
    private String docDongTho(String nhacNhap) {
        System.out.print(nhacNhap);
        if (!boDoc.hasNextLine()) {
            hetNhap = true;
            throw new NoSuchElementException("Hết dữ liệu nhập.");
        }
        return Normalizer.normalize(boDoc.nextLine(), Normalizer.Form.NFC);
    }

    private String nhap(String nhan) {
        return docDongTho(nhan + ": ").strip();
    }

    /** Mật khẩu không bị trim; ẩn ký tự khi chạy trong terminal thật. */
    private char[] nhapMatKhau(String nhan) {
        Console console = System.console();
        if (console != null && console.isTerminal()) {
            char[] matKhau = console.readPassword(nhan + ": ");
            if (matKhau == null) {
                hetNhap = true;
                throw new NoSuchElementException("Hết dữ liệu nhập.");
            }
            return Normalizer.normalize(new String(matKhau), Normalizer.Form.NFC).toCharArray();
        }
        return docDongTho(nhan + ": ").toCharArray();
    }

    private int chonMenu(String tieuDe, String nhanThoat, String... muc) {
        inDong("");
        inDong("=== " + tieuDe + " ===");
        for (int i = 0; i < muc.length; i++) {
            inDong((i + 1) + ". " + muc[i]);
        }
        inDong("0. " + nhanThoat);
        while (true) {
            String s = nhap("Chọn");
            try {
                int chon = Integer.parseInt(s);
                if (chon >= 0 && chon <= muc.length) {
                    return chon;
                }
            } catch (NumberFormatException e) {
                // báo chung bên dưới
            }
            inDong("Lựa chọn không hợp lệ, hãy nhập số từ 0 đến " + muc.length + ".");
        }
    }

    private <E extends Enum<E>> E chonEnum(String tieuDe, E[] giaTri, Function<E, String> tenHienThi) {
        String[] muc = Arrays.stream(giaTri).map(tenHienThi).toArray(String[]::new);
        int chon = chonMenu(tieuDe, "Quay lại", muc);
        return chon == 0 ? null : giaTri[chon - 1];
    }

    /** Số nguyên trong [min, max]; để trống trả null. */
    private Integer nhapSo(String nhan, int min, int max) {
        while (true) {
            String s = nhap(nhan);
            if (s.isEmpty()) {
                return null;
            }
            try {
                int so = Integer.parseInt(s.replace(".", ""));
                if (so >= min && so <= max) {
                    return so;
                }
                inDong("Hãy nhập số từ " + min + " đến " + max + ".");
            } catch (NumberFormatException e) {
                inDong("Hãy nhập một số nguyên hợp lệ.");
            }
        }
    }

    /** Số tiền nguyên đồng ≥ min, chấp nhận 30000, 30.000 hoặc 30.000đ; để trống trả null. */
    private Long nhapTien(String nhan, long min) {
        while (true) {
            String s = nhap(nhan);
            if (s.isEmpty()) {
                return null;
            }
            try {
                long tien = Long.parseLong(s.replace(".", "").replace("đ", "").replace(" ", ""));
                if (tien >= min) {
                    return tien;
                }
                inDong("Số tiền phải từ " + tien(min) + " trở lên.");
            } catch (NumberFormatException e) {
                inDong("Hãy nhập số tiền nguyên đồng, ví dụ 30000 hoặc 30.000.");
            }
        }
    }

    private LocalDate nhapNgay(String nhan) {
        while (true) {
            String s = nhap(nhan + " (dd/mm/yyyy)");
            if (s.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(s, DINH_DANG_NGAY);
            } catch (DateTimeParseException e) {
                inDong("Ngày không hợp lệ, ví dụ 31/10/2026.");
            }
        }
    }

    private boolean xacNhan(String cauHoi) {
        while (true) {
            String s = nhap(cauHoi + " (c/k)").toLowerCase(TIENG_VIET);
            if (s.equals("c") || s.equals("có")) {
                return true;
            }
            if (s.equals("k") || s.equals("không")) {
                return false;
            }
            inDong("Hãy nhập c (có) hoặc k (không).");
        }
    }

    /**
     * Chọn phần tử bằng mã đầy đủ hoặc phần đầu duy nhất trong danh sách; để trống trả null.
     * Trùng nhiều mã thì yêu cầu nhập dài hơn.
     */
    private <T> T chonTheoMa(List<T> ds, Function<T, String> layMa, String nhan) {
        if (ds.isEmpty()) {
            return null;
        }
        while (true) {
            String s = nhap(nhan).toLowerCase(Locale.ROOT);
            if (s.isEmpty()) {
                return null;
            }
            List<T> khop = ds.stream().filter(x -> layMa.apply(x).startsWith(s)).toList();
            if (khop.size() == 1) {
                return khop.getFirst();
            }
            inDong(khop.isEmpty() ? "Không có mã bắt đầu bằng \"" + s + "\" trong danh sách."
                    : "Có " + khop.size() + " mã bắt đầu bằng \"" + s + "\"; hãy nhập dài hơn.");
        }
    }

    // ================================================================ định dạng

    /** Phần đầu ngắn nhất (tối thiểu 8 ký tự) để các mã trong cùng danh sách khác nhau. */
    static Map<String, String> maNgan(Collection<String> dsMa) {
        // ponytail: so từng cặp O(n²), đủ cho vài nghìn mã; cần nhanh hơn thì sắp xếp rồi chỉ so mã kề nhau.
        Map<String, String> ketQua = new HashMap<>();
        for (String ma : dsMa) {
            int doDai = Math.min(DO_DAI_MA_NGAN, ma.length());
            for (String khac : dsMa) {
                while (!khac.equals(ma) && doDai < ma.length() && khac.startsWith(ma.substring(0, doDai))) {
                    doDai++;
                }
            }
            ketQua.put(ma, ma.substring(0, doDai));
        }
        return ketQua;
    }

    private static String mucGiam(KhuyenMai km) {
        return switch (km) {
            case KhuyenMaiPhanTram pt -> "-" + pt.getTyLePhanTram() + "%";
            case KhuyenMaiSoTien st -> "-" + tien(st.getSoTienGiamMoiDonVi());
            default -> "";
        };
    }

    static String tien(long soTien) {
        return String.format(TIENG_VIET, "%,dđ", soTien);
    }

    private static String ngay(LocalDate ngay) {
        return ngay.format(DINH_DANG_NGAY);
    }

    private static String thoiDiem(LocalDateTime thoiDiem) {
        return thoiDiem == null ? "" : thoiDiem.format(DINH_DANG_THOI_DIEM);
    }

    /** Cột rộng cố định căn trái, luôn chừa một khoảng trắng cuối; chữ dài bị cắt và thêm "…". */
    static String cot(String s, int rong) {
        String giaTri = s == null ? "" : s;
        if (giaTri.length() >= rong) {
            return giaTri.substring(0, rong - 2) + "… ";
        }
        return giaTri + " ".repeat(rong - giaTri.length());
    }

    static String cotPhai(String s, int rong) {
        if (s.length() > rong) {
            return s.substring(0, rong - 1) + "…";
        }
        return " ".repeat(rong - s.length()) + s;
    }

    private static String giuNguyen(String nhap, String cu) {
        return nhap.isEmpty() ? cu : nhap;
    }

    private static Long toLong(Integer so) {
        return so == null ? null : so.longValue();
    }

    private static void inDong(String s) {
        System.out.println(s);
    }
}
