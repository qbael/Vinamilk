package cuahang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LuuTruText {
    private static final String DONG_DAU = "VNM|1";
    private static final String TIEN_TO_DONG_CUOI = "END|";
    private static final DateTimeFormatter DINH_DANG_THOI_DIEM = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

    private final Path duongDan;
    private final Path duongDanTam;
    private final QuanLyTaiKhoan quanLyTaiKhoan;
    private final QuanLySanPham quanLySanPham;
    private final QuanLyBienTheSanPham quanLyBienThe;
    private final QuanLyKhuyenMai quanLyKhuyenMai;
    private final QuanLyDonHang quanLyDonHang;
    private final QuanLyPhieuNhap quanLyPhieuNhap;

    public LuuTruText(Path duongDan, QuanLyTaiKhoan quanLyTaiKhoan, QuanLySanPham quanLySanPham,
                      QuanLyBienTheSanPham quanLyBienThe, QuanLyKhuyenMai quanLyKhuyenMai,
                      QuanLyDonHang quanLyDonHang, QuanLyPhieuNhap quanLyPhieuNhap) {
        this.duongDan = duongDan;
        String tenFile = duongDan.getFileName().toString();
        int cham = tenFile.lastIndexOf('.');
        this.duongDanTam = duongDan.resolveSibling((cham > 0 ? tenFile.substring(0, cham) : tenFile) + ".tmp");
        this.quanLyTaiKhoan = quanLyTaiKhoan;
        this.quanLySanPham = quanLySanPham;
        this.quanLyBienThe = quanLyBienThe;
        this.quanLyKhuyenMai = quanLyKhuyenMai;
        this.quanLyDonHang = quanLyDonHang;
        this.quanLyPhieuNhap = quanLyPhieuNhap;
    }

    public Path getDuongDan() {
        return duongDan;
    }

    public boolean tonTai() {
        return Files.exists(duongDan);
    }

    /** Đọc file chính; file rỗng/hỏng báo lỗi và giữ nguyên file, không nạp một phần. */
    public void docDuLieu() throws IOException {
        String noiDung = Files.readString(duongDan, StandardCharsets.UTF_8);
        if (noiDung.isBlank()) {
            throw new IllegalArgumentException("File dữ liệu rỗng: " + duongDan);
        }
        napTuChuoi(noiDung);
    }

    /** Kiểm tra toàn bộ rồi mới thay nội dung sáu manager đang dùng. */
    public void napTuChuoi(String noiDung) {
        phanTich(noiDung, true);
    }

    /** Ghi trạng thái RAM hiện tại: kiểm tra, ghi file tạm, rồi thay file chính nguyên tử. */
    public void ghiAnToan() throws IOException {
        String noiDung = xuatThanhChuoi();
        phanTich(noiDung, false);
        if (duongDan.getParent() != null) {
            Files.createDirectories(duongDan.getParent());
        }
        Files.writeString(duongDanTam, noiDung, StandardCharsets.UTF_8);
        // ponytail: chưa fsync file tạm; thay nguyên tử chống file ghi dở, không chống mất điện.
        Files.move(duongDanTam, duongDan, StandardCopyOption.ATOMIC_MOVE);
    }

    // ---------------------------------------------------------------- ghi

    public String xuatThanhChuoi() {
        List<String> dong = new ArrayList<>();
        for (TaiKhoan tk : quanLyTaiKhoan.xuat()) {
            dong.add(noi("TAI_KHOAN", tk.getMaTaiKhoan(), tk.layVaiTro(), tk.getTenDangNhap(), tk.getSalt(),
                    tk.getSoVongBam(), tk.getMatKhauBam(), tk.getHoTen(), tk.getSoDienThoai(), tk.isDangHoatDong(),
                    tk instanceof KhachHang kh ? kh.getDiemTichLuy() : null));
        }
        for (SanPham sp : quanLySanPham.xuat()) {
            dong.add(noi("SAN_PHAM", sp.getMaSanPham(), sp.getTenSanPham(), sp.getThuongHieu(), sp.getDonViCoSo(),
                    sp.getDinhLuong(), sp.getMoTa(), sp.getLoai(), sp.isDangKinhDoanh()));
        }
        for (BienTheSanPham bt : quanLyBienThe.xuat()) {
            dong.add(noi("BIEN_THE", bt.getMaBienThe(), bt.getMaSanPham(), bt.getTenQuyCach(), bt.getKieuDongGoi(),
                    bt.getSoDonViCoSo(), bt.getGiaBan(), bt.getSoLuongTon(), bt.isDangKinhDoanh()));
        }
        for (KhuyenMai km : quanLyKhuyenMai.xuat()) {
            Object[] loaiVaGiaTri = switch (km) {
                case KhuyenMaiPhanTram pt -> new Object[]{"PHAN_TRAM", pt.getTyLePhanTram()};
                case KhuyenMaiSoTien st -> new Object[]{"SO_TIEN", st.getSoTienGiamMoiDonVi()};
                default -> throw new IllegalStateException("Loại khuyến mãi không lưu được: " + km.getClass());
            };
            dong.add(noi("KHUYEN_MAI", km.getMaKhuyenMai(), loaiVaGiaTri[0], km.getTenKhuyenMai(), km.getMaBienThe(),
                    km.getTuNgay(), km.getDenNgay(), km.isDangApDung(), loaiVaGiaTri[1]));
        }
        for (PhieuNhap pn : quanLyPhieuNhap.xuat()) {
            dong.add(noi("PHIEU_NHAP", pn.getMaPhieuNhap(), pn.getMaNguoiNhap(), pn.getTenNguonNhap(),
                    pn.getThoiDiemNhap()));
        }
        for (PhieuNhap pn : quanLyPhieuNhap.xuat()) {
            for (ChiTietPhieuNhap ct : pn.getChiTiet()) {
                dong.add(noi("CT_PHIEU_NHAP", pn.getMaPhieuNhap(), ct.getMaBienThe(), ct.getTenSanPhamLucNhap(),
                        ct.getTenQuyCachLucNhap(), ct.getSoLuong(), ct.getDonGiaNhap()));
            }
        }
        for (DonHang dh : quanLyDonHang.xuat()) {
            dong.add(noi("DON_HANG", dh.getMaDonHang(), dh.getMaKhachHang(), dh.getMaNguoiLap(),
                    dh.getMaNguoiThuTien(), dh.getThoiDiemTao(), dh.getThoiDiemThanhToan(), dh.getTrangThai(),
                    dh.getDiemSuDung(), dh.getDiemCong(), dh.getTongTienHang(), dh.getTongGiamKhuyenMai(),
                    dh.getTienGiamDiem(), dh.getTongThanhToan()));
        }
        for (DonHang dh : quanLyDonHang.xuat()) {
            for (ChiTietDonHang ct : dh.getChiTiet()) {
                dong.add(noi("CT_DON_HANG", dh.getMaDonHang(), ct.getMaBienThe(), ct.getTenSanPhamLucBan(),
                        ct.getTenQuyCachLucBan(), ct.getSoLuong(), ct.getDonGia(), ct.getMaKhuyenMai(),
                        ct.getTienGiamKhuyenMai()));
            }
        }
        StringBuilder sb = new StringBuilder(DONG_DAU).append('\n');
        for (String d : dong) {
            sb.append(d).append('\n');
        }
        return sb.append(TIEN_TO_DONG_CUOI).append(dong.size()).append('\n').toString();
    }

    private static String noi(Object... cot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cot.length; i++) {
            if (i > 0) {
                sb.append('|');
            }
            Object giaTri = cot[i];
            if (giaTri instanceof LocalDateTime td) {
                sb.append(td.format(DINH_DANG_THOI_DIEM));
            } else if (giaTri instanceof Enum<?> e) {
                sb.append(e.name());
            } else if (giaTri != null) {
                sb.append(giaTri);
            }
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- đọc và kiểm tra

    /**
     * Phân tích vào vùng tạm và kiểm tra toàn bộ đồ thị dữ liệu.
     * Chỉ khi hợp lệ và congBo = true mới thay nội dung sáu manager.
     */
    private void phanTich(String noiDung, boolean congBo) {
        List<String> dong = noiDung.lines().toList();
        if (dong.isEmpty() || !dong.getFirst().equals(DONG_DAU)) {
            throw new IllegalArgumentException("Dòng 1: phải là " + DONG_DAU + ".");
        }
        String dongCuoi = dong.getLast();
        if (dong.size() < 2 || !dongCuoi.startsWith(TIEN_TO_DONG_CUOI)) {
            throw new IllegalArgumentException("Thiếu dòng kết thúc END|N.");
        }
        int soBanGhi = dong.size() - 2;
        if (!dongCuoi.equals(TIEN_TO_DONG_CUOI + soBanGhi)) {
            throw new IllegalArgumentException("Dòng " + dong.size() + ": " + dongCuoi
                    + " không khớp số bản ghi thực tế (" + soBanGhi + ").");
        }

        Map<String, TaiKhoan> taiKhoan = new LinkedHashMap<>();
        Map<String, SanPham> sanPham = new LinkedHashMap<>();
        Map<String, BienTheSanPham> bienThe = new LinkedHashMap<>();
        Map<String, KhuyenMai> khuyenMai = new LinkedHashMap<>();
        Map<String, String[]> donHangTho = new LinkedHashMap<>();
        Map<String, String[]> phieuNhapTho = new LinkedHashMap<>();
        Map<String, Integer> soDong = new HashMap<>();
        Map<String, DanhSachChiTietDonHang> chiTietDon = new HashMap<>();
        Map<String, DanhSachChiTietPhieuNhap> chiTietPhieu = new HashMap<>();

        for (int i = 1; i <= soBanGhi; i++) {
            String[] c = dong.get(i).split("\\|", -1);
            try {
                switch (c[0]) {
                    case "TAI_KHOAN" -> {
                        TaiKhoan tk = docTaiKhoan(soCot(c, 11));
                        dat(taiKhoan, tk.getMaTaiKhoan(), tk, "mã tài khoản");
                    }
                    case "SAN_PHAM" -> {
                        soCot(c, 9);
                        SanPham sp = new SanPham(c[1], c[2], c[3], c[4], c[5], tuyChon(c[6]),
                                docEnum(LoaiSanPham.class, c[7], "Nhóm hàng"), docBoolean(c[8], "dangKinhDoanh"));
                        dat(sanPham, sp.getMaSanPham(), sp, "mã sản phẩm");
                    }
                    case "BIEN_THE" -> {
                        soCot(c, 9);
                        BienTheSanPham bt = new BienTheSanPham(c[1], c[2], c[3],
                                docEnum(KieuDongGoi.class, c[4], "Kiểu đóng gói"), docInt(c[5], "soDonViCoSo"),
                                docLong(c[6], "giaBan"), docInt(c[7], "soLuongTon"), docBoolean(c[8], "dangKinhDoanh"));
                        dat(bienThe, bt.getMaBienThe(), bt, "mã SKU");
                    }
                    case "KHUYEN_MAI" -> {
                        KhuyenMai km = docKhuyenMai(soCot(c, 9));
                        dat(khuyenMai, km.getMaKhuyenMai(), km, "mã khuyến mãi");
                    }
                    case "DON_HANG" -> {
                        dat(donHangTho, LuuTruText.vanBan(soCot(c, 14)[1], "Mã đơn hàng"), c, "mã đơn hàng");
                        soDong.put("DON_HANG|" + c[1], i + 1);
                    }
                    case "CT_DON_HANG" -> {
                        soCot(c, 9);
                        DanhSachChiTietDonHang ds = chiTietDon.computeIfAbsent(c[1], k -> new DanhSachChiTietDonHang());
                        if (ds.timTheoBienThe(c[2]) != null) {
                            throw new IllegalArgumentException("Trùng dòng SKU " + c[2] + " trong đơn " + c[1] + ".");
                        }
                        ds.them(new ChiTietDonHang(c[2], c[3], c[4], docInt(c[5], "soLuong"),
                                docLong(c[6], "donGia"), tuyChon(c[7]), docLong(c[8], "tienGiamKhuyenMai")));
                    }
                    case "PHIEU_NHAP" -> {
                        dat(phieuNhapTho, LuuTruText.vanBan(soCot(c, 5)[1], "Mã phiếu nhập"), c, "mã phiếu nhập");
                        soDong.put("PHIEU_NHAP|" + c[1], i + 1);
                    }
                    case "CT_PHIEU_NHAP" -> {
                        soCot(c, 7);
                        DanhSachChiTietPhieuNhap ds = chiTietPhieu.computeIfAbsent(c[1], k -> new DanhSachChiTietPhieuNhap());
                        if (ds.timTheoBienThe(c[2]) != null) {
                            throw new IllegalArgumentException("Trùng dòng SKU " + c[2] + " trong phiếu " + c[1] + ".");
                        }
                        ds.them(new ChiTietPhieuNhap(c[2], c[3], c[4], docInt(c[5], "soLuong"),
                                docLong(c[6], "donGiaNhap")));
                    }
                    default -> throw new IllegalArgumentException("Loại bản ghi không hợp lệ: " + c[0]);
                }
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Dòng " + (i + 1) + ": " + e.getMessage(), e);
            }
        }

        // Ghép chi tiết vào chứng từ trong vùng tạm; constructor kiểm tra bất biến và khóa chi tiết.
        List<DonHang> donHang = new ArrayList<>();
        for (String[] c : donHangTho.values()) {
            try {
                DanhSachChiTietDonHang ct = chiTietDon.remove(c[1]);
                if (ct == null) {
                    throw new IllegalArgumentException("Đơn không có dòng hàng.");
                }
                donHang.add(new DonHang(c[1], tuyChon(c[2]), c[3], tuyChon(c[4]), docThoiDiem(c[5], "thoiDiemTao"),
                        c[6].isEmpty() ? null : docThoiDiem(c[6], "thoiDiemThanhToan"),
                        docEnum(TrangThaiDonHang.class, c[7], "Trạng thái"), docInt(c[8], "diemSuDung"),
                        docInt(c[9], "diemCong"), docLong(c[10], "tongTienHang"), docLong(c[11], "tongGiamKhuyenMai"),
                        docLong(c[12], "tienGiamDiem"), docLong(c[13], "tongThanhToan"), ct));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Dòng " + soDong.get("DON_HANG|" + c[1]) + " (đơn " + c[1] + "): "
                        + e.getMessage(), e);
            }
        }
        if (!chiTietDon.isEmpty()) {
            throw new IllegalArgumentException("Chi tiết đơn mồ côi, không có đơn " + chiTietDon.keySet().iterator().next() + ".");
        }
        List<PhieuNhap> phieuNhap = new ArrayList<>();
        for (String[] c : phieuNhapTho.values()) {
            try {
                DanhSachChiTietPhieuNhap ct = chiTietPhieu.remove(c[1]);
                if (ct == null) {
                    throw new IllegalArgumentException("Phiếu không có dòng hàng.");
                }
                phieuNhap.add(new PhieuNhap(c[1], c[2], c[3], docThoiDiem(c[4], "thoiDiemNhap"), ct));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Dòng " + soDong.get("PHIEU_NHAP|" + c[1]) + " (phiếu " + c[1] + "): "
                        + e.getMessage(), e);
            }
        }
        if (!chiTietPhieu.isEmpty()) {
            throw new IllegalArgumentException("Chi tiết phiếu nhập mồ côi, không có phiếu "
                    + chiTietPhieu.keySet().iterator().next() + ".");
        }

        kiemTraLienKet(taiKhoan, sanPham, bienThe, khuyenMai, donHang, phieuNhap);

        if (congBo) {
            quanLyTaiKhoan.napDanhSach(new ArrayList<>(taiKhoan.values()));
            quanLySanPham.napDanhSach(new ArrayList<>(sanPham.values()));
            quanLyBienThe.napDanhSach(new ArrayList<>(bienThe.values()));
            quanLyKhuyenMai.napDanhSach(new ArrayList<>(khuyenMai.values()));
            quanLyDonHang.napDanhSach(donHang);
            quanLyPhieuNhap.napDanhSach(phieuNhap);
        }
    }

    /** Khóa ngoại, vai trò các bên, đối chiếu tồn/điểm với chứng từ và còn quản lý hoạt động. */
    private static void kiemTraLienKet(Map<String, TaiKhoan> taiKhoan, Map<String, SanPham> sanPham,
                                       Map<String, BienTheSanPham> bienThe, Map<String, KhuyenMai> khuyenMai,
                                       List<DonHang> donHang, List<PhieuNhap> phieuNhap) {
        Set<String> tenDangNhap = new HashSet<>();
        for (TaiKhoan tk : taiKhoan.values()) {
            if (!tenDangNhap.add(tk.getTenDangNhap())) {
                throw new IllegalArgumentException("Trùng tên đăng nhập: " + tk.getTenDangNhap() + ".");
            }
        }
        if (taiKhoan.values().stream().noneMatch(tk -> tk instanceof QuanLyCuaHang && tk.isDangHoatDong())) {
            throw new IllegalArgumentException("Không có tài khoản quản lý nào đang hoạt động.");
        }

        Set<List<Object>> quyCach = new HashSet<>();
        for (BienTheSanPham bt : bienThe.values()) {
            if (!sanPham.containsKey(bt.getMaSanPham())) {
                throw new IllegalArgumentException("SKU " + bt.getMaBienThe() + " tham chiếu sản phẩm không tồn tại "
                        + bt.getMaSanPham() + ".");
            }
            if (!quyCach.add(List.of(bt.getMaSanPham(), bt.getKieuDongGoi(), bt.getSoDonViCoSo()))) {
                throw new IllegalArgumentException("SKU " + bt.getMaBienThe() + " trùng quy cách với SKU khác cùng sản phẩm.");
            }
        }
        for (KhuyenMai km : khuyenMai.values()) {
            if (!bienThe.containsKey(km.getMaBienThe())) {
                throw new IllegalArgumentException("Khuyến mãi " + km.getMaKhuyenMai() + " tham chiếu SKU không tồn tại.");
            }
        }

        Map<String, Long> tonTheoChungTu = new HashMap<>();
        Map<String, Long> diemTheoChungTu = new HashMap<>();
        for (PhieuNhap pn : phieuNhap) {
            if (!(taiKhoan.get(pn.getMaNguoiNhap()) instanceof QuanLyCuaHang)) {
                throw new IllegalArgumentException("Phiếu " + pn.getMaPhieuNhap() + ": người nhập phải là quản lý.");
            }
            for (ChiTietPhieuNhap ct : pn.getChiTiet()) {
                if (!bienThe.containsKey(ct.getMaBienThe())) {
                    throw new IllegalArgumentException("Phiếu " + pn.getMaPhieuNhap() + ": SKU không tồn tại "
                            + ct.getMaBienThe() + ".");
                }
                tonTheoChungTu.merge(ct.getMaBienThe(), (long) ct.getSoLuong(), Long::sum);
            }
        }
        for (DonHang dh : donHang) {
            String ma = "Đơn " + dh.getMaDonHang() + ": ";
            TaiKhoan khach = dh.getMaKhachHang() == null ? null : taiKhoan.get(dh.getMaKhachHang());
            if (dh.getMaKhachHang() != null && !(khach instanceof KhachHang)) {
                throw new IllegalArgumentException(ma + "mã khách hàng không phải khách hàng thành viên.");
            }
            TaiKhoan nguoiLap = taiKhoan.get(dh.getMaNguoiLap());
            if (nguoiLap == null || (nguoiLap instanceof KhachHang && !nguoiLap.getMaTaiKhoan().equals(dh.getMaKhachHang()))) {
                throw new IllegalArgumentException(ma + "người lập không hợp lệ.");
            }
            if (dh.getMaNguoiThuTien() != null
                    && (taiKhoan.get(dh.getMaNguoiThuTien()) == null || taiKhoan.get(dh.getMaNguoiThuTien()) instanceof KhachHang)) {
                throw new IllegalArgumentException(ma + "người thu tiền phải là nhân viên hoặc quản lý.");
            }
            boolean daThanhToan = dh.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN;
            for (ChiTietDonHang ct : dh.getChiTiet()) {
                if (!bienThe.containsKey(ct.getMaBienThe())) {
                    throw new IllegalArgumentException(ma + "SKU không tồn tại " + ct.getMaBienThe() + ".");
                }
                if (ct.getMaKhuyenMai() != null) {
                    KhuyenMai km = khuyenMai.get(ct.getMaKhuyenMai());
                    if (km == null || !km.getMaBienThe().equals(ct.getMaBienThe())) {
                        throw new IllegalArgumentException(ma + "khuyến mãi " + ct.getMaKhuyenMai()
                                + " không tồn tại hoặc không thuộc SKU của dòng.");
                    }
                }
                if (daThanhToan) {
                    tonTheoChungTu.merge(ct.getMaBienThe(), (long) -ct.getSoLuong(), Long::sum);
                }
            }
            if (daThanhToan && dh.getMaKhachHang() != null) {
                diemTheoChungTu.merge(dh.getMaKhachHang(), (long) dh.getDiemCong() - dh.getDiemSuDung(), Long::sum);
            }
        }

        for (BienTheSanPham bt : bienThe.values()) {
            long tonDung = tonTheoChungTu.getOrDefault(bt.getMaBienThe(), 0L);
            if (bt.getSoLuongTon() != tonDung) {
                throw new IllegalArgumentException("Tồn SKU " + bt.getMaBienThe() + " là " + bt.getSoLuongTon()
                        + " nhưng nhập − bán theo chứng từ là " + tonDung + ".");
            }
        }
        for (TaiKhoan tk : taiKhoan.values()) {
            if (tk instanceof KhachHang kh) {
                long diemDung = diemTheoChungTu.getOrDefault(kh.getMaTaiKhoan(), 0L);
                if (kh.getDiemTichLuy() != diemDung) {
                    throw new IllegalArgumentException("Điểm khách " + kh.getTenDangNhap() + " là " + kh.getDiemTichLuy()
                            + " nhưng cộng − dùng theo đơn đã thanh toán là " + diemDung + ".");
                }
            }
        }
    }

    private static TaiKhoan docTaiKhoan(String[] c) {
        VaiTro vaiTro = docEnum(VaiTro.class, c[2], "Vai trò");
        int soVong = docInt(c[5], "soVongBam");
        boolean hoatDong = docBoolean(c[9], "dangHoatDong");
        if (vaiTro == VaiTro.KHACH_HANG) {
            if (c[10].isEmpty()) {
                throw new IllegalArgumentException("Khách hàng phải có cột điểm tích lũy.");
            }
            return new KhachHang(c[1], c[3], c[4], soVong, c[6], c[7], c[8], hoatDong, docInt(c[10], "diemTichLuy"));
        }
        if (!c[10].isEmpty()) {
            throw new IllegalArgumentException("Tài khoản nội bộ phải để trống cột điểm.");
        }
        return vaiTro == VaiTro.NHAN_VIEN_BAN_HANG
                ? new NhanVienBanHang(c[1], c[3], c[4], soVong, c[6], c[7], c[8], hoatDong)
                : new QuanLyCuaHang(c[1], c[3], c[4], soVong, c[6], c[7], c[8], hoatDong);
    }

    private static KhuyenMai docKhuyenMai(String[] c) {
        LocalDate tu = docNgay(c[5], "tuNgay");
        LocalDate den = docNgay(c[6], "denNgay");
        boolean apDung = docBoolean(c[7], "dangApDung");
        return switch (c[2]) {
            case "PHAN_TRAM" -> new KhuyenMaiPhanTram(c[1], c[3], c[4], tu, den, apDung, docInt(c[8], "giaTri"));
            case "SO_TIEN" -> new KhuyenMaiSoTien(c[1], c[3], c[4], tu, den, apDung, docLong(c[8], "giaTri"));
            default -> throw new IllegalArgumentException("Loại khuyến mãi không hợp lệ: " + c[2]);
        };
    }

    private static String[] soCot(String[] c, int soCot) {
        if (c.length != soCot) {
            throw new IllegalArgumentException(c[0] + " cần " + soCot + " cột, có " + c.length + ".");
        }
        return c;
    }

    private static <V> void dat(Map<String, V> bang, String ma, V giaTri, String tenKhoa) {
        if (bang.putIfAbsent(ma, giaTri) != null) {
            throw new IllegalArgumentException("Trùng " + tenKhoa + ": " + ma + ".");
        }
    }

    private static String tuyChon(String s) {
        return s.isEmpty() ? null : s;
    }

    private static int docInt(String s, String tenCot) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tenCot + " không phải số nguyên hợp lệ: '" + s + "'.");
        }
    }

    private static long docLong(String s, String tenCot) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tenCot + " không phải số hợp lệ: '" + s + "'.");
        }
    }

    private static boolean docBoolean(String s, String tenCot) {
        return switch (s) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(tenCot + " phải là true/false: '" + s + "'.");
        };
    }

    private static <E extends Enum<E>> E docEnum(Class<E> loai, String s, String tenCot) {
        try {
            return Enum.valueOf(loai, s);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(tenCot + " không hợp lệ: '" + s + "'.");
        }
    }

    private static LocalDate docNgay(String s, String tenCot) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(tenCot + " phải có dạng yyyy-MM-dd: '" + s + "'.");
        }
    }

    private static LocalDateTime docThoiDiem(String s, String tenCot) {
        try {
            return LocalDateTime.parse(s, DINH_DANG_THOI_DIEM);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(tenCot + " phải có dạng yyyy-MM-ddTHH:mm:ss: '" + s + "'.");
        }
    }

    // ---------------------------------------------------------------- ràng buộc trường văn bản

    /** Trường văn bản bắt buộc: không trống, không chứa ký tự phân cách của định dạng text. */
    static String vanBan(String giaTri, String tenTruong) {
        String s = vanBanTuyChon(giaTri, tenTruong);
        if (s == null) {
            throw new IllegalArgumentException(tenTruong + " không được để trống.");
        }
        return s;
    }

    /** Trường văn bản tùy chọn: trống trả null. */
    static String vanBanTuyChon(String giaTri, String tenTruong) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        String s = giaTri.strip();
        if (s.indexOf('|') >= 0 || s.indexOf('\r') >= 0 || s.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(tenTruong + " không được chứa ký tự | hoặc xuống dòng.");
        }
        return s;
    }
}
