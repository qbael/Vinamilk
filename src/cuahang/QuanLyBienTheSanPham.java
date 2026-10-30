package cuahang;

import java.util.List;

public final class QuanLyBienTheSanPham extends DanhSach<BienTheSanPham> {
    private final QuanLySanPham quanLySanPham;

    public QuanLyBienTheSanPham(QuanLySanPham quanLySanPham) {
        this.quanLySanPham = khacNull(quanLySanPham);
    }

    public BienTheSanPham timTheoMa(String maBienThe) {
        return timDauTien(bt -> bt.getMaBienThe().equals(maBienThe));
    }

    public List<BienTheSanPham> timTheoSanPham(String maSanPham) {
        return loc(bt -> bt.getMaSanPham().equals(maSanPham));
    }

    /** SKU bán được khi cả SKU và sản phẩm cha đang kinh doanh (chưa xét tồn). */
    public boolean dangBan(BienTheSanPham bienThe) {
        SanPham cha = quanLySanPham.timTheoMa(bienThe.getMaSanPham());
        return bienThe.isDangKinhDoanh() && cha != null && cha.isDangKinhDoanh();
    }

    public List<BienTheSanPham> layHangDangBan() {
        return loc(this::dangBan);
    }

    @Override
    public void them(BienTheSanPham bienThe) {
        khacNull(bienThe);
        if (quanLySanPham.timTheoMa(bienThe.getMaSanPham()) == null) {
            throw new IllegalArgumentException("Sản phẩm cha không tồn tại.");
        }
        if (timTheoMa(bienThe.getMaBienThe()) != null) {
            throw new IllegalArgumentException("Trùng mã SKU.");
        }
        if (timDauTien(bienThe::trungQuyCach) != null) {
            throw new IllegalArgumentException("Sản phẩm đã có quy cách " + bienThe.getKieuDongGoi().getTenHienThi()
                    + " " + bienThe.getSoDonViCoSo() + " đơn vị.");
        }
        if (bienThe.getSoLuongTon() != 0) {
            throw new IllegalArgumentException("SKU mới phải có tồn bằng 0; tăng tồn bằng phiếu nhập.");
        }
        danhSach.add(bienThe);
    }

    /** Chỉ đổi giá và trạng thái; quy cách và tồn giữ nguyên. */
    @Override
    public void sua(String ma, BienTheSanPham duLieuMoi) {
        kiemTraMa(ma);
        khacNull(duLieuMoi);
        BienTheSanPham cu = timTheoMa(ma);
        if (cu == null) {
            throw new IllegalArgumentException("Không tìm thấy SKU " + ma + ".");
        }
        if (!cu.cungQuyCach(duLieuMoi)) {
            throw new IllegalArgumentException(
                    "Không đổi mã, sản phẩm, quy cách hay tồn của SKU; quy cách mới hãy tạo SKU mới.");
        }
        if (duLieuMoi.isDangKinhDoanh() && !cu.isDangKinhDoanh()
                && !quanLySanPham.timTheoMa(cu.getMaSanPham()).isDangKinhDoanh()) {
            throw new IllegalStateException("Sản phẩm cha đang ngừng kinh doanh; hãy mở bán sản phẩm trước.");
        }
        cu.doiGia(duLieuMoi.getGiaBan());
        if (duLieuMoi.isDangKinhDoanh()) {
            cu.moKinhDoanh();
        } else {
            cu.ngungKinhDoanh();
        }
    }

    @Override
    public void xoa(String ma) {
        throw new UnsupportedOperationException("Không được xóa SKU; hãy ngừng kinh doanh qua dịch vụ.");
    }

    /** Liên kết sản phẩm cha và tồn đã được LuuTruText đối chiếu trước khi gọi. */
    void napDanhSach(List<BienTheSanPham> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, BienTheSanPham::getMaBienThe, "mã SKU");
        kiemTraKhongTrung(duLieuDaKiemTra,
                bt -> List.of(bt.getMaSanPham(), bt.getKieuDongGoi(), bt.getSoDonViCoSo()), "quy cách SKU");
        thayToanBo(duLieuDaKiemTra);
    }
}
