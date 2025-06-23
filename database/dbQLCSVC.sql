-- Tạo database
CREATE DATABASE QLCSVC;
USE QLCSVC;

-- Bảng roles
CREATE TABLE roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng functions
CREATE TABLE functions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Bảng permission_types
CREATE TABLE permission_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Bảng permissions
CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT,
    function_id INT,
    permission_type_id INT,
    allowed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (function_id) REFERENCES functions(id),
    FOREIGN KEY (permission_type_id) REFERENCES permission_types(id),
    UNIQUE(role_id, function_id, permission_type_id)
);

-- Bảng users
CREATE TABLE users (
    user_id VARCHAR(10) PRIMARY KEY,
    fullname VARCHAR(100),
    username VARCHAR(50),
    yearold DATE,
    email VARCHAR(100),
    phoneNumber VARCHAR(15),
    password VARCHAR(255),
    status VARCHAR(20),
    deleted TINYINT(1) DEFAULT 0,
    thumbnail VARCHAR(500),
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Bảng room_types
CREATE TABLE room_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL
);

-- Bảng room
CREATE TABLE room (
    room_id VARCHAR(10) PRIMARY KEY,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') NOT NULL,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    seating_capacity INT NOT NULL,
    deleted TINYINT DEFAULT 0,
    room_type_id INT,
    location VARCHAR(100),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- Bảng device_types
CREATE TABLE device_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(500)
);

-- Bảng devices
CREATE TABLE devices (
    id VARCHAR(255) PRIMARY KEY,
    device_name VARCHAR(255) NOT NULL,
    device_type_id INT NOT NULL,
    purchase_date DATE,
    supplier VARCHAR(255),
    price DECIMAL(15, 2),
    status ENUM('AVAILABLE', 'UNAVAILABLE', 'UNDER_MAINTENANCE', 'BROKEN', 'DISCARDED') DEFAULT 'AVAILABLE',
    room_id VARCHAR(255),
    quantity INT DEFAULT 1,
    available_quantity INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    thumbnail VARCHAR(500),
    FOREIGN KEY (room_id) REFERENCES room(room_id),
    FOREIGN KEY (device_type_id) REFERENCES device_types(id)
);

-- Bảng borrow_room
CREATE TABLE borrow_room (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_id VARCHAR(255) NOT NULL,
    borrower_id VARCHAR(255) NOT NULL,
    borrow_date DATE NOT NULL,
    start_period INT NOT NULL,
    end_period INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    borrow_reason TEXT,
    reject_reason TEXT,
    FOREIGN KEY (borrower_id) REFERENCES users(user_id),
    FOREIGN KEY (room_id) REFERENCES room(room_id)
);

-- Bảng borrow_device
CREATE TABLE borrow_device (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    borrow_date DATE,
    start_period INT,
    end_period INT,
    borrow_status VARCHAR(20),
    created_at DATETIME,
    borrow_room_id INT NULL,
    note TEXT,
    FOREIGN KEY (borrow_room_id) REFERENCES borrow_room(id)
);

-- Bảng borrow_device_detail
CREATE TABLE borrow_device_detail (
    id INT AUTO_INCREMENT PRIMARY KEY,
    borrow_device_id INT,
    device_id VARCHAR(255),
    quantity INT,
    FOREIGN KEY (borrow_device_id) REFERENCES borrow_device(id)
);

-- Bảng usage_logs
CREATE TABLE usage_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    used_at DATETIME NOT NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id)
);

-- Bảng device_borrow_requests
CREATE TABLE device_borrow_requests (
    id_request VARCHAR(10) PRIMARY KEY,
    lecturer_user VARCHAR(10) NOT NULL,
    device_id VARCHAR(10) NOT NULL,
    request_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    borrowing_request ENUM('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED') NOT NULL,
    FOREIGN KEY (lecturer_user) REFERENCES users(user_id),
    FOREIGN KEY (device_id) REFERENCES devices(id),
    CHECK (due_date > request_date)
);

