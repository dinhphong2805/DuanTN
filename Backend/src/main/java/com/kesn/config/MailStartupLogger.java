package com.kesn.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * In ra log khi khởi động để biết SMTP quên mật khẩu đã bật hay chưa (tránh nhầm file .example với file thật).
 */
@Component
public class MailStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MailStartupLogger.class);

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUser;

    @Override
    public void run(ApplicationArguments args) {
        if (mailHost == null || mailHost.isBlank()) {
            log.warn("[MAIL] Chua co spring.mail.host — quen mat khau CHUA gui email (chi in ma ra console backend). "
                    + "Them spring.mail.* vao src/main/resources/application-local.properties va restart.");
            return;
        }
        if (mailUser == null || mailUser.isBlank()
                || mailUser.contains("THAY_EMAIL") || mailUser.contains("@example")) {
            log.warn("[MAIL] spring.mail.username chua dien dung — SMTP se loi khi gui. Sua application-local.properties.");
            return;
        }
        log.info("[MAIL] SMTP da cau hinh: host={} user={} (quen mat khau se gui qua Gmail neu App Password dung).",
                mailHost, mailUser);
    }
}
