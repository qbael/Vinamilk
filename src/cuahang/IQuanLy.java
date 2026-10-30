package cuahang;

import java.util.List;

public interface IQuanLy<T> {
    List<T> xuat();

    void them(T doiTuong);

    void sua(String ma, T duLieuMoi);

    void xoa(String ma);
}
