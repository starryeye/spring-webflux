package dev.practice.user.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

@EnableR2dbcAuditing
@TestConfiguration // test class 에 따로 import 를 해야 동작한다. test 에는 component scan 이 없는듯
public class R2dbcAuditingConfigForTest {
}
