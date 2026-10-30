package cuahang;

public class ChiTietDonHang {
    private final String maBienThe;
    private final String tenSanPhamLucBan;
    private final String tenQuyCachLucBan;
    private final int soLuong;
    private final long donGia;
    private final String maKhuyenMai;
    private final long tienGiamKhuyenMai;

    public ChiTietDonHang(String maBienThe, String tenSanPhamLucBan, String tenQuyCachLucBan, int soLuong,
                          long donGia, String maKhuyenMai, long tienGiamKhuyenMai) {
        this.maBienThe = LuuTruText.vanBan(maBienThe, "Mã SKU");
        this.tenSanPhamLucBan = LuuTruText.vanBan(tenSanPhamLucBan, "Tên sản phẩm lúc bán");
        this.tenQuyCachLucBan = LuuTruText.vanBan(tenQuyCachLucBan, "Tên quy cách lúc bán");
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng phải là số nguyên dương.");
        }
        if (donGia <= 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0.");
        }
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.maKhuyenMai = LuuTruText.vanBanTuyChon(maKhuyenMai, "Mã khuyến mãi");
        if (this.maKhuyenMai == null && tienGiamKhuyenMai != 0) {
            throw new IllegalArgumentException("Không có khuyến mãi thì tiền giảm phải bằng 0.");
        }
        if (tienGiamKhuyenMai < 0 || tienGiamKhuyenMai > Math.multiplyExact(donGia, soLuong)) {
            throw new IllegalArgumentException("Tiền giảm phải nằm trong [0, tiền hàng của dòng].");
        }
        this.tienGiamKhuyenMai = tienGiamKhuyenMai;
    }

    public long tinhTienHang() {
        return Math.multiplyExact(donGia, soLuong);
    }

    public long tinhTienSauKhuyenMai() {
        return tinhTienHang() - tienGiamKhuyenMai;
    }

    public String getMaBienThe() {
        return maBienThe;
    }

    public String getTenSanPhamLucBan() {
        return tenSanPhamLucBan;
    }

    public String getTenQuyCachLucBan() {
        return tenQuyCachLucBan;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public long getDonGia() {
        return donGia;
    }

    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public long getTienGiamKhuyenMai() {
        return tienGiamKhuyenMai;
    }
}
