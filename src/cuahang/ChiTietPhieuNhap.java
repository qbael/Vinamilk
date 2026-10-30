package cuahang;

public class ChiTietPhieuNhap {
    private final String maBienThe;
    private final String tenSanPhamLucNhap;
    private final String tenQuyCachLucNhap;
    private final int soLuong;
    private final long donGiaNhap;

    public ChiTietPhieuNhap(String maBienThe, String tenSanPhamLucNhap, String tenQuyCachLucNhap, int soLuong,
                            long donGiaNhap) {
        this.maBienThe = LuuTruText.vanBan(maBienThe, "Mã SKU");
        this.tenSanPhamLucNhap = LuuTruText.vanBan(tenSanPhamLucNhap, "Tên sản phẩm lúc nhập");
        this.tenQuyCachLucNhap = LuuTruText.vanBan(tenQuyCachLucNhap, "Tên quy cách lúc nhập");
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải là số nguyên dương.");
        }
        if (donGiaNhap <= 0) {
            throw new IllegalArgumentException("Giá nhập phải lớn hơn 0.");
        }
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
        Math.multiplyExact(donGiaNhap, soLuong); // từ chối thành tiền tràn số ngay khi tạo dòng
    }

    public long tinhThanhTien() {
        return Math.multiplyExact(donGiaNhap, soLuong);
    }

    public String getMaBienThe() {
        return maBienThe;
    }

    public String getTenSanPhamLucNhap() {
        return tenSanPhamLucNhap;
    }

    public String getTenQuyCachLucNhap() {
        return tenQuyCachLucNhap;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public long getDonGiaNhap() {
        return donGiaNhap;
    }
}
