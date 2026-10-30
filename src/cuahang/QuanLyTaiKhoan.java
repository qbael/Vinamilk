package cuahang;

import java.util.List;

public final class QuanLyTaiKhoan extends DanhSach<TaiKhoan> {
    public TaiKhoan timTheoMa(String maTaiKhoan) {
        return timDauTien(tk -> tk.getMaTaiKhoan().equals(maTaiKhoan));
    }

    public TaiKhoan timTheoTenDangNhap(String tenDangNhap) {
        String ten;
        try {
            ten = TaiKhoan.chuanHoaTenDangNhap(tenDangNhap);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return timDauTien(tk -> tk.getTenDangNhap().equals(ten));
    }

    /** Số điện thoại không duy nhất nên trả danh sách. */
    public List<KhachHang> timKhachTheoSoDienThoai(String soDienThoai) {
        String so = soDienThoai == null ? "" : soDienThoai.strip();
        return danhSach.stream()
                .filter(tk -> tk instanceof KhachHang && tk.getSoDienThoai().equals(so))
                .map(tk -> (KhachHang) tk)
                .toList();
    }

    public int demQuanLyDangHoatDong() {
        return loc(tk -> tk instanceof QuanLyCuaHang && tk.isDangHoatDong()).size();
    }

    @Override
    public void them(TaiKhoan taiKhoan) {
        khacNull(taiKhoan);
        if (timTheoMa(taiKhoan.getMaTaiKhoan()) != null) {
            throw new IllegalArgumentException("Trùng mã tài khoản.");
        }
        if (timTheoTenDangNhap(taiKhoan.getTenDangNhap()) != null) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.");
        }
        if (taiKhoan instanceof KhachHang kh && kh.getDiemTichLuy() != 0) {
            throw new IllegalArgumentException("Khách hàng mới phải có điểm bằng 0.");
        }
        danhSach.add(taiKhoan);
    }

    /** Chỉ đổi trạng thái hoạt động; mọi trường khác (kể cả điểm) phải giữ nguyên. */
    @Override
    public void sua(String ma, TaiKhoan duLieuMoi) {
        kiemTraMa(ma);
        khacNull(duLieuMoi);
        TaiKhoan cu = timTheoMa(ma);
        if (cu == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản " + ma + ".");
        }
        if (!cu.cungThongTinCoDinh(duLieuMoi)) {
            throw new IllegalArgumentException("Chỉ được đổi trạng thái hoạt động của tài khoản.");
        }
        if (duLieuMoi.isDangHoatDong()) {
            cu.moKhoa();
        } else {
            cu.khoa();
        }
    }

    @Override
    public void xoa(String ma) {
        throw new UnsupportedOperationException("Không được xóa tài khoản; hãy khóa tài khoản qua dịch vụ.");
    }

    void napDanhSach(List<TaiKhoan> duLieuDaKiemTra) {
        kiemTraKhongTrung(duLieuDaKiemTra, TaiKhoan::getMaTaiKhoan, "mã tài khoản");
        kiemTraKhongTrung(duLieuDaKiemTra, TaiKhoan::getTenDangNhap, "tên đăng nhập");
        thayToanBo(duLieuDaKiemTra);
    }
}
