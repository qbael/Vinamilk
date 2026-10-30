package cuahang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DanhSach<T> implements IQuanLy<T> {
    protected final List<T> danhSach = new ArrayList<>();

    @Override
    public final List<T> xuat() {
        return List.copyOf(danhSach);
    }

    @Override
    public abstract void them(T doiTuong);

    @Override
    public abstract void sua(String ma, T duLieuMoi);

    @Override
    public abstract void xoa(String ma);

    /** Phần tử đầu tiên thỏa điều kiện, hoặc null. */
    protected final T timDauTien(Predicate<? super T> dieuKien) {
        for (T phanTu : danhSach) {
            if (dieuKien.test(phanTu)) {
                return phanTu;
            }
        }
        return null;
    }

    /** Bản sao chỉ đọc các phần tử thỏa điều kiện. */
    protected final List<T> loc(Predicate<? super T> dieuKien) {
        return danhSach.stream().filter(dieuKien).toList();
    }

    /** Dùng cho nạp/phục hồi: lô đã được lớp con kiểm tra xong. */
    protected final void thayToanBo(List<T> duLieuDaKiemTra) {
        danhSach.clear();
        danhSach.addAll(duLieuDaKiemTra);
    }

    /** Kiểm tra lô nạp: không có phần tử null, không trùng khóa. */
    protected static <E> void kiemTraKhongTrung(List<E> lo, Function<E, Object> layKhoa, String tenKhoa) {
        Set<Object> daCo = new HashSet<>();
        for (E phanTu : lo) {
            if (!daCo.add(layKhoa.apply(khacNull(phanTu)))) {
                throw new IllegalArgumentException("Trùng " + tenKhoa + ": " + layKhoa.apply(phanTu));
            }
        }
    }

    protected static String kiemTraMa(String ma) {
        if (ma == null || ma.isBlank()) {
            throw new IllegalArgumentException("Mã không được để trống.");
        }
        return ma;
    }

    protected static <E> E khacNull(E doiTuong) {
        if (doiTuong == null) {
            throw new IllegalArgumentException("Dữ liệu không được null.");
        }
        return doiTuong;
    }
}
