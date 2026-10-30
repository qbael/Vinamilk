package cuahang;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class DichVuCuaHang {
    static final ZoneId MUI_GIO = ZoneId.of("Asia/Ho_Chi_Minh");

    private final QuanLyTaiKhoan quanLyTaiKhoan;
    private final QuanLySanPham quanLySanPham;
    private final QuanLyBienTheSanPham quanLyBienThe;
    private final QuanLyKhuyenMai quanLyKhuyenMai;
    private final QuanLyDonHang quanLyDonHang;
    private final QuanLyPhieuNhap quanLyPhieuNhap;
    private final LuuTruText luuTru;

    public DichVuCuaHang(QuanLyTaiKhoan quanLyTaiKhoan, QuanLySanPham quanLySanPham,
                         QuanLyBienTheSanPham quanLyBienThe, QuanLyKhuyenMai quanLyKhuyenMai,
                         QuanLyDonHang quanLyDonHang, QuanLyPhieuNhap quanLyPhieuNhap, LuuTruText luuTru) {
        this.quanLyTaiKhoan = quanLyTaiKhoan;
        this.quanLySanPham = quanLySanPham;
        this.quanLyBienThe = quanLyBienThe;
        this.quanLyKhuyenMai = quanLyKhuyenMai;
        this.quanLyDonHang = quanLyDonHang;
        this.quanLyPhieuNhap = quanLyPhieuNhap;
        this.luuTru = luuTru;
    }

    // ================================================================ giao dịch và quyền

    /**
     * Một giao dịch: chụp trạng thái, đổi RAM, lưu an toàn. Lỗi ở bất kỳ bước nào thì
     * phục hồi RAM từ bản chụp; phục hồi cũng lỗi thì dừng ứng dụng (Error).
     */
    private void giaoDich(Runnable thayDoi) {
        String banCu = luuTru.xuatThanhChuoi();
        try {
            thayDoi.run();
            luuTru.ghiAnToan();
        } catch (IOException | RuntimeException e) {
            try {
                luuTru.napTuChuoi(banCu);
            } catch (RuntimeException loiPhucHoi) {
                loiPhucHoi.addSuppressed(e);
                throw new Error("Không phục hồi được dữ liệu trong bộ nhớ sau lỗi lưu; dừng để tránh sai lệch.",
                        loiPhucHoi);
            }
            if (e instanceof IOException) {
                throw new IllegalStateException("Không lưu được dữ liệu, thao tác chưa hoàn tất: " + e.getMessage(), e);
            }
            throw (RuntimeException) e;
        }
    }

    /** Tra lại tài khoản đang hoạt động theo mã phiên và kiểm tra vai trò được phép. */
    private TaiKhoan yeuCau(String maNguoiGoi, VaiTro... vaiTroDuocPhep) {
        TaiKhoan tk = maNguoiGoi == null ? null : quanLyTaiKhoan.timTheoMa(maNguoiGoi);
        if (tk == null || !tk.isDangHoatDong()) {
            throw new IllegalStateException("Phiên đăng nhập không còn hợp lệ hoặc tài khoản đã bị khóa.");
        }
        if (vaiTroDuocPhep.length > 0 && !List.of(vaiTroDuocPhep).contains(tk.layVaiTro())) {
            throw new SecurityException("Bạn không có quyền thực hiện chức năng này.");
        }
        return tk;
    }

    private TaiKhoan yeuCauQuanLy(String maNguoiGoi) {
        return yeuCau(maNguoiGoi, VaiTro.QUAN_LY_CUA_HANG);
    }

    static LocalDateTime bayGio() {
        return LocalDateTime.now(MUI_GIO).truncatedTo(ChronoUnit.SECONDS);
    }

    static LocalDate homNay() {
        return LocalDate.now(MUI_GIO);
    }

    // ================================================================ hệ thống và tài khoản

    public boolean canKhoiTao() {
        return !luuTru.tonTai() && quanLyTaiKhoan.xuat().isEmpty();
    }

    /** Lần chạy đầu: tạo quản lý đầu tiên do người vận hành nhập và ghi file dữ liệu ban đầu. */
    public String khoiTaoHeThong(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        if (!canKhoiTao()) {
            throw new IllegalStateException("Hệ thống đã có dữ liệu, không khởi tạo lại.");
        }
        QuanLyCuaHang quanLy = new QuanLyCuaHang(tenDangNhap, matKhau, hoTen, soDienThoai);
        quanLyTaiKhoan.them(quanLy);
        try {
            luuTru.ghiAnToan();
        } catch (IOException | RuntimeException e) {
            quanLyTaiKhoan.napDanhSach(List.of());
            throw new IllegalStateException("Không ghi được file dữ liệu ban đầu: " + e.getMessage(), e);
        }
        return quanLy.getMaTaiKhoan();
    }

    public String dangKyKhachHang(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        if (quanLyTaiKhoan.timTheoTenDangNhap(tenDangNhap) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        KhachHang khach = new KhachHang(tenDangNhap, matKhau, hoTen, soDienThoai);
        giaoDich(() -> quanLyTaiKhoan.them(khach));
        return khach.getMaTaiKhoan();
    }

    /** Trả mã tài khoản để giao diện giữ làm phiên. */
    public String dangNhap(String tenDangNhap, char[] matKhau) {
        TaiKhoan tk = quanLyTaiKhoan.timTheoTenDangNhap(tenDangNhap);
        if (tk == null || !tk.kiemTraMatKhau(matKhau)) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        if (!tk.isDangHoatDong()) {
            throw new IllegalStateException("Tài khoản đã bị khóa.");
        }
        return tk.getMaTaiKhoan();
    }

    public TaiKhoan layTaiKhoanDangNhap(String maNguoiGoi) {
        return yeuCau(maNguoiGoi);
    }

    public String taoTaiKhoanNoiBo(String maQuanLy, VaiTro vaiTro, String tenDangNhap, char[] matKhau,
                                   String hoTen, String soDienThoai) {
        yeuCauQuanLy(maQuanLy);
        if (quanLyTaiKhoan.timTheoTenDangNhap(tenDangNhap) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        TaiKhoan moi = switch (vaiTro) {
            case NHAN_VIEN_BAN_HANG -> new NhanVienBanHang(tenDangNhap, matKhau, hoTen, soDienThoai);
            case QUAN_LY_CUA_HANG -> new QuanLyCuaHang(tenDangNhap, matKhau, hoTen, soDienThoai);
            case KHACH_HANG -> throw new IllegalArgumentException("Khách hàng tự đăng ký, không tạo ở đây.");
        };
        giaoDich(() -> quanLyTaiKhoan.them(moi));
        return moi.getMaTaiKhoan();
    }

    public List<TaiKhoan> danhSachTaiKhoan(String maQuanLy) {
        yeuCauQuanLy(maQuanLy);
        return quanLyTaiKhoan.xuat();
    }

    /** Trả true nếu quản lý tự khóa mình: phiên hiện tại phải kết thúc. */
    public boolean khoaTaiKhoan(String maQuanLy, String maTaiKhoan) {
        doiTrangThaiTaiKhoan(maQuanLy, maTaiKhoan, false);
        return maTaiKhoan.equals(maQuanLy);
    }

    public void moKhoaTaiKhoan(String maQuanLy, String maTaiKhoan) {
        doiTrangThaiTaiKhoan(maQuanLy, maTaiKhoan, true);
    }

    private void doiTrangThaiTaiKhoan(String maQuanLy, String maTaiKhoan, boolean hoatDong) {
        yeuCauQuanLy(maQuanLy);
        TaiKhoan tk = quanLyTaiKhoan.timTheoMa(maTaiKhoan);
        if (tk == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        if (tk.isDangHoatDong() == hoatDong) {
            throw new IllegalStateException(hoatDong ? "Tài khoản đang hoạt động." : "Tài khoản đã bị khóa.");
        }
        if (!hoatDong && tk instanceof QuanLyCuaHang && quanLyTaiKhoan.demQuanLyDangHoatDong() <= 1) {
            throw new IllegalStateException("Không được khóa quản lý đang hoạt động cuối cùng.");
        }
        giaoDich(() -> quanLyTaiKhoan.sua(maTaiKhoan, tk.banSaoVoiTrangThai(hoatDong)));
    }

    /** Nhân viên/quản lý tra thành viên theo số điện thoại để phục vụ bán hàng. */
    public List<KhachHang> traThanhVien(String maNguoiGoi, String soDienThoai) {
        yeuCau(maNguoiGoi, VaiTro.NHAN_VIEN_BAN_HANG, VaiTro.QUAN_LY_CUA_HANG);
        return quanLyTaiKhoan.timKhachTheoSoDienThoai(soDienThoai);
    }

    /** Khách chỉ xem chính mình; nhân viên/quản lý xem thành viên bất kỳ. */
    public KhachHang xemKhachHang(String maNguoiGoi, String maKhachHang) {
        TaiKhoan nguoiGoi = yeuCau(maNguoiGoi);
        if (nguoiGoi instanceof KhachHang && !nguoiGoi.getMaTaiKhoan().equals(maKhachHang)) {
            throw new SecurityException("Bạn chỉ được xem thông tin của chính mình.");
        }
        if (!(quanLyTaiKhoan.timTheoMa(maKhachHang) instanceof KhachHang khach)) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng thành viên.");
        }
        return khach;
    }

    // ================================================================ danh mục

    public boolean dangBan(BienTheSanPham bienThe) {
        return quanLyBienThe.dangBan(bienThe);
    }

    public SanPham timSanPham(String maSanPham) {
        return quanLySanPham.timTheoMa(maSanPham);
    }

    /** Quản lý thấy cả mục ngừng bán; người khác chỉ thấy sản phẩm đang kinh doanh. */
    public List<SanPham> danhSachSanPham(String maNguoiGoi) {
        boolean laQuanLy = yeuCau(maNguoiGoi) instanceof QuanLyCuaHang;
        return quanLySanPham.xuat().stream().filter(sp -> laQuanLy || sp.isDangKinhDoanh()).toList();
    }

    /**
     * Tìm SKU theo tên sản phẩm, tên quy cách, nhóm hàng, kiểu đóng gói hoặc phần đầu mã
     * sản phẩm/SKU; từ khóa trống trả tất cả. Quản lý thấy cả SKU ngừng bán.
     */
    public List<BienTheSanPham> timBienThe(String maNguoiGoi, String tuKhoa) {
        boolean laQuanLy = yeuCau(maNguoiGoi) instanceof QuanLyCuaHang;
        String tu = tuKhoa == null ? "" : tuKhoa.strip().toLowerCase(Locale.ROOT);
        return quanLyBienThe.xuat().stream()
                .filter(bt -> laQuanLy || quanLyBienThe.dangBan(bt))
                .filter(bt -> tu.isEmpty() || khopTuKhoa(bt, tu))
                .toList();
    }

    private boolean khopTuKhoa(BienTheSanPham bt, String tu) {
        SanPham sp = quanLySanPham.timTheoMa(bt.getMaSanPham());
        return bt.getMaBienThe().startsWith(tu)
                || bt.getMaSanPham().startsWith(tu)
                || bt.getTenQuyCach().toLowerCase(Locale.ROOT).contains(tu)
                || bt.getKieuDongGoi().getTenHienThi().toLowerCase(Locale.ROOT).contains(tu)
                || sp.getTenSanPham().toLowerCase(Locale.ROOT).contains(tu)
                || sp.getLoai().getTenHienThi().toLowerCase(Locale.ROOT).contains(tu);
    }

    public String themSanPham(String maQuanLy, String tenSanPham, String thuongHieu, String donViCoSo,
                              String dinhLuong, String moTa, LoaiSanPham loai) {
        yeuCauQuanLy(maQuanLy);
        SanPham sp = new SanPham(tenSanPham, thuongHieu, donViCoSo, dinhLuong, moTa, loai);
        giaoDich(() -> quanLySanPham.them(sp));
        return sp.getMaSanPham();
    }

    public void capNhatSanPham(String maQuanLy, String maSanPham, String tenSanPham, String thuongHieu,
                               String moTa, boolean dangKinhDoanh) {
        yeuCauQuanLy(maQuanLy);
        SanPham cu = quanLySanPham.timTheoMa(maSanPham);
        if (cu == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm.");
        }
        SanPham moi = new SanPham(maSanPham, tenSanPham, thuongHieu, cu.getDonViCoSo(), cu.getDinhLuong(), moTa,
                cu.getLoai(), dangKinhDoanh);
        giaoDich(() -> quanLySanPham.sua(maSanPham, moi));
    }

    public String themBienThe(String maQuanLy, String maSanPham, String tenQuyCach, KieuDongGoi kieuDongGoi,
                              int soDonViCoSo, long giaBan) {
        yeuCauQuanLy(maQuanLy);
        BienTheSanPham bt = new BienTheSanPham(maSanPham, tenQuyCach, kieuDongGoi, soDonViCoSo, giaBan);
        giaoDich(() -> quanLyBienThe.them(bt));
        return bt.getMaBienThe();
    }

    public void capNhatBienThe(String maQuanLy, String maBienThe, long giaBan, boolean dangKinhDoanh) {
        yeuCauQuanLy(maQuanLy);
        BienTheSanPham cu = timBienTheCoSan(maBienThe);
        BienTheSanPham moi = new BienTheSanPham(maBienThe, cu.getMaSanPham(), cu.getTenQuyCach(),
                cu.getKieuDongGoi(), cu.getSoDonViCoSo(), giaBan, cu.getSoLuongTon(), dangKinhDoanh);
        giaoDich(() -> quanLyBienThe.sua(maBienThe, moi));
    }

    private BienTheSanPham timBienTheCoSan(String maBienThe) {
        BienTheSanPham bt = quanLyBienThe.timTheoMa(maBienThe);
        if (bt == null) {
            throw new IllegalArgumentException("Không tìm thấy SKU.");
        }
        return bt;
    }

    public List<BienTheSanPham> xemTonKho(String maQuanLy) {
        yeuCauQuanLy(maQuanLy);
        return quanLyBienThe.xuat();
    }

    // ================================================================ nhập hàng

    /** Thêm dòng vào phiếu đang soạn; cùng SKU thì cộng lượng nếu cùng giá nhập. */
    public void themVaoPhieuNhap(String maQuanLy, DanhSachChiTietPhieuNhap phieu, String maBienThe, int soLuong,
                                 long donGiaNhap) {
        yeuCauQuanLy(maQuanLy);
        phieu.them(dongNhap(maBienThe, soLuong, donGiaNhap));
    }

    /** Thay nguyên dòng SKU trên phiếu đang soạn bằng lượng/giá mới. */
    public void suaDongPhieuNhap(String maQuanLy, DanhSachChiTietPhieuNhap phieu, String maBienThe, int soLuong,
                                 long donGiaNhap) {
        yeuCauQuanLy(maQuanLy);
        phieu.sua(maBienThe, dongNhap(maBienThe, soLuong, donGiaNhap));
    }

    public void xoaDongPhieuNhap(String maQuanLy, DanhSachChiTietPhieuNhap phieu, String maBienThe) {
        yeuCauQuanLy(maQuanLy);
        phieu.xoa(maBienThe);
    }

    private ChiTietPhieuNhap dongNhap(String maBienThe, int soLuong, long donGiaNhap) {
        BienTheSanPham bt = timBienTheCoSan(maBienThe);
        SanPham sp = quanLySanPham.timTheoMa(bt.getMaSanPham());
        return new ChiTietPhieuNhap(maBienThe, sp.getTenSanPham(), bt.getTenQuyCach(), soLuong, donGiaNhap);
    }

    /**
     * Ghi nhận phiếu (đã nhận hàng, trả đủ tiền) và tăng tồn trong cùng một lần lưu.
     * Phiếu được dựng từ bản sao các dòng nên bản đang soạn vẫn sửa được nếu lưu lỗi.
     */
    public String nhapHang(String maQuanLy, String tenNguonNhap, DanhSachChiTietPhieuNhap phieuDangSoan) {
        yeuCauQuanLy(maQuanLy);
        DanhSachChiTietPhieuNhap chiTiet = new DanhSachChiTietPhieuNhap();
        for (ChiTietPhieuNhap ct : phieuDangSoan.xuat()) {
            Math.addExact(timBienTheCoSan(ct.getMaBienThe()).getSoLuongTon(), ct.getSoLuong());
            chiTiet.them(ct);
        }
        PhieuNhap phieu = new PhieuNhap(maQuanLy, tenNguonNhap, bayGio(), chiTiet);
        giaoDich(() -> {
            quanLyPhieuNhap.them(phieu);
            for (ChiTietPhieuNhap ct : phieu.getChiTiet()) {
                quanLyBienThe.timTheoMa(ct.getMaBienThe()).tangTon(ct.getSoLuong());
            }
        });
        return phieu.getMaPhieuNhap();
    }

    public List<PhieuNhap> danhSachPhieuNhap(String maQuanLy) {
        yeuCauQuanLy(maQuanLy);
        return quanLyPhieuNhap.xuat();
    }

    public PhieuNhap xemPhieuNhap(String maQuanLy, String maPhieuNhap) {
        yeuCauQuanLy(maQuanLy);
        PhieuNhap pn = quanLyPhieuNhap.timTheoMa(maPhieuNhap);
        if (pn == null) {
            throw new IllegalArgumentException("Không tìm thấy phiếu nhập.");
        }
        return pn;
    }
}
