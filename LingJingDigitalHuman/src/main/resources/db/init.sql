-- ============================================================
-- 灵境智游 数据库初始化脚本
-- 数据库：lingjing_digital_human
-- ============================================================

CREATE DATABASE IF NOT EXISTS lingjing_digital_human
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE lingjing_digital_human;

-- ============================================================
-- 第一批：无外键依赖的表
-- ============================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt加密',
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'user' COMMENT 'user/admin',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 景点信息表
CREATE TABLE IF NOT EXISTS attraction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '景点名称',
    description TEXT COMMENT '简要介绍',
    detail TEXT COMMENT '详细介绍',
    area VARCHAR(50) COMMENT '所属区域',
    latitude DECIMAL(10,7) COMMENT '纬度',
    longitude DECIMAL(10,7) COMMENT '经度',
    cover_image VARCHAR(255) COMMENT '封面图',
    images TEXT COMMENT '更多图片(JSON数组)',
    opening_hours VARCHAR(100) COMMENT '开放时间',
    duration VARCHAR(50) COMMENT '建议游览时长',
    tags VARCHAR(255) COMMENT '标签(逗号分隔)',
    qr_code VARCHAR(255) COMMENT '二维码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 1=显示 0=隐藏',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点信息表';

-- 3. 推荐路线表
CREATE TABLE IF NOT EXISTS tour_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '路线名称',
    description TEXT COMMENT '路线描述',
    duration VARCHAR(50) COMMENT '建议时长',
    cover_image VARCHAR(255),
    tags VARCHAR(255) COMMENT '标签(逗号分隔)，如"历史文化,人文"',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1 COMMENT '1=显示 0=隐藏',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐路线表';

