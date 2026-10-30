package cuahang;

public class QuanLyCuaHang extends TaiKhoan {
    public QuanLyCuaHang(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        super(tenDangNhap, matKhau, hoTen, soDienThoai);
    }

    QuanLyCuaHang(String maTaiKhoan, String tenDangNhap, String salt, int soVongBam, String matKhauBam,
                  String hoTen, String soDienThoai, boolean dangHoatDong) {
        super(maTaiKhoan, tenDangNhap, salt, soVongBam, matKhauBam, hoTen, soDienThoai, dangHoatDong);
    }

    @Override
    public VaiTro layVaiTro() {
        return VaiTro.QUAN_LY_CUA_HANG;
    }

    @Override
    QuanLyCuaHang banSaoVoiTrangThai(boolean dangHoatDongMoi) {
        return new QuanLyCuaHang(getMaTaiKhoan(), getTenDangNhap(), getSalt(), getSoVongBam(), getMatKhauBam(),
                getHoTen(), getSoDienThoai(), dangHoatDongMoi);
    }
}