-- Bảng borrowing_room_request
CREATE TABLE borrowing_room_request (
    id_request VARCHAR(10) PRIMARY KEY,
    lecturer_user VARCHAR(10) NOT NULL,
    room_id VARCHAR(10) NOT NULL,
    request_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    borrowing_request ENUM('PENDING', 'APPROVED', 'RETURNED', 'REJECTED') NOT NULL,
    FOREIGN KEY (lecturer_user) REFERENCES users(user_id),
    FOREIGN KEY (room_id) REFERENCES room(room_id),
    CHECK (due_date > request_date)
);

-- Bảng incident (Cập nhật với cột note)
CREATE TABLE incident (
    id_report VARCHAR(10) PRIMARY KEY,
    reported_by VARCHAR(10) NOT NULL,
    device_id VARCHAR(10),
    room_id VARCHAR(10),
    description TEXT NOT NULL,
    report_date DATETIME NOT NULL,
    handled_by VARCHAR(10),
    status ENUM('SENT', 'RESOLVED') NOT NULL,
    note VARCHAR(255), -- Thêm cột ghi chú
    FOREIGN KEY (reported_by) REFERENCES users(user_id),
    FOREIGN KEY (device_id) REFERENCES devices(id),
    FOREIGN KEY (room_id) REFERENCES room(room_id),
    FOREIGN KEY (handled_by) REFERENCES users(user_id),
    CHECK ((device_id IS NOT NULL OR room_id IS NOT NULL))
);

-- Trigger tự động tạo ID
-- Trigger cho bảng room
DELIMITER //
CREATE TRIGGER before_insert_room
BEFORE INSERT ON room
FOR EACH ROW
BEGIN
    DECLARE new_id INT;
    SET new_id = (SELECT COALESCE(MAX(CAST(SUBSTRING(room_id, 2) AS UNSIGNED)), 0) + 1 FROM room WHERE room_id LIKE 'R%');
    SET NEW.room_id = CONCAT('R', LPAD(new_id, 3, '0'));
END //
DELIMITER ;

-- Trigger cho bảng devices
DELIMITER //
CREATE TRIGGER before_insert_devices
BEFORE INSERT ON devices
FOR EACH ROW
BEGIN
    DECLARE max_id INT;
    DECLARE new_id VARCHAR(10);
    SELECT IFNULL(MAX(CAST(SUBSTRING(id, 2) AS UNSIGNED)), 0)
    INTO max_id
    FROM devices;
    SET new_id = CONCAT('D', LPAD(max_id + 1, 3, '0'));
    SET NEW.id = new_id;
END //
DELIMITER ;

-- Trigger cho bảng borrowing_room_request
DELIMITER //
CREATE TRIGGER before_insert_borrowing_room_request
BEFORE INSERT ON borrowing_room_request
FOR EACH ROW
BEGIN
    DECLARE new_id INT;
    SET new_id = (SELECT COALESCE(MAX(CAST(SUBSTRING(id_request, 3) AS UNSIGNED)), 0) + 1 FROM borrowing_room_request WHERE id_request LIKE 'BR%');
    SET NEW.id_request = CONCAT('BR', LPAD(new_id, 3, '0'));
END //
DELIMITER ;

-- Trigger cho bảng device_borrow_requests
DELIMITER //
CREATE TRIGGER before_insert_device_borrow_requests
BEFORE INSERT ON device_borrow_requests
FOR EACH ROW
BEGIN
    DECLARE new_id INT;
    SET new_id = (SELECT COALESCE(MAX(CAST(SUBSTRING(id_request, 3) AS UNSIGNED)), 0) + 1 FROM device_borrow_requests WHERE id_request LIKE 'BD%');
    SET NEW.id_request = CONCAT('BD', LPAD(new_id, 3, '0'));
END //
DELIMITER ;

-- Trigger cho bảng incident
DELIMITER //
CREATE TRIGGER before_insert_incident
BEFORE INSERT ON incident
FOR EACH ROW
BEGIN
    DECLARE new_id INT;
    SET new_id = (SELECT COALESCE(MAX(CAST(SUBSTRING(id_report, 4) AS UNSIGNED)), 0) + 1 FROM incident WHERE id_report LIKE 'INC%');
    SET NEW.id_report = CONCAT('INC', LPAD(new_id, 3, '0'));