-- 路线-景点关联表
CREATE TABLE IF NOT EXISTS route_attraction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    sort_order INT DEFAULT 0 COMMENT '在路线中的顺序',
    KEY idx_route_id (route_id),
    KEY idx_attraction_id (attraction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线景点关联表';

-- 用户自定义路线表
CREATE TABLE IF NOT EXISTS user_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '路线名称',
    attraction_ids JSON COMMENT '排序后的景点ID列表',
    total_distance_meters INT DEFAULT 0 COMMENT '总路程(米)',
    estimated_minutes INT DEFAULT 0 COMMENT '预计耗时(分钟)',
    attraction_count INT DEFAULT 0 COMMENT '景点数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义路线表';

-- 4. 成就定义表
CREATE TABLE IF NOT EXISTS achievement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '成就名称',
    description TEXT COMMENT '成就描述',
    icon VARCHAR(255) COMMENT '图标',
    condition_desc VARCHAR(255) COMMENT '达成条件描述',
    condition_type VARCHAR(50) COMMENT '条件类型: checkin_count/all_attractions',
    condition_value INT COMMENT '条件数值',
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';

-- 5. 数字人配置表
CREATE TABLE IF NOT EXISTS digital_human_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值(JSON)',
    description VARCHAR(255),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字人配置表';

-- ============================================================
-- 第二批：依赖用户/景点的表
-- ============================================================

-- 6. 对话会话表
CREATE TABLE IF NOT EXISTS conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(200) COMMENT '会话标题',
    status TINYINT DEFAULT 1 COMMENT '1=活跃 0=已关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

-- 7. 消息记录表
CREATE TABLE IF NOT EXISTS message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT 'user/assistant',
    content TEXT COMMENT '消息内容',
    content_type VARCHAR(20) DEFAULT 'text' COMMENT 'text/voice/photo',
    image_url VARCHAR(500) COMMENT '图片消息的URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_conversation_id (conversation_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表';

-- 8. 打卡记录表
CREATE TABLE IF NOT EXISTS check_in (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    attraction_id BIGINT NOT NULL,
    latitude DECIMAL(10,7) COMMENT '打卡时纬度',
    longitude DECIMAL(10,7) COMMENT '打卡时经度',
    check_in_type VARCHAR(20) COMMENT 'gps/qrcode',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_attraction_id (attraction_id),
    UNIQUE KEY uk_user_attraction (user_id, attraction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- ============================================================
-- 第三批：知识库、日志、成就
-- ============================================================

-- 9. 知识库文档表
CREATE TABLE IF NOT EXISTS kb_document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) COMMENT '文档标题',
    file_name VARCHAR(255) COMMENT '原始文件名',
    file_path VARCHAR(255) COMMENT '存储路径',
    file_type VARCHAR(20) COMMENT 'txt/docx',
    file_size BIGINT COMMENT '文件大小(字节)',
    status TINYINT DEFAULT 0 COMMENT '0=未解析 1=已解析 2=解析失败',
    chunk_count INT DEFAULT 0 COMMENT '切片数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 10. 知识切片表
CREATE TABLE IF NOT EXISTS kb_chunk (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    content TEXT COMMENT '切片内容',
    seq INT COMMENT '切片序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_document_id (document_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识切片表';

-- 11. 交互日志表
CREATE TABLE IF NOT EXISTS interaction_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    interaction_type VARCHAR(50) COMMENT 'chat/checkin/query',
    content TEXT COMMENT '交互内容',
    sentiment VARCHAR(20) COMMENT 'positive/neutral/negative',
    duration_ms INT COMMENT '耗时(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交互日志表';

-- 12. 用户成就表
CREATE TABLE IF NOT EXISTS user_achievement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_achievement (user_id, achievement_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 景点数据（22条）
INSERT INTO attraction (id, name, description, area, latitude, longitude, opening_hours, duration, tags, sort_order) VALUES
(1, '灵山大照壁', '景区标志性门户，赵朴初先生亲笔题写"灵山胜境"鎏金大字，被誉为"华夏第一壁"。', '灵山胜境', 31.4385000, 120.0870000, '全天开放', '10分钟', '打卡,文化', 1),
(2, '五明桥', '五座汉白玉石桥并列排布，代表佛教五种核心智慧，过桥寓意开启智慧走向觉悟。', '灵山胜境', 31.4387000, 120.0868000, '全天开放', '10分钟', '文化,建筑', 2),
(3, '佛足坛', '巨型青铜佛足印，复刻佛祖释迦牟尼真身脚印，足心刻有32种吉祥图案。', '灵山胜境', 31.4389000, 120.0865000, '全天开放', '15分钟', '祈福,文化', 3),
(4, '五智门', '五门六柱石牌坊，五门象征五方五佛，六柱代表佛教六度波罗蜜，进入即踏入核心朝圣区。', '灵山胜境', 31.4392000, 120.0866000, '全天开放', '10分钟', '建筑,文化', 4),
(5, '菩提大道', '长约250米的禅意步道，两侧种植从印度引进的近百棵菩提树，形成天然禅意拱廊。', '灵山胜境', 31.4395000, 120.0867000, '全天开放', '15分钟', '自然,禅意', 5),
(6, '九龙灌浴', '大型音乐动态群雕，再现释迦牟尼诞生时"花开见佛，九龙沐浴"的祥瑞景象，每日多场表演。', '灵山胜境', 31.4398000, 120.0868000, '10:00/11:30/13:30/15:00', '20分钟', '表演,祈福', 6),
(7, '降魔浮雕', '巨型花岗岩浮雕，生动再现佛陀在菩提树下战胜魔王波旬诱惑与威胁、觉悟成佛的历程。', '灵山胜境', 31.4401000, 120.0867000, '全天开放', '15分钟', '文化,艺术', 7),
(8, '阿育王柱', '通高16.9米、重180吨的整块花岗岩石柱，柱头四头狮子象征佛法向四方传播。', '灵山胜境', 31.4403000, 120.0868000, '全天开放', '10分钟', '文化,地标', 8),
(9, '百子戏弥勒', '青铜群雕，弥勒佛呈卧姿，身上塑有百名嬉戏孩童，寓意"多子多福、家庭和睦"。', '灵山胜境', 31.4405000, 120.0870000, '全天开放', '15分钟', '祈福,亲子', 9),
(10, '祥符禅寺', '始建于唐贞观年间的千年古刹，由玄奘弟子窥基大师开坛讲经，寺内有千年银杏、六角古井等珍贵遗迹。', '灵山胜境', 31.4408000, 120.0872000, '全天开放', '30分钟', '文化,历史,祈福', 10),
(11, '灵山大佛', '世界最高露天青铜释迦牟尼立像，通高88米，总高101.5米，耗铜725吨，登顶可抱佛脚、俯瞰太湖全景。', '灵山胜境', 31.4415000, 120.0875000, '8:00-17:00', '60分钟', '地标,祈福,文化', 11),
(12, '佛教文化博览馆', '设于灵山大佛座基内，三层展厅系统展示五方五佛、四大名山、世界佛教发展史，三层万佛殿有9999尊小佛像。', '灵山胜境', 31.4413000, 120.0878000, '8:00-17:00', '40分钟', '文化,科普', 12),
(13, '灵山梵宫', '建筑面积7.2万平方米，被誉为"东方卢浮宫"，汇集东阳木雕、琉璃、油画等非遗艺术，世界佛教论坛永久会址。', '灵山胜境', 31.4400000, 120.0850000, '9:00-17:00', '90分钟', '艺术,文化,建筑', 13),
(14, '五印坛城', '藏传佛教风格建筑，白墙红边金顶，四面环水，供奉五方五佛，有转经筒长廊，可俯瞰灵山全貌。', '灵山胜境', 31.4395000, 120.0845000, '9:00-17:00', '40分钟', '文化,藏传佛教', 14),
(15, '曼飞龙塔', '南传佛教白塔，由一座主塔和八座小塔组成九塔组合，复刻云南西双版纳曼飞龙白塔形制。', '灵山胜境', 31.4398000, 120.0842000, '全天开放', '20分钟', '建筑,文化', 15),
(16, '无尽意斋', '赵朴初先生纪念馆，复刻北京故居四合院风格，展示其生平事迹、书法作品及与灵山的深厚渊源。', '灵山胜境', 31.4410000, 120.0865000, '9:00-17:00', '30分钟', '文化,人文', 16),
(17, '拈花广场', '拈花湾禅意小镇门户，中央矗立"拈花微笑"主题雕塑，源自佛教"迦叶拈花、佛陀微笑"的典故。', '拈花湾', 31.4180000, 120.1030000, '9:00-21:30', '15分钟', '打卡,禅意', 17),
(18, '梵天花海', '占地约30000平方米的花卉景观区，四季种植不同花卉（格桑花、波斯菊等），体现"一花一世界"的禅意。', '拈花湾', 31.4175000, 120.1015000, '9:00-21:30', '30分钟', '自然,拍照', 18),
(19, '香月花街', '长约800米的禅意商业街，融合中式禅意与日式町屋建筑风格，涵盖文创、非遗手作、特色餐饮。', '拈花湾', 31.4178000, 120.1035000, '9:00-21:30', '60分钟', '购物,美食,文化', 19),
(20, '拈花堂', '禅意体验场所，提供禅坐冥想、抄经、禅茶品鉴等体验，感受"静心、修身、悟道"的禅意生活。', '拈花湾', 31.4176000, 120.1040000, '9:30-19:00', '30分钟', '体验,禅意', 20),
(21, '五灯湖', '小镇水景核心，夜间《禅行》灯光秀如梦如幻，五灯象征"五智"，传递"心似湖水"的禅理。', '拈花湾', 31.4170000, 120.1035000, '9:00-21:30', '30分钟', '夜景,表演', 21),
(22, '鹿鸣谷', '占地约20000平方米的山林自然景观，植被覆盖率达90%以上，小镇最静谧的区域，适合漫步放松。', '拈花湾', 31.4165000, 120.1010000, '全天开放', '40分钟', '自然,清幽', 22);

-- 路线数据（3条）
INSERT INTO tour_route (id, name, description, duration, tags, sort_order) VALUES
(1, '历史文化爱好者路线', '深度游览灵山胜境历史文化精华，涵盖灵山大照壁、祥符禅寺、灵山大佛、灵山梵宫、五印坛城等核心文化景点，全程约6小时。', '6小时', '历史文化', 1),
(2, '自然风光爱好者路线', '全景游览灵山胜境自然与人文风光，涵盖菩提大道、灵山大佛、曼飞龙塔、灵山精舍等景点，全程约5小时。', '5小时', '自然风光', 2),
(3, '亲子家庭路线', '轻松愉快的亲子之旅，涵盖九龙灌浴、佛手广场、百子戏弥勒、梵宫等互动性强的景点，全程约4小时。', '4小时', '亲子家庭', 3);

-- 路线-景点关联
INSERT INTO route_attraction (route_id, attraction_id, sort_order) VALUES
-- 路线1：历史文化爱好者路线（6小时深度游）
(1, 1, 1),  (1, 2, 2),  (1, 3, 3),  (1, 4, 4),  (1, 9, 5),
(1, 10, 6), (1, 11, 7), (1, 12, 8), (1, 13, 9), (1, 14, 10), (1, 16, 11),
-- 路线2：自然风光爱好者路线（5小时全景游）
(2, 3, 1),  (2, 6, 2),  (2, 5, 3),  (2, 11, 4), (2, 15, 5),
(2, 13, 6), (2, 17, 7), (2, 18, 8),
-- 路线3：亲子家庭路线（4小时轻松游）
(3, 6, 1),  (3, 9, 2),  (3, 10, 3), (3, 13, 4), (3, 14, 5),
(3, 17, 6), (3, 21, 7);

-- 成就数据（9条）
INSERT INTO achievement (id, name, description, condition_desc, condition_type, condition_value, sort_order) VALUES
(1, '初入灵山', '完成首次景点打卡，开启灵山之旅', '打卡1个景点', 'checkin_count', 1, 1),
(2, '佛国行者', '打卡3个不同景点，探索灵山胜境', '打卡3个景点', 'checkin_count', 3, 2),
(3, '禅意初悟', '打卡5个景点，渐入禅境', '打卡5个景点', 'checkin_count', 5, 3),
(4, '灵山探秘', '打卡8个景点，探索不止', '打卡8个景点', 'checkin_count', 8, 4),
(5, '佛光普照', '打卡10个景点，佛光相伴', '打卡10个景点', 'checkin_count', 10, 5),
(6, '拈花一笑', '打卡12个景点，拈花会心一笑', '打卡12个景点', 'checkin_count', 12, 6),
(7, '步步生莲', '打卡15个景点，步步生莲', '打卡15个景点', 'checkin_count', 15, 7),
(8, '般若智慧', '打卡18个景点，智慧圆融', '打卡18个景点', 'checkin_count', 18, 8),
(9, '灵山通', '打卡全部景点，成为灵山通', '打卡全部景点', 'all_attractions', 22, 9);

-- 数字人配置（默认配置）
INSERT INTO digital_human_config (config_key, config_value, description) VALUES
('default', '{
    "name": "灵宝",
    "greeting": "你好！我是灵宝，你的智能导游。欢迎来到灵山胜境！我可以为你介绍景点、推荐路线、解答问题。请问有什么可以帮你的吗？",
    "style": "古风典雅",
    "tts_enabled": true,
    "asr_enabled": true,
    "personality": "温柔知性",
    "model": "qwen-max",
    "tts_speed": 1.0
}', '数字人默认配置');

-- 为已有 message 表补充 image_url 列（此前手动建表的需执行此行）
-- ALTER TABLE message ADD COLUMN image_url VARCHAR(500) COMMENT 'image URL' AFTER content_type;
