package cuahang;

import cuahang.danhsach.QuanLyBienTheSanPham;
import cuahang.danhsach.QuanLyDonHang;
import cuahang.danhsach.QuanLyKhuyenMai;
import cuahang.danhsach.QuanLyPhieuNhap;
import cuahang.danhsach.QuanLySanPham;
import cuahang.danhsach.QuanLyTaiKhoan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    /** Lớp chỉ chứa điểm khởi chạy main, không tạo đối tượng. */
    private Main() {
    }

    public static void main(String[] args) {
        Path duongDan = Path.of("data").toAbsolutePath();
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

        System.out.println("Thư mục dữ liệu: " + duongDan);
        try (Scanner boDoc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            GiaoDienConsole giaoDien = new GiaoDienConsole(dichVu, boDoc);
            if (luuTru.tonTai()) {
                luuTru.docDuLieu();
            } else if (!giaoDien.khoiTaoHeThong(duongDan)) {
                System.out.println("Chưa khởi tạo hệ thống, thoát chương trình.");
                return;
            }
            giaoDien.chay();
        } catch (NoSuchElementException e) {
            // hết input trước khi vào menu (vd. Ctrl+D ở câu hỏi khởi tạo): báo như mọi chỗ khác
            System.out.println("Hết dữ liệu nhập, kết thúc chương trình.");
        } catch (IOException | RuntimeException e) {
            System.err.println("Không thể tiếp tục: " + e.getMessage());
            System.exit(1);
        } catch (Error e) {
            System.err.println("Lỗi nghiêm trọng: " + e.getMessage());
            System.exit(2);
        }
    }
}
