package cuahang;

public final class DanhSachChiTietDonHang extends DanhSach<ChiTietDonHang> {
    private boolean daChot = false;

    public ChiTietDonHang timTheoBienThe(String maBienThe) {
        return timDauTien(ct -> ct.getMaBienThe().equals(maBienThe));
    }

    /**
     * Thêm dòng; nếu SKU đã có thì gộp lượng thành một dòng theo giá mới nhất.
     * Dòng gộp bỏ khuyến mãi cũ: dịch vụ tính lại chương trình tốt nhất cho cả dòng
     * khi báo giá, không cộng hai khoản giảm đã làm tròn.
     */
    @Override
    public void them(ChiTietDonHang chiTiet) {
        kiemTraChuaChot();
        khacNull(chiTiet);
        ChiTietDonHang cu = timTheoBienThe(chiTiet.getMaBienThe());
        if (cu == null) {
            danhSach.add(chiTiet);
            return;
        }
        ChiTietDonHang gop = new ChiTietDonHang(chiTiet.getMaBienThe(), chiTiet.getTenSanPhamLucBan(),
                chiTiet.getTenQuyCachLucBan(), Math.addExact(cu.getSoLuong(), chiTiet.getSoLuong()),
                chiTiet.getDonGia(), null, 0);
        danhSach.set(danhSach.indexOf(cu), gop);
    }

    /** Thay nguyên dòng cùng SKU (không cộng lượng). */
    @Override
    public void sua(String maBienThe, ChiTietDonHang duLieuMoi) {
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
        ChiTietDonHang ct = timTheoBienThe(maBienThe);
        if (ct == null) {
            throw new IllegalArgumentException("Không có dòng SKU " + maBienThe + " trong đơn.");
        }
        return danhSach.indexOf(ct);
    }

    private void kiemTraChuaChot() {
        if (daChot) {
            throw new IllegalStateException("Chi tiết đơn đã chốt, không được thay đổi.");
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

    public long tinhTongTienHang() {
        long tong = 0;
        for (ChiTietDonHang ct : danhSach) {
            tong = Math.addExact(tong, ct.tinhTienHang());
        }
        return tong;
    }

    public long tinhTongGiam() {
        long tong = 0;
        for (ChiTietDonHang ct : danhSach) {
            tong = Math.addExact(tong, ct.getTienGiamKhuyenMai());
        }
        return tong;
    }
}
