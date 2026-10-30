package cuahang;

public enum KieuDongGoi {
    BAN_LE("Bán lẻ"),
    LOC("Lốc"),
    THUNG("Thùng");

    private final String tenHienThi;

    KieuDongGoi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    /** BAN_LE đúng 1 đơn vị cơ sở; LOC/THUNG nhiều hơn 1. */
    public boolean hopLe(int soDonViCoSo) {
        return this == BAN_LE ? soDonViCoSo == 1 : soDonViCoSo > 1;
    }
}
