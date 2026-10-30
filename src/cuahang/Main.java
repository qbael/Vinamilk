package cuahang;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Path duongDan = Path.of("data", "du-lieu.txt").toAbsolutePath();
        QuanLyTaiKhoan quanLyTaiKhoan = new QuanLyTaiKhoan();
        QuanLySanPham quanLySanPham = new QuanLySanPham();
        QuanLyBienTheSanPham quanLyBienThe = new QuanLyBienTheSanPham(quanLySanPham);
        QuanLyKhuyenMai quanLyKhuyenMai = new QuanLyKhuyenMai(quanLyBienThe);
        QuanLyDonHang quanLyDonHang = new QuanLyDonHang();
        QuanLyPhieuNhap quanLyPhieuNhap = new QuanLyPhieuNhap();
        LuuTruText luuTru = new LuuTruText(duongDan, quanLyTaiKhoan, quanLySanPham, quanLyBienThe,
                quanLyKhuyenMai, quanLyDonHang, quanLyPhieuNhap);
        DichVuCuaHang dichVu = new DichVuCuaHang(quanLyTaiKhoan, quanLySanPham, quanLyBienThe, quanLyKhuyenMai,
                quanLyDonHang, quanLyPhieuNhap, luuTru);

        System.out.println("File dữ liệu: " + duongDan);
        try (Scanner boDoc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            GiaoDienConsole giaoDien = new GiaoDienConsole(dichVu, boDoc);
            if (luuTru.tonTai()) {
                luuTru.docDuLieu();
            } else if (!giaoDien.khoiTaoHeThong(duongDan)) {
                System.out.println("Chưa khởi tạo hệ thống, thoát chương trình.");
                return;
            }
            giaoDien.chay();
        } catch (IOException | RuntimeException e) {
            System.err.println("Không thể tiếp tục: " + e.getMessage());
            System.exit(1);
        } catch (Error e) {
            System.err.println("Lỗi nghiêm trọng: " + e.getMessage());
            System.exit(2);
        }
    }
}
