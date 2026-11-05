# flight-booking —— 高并发民航机票预订与订单流转系统（实训项目）

按 Day2-3 讲义 2.3（2）的三层架构与多模块工程规范组织。

## 包结构

```
flight-booking
├── pom.xml                    # 父 POM（packaging=pom，统一依赖版本 dependencyManagement）
├── README.md
├── .gitignore
├── docker-compose.yml         # MySQL / Redis / RocketMQ 一键启动
├── docs/
│   ├── sql/init.sql           # 建表与造数脚本
│   ├── jmeter/                # 压测脚本
│   └── api/                   # 接口文档
│
├── flight-common/             # ===== 基础设施模块 =====
│   └── com.example.flight.common
│       ├── result/            # Result 统一返回体
│       ├── exception/         # BusinessException / GlobalExceptionHandler
│       ├── security/          # JwtInterceptor
│       └── util/              # JwtUtil / SnowflakeIdWorker
│
├── flight-user/               # ===== 用户模块 =====
│   └── com.example.flight.user (controller / service / mapper / entity)
│
├── flight-flight/             # ===== 航班模块 =====
│   └── com.example.flight.flight (controller / service.impl / mapper / entity)
│
├── flight-order/              # ===== 订单模块 =====
│   └── com.example.flight.order (controller / service.impl / mapper / entity / enums / mq)
│
├── flight-payment/            # ===== 支付模块 =====
│   └── com.example.flight.payment (service.impl / mapper / entity)
│
├── flight-ticket/             # ===== 出票模块 =====
│   └── com.example.flight.ticket (mq / mapper / entity)
│
└── flight-boot/               # ===== 启动聚合模块 =====
    └── com.example.flight.FlightBookingApplication
        + resources/ (application*.yml / static)
```

## 模块依赖

```
flight-common  ←  flight-user
flight-common  ←  flight-flight  ←  flight-order  ←  flight-payment
flight-common  ←  flight-order   ←  flight-ticket  ←  flight-payment
全部模块        ←  flight-boot（启动聚合）
```

## 快速开始

```bash
# 1. 启动中间件（MySQL 3306 / Redis 6379 / RocketMQ 9876）
docker compose up -d

# 2. 编译打包
mvn clean package -DskipTests

# 3. 启动服务（端口 8081）
java -jar flight-boot/target/flight-boot-1.0.0.jar

# 或开发期直接运行
mvn spring-boot:run -pl flight-boot
```

- 接口文档：http://localhost:8081/doc.html
- 首页演示页：http://localhost:8081/
