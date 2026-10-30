package cuahang;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DonHang {
    static final long TIEN_MOI_DIEM = 100;
    static final long TIEN_TOI_THIEU_MOI_DIEM_DUNG = 500;
    static final long TIEN_DE_DUOC_MOT_DIEM = 10_000;

    private final String maDonHang;
    private final String maKhachHang;
    private final String maNguoiLap;
    private String maNguoiThuTien;
    private final LocalDateTime thoiDiemTao;
    private LocalDateTime thoiDiemThanhToan;
    private TrangThaiDonHang trangThai;
    private int diemSuDung;
    private int diemCong;
    private long tongTienHang;
    private long tongGiamKhuyenMai;
    private long tienGiamDiem;
    private long tongThanhToan;
    private DanhSachChiTietDonHang chiTiet;

    /** Đơn mới chờ thanh toán; tổng là tạm tính từ các dòng, chưa dùng điểm. */
    public DonHang(String maKhachHang, String maNguoiLap, DanhSachChiTietDonHang chiTiet,
                   LocalDateTime thoiDiemTao) {
        this(UUID.randomUUID().toString(), maKhachHang, maNguoiLap, null, thoiDiemTao, null,
                TrangThaiDonHang.CHO_THANH_TOAN, 0, 0, tongTienHangCua(chiTiet), tongGiamCua(chiTiet), 0,
                tongTienHangCua(chiTiet) - tongGiamCua(chiTiet), chiTiet);
    }

    /** Dựng lại đơn đã lưu; kiểm tra toàn bộ bất biến rồi khóa chi tiết. */
    DonHang(String maDonHang, String maKhachHang, String maNguoiLap, String maNguoiThuTien,
            LocalDateTime thoiDiemTao, LocalDateTime thoiDiemThanhToan, TrangThaiDonHang trangThai,
            int diemSuDung, int diemCong, long tongTienHang, long tongGiamKhuyenMai, long tienGiamDiem,
            long tongThanhToan, DanhSachChiTietDonHang chiTiet) {
        this.maDonHang = LuuTruText.vanBan(maDonHang, "Mã đơn hàng");
        this.maKhachHang = LuuTruText.vanBanTuyChon(maKhachHang, "Mã khách hàng");
        this.maNguoiLap = LuuTruText.vanBan(maNguoiLap, "Mã người lập");
        this.maNguoiThuTien = LuuTruText.vanBanTuyChon(maNguoiThuTien, "Mã người thu tiền");
        if (thoiDiemTao == null || trangThai == null) {
            throw new IllegalArgumentException("Đơn phải có thời điểm tạo và trạng thái.");
        }
        kiemTraChiTietMoi(chiTiet);
        if (tongTienHang != chiTiet.tinhTongTienHang() || tongGiamKhuyenMai != chiTiet.tinhTongGiam()) {
            throw new IllegalArgumentException("Tổng tiền hàng/giảm khuyến mãi không khớp các dòng.");
        }
        long sauKhuyenMai = tongTienHang - tongGiamKhuyenMai;
        if (trangThai == TrangThaiDonHang.DA_THANH_TOAN) {
            if (this.maNguoiThuTien == null || thoiDiemThanhToan == null) {
                throw new IllegalArgumentException("Đơn đã thanh toán phải có người và thời điểm thu tiền.");
            }
            if (thoiDiemThanhToan.isBefore(thoiDiemTao)) {
                throw new IllegalArgumentException("Thời điểm thanh toán không được trước thời điểm tạo.");
            }
            kiemTraDiemSuDung(diemSuDung, sauKhuyenMai);
            if (tienGiamDiem != Math.multiplyExact(diemSuDung, TIEN_MOI_DIEM)
                    || tongThanhToan != sauKhuyenMai - tienGiamDiem
                    || diemCong != tinhDiemCong(tongThanhToan)) {
                throw new IllegalArgumentException("Tiền giảm điểm, tổng thanh toán hoặc điểm cộng không khớp.");
            }
        } else if (this.maNguoiThuTien != null || thoiDiemThanhToan != null || diemSuDung != 0
                || diemCong != 0 || tienGiamDiem != 0 || tongThanhToan != sauKhuyenMai) {
            throw new IllegalArgumentException("Đơn chờ/hủy không có người thu, điểm hoặc tiền giảm điểm.");
        }
        this.thoiDiemTao = thoiDiemTao;
        this.thoiDiemThanhToan = thoiDiemThanhToan;
        this.trangThai = trangThai;
        this.diemSuDung = diemSuDung;
        this.diemCong = diemCong;
        this.tongTienHang = tongTienHang;
        this.tongGiamKhuyenMai = tongGiamKhuyenMai;
        this.tienGiamDiem = tienGiamDiem;
        this.tongThanhToan = tongThanhToan;
        chiTiet.khoaChinhSua();
        this.chiTiet = chiTiet;
    }

    private static long tongTienHangCua(DanhSachChiTietDonHang chiTiet) {
        return chiTiet == null ? 0 : chiTiet.tinhTongTienHang();
    }

    private static long tongGiamCua(DanhSachChiTietDonHang chiTiet) {
        return chiTiet == null ? 0 : chiTiet.tinhTongGiam();
    }

    private static void kiemTraChiTietMoi(DanhSachChiTietDonHang chiTiet) {
        if (chiTiet == null || chiTiet.isRong()) {
            throw new IllegalArgumentException("Đơn phải có ít nhất một dòng hàng.");
        }
        if (chiTiet.isDaChot()) {
            throw new IllegalArgumentException("Danh sách chi tiết đã thuộc chứng từ khác.");
        }
    }

    /** Số điểm tối đa theo tiền: tiền giảm điểm không vượt 20% sau khuyến mãi. */
    static int diemToiDaTheoTien(long sauKhuyenMai) {
        return (int) Math.min(sauKhuyenMai / TIEN_TOI_THIEU_MOI_DIEM_DUNG, Integer.MAX_VALUE);
    }

    private void kiemTraDiemSuDung(int diem, long sauKhuyenMai) {
        if (diem < 0) {
            throw new IllegalArgumentException("Số điểm dùng không được âm.");
        }
        if (maKhachHang == null && diem > 0) {
            throw new IllegalArgumentException("Khách vãng lai không được dùng điểm.");
        }
        int toiDa = diemToiDaTheoTien(sauKhuyenMai);
        if (diem > toiDa) {
            throw new IllegalArgumentException("Vượt mức đổi điểm: tối đa " + toiDa + " điểm cho đơn này.");
        }
    }

    private int tinhDiemCong(long soTienThucTra) {
        return maKhachHang == null ? 0 : Math.toIntExact(soTienThucTra / TIEN_DE_DUOC_MOT_DIEM);
    }

    /**
     * Chốt thanh toán bằng các dòng đã tính lại giá/khuyến mãi hiện hành.
     * Dòng mới phải giữ đúng bộ SKU và số lượng đã đặt. Không trừ tồn/điểm, không ghi file.
     */
    void chotThanhToan(DanhSachChiTietDonHang chiTietMoi, String maNguoiThuTien, LocalDateTime thoiDiem,
                       int diemSuDung) {
        kiemTraDangCho();
        kiemTraChiTietMoi(chiTietMoi);
        if (!soLuongTheoSku(chiTietMoi.xuat()).equals(soLuongTheoSku(chiTiet.xuat()))) {
            throw new IllegalArgumentException("Dòng thanh toán phải giữ đúng SKU và số lượng đã đặt.");
        }
        String nguoiThu = LuuTruText.vanBan(maNguoiThuTien, "Mã người thu tiền");
        if (thoiDiem == null || thoiDiem.isBefore(thoiDiemTao)) {
            throw new IllegalArgumentException("Thời điểm thanh toán không hợp lệ.");
        }
        long tien = chiTietMoi.tinhTongTienHang();
        long giam = chiTietMoi.tinhTongGiam();
        long sau = tien - giam;
        kiemTraDiemSuDung(diemSuDung, sau);
        long giamDiem = Math.multiplyExact(diemSuDung, TIEN_MOI_DIEM);
        long thanhToan = sau - giamDiem;

        this.maNguoiThuTien = nguoiThu;
        this.thoiDiemThanhToan = thoiDiem;
        this.tongTienHang = tien;
        this.tongGiamKhuyenMai = giam;
        this.diemSuDung = diemSuDung;
        this.tienGiamDiem = giamDiem;
        this.tongThanhToan = thanhToan;
        this.diemCong = tinhDiemCong(thanhToan);
        chiTietMoi.khoaChinhSua();
        this.chiTiet = chiTietMoi;
        this.trangThai = TrangThaiDonHang.DA_THANH_TOAN;
    }

    private static Map<String, Integer> soLuongTheoSku(List<ChiTietDonHang> dong) {
        Map<String, Integer> ketQua = new HashMap<>();
        for (ChiTietDonHang ct : dong) {
            ketQua.put(ct.getMaBienThe(), ct.getSoLuong());
        }
        return ketQua;
    }

    void huy() {
        kiemTraDangCho();
        trangThai = TrangThaiDonHang.DA_HUY;
    }

    private void kiemTraDangCho() {
        if (trangThai == TrangThaiDonHang.DA_THANH_TOAN) {
            throw new IllegalStateException("Đơn đã thanh toán.");
        }
        if (trangThai == TrangThaiDonHang.DA_HUY) {
            throw new IllegalStateException("Đơn đã hủy.");
        }
    }

    /** Bản sao tách rời để xem trước bảng tính thanh toán mà không đụng vào đơn thật. */
    DonHang banSao() {
        DanhSachChiTietDonHang dong = new DanhSachChiTietDonHang();
        chiTiet.xuat().forEach(dong::them);
        return new DonHang(maDonHang, maKhachHang, maNguoiLap, maNguoiThuTien, thoiDiemTao, thoiDiemThanhToan,
                trangThai, diemSuDung, diemCong, tongTienHang, tongGiamKhuyenMai, tienGiamDiem, tongThanhToan,
                dong);
    }

    public List<ChiTietDonHang> getChiTiet() {
        return chiTiet.xuat();
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public String getMaNguoiLap() {
        return maNguoiLap;
    }

    public String getMaNguoiThuTien() {
        return maNguoiThuTien;
    }

    public LocalDateTime getThoiDiemTao() {
        return thoiDiemTao;
    }

    public LocalDateTime getThoiDiemThanhToan() {
        return thoiDiemThanhToan;
    }

    public TrangThaiDonHang getTrangThai() {
        return trangThai;
    }

    public int getDiemSuDung() {
        return diemSuDung;
    }

    public int getDiemCong() {
        return diemCong;
    }

    public long getTongTienHang() {
        return tongTienHang;
    }

    public long getTongGiamKhuyenMai() {
        return tongGiamKhuyenMai;
    }

    public long getTienSauKhuyenMai() {
        return tongTienHang - tongGiamKhuyenMai;
    }

    public long getTienGiamDiem() {
        return tienGiamDiem;
    }

    public long getTongThanhToan() {
        return tongThanhToan;
    }
}
