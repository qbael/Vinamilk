package cuahang;

public class LuuTruText {
    // Phần đọc/ghi file được viết ở giai đoạn 3.

    /** Trường văn bản bắt buộc: không trống, không chứa ký tự phân cách của định dạng text. */
    static String vanBan(String giaTri, String tenTruong) {
        String s = vanBanTuyChon(giaTri, tenTruong);
        if (s == null) {
            throw new IllegalArgumentException(tenTruong + " không được để trống.");
        }
        return s;
    }

    /** Trường văn bản tùy chọn: trống trả null. */
    static String vanBanTuyChon(String giaTri, String tenTruong) {
        if (giaTri == null || giaTri.isBlank()) {
            return null;
        }
        String s = giaTri.strip();
        if (s.indexOf('|') >= 0 || s.indexOf('\r') >= 0 || s.indexOf('\n') >= 0) {
            throw new IllegalArgumentException(tenTruong + " không được chứa ký tự | hoặc xuống dòng.");
        }
        return s;
    }
}
