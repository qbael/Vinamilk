package cuahang.danhsach;

import cuahang.sanpham.LoaiSanPham;
import cuahang.sanpham.SanPham;

import java.util.List;
import java.util.Locale;

public final class QuanLySanPham extends DanhSach<SanPham> {
    /** Danh sách rỗng; Main tạo một lần rồi dùng chung cho dịch vụ và lưu trữ. */
    public QuanLySanPham() {
        super();
    }

    public SanPham timTheoMa(String maSanPham) {
        return timDauTien(sp -> sp.getMaSanPham().equals(maSanPham));
    }

    public List<SanPham> timTheoTen(String tuKhoa) {
        String tu = tuKhoa == null ? "" : tuKhoa.strip().toLowerCase(Locale.ROOT);
        return loc(sp -> sp.getTenSanPham().toLowerCase(Locale.ROOT).contains(tu));
    }

    public List<SanPham> timTheoLoai(LoaiSanPham loai) {
        return loc(sp -> sp.getLoai() == loai);
    }

    @Override
    public void them(SanPham sanPham) {
        khacNull(sanPham);
        if (timTheoMa(sanPham.getMaSanPham()) != null) {
            throw new IllegalArgumentException("Trùng mã sản phẩm.");
        }
        danhSach.add(sanPham);
    }

    /** Chỉ đổi tên, thương hiệu, mô tả và trạng thái; đơn vị, định lượng, nhóm giữ nguyên. */
    @Override
    public void sua(String ma, SanPham duLieuMoi) {
        kiemTraMa(ma);
        khacNull(duLieuMoi);
        SanPham cu = timTheoMa(ma);
        if (cu == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm " + ma + ".");
        }
        if (!cu.cungHangHoa(duLieuMoi)) {
            throw new IllegalArgumentException(
                    "Không đổi mã, đơn vị, định lượng hay nhóm hàng; hãy tạo sản phẩm mới.");
        }
        cu.capNhatThongTin(duLieuMoi.getTenSanPham(), duLieuMoi.getThuongHieu(), duLieuMoi.getMoTa());
        if (duLieuMoi.isDangKinhDoanh()) {
            cu.moKinhDoanh();
        } else {
            cu.ngungKinhDoanh();
        }
    }

    /**
     * Xóa hẳn sản phẩm. Lớp này không thấy danh sách SKU nên DichVuCuaHang.xoaSanPham phải kiểm tra
     * sản phẩm không còn SKU trước; lưu file cũng từ chối nếu còn SKU trỏ tới.
     */
    @Override
    public void xoa(String ma) {
        kiemTraMa(ma);
        SanPham sp = timTheoMa(ma);
        if (sp == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm " + ma + ".");
        }
        danhSach.remove(sp);
    }

    public void napDanhSach(List<SanPham> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, SanPham::getMaSanPham, "mã sản phẩm");
        thayToanBo(duLieuDaKiemTra);
    }
}
