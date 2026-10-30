package cuahang;

public class NhanVienBanHang extends TaiKhoan {
    public NhanVienBanHang(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        super(tenDangNhap, matKhau, hoTen, soDienThoai);
    }

    NhanVienBanHang(String maTaiKhoan, String tenDangNhap, String salt, int soVongBam, String matKhauBam,
                    String hoTen, String soDienThoai, boolean dangHoatDong) {
        super(maTaiKhoan, tenDangNhap, salt, soVongBam, matKhauBam, hoTen, soDienThoai, dangHoatDong);
    }

    @Override
    public VaiTro layVaiTro() {
        return VaiTro.NHAN_VIEN_BAN_HANG;
    }

    @Override
    NhanVienBanHang banSaoVoiTrangThai(boolean dangHoatDongMoi) {
        return new NhanVienBanHang(getMaTaiKhoan(), getTenDangNhap(), getSalt(), getSoVongBam(), getMatKhauBam(),
                getHoTen(), getSoDienThoai(), dangHoatDongMoi);
    }
}
