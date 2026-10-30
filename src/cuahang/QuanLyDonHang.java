package cuahang;

import java.time.LocalDate;
import java.util.List;

public final class QuanLyDonHang extends DanhSach<DonHang> {
    public DonHang timTheoMa(String maDonHang) {
        return timDauTien(dh -> dh.getMaDonHang().equals(maDonHang));
    }

    public List<DonHang> timTheoKhach(String maKhachHang) {
        return loc(dh -> maKhachHang != null && maKhachHang.equals(dh.getMaKhachHang()));
    }

    public List<DonHang> timTheoTrangThai(TrangThaiDonHang trangThai) {
        return loc(dh -> dh.getTrangThai() == trangThai);
    }

    /** Đơn đã thanh toán theo ngày thanh toán, bao gồm hai đầu. */
    public List<DonHang> timDaThanhToanTrongKhoang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        }
        return loc(dh -> dh.getTrangThai() == TrangThaiDonHang.DA_THANH_TOAN
                && !dh.getThoiDiemThanhToan().toLocalDate().isBefore(tuNgay)
                && !dh.getThoiDiemThanhToan().toLocalDate().isAfter(denNgay));
    }

    @Override
    public void them(DonHang donHang) {
        khacNull(donHang);
        if (timTheoMa(donHang.getMaDonHang()) != null) {
            throw new IllegalArgumentException("Trùng mã đơn hàng.");
        }
        if (donHang.getTrangThai() != TrangThaiDonHang.CHO_THANH_TOAN) {
            throw new IllegalArgumentException("Đơn mới phải ở trạng thái chờ thanh toán.");
        }
        danhSach.add(donHang);
    }

    @Override
    public void sua(String ma, DonHang duLieuMoi) {
        throw new UnsupportedOperationException(
                "Không được sửa trực tiếp đơn hàng; hãy dùng nghiệp vụ thanh toán hoặc hủy qua dịch vụ.");
    }

    @Override
    public void xoa(String ma) {
        throw new UnsupportedOperationException("Không được xóa đơn hàng; hãy hủy đơn chờ qua dịch vụ.");
    }

    /** Bất biến từng đơn đã được constructor DonHang kiểm tra khi dựng lại. */
    void napDanhSach(List<DonHang> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, DonHang::getMaDonHang, "mã đơn hàng");
        thayToanBo(duLieuDaKiemTra);
    }
}
