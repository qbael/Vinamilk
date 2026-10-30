package cuahang;

public enum LoaiSanPham {
    SUA("Sữa"),
    SUA_CHUA("Sữa chua"),
    KEM("Kem"),
    NUOC_GIAI_KHAT("Nước giải khát"),
    PHO_MAI("Phô mai");

    private final String tenHienThi;

    LoaiSanPham(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}
