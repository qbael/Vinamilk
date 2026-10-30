package cuahang;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
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

    public BienTheSanPham timBienTheTheoMa(String maBienThe) {
        return quanLyBienThe.timTheoMa(maBienThe);
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

    // ================================================================ giỏ hàng, đặt hàng, hủy

    /**
     * Thêm SKU vào giỏ đang soạn; chọn lại cùng SKU thì gộp lượng.
     * Kiểm tra tồn trên tổng lượng đã gộp, không chỉ lần chọn này.
     */
    public void themVaoGio(String maNguoiGoi, DanhSachChiTietDonHang gio, String maBienThe, int soLuong) {
        yeuCau(maNguoiGoi);
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
        }
        ChiTietDonHang cu = gio.timTheoBienThe(maBienThe);
        int tong = cu == null ? soLuong : Math.addExact(cu.getSoLuong(), soLuong);
        ChiTietDonHang dong = dongBan(timBienTheDangBan(maBienThe), tong, null, 0);
        if (cu == null) {
            gio.them(dong);
        } else {
            gio.sua(maBienThe, dong);
        }
    }

    /** Đặt lại số lượng một dòng trong giỏ (thay, không cộng). */
    public void suaSoLuongTrongGio(String maNguoiGoi, DanhSachChiTietDonHang gio, String maBienThe, int soLuongMoi) {
        yeuCau(maNguoiGoi);
        gio.sua(maBienThe, dongBan(timBienTheDangBan(maBienThe), soLuongMoi, null, 0));
    }

    public void xoaKhoiGio(String maNguoiGoi, DanhSachChiTietDonHang gio, String maBienThe) {
        yeuCau(maNguoiGoi);
        gio.xoa(maBienThe);
    }

    /** Bảng giá tạm tính của giỏ theo giá và khuyến mãi hôm nay; không lưu gì. */
    public DanhSachChiTietDonHang baoGia(String maNguoiGoi, DanhSachChiTietDonHang gio) {
        yeuCau(maNguoiGoi);
        return tinhGia(gio.xuat(), homNay());
    }

    /** Khách tự đặt đơn nhận tại cửa hàng; đơn luôn gắn chính khách đang đăng nhập. */
    public String datHang(String maKhachHang, DanhSachChiTietDonHang gio) {
        yeuCau(maKhachHang, VaiTro.KHACH_HANG);
        return luuDonMoi(maKhachHang, maKhachHang, gio);
    }

    /** Nhân viên/quản lý lập đơn tại quầy; maKhachHang null nghĩa là khách vãng lai. */
    public String lapDonTaiQuay(String maNguoiLap, String maKhachHang, DanhSachChiTietDonHang gio) {
        yeuCau(maNguoiLap, VaiTro.NHAN_VIEN_BAN_HANG, VaiTro.QUAN_LY_CUA_HANG);
        if (maKhachHang != null) {
            khachDangHoatDong(maKhachHang);
        }
        return luuDonMoi(maKhachHang, maNguoiLap, gio);
    }

    private String luuDonMoi(String maKhachHang, String maNguoiLap, DanhSachChiTietDonHang gio) {
        DonHang don = new DonHang(maKhachHang, maNguoiLap, tinhGia(gio.xuat(), homNay()), bayGio());
        giaoDich(() -> quanLyDonHang.them(don));
        return don.getMaDonHang();
    }

    /** Khách chỉ thấy đơn của mình; nhân viên/quản lý thấy mọi đơn. trangThai null = tất cả. */
    public List<DonHang> danhSachDon(String maNguoiGoi, TrangThaiDonHang trangThai) {
        TaiKhoan nguoiGoi = yeuCau(maNguoiGoi);
        List<DonHang> don = nguoiGoi instanceof KhachHang
                ? quanLyDonHang.timTheoKhach(nguoiGoi.getMaTaiKhoan())
                : quanLyDonHang.xuat();
        return don.stream().filter(dh -> trangThai == null || dh.getTrangThai() == trangThai).toList();
    }

    public DonHang xemDon(String maNguoiGoi, String maDonHang) {
        TaiKhoan nguoiGoi = yeuCau(maNguoiGoi);
        DonHang don = quanLyDonHang.timTheoMa(maDonHang);
        if (don == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng.");
        }
        if (nguoiGoi instanceof KhachHang && !nguoiGoi.getMaTaiKhoan().equals(don.getMaKhachHang())) {
            throw new SecurityException("Đơn hàng không thuộc tài khoản của bạn.");
        }
        return don;
    }

    /** Hủy đơn chờ: chỉ đổi trạng thái, không hoàn tồn/điểm vì đơn chờ chưa trừ. */
    public void huyDon(String maNguoiGoi, String maDonHang) {
        DonHang don = xemDon(maNguoiGoi, maDonHang);
        if (don.getTrangThai() != TrangThaiDonHang.CHO_THANH_TOAN) {
            throw new IllegalStateException("Chỉ hủy được đơn đang chờ thanh toán (đơn này: "
                    + don.getTrangThai().getTenHienThi() + ").");
        }
        giaoDich(() -> quanLyDonHang.timTheoMa(maDonHang).huy());
    }

    /** Lịch sử điểm: các đơn đã thanh toán của khách (điểm dùng/cộng nằm trên đơn). */
    public List<DonHang> lichSuDiem(String maNguoiGoi, String maKhachHang) {
        xemKhachHang(maNguoiGoi, maKhachHang);
        return quanLyDonHang.timTheoKhach(maKhachHang).stream()
                .filter(dh -> dh.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN)
                .toList();
    }

    // ================================================================ thanh toán

    /** Số điểm tối đa khách được dùng cho đơn theo giá hiện hành: min(điểm có, 20% sau khuyến mãi). */
    public int diemToiDaDuocDung(String maNguoiThu, String maDonHang) {
        DonHang don = donChoThanhToan(maNguoiThu, maDonHang);
        if (don.getMaKhachHang() == null) {
            return 0;
        }
        DanhSachChiTietDonHang dong = tinhGia(don.getChiTiet(), homNay());
        return Math.min(khachDangHoatDong(don.getMaKhachHang()).getDiemTichLuy(),
                DonHang.diemToiDaTheoTien(dong.tinhTongTienHang() - dong.tinhTongGiam()));
    }

    /**
     * Bảng tính cuối theo giá/khuyến mãi hiện hành và số điểm khách chọn,
     * trên một bản sao tách rời; đơn thật và tồn/điểm không đổi.
     */
    public DonHang xemTruocThanhToan(String maNguoiThu, String maDonHang, int diemSuDung) {
        DonHang don = donChoThanhToan(maNguoiThu, maDonHang);
        DonHang banSao = don.banSao();
        banSao.chotThanhToan(dongThanhToan(don, diemSuDung, homNay()), maNguoiThu, bayGio(), diemSuDung);
        return banSao;
    }

    /**
     * Thu tiền: tính lại, đối chiếu với tổng khách đã xác nhận, rồi chốt đơn + trừ tồn
     * + trừ/cộng điểm trong cùng một lần lưu. Chỉ trả về khi đã lưu thành công.
     */
    public DonHang thanhToan(String maNguoiThu, String maDonHang, int diemSuDung, long tongDaXacNhan) {
        DonHang don = donChoThanhToan(maNguoiThu, maDonHang);
        LocalDateTime thoiDiem = bayGio();
        DanhSachChiTietDonHang dong = dongThanhToan(don, diemSuDung, thoiDiem.toLocalDate());
        giaoDich(() -> {
            DonHang dh = quanLyDonHang.timTheoMa(maDonHang);
            dh.chotThanhToan(dong, maNguoiThu, thoiDiem, diemSuDung);
            if (dh.getTongThanhToan() != tongDaXacNhan) {
                throw new IllegalStateException(
                        "Số tiền phải trả đã thay đổi so với bảng tính khách xác nhận; hãy xem lại.");
            }
            for (ChiTietDonHang ct : dh.getChiTiet()) {
                quanLyBienThe.timTheoMa(ct.getMaBienThe()).giamTon(ct.getSoLuong());
            }
            if (dh.getMaKhachHang() != null) {
                KhachHang khach = (KhachHang) quanLyTaiKhoan.timTheoMa(dh.getMaKhachHang());
                khach.truDiem(dh.getDiemSuDung());
                khach.congDiem(dh.getDiemCong());
            }
        });
        return quanLyDonHang.timTheoMa(maDonHang);
    }

    private DonHang donChoThanhToan(String maNguoiThu, String maDonHang) {
        yeuCau(maNguoiThu, VaiTro.NHAN_VIEN_BAN_HANG, VaiTro.QUAN_LY_CUA_HANG);
        DonHang don = xemDon(maNguoiThu, maDonHang);
        if (don.getTrangThai() != TrangThaiDonHang.CHO_THANH_TOAN) {
            throw new IllegalStateException(don.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN
                    ? "Đơn đã thanh toán." : "Đơn đã hủy.");
        }
        if (don.getMaKhachHang() != null) {
            khachDangHoatDong(don.getMaKhachHang());
        }
        return don;
    }

    /** Dòng đã tính lại giá/khuyến mãi của ngày thanh toán, sau khi kiểm tra điểm khách đang có. */
    private DanhSachChiTietDonHang dongThanhToan(DonHang don, int diemSuDung, LocalDate ngay) {
        if (don.getMaKhachHang() != null) {
            int diemCo = khachDangHoatDong(don.getMaKhachHang()).getDiemTichLuy();
            if (diemSuDung > diemCo) {
                throw new IllegalArgumentException("Khách không đủ điểm: hiện có " + diemCo + ".");
            }
        }
        return tinhGia(don.getChiTiet(), ngay);
    }

    private KhachHang khachDangHoatDong(String maKhachHang) {
        if (!(quanLyTaiKhoan.timTheoMa(maKhachHang) instanceof KhachHang khach)) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng thành viên.");
        }
        if (!khach.isDangHoatDong()) {
            throw new IllegalStateException("Tài khoản khách hàng đã bị khóa.");
        }
        return khach;
    }

    /**
     * Dựng danh sách dòng mới theo giá bán và khuyến mãi tốt nhất của ngày cho cả dòng.
     * Mọi SKU phải đang bán và đủ tồn; không tự bỏ dòng nào.
     */
    private DanhSachChiTietDonHang tinhGia(List<ChiTietDonHang> dong, LocalDate ngay) {
        if (dong.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }
        DanhSachChiTietDonHang ketQua = new DanhSachChiTietDonHang();
        for (ChiTietDonHang ct : dong) {
            BienTheSanPham bt = timBienTheDangBan(ct.getMaBienThe());
            KhuyenMai km = quanLyKhuyenMai.timTotNhat(bt.getMaBienThe(), ngay, bt.getGiaBan(), ct.getSoLuong());
            ketQua.them(dongBan(bt, ct.getSoLuong(), km == null ? null : km.getMaKhuyenMai(),
                    km == null ? 0 : km.tinhTienGiam(bt.getGiaBan(), ct.getSoLuong())));
        }
        return ketQua;
    }

    private BienTheSanPham timBienTheDangBan(String maBienThe) {
        BienTheSanPham bt = timBienTheCoSan(maBienThe);
        if (!quanLyBienThe.dangBan(bt)) {
            throw new IllegalArgumentException("SKU " + bt.getTenQuyCach() + " đang ngừng bán.");
        }
        return bt;
    }

    /** Dòng bán chụp tên/giá hiện tại; số lượng không vượt tồn hiện có. */
    private ChiTietDonHang dongBan(BienTheSanPham bt, int soLuong, String maKhuyenMai, long tienGiam) {
        if (soLuong > bt.getSoLuongTon()) {
            throw new IllegalArgumentException("Không đủ hàng " + bt.getTenQuyCach() + ": còn "
                    + bt.getSoLuongTon() + ", cần " + soLuong + ".");
        }
        SanPham sp = quanLySanPham.timTheoMa(bt.getMaSanPham());
        return new ChiTietDonHang(bt.getMaBienThe(), sp.getTenSanPham(), bt.getTenQuyCach(), soLuong,
                bt.getGiaBan(), maKhuyenMai, tienGiam);
    }

    // ================================================================ khuyến mãi

    /** Quản lý thấy mọi chương trình; người khác chỉ thấy chương trình đang có hiệu lực hôm nay. */
    public List<KhuyenMai> danhSachKhuyenMai(String maNguoiGoi) {
        boolean laQuanLy = yeuCau(maNguoiGoi) instanceof QuanLyCuaHang;
        LocalDate ngay = homNay();
        return quanLyKhuyenMai.xuat().stream().filter(km -> laQuanLy || km.coHieuLuc(ngay)).toList();
    }

    /** Chương trình tốt nhất hôm nay cho một đơn vị SKU, để hiển thị danh mục; null nếu không có. */
    public KhuyenMai khuyenMaiHienHanh(BienTheSanPham bienThe) {
        return quanLyKhuyenMai.timTotNhat(bienThe.getMaBienThe(), homNay(), bienThe.getGiaBan(), 1);
    }

    public String tenKhuyenMai(String maKhuyenMai) {
        KhuyenMai km = quanLyKhuyenMai.timTheoMa(maKhuyenMai);
        return km == null ? maKhuyenMai : km.getTenKhuyenMai();
    }

    /** laPhanTram: giaTri là tỷ lệ 1–100; ngược lại là số đồng giảm mỗi đơn vị SKU. */
    public String themKhuyenMai(String maQuanLy, boolean laPhanTram, String tenKhuyenMai, String maBienThe,
                                LocalDate tuNgay, LocalDate denNgay, long giaTri) {
        yeuCauQuanLy(maQuanLy);
        timBienTheCoSan(maBienThe);
        KhuyenMai km = laPhanTram
                ? new KhuyenMaiPhanTram(tenKhuyenMai, maBienThe, tuNgay, denNgay, tyLe(giaTri))
                : new KhuyenMaiSoTien(tenKhuyenMai, maBienThe, tuNgay, denNgay, giaTri);
        giaoDich(() -> quanLyKhuyenMai.them(km));
        return km.getMaKhuyenMai();
    }

    /** Sửa tên, thời hạn và mức giảm cho các lần thanh toán sau; không đổi SKU/loại giảm. */
    public void capNhatKhuyenMai(String maQuanLy, String maKhuyenMai, String tenKhuyenMai, LocalDate tuNgay,
                                 LocalDate denNgay, long giaTri) {
        yeuCauQuanLy(maQuanLy);
        KhuyenMai cu = timKhuyenMaiCoSan(maKhuyenMai);
        giaoDich(() -> quanLyKhuyenMai.sua(maKhuyenMai,
                banSaoKhuyenMai(cu, tenKhuyenMai, tuNgay, denNgay, cu.isDangApDung(), giaTri)));
    }

    public void ngungKhuyenMai(String maQuanLy, String maKhuyenMai) {
        yeuCauQuanLy(maQuanLy);
        KhuyenMai cu = timKhuyenMaiCoSan(maKhuyenMai);
        if (!cu.isDangApDung()) {
            throw new IllegalStateException("Chương trình đã ngừng áp dụng.");
        }
        long giaTri = cu instanceof KhuyenMaiPhanTram pt ? pt.getTyLePhanTram()
                : ((KhuyenMaiSoTien) cu).getSoTienGiamMoiDonVi();
        giaoDich(() -> quanLyKhuyenMai.sua(maKhuyenMai,
                banSaoKhuyenMai(cu, cu.getTenKhuyenMai(), cu.getTuNgay(), cu.getDenNgay(), false, giaTri)));
    }

    private KhuyenMai timKhuyenMaiCoSan(String maKhuyenMai) {
        KhuyenMai km = quanLyKhuyenMai.timTheoMa(maKhuyenMai);
        if (km == null) {
            throw new IllegalArgumentException("Không tìm thấy khuyến mãi.");
        }
        return km;
    }

    private static KhuyenMai banSaoKhuyenMai(KhuyenMai cu, String ten, LocalDate tuNgay, LocalDate denNgay,
                                             boolean dangApDung, long giaTri) {
        return cu instanceof KhuyenMaiPhanTram
                ? new KhuyenMaiPhanTram(cu.getMaKhuyenMai(), ten, cu.getMaBienThe(), tuNgay, denNgay, dangApDung, tyLe(giaTri))
                : new KhuyenMaiSoTien(cu.getMaKhuyenMai(), ten, cu.getMaBienThe(), tuNgay, denNgay, dangApDung, giaTri);
    }

    private static int tyLe(long giaTri) {
        if (giaTri < 1 || giaTri > 100) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải từ 1 đến 100%.");
        }
        return (int) giaTri;
    }

    // ================================================================ báo cáo

    /**
     * Tổng kết thu-chi trong kỳ [tuNgay, denNgay]: thu theo ngày thanh toán của đơn đã thanh toán,
     * chi theo ngày nhập; dùng giá trị đã chốt trên chứng từ. Chênh lệch không phải lợi nhuận.
     */
    public LinkedHashMap<String, Long> tongKetThuChi(String maQuanLy, LocalDate tuNgay, LocalDate denNgay) {
        yeuCauQuanLy(maQuanLy);
        long doanhThuGop = 0;
        long giamKhuyenMai = 0;
        long giamDiem = 0;
        List<DonHang> donTrongKy = quanLyDonHang.timDaThanhToanTrongKhoang(tuNgay, denNgay);
        for (DonHang dh : donTrongKy) {
            doanhThuGop = Math.addExact(doanhThuGop, dh.getTongTienHang());
            giamKhuyenMai = Math.addExact(giamKhuyenMai, dh.getTongGiamKhuyenMai());
            giamDiem = Math.addExact(giamDiem, dh.getTienGiamDiem());
        }
        long chiNhap = 0;
        for (PhieuNhap pn : quanLyPhieuNhap.timTrongKhoang(tuNgay, denNgay)) {
            chiNhap = Math.addExact(chiNhap, pn.tinhTongTien());
        }
        long thuRong = doanhThuGop - giamKhuyenMai - giamDiem;
        LinkedHashMap<String, Long> baoCao = new LinkedHashMap<>();
        baoCao.put("Doanh thu gộp", doanhThuGop);
        baoCao.put("Giảm do khuyến mãi", giamKhuyenMai);
        baoCao.put("Giảm do điểm", giamDiem);
        baoCao.put("Thu bán hàng ròng", thuRong);
        baoCao.put("Chi nhập hàng", chiNhap);
        baoCao.put("Chênh lệch thu − chi nhập", Math.subtractExact(thuRong, chiNhap));
        baoCao.put("Số đơn đã thanh toán", (long) donTrongKy.size());
        return baoCao;
    }

    /** Họ tên để in trên đơn/hóa đơn; không trả dữ liệu xác thực. */
    public String hoTen(String maTaiKhoan) {
        TaiKhoan tk = maTaiKhoan == null ? null : quanLyTaiKhoan.timTheoMa(maTaiKhoan);
        return tk == null ? "" : tk.getHoTen();
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
