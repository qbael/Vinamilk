package cuahang;

public final class DanhSachChiTietPhieuNhap extends DanhSach<ChiTietPhieuNhap> {
    private boolean daChot = false;

    public ChiTietPhieuNhap timTheoBienThe(String maBienThe) {
        return timDauTien(ct -> ct.getMaBienThe().equals(maBienThe));
    }

    /** Thêm dòng; cùng SKU thì cộng lượng nếu cùng giá nhập, khác giá thì từ chối. */
    @Override
    public void them(ChiTietPhieuNhap chiTiet) {
        kiemTraChuaChot();
        khacNull(chiTiet);
        ChiTietPhieuNhap cu = timTheoBienThe(chiTiet.getMaBienThe());
        if (cu == null) {
            danhSach.add(chiTiet);
            return;
        }
        if (cu.getDonGiaNhap() != chiTiet.getDonGiaNhap()) {
            throw new IllegalArgumentException("SKU đã có trên phiếu với giá nhập " + cu.getDonGiaNhap()
                    + "; hãy sửa dòng để thống nhất một đơn giá hoặc lập phiếu khác.");
        }
        ChiTietPhieuNhap gop = new ChiTietPhieuNhap(cu.getMaBienThe(), cu.getTenSanPhamLucNhap(),
                cu.getTenQuyCachLucNhap(), Math.addExact(cu.getSoLuong(), chiTiet.getSoLuong()),
                cu.getDonGiaNhap());
        danhSach.set(danhSach.indexOf(cu), gop);
    }

    /** Thay nguyên dòng cùng SKU bằng lượng/giá mới (không cộng lượng). */
    @Override
    public void sua(String maBienThe, ChiTietPhieuNhap duLieuMoi) {
        kiemTraChuaChot();
        kiemTraMa(maBienThe);
        khacNull(duLieuMoi);
        if (!duLieuMoi.getMaBienThe().equals(maBienThe)) {
            throw new IllegalArgumentException("Dòng mới phải giữ cùng mã SKU.");
        }
        danhSach.set(viTri(maBienThe), duLieuMoi);
    }

    @Override
    public void xoa(String maBienThe) {
        kiemTraChuaChot();
        kiemTraMa(maBienThe);
        danhSach.remove(viTri(maBienThe));
    }

    private int viTri(String maBienThe) {
        ChiTietPhieuNhap ct = timTheoBienThe(maBienThe);
        if (ct == null) {
            throw new IllegalArgumentException("Không có dòng SKU " + maBienThe + " trên phiếu.");
        }
        return danhSach.indexOf(ct);
    }

    private void kiemTraChuaChot() {
        if (daChot) {
            throw new IllegalStateException("Phiếu nhập đã ghi nhận, không được thay đổi chi tiết.");
        }
    }

    void khoaChinhSua() {
        daChot = true;
    }

    public boolean isDaChot() {
        return daChot;
    }

    public boolean isRong() {
        return danhSach.isEmpty();
    }

    public long tinhTongTien() {
        long tong = 0;
        for (ChiTietPhieuNhap ct : danhSach) {
            tong = Math.addExact(tong, ct.tinhThanhTien());
        }
        return tong;
    }
}
