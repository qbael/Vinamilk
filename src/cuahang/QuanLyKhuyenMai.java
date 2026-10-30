package cuahang;

import java.time.LocalDate;
import java.util.List;

public final class QuanLyKhuyenMai extends DanhSach<KhuyenMai> {
    private final QuanLyBienTheSanPham quanLyBienTheSanPham;

    public QuanLyKhuyenMai(QuanLyBienTheSanPham quanLyBienTheSanPham) {
        this.quanLyBienTheSanPham = khacNull(quanLyBienTheSanPham);
    }

    public KhuyenMai timTheoMa(String maKhuyenMai) {
        return timDauTien(km -> km.getMaKhuyenMai().equals(maKhuyenMai));
    }

    public List<KhuyenMai> timTheoBienThe(String maBienThe) {
        return loc(km -> km.getMaBienThe().equals(maBienThe));
    }

    /**
     * Chương trình hiệu lực giảm nhiều nhất cho cả dòng; bằng nhau chọn mã nhỏ nhất.
     * Gọi tinhTienGiam đa hình, không phân nhánh theo lớp con. Không có thì trả null.
     */
    public KhuyenMai timTotNhat(String maBienThe, LocalDate ngay, long donGia, int soLuong) {
        KhuyenMai totNhat = null;
        long giamNhat = -1;
        for (KhuyenMai km : timTheoBienThe(maBienThe)) {
            if (!km.coHieuLuc(ngay)) {
                continue;
            }
            long giam = km.tinhTienGiam(donGia, soLuong);
            if (giam > giamNhat || (giam == giamNhat && km.getMaKhuyenMai().compareTo(totNhat.getMaKhuyenMai()) < 0)) {
                totNhat = km;
                giamNhat = giam;
            }
        }
        return totNhat;
    }

    @Override
    public void them(KhuyenMai khuyenMai) {
        khacNull(khuyenMai);
        if (timTheoMa(khuyenMai.getMaKhuyenMai()) != null) {
            throw new IllegalArgumentException("Trùng mã khuyến mãi.");
        }
        if (quanLyBienTheSanPham.timTheoMa(khuyenMai.getMaBienThe()) == null) {
            throw new IllegalArgumentException("SKU áp dụng không tồn tại.");
        }
        danhSach.add(khuyenMai);
    }

    /** Đổi tên, thời hạn, mức giảm hoặc ngừng; không đổi SKU/loại giảm và không mở lại chương trình đã ngừng. */
    @Override
    public void sua(String ma, KhuyenMai duLieuMoi) {
        kiemTraMa(ma);
        khacNull(duLieuMoi);
        KhuyenMai cu = timTheoMa(ma);
        if (cu == null) {
            throw new IllegalArgumentException("Không tìm thấy khuyến mãi " + ma + ".");
        }
        if (cu.getClass() != duLieuMoi.getClass() || !cu.getMaKhuyenMai().equals(duLieuMoi.getMaKhuyenMai())
                || !cu.getMaBienThe().equals(duLieuMoi.getMaBienThe())) {
            throw new IllegalArgumentException("Không đổi mã, SKU hay loại giảm; hãy tạo chương trình mới.");
        }
        if (!cu.isDangApDung() && duLieuMoi.isDangApDung()) {
            throw new IllegalStateException("Chương trình đã ngừng không được mở lại; hãy tạo chương trình mới.");
        }
        cu.capNhatThongTin(duLieuMoi.getTenKhuyenMai(), duLieuMoi.getTuNgay(), duLieuMoi.getDenNgay());
        switch (cu) {
            case KhuyenMaiPhanTram pt -> pt.doiTyLePhanTram(((KhuyenMaiPhanTram) duLieuMoi).getTyLePhanTram());
            case KhuyenMaiSoTien st -> st.doiSoTienGiamMoiDonVi(((KhuyenMaiSoTien) duLieuMoi).getSoTienGiamMoiDonVi());
            default -> throw new IllegalArgumentException("Loại khuyến mãi không hỗ trợ.");
        }
        if (!duLieuMoi.isDangApDung()) {
            cu.ngungApDung();
        }
    }

    @Override
    public void xoa(String ma) {
        throw new UnsupportedOperationException("Không được xóa khuyến mãi; hãy ngừng áp dụng qua dịch vụ.");
    }

    void napDanhSach(List<KhuyenMai> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, KhuyenMai::getMaKhuyenMai, "mã khuyến mãi");
        thayToanBo(duLieuDaKiemTra);
    }
}
