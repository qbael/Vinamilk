package cuahang;

import java.time.LocalDate;
import java.util.UUID;

public class KhuyenMaiPhanTram extends KhuyenMai {
    private int tyLePhanTram;

    public KhuyenMaiPhanTram(String tenKhuyenMai, String maBienThe, LocalDate tuNgay, LocalDate denNgay,
                             int tyLePhanTram) {
        this(UUID.randomUUID().toString(), tenKhuyenMai, maBienThe, tuNgay, denNgay, true, tyLePhanTram);
    }

    KhuyenMaiPhanTram(String maKhuyenMai, String tenKhuyenMai, String maBienThe, LocalDate tuNgay,
                      LocalDate denNgay, boolean dangApDung, int tyLePhanTram) {
        super(maKhuyenMai, tenKhuyenMai, maBienThe, tuNgay, denNgay, dangApDung);
        kiemTraTyLe(tyLePhanTram);
        this.tyLePhanTram = tyLePhanTram;
    }

    private static void kiemTraTyLe(int tyLe) {
        if (tyLe < 1 || tyLe > 100) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải từ 1 đến 100%.");
        }
    }

    /** floor(T × p / 100) tính trên cả dòng; tách T = 100a + b để phép nhân trung gian không tràn. */
    @Override
    public long tinhTienGiam(long donGia, int soLuong) {
        long tienHang = tinhTienHang(donGia, soLuong);
        return tienHang / 100 * tyLePhanTram + tienHang % 100 * tyLePhanTram / 100;
    }

    void doiTyLePhanTram(int tyLeMoi) {
        kiemTraTyLe(tyLeMoi);
        tyLePhanTram = tyLeMoi;
    }

    public int getTyLePhanTram() {
        return tyLePhanTram;
    }
}
