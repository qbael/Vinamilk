package cuahang;

import java.io.IOException;
import java.nio.file.Path;

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

        System.out.println("File dữ liệu: " + duongDan);
        try {
            if (!luuTru.tonTai()) {
                // Khởi tạo hệ thống mới được nối vào ở giai đoạn dịch vụ/giao diện.
                System.out.println("Chưa có file dữ liệu. Chức năng khởi tạo sẽ có ở giai đoạn sau.");
                return;
            }
            luuTru.docDuLieu();
            System.out.printf("Đã nạp: %d tài khoản, %d sản phẩm, %d SKU, %d khuyến mãi, %d đơn hàng, %d phiếu nhập.%n",
                    quanLyTaiKhoan.xuat().size(), quanLySanPham.xuat().size(), quanLyBienThe.xuat().size(),
                    quanLyKhuyenMai.xuat().size(), quanLyDonHang.xuat().size(), quanLyPhieuNhap.xuat().size());
        } catch (IOException | RuntimeException e) {
            System.err.println("Không thể khởi động: " + e.getMessage());
            System.exit(1);
        }
    }
}