END //
DELIMITER ;

-- Trigger cho bảng users
DELIMITER //
CREATE TRIGGER before_insert_users
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    DECLARE max_id INT;
    DECLARE new_id VARCHAR(10);
    SELECT IFNULL(MAX(CAST(SUBSTRING(user_id, 4) AS UNSIGNED)), 0)
    INTO max_id
    FROM users;
    SET new_id = CONCAT('MTL', LPAD(max_id + 1, 4, '0'));
    IF NEW.user_id IS NULL OR NEW.user_id = '' THEN
        SET NEW.user_id = new_id;
    END IF;
END //
DELIMITER ;

-- Trigger cập nhật trạng thái phòng khi đơn mượn được duyệt hoặc từ chối
DELIMITER //
CREATE TRIGGER after_update_borrowing_room_request
AFTER UPDATE ON borrowing_room_request
FOR EACH ROW
BEGIN
    IF NEW.borrowing_request = 'APPROVED' THEN
        UPDATE room
        SET status = 'OCCUPIED'
        WHERE room_id = NEW.room_id;
    ELSEIF NEW.borrowing_request IN ('REJECTED', 'RETURNED') THEN
        UPDATE room
        SET status = 'AVAILABLE'
        WHERE room_id = NEW.room_id;
    END IF;
END //
DELIMITER ;

-- Trigger cập nhật trạng thái thiết bị khi đơn mượn được duyệt hoặc từ chối
DELIMITER //
CREATE TRIGGER after_update_device_borrow_requests
AFTER UPDATE ON device_borrow_requests
FOR EACH ROW
BEGIN
    IF NEW.borrowing_request = 'APPROVED' THEN
        UPDATE devices
        SET status = 'UNAVAILABLE'
        WHERE id = NEW.device_id;
    ELSEIF NEW.borrowing_request IN ('REJECTED', 'COMPLETED', 'CANCELLED') THEN
        UPDATE devices
        SET status = 'AVAILABLE'
        WHERE id = NEW.device_id;
    END IF;
END //
DELIMITER ;

-- 1. Insert roles
INSERT INTO roles (role_id, role_name) VALUES
(3, 'Quản trị viên'),
(5, 'Giáo viên'),
(6, 'Bảo trì');

