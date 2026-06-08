# Báo cáo phân tích Access Token, Refresh Token và cơ chế Revoke

## 1. Sự khác biệt giữa Access Token và Refresh Token

Hệ thống sử dụng mô hình xác thực JWT kết hợp Access Token và Refresh Token nhằm cân bằng giữa tính bảo mật và trải
nghiệm người dùng.

### Access Token

Access Token là loại token được sử dụng trực tiếp để truy cập các API nghiệp vụ của hệ thống. Sau khi đăng nhập thành
công, Client sẽ đính kèm Access Token vào header Authorization của mỗi request.

Đặc điểm:

* Mục đích: Cấp quyền truy cập tài nguyên.
* Thời gian sống ngắn (ví dụ: 15 phút).
* Chứa các thông tin cần thiết như username và danh sách quyền (roles).
* Được gửi thường xuyên trong mọi request.

Ưu điểm của việc sử dụng Access Token ngắn hạn là giảm thiểu thiệt hại nếu token bị đánh cắp. Kẻ tấn công chỉ có thể sử
dụng token trong khoảng thời gian ngắn trước khi token hết hạn.

### Refresh Token

Refresh Token được sử dụng để xin cấp lại Access Token mới khi Access Token hết hạn mà không yêu cầu người dùng đăng
nhập lại.

Đặc điểm:

* Mục đích: Duy trì phiên làm việc.
* Thời gian sống dài hơn (ví dụ: 7 ngày).
* Không được sử dụng để truy cập trực tiếp các API nghiệp vụ.
* Chỉ được gửi tới endpoint refresh token.

Việc sử dụng Refresh Token giúp người dùng có trải nghiệm liên tục, tránh phải nhập lại tài khoản và mật khẩu nhiều lần
trong ngày.

### So sánh

| Tiêu chí         | Access Token | Refresh Token                |
|------------------|--------------|------------------------------|
| Mục đích         | Truy cập API | Cấp lại Access Token         |
| Thời gian sống   | Ngắn         | Dài                          |
| Tần suất sử dụng | Mọi request  | Chỉ khi Access Token hết hạn |
| Mức độ rủi ro    | Trung bình   | Cao                          |
| Lưu trong DB     | Có thể lưu   | Bắt buộc lưu                 |

### Phương án lưu trữ an toàn phía Client

Đối với ứng dụng Web, Refresh Token nên được lưu trong HttpOnly Cookie để tránh bị truy cập bởi JavaScript và giảm nguy
cơ tấn công XSS.

Access Token có thể được lưu trong bộ nhớ tạm thời của ứng dụng (memory) hoặc lưu trữ ngắn hạn phía Client. Không nên
lưu Refresh Token trong Local Storage vì có nguy cơ bị đánh cắp thông qua các lỗ hổng XSS.

---

## 2. Rủi ro khi bị lộ Token và vai trò của cơ chế Revoke

### Trường hợp lộ Access Token

Nếu Access Token bị đánh cắp, kẻ tấn công có thể sử dụng token đó để truy cập các API của hệ thống với quyền của người
dùng hợp pháp.

Tuy nhiên, do Access Token có thời gian sống ngắn nên phạm vi ảnh hưởng được giới hạn. Sau khi hết hạn, token sẽ không
còn giá trị sử dụng.

### Trường hợp lộ Refresh Token

Đây là tình huống nguy hiểm hơn.

Nếu kẻ tấn công sở hữu một Refresh Token còn hiệu lực, họ có thể liên tục yêu cầu hệ thống cấp Access Token mới mà không
cần biết mật khẩu của người dùng.

Điều này có thể dẫn tới việc chiếm quyền truy cập tài khoản trong thời gian dài.

### Cơ chế phòng vệ bằng cờ Revoked

Để giảm thiểu rủi ro, hệ thống lưu trạng thái của từng token trong bảng Token:

```text
id
token_value
token_type
revoked
expired
employee_id
```

Khi người dùng đăng xuất hoặc quản trị viên muốn vô hiệu hóa quyền truy cập, hệ thống không xóa token khỏi cơ sở dữ liệu
mà cập nhật:

```text
revoked = true
expired = true
```

Trong quá trình xác thực, ngoài việc kiểm tra chữ ký JWT và thời gian hết hạn, hệ thống còn truy vấn bảng Token để kiểm
tra trạng thái revoke.

Nếu token có:

```text
revoked = true
```

hoặc

```text
expired = true
```

thì request sẽ bị từ chối với mã lỗi:

```http
401 Unauthorized
```

Nhờ cơ chế này, ngay cả khi một Refresh Token hoặc Access Token bị đánh cắp, quản trị viên vẫn có thể chủ động vô hiệu
hóa token đó từ phía máy chủ. Điều này giúp hệ thống thực hiện được chức năng thu hồi phiên làm việc tức thời và nâng
cao đáng kể mức độ bảo mật của toàn bộ hệ thống.
