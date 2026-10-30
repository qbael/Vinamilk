package cuahang;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PhieuNhap {
    private final String maPhieuNhap;
    private final String maNguoiNhap;
    private final String tenNguonNhap;
    private final LocalDateTime thoiDiemNhap;
    private final DanhSachChiTietPhieuNhap chiTiet;

    public PhieuNhap(String maNguoiNhap, String tenNguonNhap, LocalDateTime thoiDiemNhap,
                     DanhSachChiTietPhieuNhap chiTiet) {
        this(UUID.randomUUID().toString(), maNguoiNhap, tenNguonNhap, thoiDiemNhap, chiTiet);
    }

    PhieuNhap(String maPhieuNhap, String maNguoiNhap, String tenNguonNhap, LocalDateTime thoiDiemNhap,
              DanhSachChiTietPhieuNhap chiTiet) {
        this.maPhieuNhap = LuuTruText.vanBan(maPhieuNhap, "Mã phiếu nhập");
        this.maNguoiNhap = LuuTruText.vanBan(maNguoiNhap, "Mã người nhập");
        this.tenNguonNhap = LuuTruText.vanBan(tenNguonNhap, "Tên nguồn nhập");
        if (thoiDiemNhap == null) {
            throw new IllegalArgumentException("Phiếu phải có thời điểm nhập.");
        }
        if (chiTiet == null || chiTiet.isRong()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một dòng.");
        }
        if (chiTiet.isDaChot()) {
            throw new IllegalArgumentException("Danh sách chi tiết đã thuộc chứng từ khác.");
        }
        chiTiet.tinhTongTien();
        this.thoiDiemNhap = thoiDiemNhap;
        chiTiet.khoaChinhSua();
        this.chiTiet = chiTiet;
    }

    public long tinhTongTien() {
        return chiTiet.tinhTongTien();
    }

    public List<ChiTietPhieuNhap> getChiTiet() {
        return chiTiet.xuat();
    }

    public String getMaPhieuNhap() {
        return maPhieuNhap;
    }

    public String getMaNguoiNhap() {
        return maNguoiNhap;
    }

    public String getTenNguonNhap() {
        return tenNguonNhap;
    }

    public LocalDateTime getThoiDiemNhap() {
        return thoiDiemNhap;
    }
}
