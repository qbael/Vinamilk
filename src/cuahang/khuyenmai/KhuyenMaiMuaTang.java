package cuahang.khuyenmai;

import java.time.LocalDate;
import java.util.UUID;

public class KhuyenMaiMuaTang extends KhuyenMai {
    private int soLuongMua;
    private int soLuongTang;

    public KhuyenMaiMuaTang(String tenKhuyenMai, String maBienThe, LocalDate tuNgay, LocalDate denNgay,
                            int soLuongMua, int soLuongTang) {
        this(UUID.randomUUID().toString(), tenKhuyenMai, maBienThe, tuNgay, denNgay, true, soLuongMua, soLuongTang);
    }

    public KhuyenMaiMuaTang(String maKhuyenMai, String tenKhuyenMai, String maBienThe, LocalDate tuNgay,
                            LocalDate denNgay, boolean dangApDung, int soLuongMua, int soLuongTang) {
        super(maKhuyenMai, tenKhuyenMai, maBienThe, tuNgay, denNgay, dangApDung);
        doiSoLuongMuaTang(soLuongMua, soLuongTang);
    }

    /** Số lượng gồm cả hàng tặng; chỉ tặng khi đủ một nhóm mua + tặng cùng SKU. */
    @Override
    public long tinhTienGiam(long donGia, int soLuong) {
        tinhTienHang(donGia, soLuong);
        long soLuongMienPhi = (long) (soLuong / (soLuongMua + soLuongTang)) * soLuongTang;
        return Math.multiplyExact(donGia, soLuongMienPhi);
    }

    public void doiSoLuongMuaTang(int soLuongMua, int soLuongTang) {
        if (soLuongMua <= 0 || soLuongTang <= 0) {
            throw new IllegalArgumentException("Số lượng mua và số lượng tặng phải là số nguyên dương.");
        }
        Math.addExact(soLuongMua, soLuongTang);
        this.soLuongMua = soLuongMua;
        this.soLuongTang = soLuongTang;
    }

    public int getSoLuongMua() {
        return soLuongMua;
    }

    public int getSoLuongTang() {
        return soLuongTang;
    }
}
