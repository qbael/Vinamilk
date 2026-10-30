package cuahang;

import java.time.LocalDate;
import java.util.UUID;

public class KhuyenMaiSoTien extends KhuyenMai {
    private long soTienGiamMoiDonVi;

    public KhuyenMaiSoTien(String tenKhuyenMai, String maBienThe, LocalDate tuNgay, LocalDate denNgay,
                           long soTienGiamMoiDonVi) {
        this(UUID.randomUUID().toString(), tenKhuyenMai, maBienThe, tuNgay, denNgay, true, soTienGiamMoiDonVi);
    }

    KhuyenMaiSoTien(String maKhuyenMai, String tenKhuyenMai, String maBienThe, LocalDate tuNgay,
                    LocalDate denNgay, boolean dangApDung, long soTienGiamMoiDonVi) {
        super(maKhuyenMai, tenKhuyenMai, maBienThe, tuNgay, denNgay, dangApDung);
        kiemTraSoTien(soTienGiamMoiDonVi);
        this.soTienGiamMoiDonVi = soTienGiamMoiDonVi;
    }

    private static void kiemTraSoTien(long soTien) {
        if (soTien <= 0) {
            throw new IllegalArgumentException("Số tiền giảm phải lớn hơn 0.");
        }
    }

    /** min(a, g) × q: giảm vượt đơn giá bị chặn ở đơn giá. */
    @Override
    public long tinhTienGiam(long donGia, int soLuong) {
        tinhTienHang(donGia, soLuong);
        return Math.multiplyExact(Math.min(soTienGiamMoiDonVi, donGia), soLuong);
    }

    void doiSoTienGiamMoiDonVi(long soTienMoi) {
        kiemTraSoTien(soTienMoi);
        soTienGiamMoiDonVi = soTienMoi;
    }

    public long getSoTienGiamMoiDonVi() {
        return soTienGiamMoiDonVi;
    }
}