-- 2. Insert users
INSERT INTO users (user_id, fullname, username, yearold, email, phoneNumber, password, status, deleted, thumbnail, role_id)
VALUES
('MTL0001', 'Nguyễn Anh Nguyên', 'nguyen34', '2000-12-09', 'nanh@gmail.com', '0983772722', 'c985809daeefd685a992c96fd7f64c0ab6c50e9ad97f89859e6b46a8e562c99c', 'ACTIVE', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1745922241/bfmj6ald6mw73zrddcn5.jpg', 3),
('MTL0002', 'Vo Thien Linh', 'vothienlinh', '1999-05-15', 'vothienlinh2@gmail.com', '0912345678', 'c985809daeefd685a992c96fd7f64c0ab6c50e9ad97f89859e6b46a8e562c99c', 'ACTIVE', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1745922241/bfmj6ald6mw73zrddcn5.jpg', 5),
('MTL0003', 'Nguyen Van A', 'nguyenvana', '1998-03-20', 'vana@example.com', '0912345678', 'c985809daeefd685a992c96fd7f64c0ab6c50e9ad97f89859e6b46a8e562c99c', 'ACTIVE', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1745922241/bfmj6ald6mw73zrddcn5.jpg', 6);

-- 3. Insert room_types
INSERT INTO room_types (id, type_name) VALUES
(1, 'Phòng học lý thuyết'),
(2, 'Phòng thí nghiệm'),
(3, 'Phòng máy tính'),
(4, 'Phòng họp'),
(5, 'Phòng đa năng'),
(6, 'Kho thiết bị');

-- 4. Insert rooms
INSERT INTO room (room_id, status, room_number, seating_capacity, deleted, room_type_id, location) VALUES
('R001', 'AVAILABLE', 'A101', 51, 0, 1, 'Khu A1'),
('R002', 'OCCUPIED', 'A102', 50, 0, 2, 'Khu A1'),
('R003', 'AVAILABLE', 'A104', 50, 0, 3, 'Khu A1'),
('R004', 'AVAILABLE', 'A201', 45, 0, 4, 'Khu A2'),
('R005', 'AVAILABLE', 'A202', 30, 0, 5, 'Khu A2'),
('R006', 'AVAILABLE', 'A203', 60, 0, 6, 'Khu A2'),
('R007', 'AVAILABLE', 'A204', 25, 0, 1, 'Khu A2'),
('R008', 'AVAILABLE', 'A205', 40, 0, 2, 'Khu A2'),
('R009', 'AVAILABLE', 'A206', 35, 0, 3, 'Khu A2'),
('R010', 'AVAILABLE', 'A207', 20, 0, 4, 'Khu A2'),
('R011', 'AVAILABLE', 'A208', 5, 0, 5, 'Khu A2'),
('R012', 'AVAILABLE', 'A209', 22, 0, 6, 'Khu A2'),
('R013', 'AVAILABLE', 'A210', 55, 0, 1, 'Khu A2'),
('R014', 'AVAILABLE', 'A211', 15, 0, 2, 'Khu A2'),
('R015', 'AVAILABLE', 'A212', 18, 0, 3, 'Khu A2'),
('R016', 'AVAILABLE', 'A213', 28, 0, 4, 'Khu A2'),
('R017', 'AVAILABLE', 'A214', 8, 0, 5, 'Khu A2');

INSERT INTO device_types (id, type_name) VALUES
(1, 'Thiết bị trình chiếu và âm thanh'),
(2, 'Máy tính'),
(3, 'Bộ phát wifi'),
(4, 'Ổ cắm điện'),
(5, 'Quạt trần'),
(6, 'Bóng đèn');

-- 5. Insert devices
INSERT INTO devices (
    id, device_name, device_type_id, purchase_date, supplier, price, status,
    room_id, quantity, available_quantity, created_at, updated_at, deleted, thumbnail
) VALUES
('D001', 'micro', 1, '2006-09-12', 'anhh', 120000.00, 'UNDER_MAINTENANCE', 'R001', 1, 1, '2025-05-01 00:00:00', '2025-05-30 10:01:48', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747476219/tjwrnnfzgxengjil9ezx.jpg'),
('D002', 'máy chiếu', 1, '2008-09-13', 'addi', 12000000.00, 'AVAILABLE', 'R002', 1, 1, '2025-05-01 00:00:00', '2025-05-02 18:40:09', 1, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1746185908/parmi9gbcnpmape8k2bg.jpg'),
('D003', 'Máy chiếu', 1, '2005-09-12', 'ad', 120000000.00, 'AVAILABLE', 'R003', 1, 1, '2025-05-02 00:00:00', '2025-05-29 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747476240/nuajtmk7d7cwfv0bfaib.jpg'),
('D004', 'Loa', 1, '2000-09-12', 'dsd', 120000000.00, 'UNAVAILABLE', 'R002', 2, 2, '2025-05-02 00:00:00', '2025-05-02 18:55:39', 1, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1746186928/unje6kfrnntmrpawzvhx.jpg'),
('D005', 'Loa', 1, '2010-04-28', 'adidas', 14000000.00, 'AVAILABLE', 'R001', 3, 3, '2025-05-16 00:00:00', '2025-05-18 21:00:29', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747476309/ve1at9h62ce6y5g4kzrq.jpg'),
('D006', 'micro', 1, '2014-04-29', 'addd', 120000.00, 'UNAVAILABLE', 'R004', 2, 2, '2025-05-16 00:00:00', '2025-05-21 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747379389/lw87w2rui2qci0uqtscc.jpg'),
('D007', 'máy chiếu', 1, '2021-05-13', 'qer', 14000000.00, 'UNAVAILABLE', 'R003', 1, 1, '2025-05-16 00:00:00', '2025-05-18 21:00:58', 1, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747381895/qvp28lvaplekq8pu2l85.jpg'),
('D008', 'Loa', 1, '2010-04-29', 'er', 13000000.00, 'UNAVAILABLE', 'R005', 1, 1, '2025-05-16 00:00:00', '2025-05-21 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747382464/vgtmkw2x0bmwhg5mdgxt.jpg'),
('D009', 'micro', 1, '2021-05-05', 'df', 1200000.00, 'UNAVAILABLE', 'R002', 1, 1, '2025-05-16 00:00:00', '2025-05-18 21:00:58', 1, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747382757/okx6cjpnayzyqzcbfnsv.jpg'),
('D010', 'máy chiếu', 1, '2021-04-29', 'fg', 12000000.00, 'AVAILABLE', 'R001', 1, 1, '2025-05-16 00:00:00', '2025-06-01 08:58:40', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747383878/ttzqcg7esxlycmvahahy.jpg'),
('D011', 'micro', 1, '2018-04-28', 'df', 1300000.00, 'UNAVAILABLE', 'R003', 1, 1, '2025-05-16 00:00:00', '2025-05-20 20:43:01', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747385161/t8fqdylcpl296v4omaq8.jpg'),
('D012', 'máy chiếu', 1, '2021-04-29', 'qq', 13000000.00, 'UNAVAILABLE', 'R002', 2, 2, '2025-05-16 00:00:00', '2025-05-18 21:02:14', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747394291/zvbxfsh5ptkpgy4syiu2.jpg'),
('D013', 'Loa', 1, '2021-05-11', 'fe', 120000000.00, 'UNDER_MAINTENANCE', 'R003', 3, 3, '2025-05-18 00:00:00', '2025-05-18 21:00:21', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747557414/ocraqs2yv9lb8r2lknn0.jpg'),
('D014', 'Máy tính', 2, '2016-05-04', 'lind', 19000000.00, 'UNAVAILABLE', 'R008', 20, 20, '2025-05-22 00:00:00', '2025-05-22 13:09:29', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747894064/jppih1urlvit6p8u6oit.jpg'),
('D015', 'Bộ phát wifi', 3, '2021-04-28', 'kfc', 1000000.00, 'BROKEN', 'R009', 1, 1, '2025-05-22 00:00:00', '2025-05-22 14:27:35', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747894288/xn2wuh5rzzpxxslgrbbt.jpg'),
('D016', 'Ổ cắm điện', 4, '2022-05-03', 'xyz', 200000.00, 'BROKEN', 'R012', 3, 3, '2025-05-22 00:00:00', '2025-05-22 14:27:35', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747894389/vs1uvoyzuv3wp7homn3n.jpg'),
('D017', 'Quạt trần', 5, '2018-05-11', 'denis', 2000000.00, 'AVAILABLE', 'R015', 6, 6, '2025-05-22 00:00:00', '2025-05-22 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747897818/d9ngbxibhoxuxxmxyrrp.jpg'),
('D018', 'Bóng đèn', 6, '2019-04-29', 'dass', 400000.00, 'UNDER_MAINTENANCE', 'R014', 6, 6, '2025-05-22 00:00:00', '2025-05-30 10:01:48', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1747898017/lgzhdj9bwoww3gnq46er.jpg'),
('D019', 'Bộ phát wifi', 3, '2013-04-29', 'rebe', 500000.00, 'AVAILABLE', 'R017', 2, 2, '2025-05-25 00:00:00', '2025-05-29 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1748142113/h4tpgqxhvblyocm8kogq.jpg'),
('D020', 'Ổ cắm điện', 4, '2021-04-30', 'qer', 120000.00, 'AVAILABLE', 'R014', 3, 3, '2025-05-29 00:00:00', '2025-05-29 00:00:00', 0, 'https://res.cloudinary.com/dtuhfcdph/image/upload/v1748527511/w8grorlxgceiwk0goulr.jpg');

-- Insert borrow_room
INSERT INTO borrow_room (
    id, room_id, borrower_id, borrow_date, start_period, end_period, status, created_at, borrow_reason
) VALUES
    (2, 'R001', 'MTL0001', '2025-06-02', 1, 3, 'PENDING', '2025-06-01 11:55:56', NULL),
    (5, 'R001', 'MTL0001', '2025-06-02', 4, 5, 'PENDING', '2025-06-01 18:05:04', NULL),
    (6, 'R001', 'MTL0001', '2025-06-02', 7, 10, 'CANCELLED', '2025-06-01 20:39:06', NULL),
    (7, 'R002', 'MTL0001', '2025-06-02', 3, 5, 'REJECTED', '2025-06-01 20:39:35', NULL),
    (8, 'R004', 'MTL0001', '2025-06-03', 5, 10, 'APPROVED', '2025-06-01 21:03:23', NULL),
    (9, 'R007', 'MTL0001', '2025-06-03', 1, 5, 'PENDING', '2025-06-02 14:47:55', 'Dùng để họp.'),
    (10, 'R001', 'MTL0001', '2025-06-03', 11, 13, 'PENDING', '2025-06-02 22:31:36', NULL),
    (11, 'R001', 'MTL0001', '2025-06-02', 7, 10, 'PENDING', '2025-06-03 14:25:03', NULL),
    (12, 'R001', 'MTL0001', '2025-06-04', 1, 14, 'CANCELLED', '2025-06-03 14:39:14', 'Dùng để họp'),
    (13, 'R001', 'MTL0001', '2025-06-04', 1, 14, 'PENDING', '2025-06-03 14:46:00', 'Dùng để họp'),
    (14, 'R001', 'MTL0001', '2025-06-02', 6, 7, 'CANCELLED', '2025-06-03 14:52:15', NULL),
    (15, 'R001', 'MTL0001', '2025-06-02', 6, 6, 'PENDING', '2025-06-03 14:56:52', NULL);

-- 6. Insert device_borrow_requests
INSERT INTO device_borrow_requests (lecturer_user, device_id, request_date, due_date, borrowing_request)
VALUES
('MTL0001', (SELECT id FROM devices WHERE device_name = 'micro' LIMIT 1), '2025-04-21 14:00:00', '2025-04-21 16:00:00', 'PENDING'),
('MTL0003', (SELECT id FROM devices WHERE device_name = 'Bộ phát wifi' LIMIT 1), '2025-04-20 08:00:00', '2025-04-20 10:00:00', 'APPROVED');

-- 7. Insert incident (Cập nhật với cột note)
INSERT INTO incident (reported_by, room_id, description, report_date, status, note)
VALUES
('MTL0001', 'R001', 'Broken projector in room A101', '2025-04-20 09:00:00', 'SENT', 'Kiểm tra máy chiếu không hoạt động'),
('MTL0002', 'R002', 'Light flickering in room A102', '2025-06-01 10:00:00', 'SENT', 'Bóng đèn cần thay thế');

-- 8. Insert permission_types
INSERT INTO permission_types (id, name) VALUES
(1, "Thêm mới"),
(2, "Chỉnh sửa"),
(3, "Xóa"),
(4, "Xem"),
(5, "Phân quyền");

-- 9. Insert functions
INSERT INTO functions (id, name) VALUES
(1, 'Tổng quan'),
(2, 'Quản lý thiết bị'),
(3, 'Quản lý phòng'),
(4, 'Quản lý người dùng'),
(5, 'Nhóm quyền'),
(6, 'Thống kê'),
(7, 'Mượn thiết bị'),
(8, 'Mượn phòng'),
(9, 'Báo cáo sự cố'),
(10, 'Xử lý sự cố');

-- 10. Insert permissions
INSERT INTO permissions (id, role_id, function_id, permission_type_id, allowed) VALUES
(2868, 5, 1, 4, 0),
(2869, 6, 1, 4, 0),
(2870, 3, 1, 4, 1),
(2871, 5, 2, 1, 0),
(2872, 6, 2, 1, 0),
(2873, 3, 2, 1, 1),
(2874, 5, 2, 2, 0),
(2875, 6, 2, 2, 0),
(2876, 3, 2, 2, 1),
(2877, 5, 2, 3, 0),
(2878, 6, 2, 3, 0),
(2879, 3, 2, 3, 1),
(2880, 5, 2, 4, 0),
(2881, 6, 2, 4, 0),
(2882, 3, 2, 4, 1),
(2883, 5, 3, 1, 0),
(2884, 6, 3, 1, 0),
(2885, 3, 3, 1, 1),
(2886, 5, 3, 2, 0),
(2887, 6, 3, 2, 0),
(2888, 3, 3, 2, 1),
(2889, 5, 3, 3, 0),
(2890, 6, 3, 3, 0),
(2891, 3, 3, 3, 1),
(2892, 5, 3, 4, 0),
(2893, 6, 3, 4, 0),
(2894, 3, 3, 4, 1),
(2895, 5, 4, 1, 0),
(2896, 6, 4, 1, 0),
(2897, 3, 4, 1, 1),
(2898, 5, 4, 2, 0),
(2899, 6, 4, 2, 0),
(2900, 3, 4, 2, 1),
(2901, 5, 4, 3, 0),
(2902, 6, 4, 3, 0),
(2903, 3, 4, 3, 1),
(2904, 5, 4, 4, 0),
(2905, 6, 4, 4, 0),
(2906, 3, 4, 4, 1),
(2907, 5, 5, 1, 0),
(2908, 6, 5, 1, 0),
(2909, 3, 5, 1, 1),
(2910, 5, 5, 2, 0),
(2911, 6, 5, 2, 0),
(2912, 3, 5, 2, 1),
(2913, 5, 5, 3, 0),
(2914, 6, 5, 3, 0),
(2915, 3, 5, 3, 1),
(2916, 5, 5, 4, 0),
(2917, 6, 5, 4, 0),
(2918, 3, 5, 4, 1),
(2919, 5, 5, 5, 0),
(2920, 6, 5, 5, 0),
(2921, 3, 5, 5, 1),
(2922, 5, 6, 4, 0),
(2923, 6, 6, 4, 0),
(2924, 3, 6, 4, 1),
(2925, 5, 7, 4, 1),
(2926, 6, 7, 4, 0),
(2927, 3, 7, 4, 0),
(2928, 5, 8, 4, 1),
(2929, 6, 8, 4, 0),
(2930, 3, 8, 4, 0),
(2931, 5, 9, 4, 1),
(2932, 6, 9, 4, 0),
(2933, 3, 9, 4, 1),
(2934, 5, 10, 4, 0),
(2935, 6, 10, 4, 1),
(2936, 3, 10, 4, 0);

-- 11. Insert usage_logs
INSERT INTO usage_logs (id, device_id, used_at) VALUES
(1, 'D014', '2025-01-05 09:00:00'),
(2, 'D014', '2025-01-10 14:20:00'),
(3, 'D014', '2025-02-03 08:15:00'),
(4, 'D014', '2025-02-11 10:45:00'),
(5, 'D014', '2025-03-01 09:00:00'),
(6, 'D014', '2025-03-15 11:30:00'),
(7, 'D014', '2025-04-01 08:00:00'),
(8, 'D019', '2025-01-15 10:00:00'),
(9, 'D019', '2025-02-05 09:30:00'),
(10, 'D019', '2025-02-20 14:00:00'),
(11, 'D019', '2025-03-10 13:45:00'),
(12, 'D019', '2025-04-12 10:20:00'),
(13, 'D012', '2025-03-05 08:00:00'),
(14, 'D004', '2025-03-25 15:10:00'),
(15, 'D012', '2025-04-05 10:00:00'),
(16, 'D013', '2025-01-07 08:30:00'),
(17, 'D013', '2025-03-10 09:00:00'),
(18, 'D018', '2025-02-01 10:15:00'),
(19, 'D018', '2025-04-01 14:30:00'),
(20, 'D018', '2025-04-18 11:45:00');

