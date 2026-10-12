package com.health.canteen.service.impl;

import com.health.canteen.constant.RedisConstant;
import com.health.canteen.exception.BusinessException;
import com.health.canteen.service.CaptchaService;
import com.health.canteen.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 图形验证码：字符由 SecureRandom 生成，图片用 Java2D 本地绘制，答案只存服务端 Redis
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /**
     * 去掉易混淆的 0/O/1/I
     */
    private static final char[] CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int LENGTH = 4;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public CaptchaVO generate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            code.append(CHARS[random.nextInt(CHARS.length)]);
        }
        String key = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(RedisConstant.CAPTCHA_KEY + key, code.toString(),
                RedisConstant.CAPTCHA_TTL);
        return new CaptchaVO(key, draw(code.toString()));
    }

    @Override
    public void verify(String key, String code) {
        if (isBlank(key) || isBlank(code)) {
            throw new BusinessException("请输入图形验证码");
        }
        String cached = stringRedisTemplate.opsForValue().get(RedisConstant.CAPTCHA_KEY + key);
        if (cached == null) {
            throw new BusinessException("图形验证码已过期，请重新获取");
        }
        if (!cached.equalsIgnoreCase(code.trim())) {
            throw new BusinessException("图形验证码错误");
        }
        //一次一用，防止同一验证码被重放
        stringRedisTemplate.delete(RedisConstant.CAPTCHA_KEY + key);
    }

    private String draw(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(120 + random.nextInt(100), 120 + random.nextInt(100), 120 + random.nextInt(100)));
                g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
            }
            int step = WIDTH / (LENGTH + 1);
            for (int i = 0; i < code.length(); i++) {
                int x = step * (i + 1) - 6;
                int y = 30;
                g.setColor(new Color(random.nextInt(110), random.nextInt(110), random.nextInt(140)));
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24 + random.nextInt(6)));
                double angle = (random.nextDouble() - 0.5) / 5;
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-angle, x, y);
            }
        } finally {
            g.dispose();
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("验证码图片生成失败", e);
            throw new BusinessException("验证码生成失败，请重试");
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

}
