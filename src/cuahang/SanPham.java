package cuahang;

import java.util.Objects;
import java.util.UUID;

public class SanPham {
    private final String maSanPham;
    private String tenSanPham;
    private String thuongHieu;
    private final String donViCoSo;
    private final String dinhLuong;
    private String moTa;
    private final LoaiSanPham loai;
    private boolean dangKinhDoanh;

    public SanPham(String tenSanPham, String thuongHieu, String donViCoSo, String dinhLuong, String moTa,
                   LoaiSanPham loai) {
        this(UUID.randomUUID().toString(), tenSanPham, thuongHieu, donViCoSo, dinhLuong, moTa, loai, true);
    }

    /** Dựng lại sản phẩm đã có mã: nạp file hoặc làm duLieuMoi cho QuanLySanPham.sua. */
    SanPham(String maSanPham, String tenSanPham, String thuongHieu, String donViCoSo, String dinhLuong,
            String moTa, LoaiSanPham loai, boolean dangKinhDoanh) {
        this.maSanPham = LuuTruText.vanBan(maSanPham, "Mã sản phẩm");
        this.tenSanPham = LuuTruText.vanBan(tenSanPham, "Tên sản phẩm");
        this.thuongHieu = LuuTruText.vanBan(thuongHieu, "Thương hiệu");
        this.donViCoSo = LuuTruText.vanBan(donViCoSo, "Đơn vị cơ sở");
        this.dinhLuong = LuuTruText.vanBan(dinhLuong, "Định lượng");
        this.moTa = LuuTruText.vanBanTuyChon(moTa, "Mô tả");
        if (loai == null) {
            throw new IllegalArgumentException("Phải chọn nhóm hàng.");
        }
        this.loai = loai;
        this.dangKinhDoanh = dangKinhDoanh;
    }

    void capNhatThongTin(String tenSanPham, String thuongHieu, String moTa) {
        String ten = LuuTruText.vanBan(tenSanPham, "Tên sản phẩm");
        String hieu = LuuTruText.vanBan(thuongHieu, "Thương hiệu");
        String mo = LuuTruText.vanBanTuyChon(moTa, "Mô tả");
        this.tenSanPham = ten;
        this.thuongHieu = hieu;
        this.moTa = mo;
    }

    void ngungKinhDoanh() {
        dangKinhDoanh = false;
    }

    void moKinhDoanh() {
        dangKinhDoanh = true;
    }

    /** Các trường xác định hàng hóa, không được đổi qua sửa. */
    boolean cungHangHoa(SanPham khac) {
        return maSanPham.equals(khac.maSanPham)
                && donViCoSo.equals(khac.donViCoSo)
                && dinhLuong.equals(khac.dinhLuong)
                && loai == khac.loai;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public String getThuongHieu() {
        return thuongHieu;
    }

    public String getDonViCoSo() {
        return donViCoSo;
    }

    public String getDinhLuong() {
        return dinhLuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public LoaiSanPham getLoai() {
        return loai;
    }

    public boolean isDangKinhDoanh() {
        return dangKinhDoanh;
    }

    @Override
    public String toString() {
        return tenSanPham + " (" + Objects.requireNonNullElse(thuongHieu, "") + ")";
    }
}
