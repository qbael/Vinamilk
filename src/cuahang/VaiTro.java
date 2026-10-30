package cuahang;

public enum VaiTro {
    KHACH_HANG("Khách hàng"),
    NHAN_VIEN_BAN_HANG("Nhân viên bán hàng"),
    QUAN_LY_CUA_HANG("Quản lý cửa hàng");

    private final String tenHienThi;

    VaiTro(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}
