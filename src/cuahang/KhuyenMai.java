package cuahang;

import java.time.LocalDate;

public abstract class KhuyenMai implements ChinhSachGiamGia {
    private final String maKhuyenMai;
    private String tenKhuyenMai;
    private final String maBienThe;
    private LocalDate tuNgay;
    private LocalDate denNgay;
    private boolean dangApDung;

    protected KhuyenMai(String maKhuyenMai, String tenKhuyenMai, String maBienThe, LocalDate tuNgay,
                        LocalDate denNgay, boolean dangApDung) {
        this.maKhuyenMai = LuuTruText.vanBan(maKhuyenMai, "Mã khuyến mãi");
        this.maBienThe = LuuTruText.vanBan(maBienThe, "Mã SKU áp dụng");
        this.tenKhuyenMai = LuuTruText.vanBan(tenKhuyenMai, "Tên khuyến mãi");
        kiemTraKhoangNgay(tuNgay, denNgay);
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
        this.dangApDung = dangApDung;
    }

    private static void kiemTraKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Phải có ngày bắt đầu và ngày kết thúc.");
        }
        if (tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }

    /** Dùng chung cho lớp con: kiểm tra đầu vào của tinhTienGiam. */
    protected static long tinhTienHang(long donGia, int soLuong) {
        if (donGia <= 0 || soLuong <= 0) {
            throw new IllegalArgumentException("Đơn giá và số lượng phải dương.");
        }
        return Math.multiplyExact(donGia, soLuong);
    }

    public boolean coHieuLuc(LocalDate ngay) {
        return dangApDung && !ngay.isBefore(tuNgay) && !ngay.isAfter(denNgay);
    }

    @Override
    public abstract long tinhTienGiam(long donGia, int soLuong);

    void capNhatThongTin(String tenKhuyenMai, LocalDate tuNgay, LocalDate denNgay) {
        String ten = LuuTruText.vanBan(tenKhuyenMai, "Tên khuyến mãi");
        kiemTraKhoangNgay(tuNgay, denNgay);
        this.tenKhuyenMai = ten;
        this.tuNgay = tuNgay;
        this.denNgay = denNgay;
    }

    void ngungApDung() {
        dangApDung = false;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public String getTenKhuyenMai() {
        return tenKhuyenMai;
    }

    public String getMaBienThe() {
        return maBienThe;
    }

    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public LocalDate getDenNgay() {
        return denNgay;
    }

    public boolean isDangApDung() {
        return dangApDung;
    }
}
