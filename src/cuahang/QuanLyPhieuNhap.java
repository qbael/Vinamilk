package cuahang;

import java.time.LocalDate;
import java.util.List;

public final class QuanLyPhieuNhap extends DanhSach<PhieuNhap> {
    public PhieuNhap timTheoMa(String maPhieuNhap) {
        return timDauTien(pn -> pn.getMaPhieuNhap().equals(maPhieuNhap));
    }

    /** Phiếu theo ngày nhập, bao gồm hai đầu. */
    public List<PhieuNhap> timTrongKhoang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null || denNgay == null || tuNgay.isAfter(denNgay)) {
            throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        }
        return loc(pn -> !pn.getThoiDiemNhap().toLocalDate().isBefore(tuNgay)
                && !pn.getThoiDiemNhap().toLocalDate().isAfter(denNgay));
    }

    @Override
    public void them(PhieuNhap phieuNhap) {
        khacNull(phieuNhap);
        if (timTheoMa(phieuNhap.getMaPhieuNhap()) != null) {
            throw new IllegalArgumentException("Phiếu nhập này đã được ghi nhận.");
        }
        danhSach.add(phieuNhap);
    }

    @Override
    public void sua(String ma, PhieuNhap duLieuMoi) {
        throw new UnsupportedOperationException("Không được sửa phiếu nhập đã ghi nhận.");
    }

    @Override
    public void xoa(String ma) {
        throw new UnsupportedOperationException("Không được xóa phiếu nhập đã ghi nhận.");
    }

    void napDanhSach(List<PhieuNhap> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, PhieuNhap::getMaPhieuNhap, "mã phiếu nhập");
        thayToanBo(duLieuDaKiemTra);
    }
}
