-- Dummy users seed
-- Common login password for every row: test1234!
-- BCrypt hash generated from the application password encoder.

INSERT IGNORE INTO users (
    nickname,
    email,
    password,
    phone_number,
    address,
    role,
    is_privacy_agreed
) VALUES
    ('user01', 'user01@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0001', 'Seoul Gangnam Yeoksam 101-1', 'USER', true),
    ('user02', 'user02@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0002', 'Seoul Gangnam Samseong 102-2', 'USER', true),
    ('user03', 'user03@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0003', 'Seoul Seocho Seocho 103-3', 'USER', true),
    ('user04', 'user04@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0004', 'Seoul Songpa Jamsil 104-4', 'USER', true),
    ('user05', 'user05@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0005', 'Seoul Mapo Yeonnam 105-5', 'USER', true),
    ('user06', 'user06@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0006', 'Seoul Yongsan Hannam 106-6', 'USER', true),
    ('user07', 'user07@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0007', 'Seoul Seongdong Seongsu 107-7', 'USER', true),
    ('user08', 'user08@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0008', 'Seoul Gwangjin Jayang 108-8', 'USER', true),
    ('user09', 'user09@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0009', 'Seoul Gangdong Cheonho 109-9', 'USER', true),
    ('user10', 'user10@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0010', 'Seoul Dongjak Sangdo 110-10', 'USER', true),
    ('user11', 'user11@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0011', 'Seoul Yeongdeungpo Yeouido 111-11', 'USER', true),
    ('user12', 'user12@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0012', 'Seoul Guro Guro 112-12', 'USER', true),
    ('user13', 'user13@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-1000-0013', 'Seoul Gwanak Bongcheon 113-13', 'USER', true),
    ('hunter01', 'hunter01@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0001', 'Seoul Gangseo Hwagok 201-1', 'HUNTER', true),
    ('hunter02', 'hunter02@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0002', 'Seoul Nowon Sanggye 202-2', 'HUNTER', true),
    ('hunter03', 'hunter03@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0003', 'Seoul Eunpyeong Bulgwang 203-3', 'HUNTER', true),
    ('hunter04', 'hunter04@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0004', 'Seoul Jungnang Myeonmok 204-4', 'HUNTER', true),
    ('hunter05', 'hunter05@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0005', 'Seoul Dobong Chang 205-5', 'HUNTER', true),
    ('hunter06', 'hunter06@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0006', 'Seoul Seodaemun Hongje 206-6', 'HUNTER', true),
    ('hunter07', 'hunter07@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0007', 'Seoul Yangcheon Sinjeong 207-7', 'HUNTER', true),
    ('hunter08', 'hunter08@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0008', 'Seoul Geumcheon Doksan 208-8', 'HUNTER', true),
    ('hunter09', 'hunter09@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0009', 'Seoul Seongbuk Dongseon 209-9', 'HUNTER', true),
    ('hunter10', 'hunter10@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0010', 'Seoul Dongdaemun Jegi 210-10', 'HUNTER', true),
    ('hunter11', 'hunter11@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0011', 'Seoul Yongsan Itaewon 211-11', 'HUNTER', true),
    ('hunter12', 'hunter12@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0012', 'Seoul Gangbuk Mia 212-12', 'HUNTER', true),
    ('hunter13', 'hunter13@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0013', 'Seoul Jung Pil-dong 213-13', 'HUNTER', true),
    ('hunter14', 'hunter14@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0014', 'Seoul Jongno Hyehwa 214-14', 'HUNTER', true),
    ('hunter15', 'hunter15@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-2000-0015', 'Seoul Gangnam Nonhyeon 215-15', 'HUNTER', true),
    ('admin01', 'admin01@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-9000-0001', 'Seoul Jongno Cheongun 901-1', 'ADMIN', true),
    ('admin02', 'admin02@bugbug.local', '$2a$10$bczOUZ1fGicnj7AFmX1Jj.09VNL3/HYDU5hehA1IvS34vK4SVfVwu', '010-9000-0002', 'Seoul Jung Myeong 902-2', 'ADMIN', true);

INSERT INTO hunters (
    user_id,
    name,
    pledge_agreed,
    grade,
    request_count,
    response_count
)
SELECT
    u.user_id,
    u.nickname,
    true,
    'SILVER_STARTER',
    0,
    0
FROM users u
WHERE u.role = 'HUNTER'
  AND u.email IN (
      'hunter01@bugbug.local',
      'hunter02@bugbug.local',
      'hunter03@bugbug.local',
      'hunter04@bugbug.local',
      'hunter05@bugbug.local',
      'hunter06@bugbug.local',
      'hunter07@bugbug.local',
      'hunter08@bugbug.local',
      'hunter09@bugbug.local',
      'hunter10@bugbug.local',
      'hunter11@bugbug.local',
      'hunter12@bugbug.local',
      'hunter13@bugbug.local',
      'hunter14@bugbug.local',
      'hunter15@bugbug.local'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM hunters h
      WHERE h.user_id = u.user_id
  );
