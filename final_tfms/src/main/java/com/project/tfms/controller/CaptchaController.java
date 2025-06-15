package com.project.tfms.controller;
 
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.DropShadowGimpyRenderer;
import cn.apiclub.captcha.noise.CurvedLineNoiseProducer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;
import cn.apiclub.captcha.text.renderer.DefaultWordRenderer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
 
import javax.imageio.ImageIO;
import java.io.OutputStream;
 
@Controller
public class CaptchaController {
 
    @GetMapping("/captcha.jpg")
    public void generateCaptcha(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws Exception {
        Captcha captcha = new Captcha.Builder(200, 50) // Width, Height
                .addBackground(new GradiatedBackgroundProducer())
                .addText(new DefaultTextProducer(), new DefaultWordRenderer()) // Add text
                .addNoise(new CurvedLineNoiseProducer()) // Add noise
                .gimp(new DropShadowGimpyRenderer()) // Apply distortion
                .build();
 
        // Store the CAPTCHA text in the session
        // This is key: both login pages will use this session attribute for validation
        session.setAttribute("captchaText", captcha.getAnswer());
 
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
 
        try (OutputStream out = response.getOutputStream()) {
            ImageIO.write(captcha.getImage(), "jpg", out);
            out.flush();
        }
    }
}