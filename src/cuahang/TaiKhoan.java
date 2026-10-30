package cuahang;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public abstract class TaiKhoan {
    static final int SO_VONG_BAM = 600_000;
    private static final int SO_VONG_TOI_DA = 10_000_000;
    private static final int DO_DAI_SALT = 16;
    private static final int DO_DAI_BAM = 32;
    private static final int DO_DAI_MAT_KHAU_TOI_THIEU = 6;
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();

    private final String maTaiKhoan;
    private final String tenDangNhap;
    private final String hoTen;
    private final String soDienThoai;
    private final String salt;
    private final String matKhauBam;
    private final int soVongBam;
    private boolean dangHoatDong;

    /** Tạo tài khoản mới: sinh mã, salt và băm mật khẩu. */
    protected TaiKhoan(String tenDangNhap, char[] matKhau, String hoTen, String soDienThoai) {
        if (matKhau == null || matKhau.length < DO_DAI_MAT_KHAU_TOI_THIEU) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự.");
        }
        this.maTaiKhoan = UUID.randomUUID().toString();
        this.tenDangNhap = chuanHoaTenDangNhap(tenDangNhap);
        this.hoTen = LuuTruText.vanBan(hoTen, "Họ tên");
        this.soDienThoai = kiemTraSoDienThoai(soDienThoai);
        byte[] saltMoi = new byte[DO_DAI_SALT];
        NGAU_NHIEN.nextBytes(saltMoi);
        this.salt = Base64.getEncoder().encodeToString(saltMoi);
        this.soVongBam = SO_VONG_BAM;
        this.matKhauBam = Base64.getEncoder().encodeToString(bam(matKhau, saltMoi, SO_VONG_BAM));
        this.dangHoatDong = true;
    }

    /** Dựng lại tài khoản đã lưu; không băm lại hash. */
    protected TaiKhoan(String maTaiKhoan, String tenDangNhap, String salt, int soVongBam, String matKhauBam,
                       String hoTen, String soDienThoai, boolean dangHoatDong) {
        this.maTaiKhoan = LuuTruText.vanBan(maTaiKhoan, "Mã tài khoản");
        this.tenDangNhap = chuanHoaTenDangNhap(tenDangNhap);
        if (!this.tenDangNhap.equals(tenDangNhap)) {
            throw new IllegalArgumentException("Tên đăng nhập đã lưu chưa được chuẩn hóa: " + tenDangNhap);
        }
        if (soVongBam <= 0 || soVongBam > SO_VONG_TOI_DA) {
            throw new IllegalArgumentException("Số vòng băm không hợp lệ: " + soVongBam);
        }
        if (giaiMa(salt, "Salt").length < DO_DAI_SALT || giaiMa(matKhauBam, "Mật khẩu băm").length != DO_DAI_BAM) {
            throw new IllegalArgumentException("Kích thước salt hoặc mật khẩu băm không hợp lệ.");
        }
        this.salt = salt;
        this.soVongBam = soVongBam;
        this.matKhauBam = matKhauBam;
        this.hoTen = LuuTruText.vanBan(hoTen, "Họ tên");
        this.soDienThoai = kiemTraSoDienThoai(soDienThoai);
        this.dangHoatDong = dangHoatDong;
    }

    public abstract VaiTro layVaiTro();

    /** Bản sao cùng dữ liệu, chỉ khác trạng thái hoạt động; dùng làm duLieuMoi cho QuanLyTaiKhoan.sua. */
    abstract TaiKhoan banSaoVoiTrangThai(boolean dangHoatDongMoi);

    public static String chuanHoaTenDangNhap(String tenDangNhap) {
        String ten = LuuTruText.vanBan(tenDangNhap, "Tên đăng nhập").toLowerCase(Locale.ROOT);
        if (ten.chars().anyMatch(Character::isWhitespace)) {
            throw new IllegalArgumentException("Tên đăng nhập không được chứa khoảng trắng.");
        }
        return ten;
    }

    private static String kiemTraSoDienThoai(String soDienThoai) {
        String so = LuuTruText.vanBan(soDienThoai, "Số điện thoại");
        if (!so.matches("0\\d{9,10}")) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10–11 chữ số.");
        }
        return so;
    }

    private static byte[] giaiMa(String base64, String tenTruong) {
        try {
            return Base64.getDecoder().decode(LuuTruText.vanBan(base64, tenTruong));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(tenTruong + " không phải Base64 hợp lệ.");
        }
    }

    private static byte[] bam(char[] matKhau, byte[] salt, int soVong) {
        PBEKeySpec spec = new PBEKeySpec(matKhau, salt, soVong, DO_DAI_BAM * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JDK không hỗ trợ PBKDF2WithHmacSHA256.", e);
        } finally {
            spec.clearPassword();
        }
    }

    public boolean kiemTraMatKhau(char[] matKhau) {
        if (matKhau == null || matKhau.length == 0) {
            return false;
        }
        byte[] ketQua = bam(matKhau, Base64.getDecoder().decode(salt), soVongBam);
        return MessageDigest.isEqual(ketQua, Base64.getDecoder().decode(matKhauBam));
    }

    void khoa() {
        dangHoatDong = false;
    }

    void moKhoa() {
        dangHoatDong = true;
    }

    /** So sánh mọi trường trừ trạng thái hoạt động (và điểm ở lớp con). */
    boolean cungThongTinCoDinh(TaiKhoan khac) {
        return getClass() == khac.getClass()
                && maTaiKhoan.equals(khac.maTaiKhoan)
                && tenDangNhap.equals(khac.tenDangNhap)
                && hoTen.equals(khac.hoTen)
                && soDienThoai.equals(khac.soDienThoai)
                && salt.equals(khac.salt)
                && matKhauBam.equals(khac.matKhauBam)
                && soVongBam == khac.soVongBam;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    String getSalt() {
        return salt;
    }

    String getMatKhauBam() {
        return matKhauBam;
    }

    int getSoVongBam() {
        return soVongBam;
    }

    public boolean isDangHoatDong() {
        return dangHoatDong;
    }
}
