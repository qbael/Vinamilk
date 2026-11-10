package cuahang;

import java.util.UUID;

public class BienTheSanPham {
    private final String maBienThe;
    private final String maSanPham;
    private final String tenQuyCach;
    private final KieuDongGoi kieuDongGoi;
    private final int soDonViCoSo;
    private int soLuongTon;
    private int soLuongDaGiu;
    private long giaBan;
    private boolean dangKinhDoanh;

    /** SKU mới: tồn 0, đang kinh doanh. */
    public BienTheSanPham(String maSanPham, String tenQuyCach, KieuDongGoi kieuDongGoi, int soDonViCoSo,
                          long giaBan) {
        this(UUID.randomUUID().toString(), maSanPham, tenQuyCach, kieuDongGoi, soDonViCoSo, giaBan, 0, true);
    }

    /** Dựng lại SKU đã có mã: nạp file hoặc làm duLieuMoi cho QuanLyBienTheSanPham.sua. */
    BienTheSanPham(String maBienThe, String maSanPham, String tenQuyCach, KieuDongGoi kieuDongGoi,
                   int soDonViCoSo, long giaBan, int soLuongTon, boolean dangKinhDoanh) {
        this.maBienThe = LuuTruText.vanBan(maBienThe, "Mã SKU");
        this.maSanPham = LuuTruText.vanBan(maSanPham, "Mã sản phẩm");
        this.tenQuyCach = LuuTruText.vanBan(tenQuyCach, "Tên quy cách");
        if (kieuDongGoi == null) {
            throw new IllegalArgumentException("Phải chọn kiểu đóng gói.");
        }
        if (!kieuDongGoi.hopLe(soDonViCoSo)) {
            throw new IllegalArgumentException(kieuDongGoi == KieuDongGoi.BAN_LE
                    ? "Bán lẻ phải có đúng 1 đơn vị cơ sở."
                    : "Lốc/thùng phải có hơn 1 đơn vị cơ sở.");
        }
        kiemTraGia(giaBan);
        if (soLuongTon < 0) {
            throw new IllegalArgumentException("Tồn kho không được âm.");
        }
        this.kieuDongGoi = kieuDongGoi;
        this.soDonViCoSo = soDonViCoSo;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.dangKinhDoanh = dangKinhDoanh;
    }

    private static void kiemTraGia(long gia) {
        if (gia <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0.");
        }
    }

    void doiGia(long giaMoi) {
        kiemTraGia(giaMoi);
        giaBan = giaMoi;
    }

    void tangTon(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Lượng tăng tồn phải dương.");
        }
        soLuongTon = Math.addExact(soLuongTon, soLuong);
    }

    void giamTon(int soLuong) {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Lượng giảm tồn phải dương.");
        }
        if (soLuong > getSoLuongTon()) {
            throw new IllegalArgumentException("Không đủ tồn cho " + tenQuyCach + ": còn " + getSoLuongTon() + ".");
        }
        soLuongTon -= soLuong;
    }

    void giuTon(int soLuong) {
        if (soLuong <= 0 || soLuong > getSoLuongTon()) {
            throw new IllegalArgumentException("Không đủ tồn để giữ hàng " + tenQuyCach + ": còn " + getSoLuongTon() + ".");
        }
        soLuongDaGiu += soLuong;
    }

    void hoanTon(int soLuong) {
        if (soLuong <= 0 || soLuong > soLuongDaGiu) {
            throw new IllegalArgumentException("Số lượng hoàn phải dương và không vượt số hàng đang giữ.");
        }
        soLuongDaGiu -= soLuong;
    }

    void xuatHangDaGiu(int soLuong) {
        hoanTon(soLuong);
        soLuongTon -= soLuong;
    }

    void ngungKinhDoanh() {
        dangKinhDoanh = false;
    }

    void moKinhDoanh() {
        dangKinhDoanh = true;
    }

    /** Các trường xác định quy cách, không được đổi qua sửa (kể cả tồn). */
    boolean cungQuyCach(BienTheSanPham khac) {
        return maBienThe.equals(khac.maBienThe)
                && maSanPham.equals(khac.maSanPham)
                && tenQuyCach.equals(khac.tenQuyCach)
                && kieuDongGoi == khac.kieuDongGoi
                && soDonViCoSo == khac.soDonViCoSo
                && soLuongTon == khac.soLuongTon;
    }

    /** Khóa tự nhiên (maSanPham, kieuDongGoi, soDonViCoSo). */
    boolean trungQuyCach(BienTheSanPham khac) {
        return maSanPham.equals(khac.maSanPham)
                && kieuDongGoi == khac.kieuDongGoi
                && soDonViCoSo == khac.soDonViCoSo;
    }

    public String getMaBienThe() {
        return maBienThe;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public String getTenQuyCach() {
        return tenQuyCach;
    }

    public KieuDongGoi getKieuDongGoi() {
        return kieuDongGoi;
    }

    public int getSoDonViCoSo() {
        return soDonViCoSo;
    }

    /** Tồn khả dụng, đã trừ lượng giữ cho các đơn chờ thanh toán. */
    public int getSoLuongTon() {
        return soLuongTon - soLuongDaGiu;
    }

    /** Tồn thực tế dùng để lưu file và đối chiếu nhập − bán. */
    int getSoLuongTonThucTe() {
        return soLuongTon;
    }

    public long getGiaBan() {
        return giaBan;
    }

    public boolean isDangKinhDoanh() {
        return dangKinhDoanh;
    }
}
