package cuahang.taikhoan;

public class KhachHang extends TaiKhoan {
    private int diemTichLuy;

    public KhachHang(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        super(tenDangNhap, matKhau, hoTen, soDienThoai);
        this.diemTichLuy = 0;
    }

    public KhachHang(String maTaiKhoan, String tenDangNhap, String salt, int soVongBam, String matKhauBam,
              String hoTen, String soDienThoai, boolean dangHoatDong, int diemTichLuy) {
        super(maTaiKhoan, tenDangNhap, salt, soVongBam, matKhauBam, hoTen, soDienThoai, dangHoatDong);
        if (diemTichLuy < 0) {
            throw new IllegalArgumentException("Điểm tích lũy không được âm.");
        }
        this.diemTichLuy = diemTichLuy;
    }

    @Override
    public VaiTro layVaiTro() {
        return VaiTro.KHACH_HANG;
    }

    @Override
    public KhachHang banSaoVoiTrangThai(boolean dangHoatDongMoi) {
        return new KhachHang(getMaTaiKhoan(), getTenDangNhap(), getSalt(), getSoVongBam(), getMatKhauBam(),
                getHoTen(), getSoDienThoai(), dangHoatDongMoi, diemTichLuy);
    }

    @Override
    public boolean cungThongTinCoDinh(TaiKhoan khac) {
        return super.cungThongTinCoDinh(khac) && diemTichLuy == ((KhachHang) khac).diemTichLuy;
    }

    public void congDiem(int soDiem) {
        if (soDiem < 0) {
            throw new IllegalArgumentException("Số điểm cộng không được âm.");
        }
        diemTichLuy = Math.addExact(diemTichLuy, soDiem);
    }

    public void truDiem(int soDiem) {
        if (soDiem < 0) {
            throw new IllegalArgumentException("Số điểm trừ không được âm.");
        }
        if (soDiem > diemTichLuy) {
            throw new IllegalArgumentException("Không đủ điểm: hiện có " + diemTichLuy + ".");
        }
        diemTichLuy -= soDiem;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }
}
