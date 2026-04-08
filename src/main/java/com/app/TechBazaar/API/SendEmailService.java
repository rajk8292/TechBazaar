package com.app.TechBazaar.API;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.app.TechBazaar.Model.Orders;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Service.InvoiceService;

import jakarta.mail.internet.MimeMessage;

@Service
public class SendEmailService {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private InvoiceService invoiceService;


	/*
	 * ===================================================== COMMON HTML EMAIL
	 * SENDER =====================================================
	 */
	private void sendHtmlMail(String to, String subject, String content) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content, true);

			mailSender.send(mimeMessage);

		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private String buildTemplate(String title, String userName, String bodyContent, String headerGradient,
			String btnColor, String btnText, String btnLink, String iconHtml) {

		String buttonHtml = "";
		if (btnText != null && !btnText.isEmpty() && btnLink != null && !btnLink.isEmpty()) {
			buttonHtml = "<a href='" + btnLink + "' class='btn' style='background:" + btnColor
					+ "; box-shadow: 0 4px 14px 0 " + btnColor + "40;'>" + btnText + "</a>";
		}

		return "<!DOCTYPE html>" + "<html lang='en'>"
				+ "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
				+ "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>"
				+ "<style>"
				+ "body{font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color:#f4f7f6; margin:0; padding:40px 0; -webkit-font-smoothing: antialiased;}"
				+ ".wrapper{width:100%; table-layout:fixed; background-color:#f4f7f6; padding-bottom:40px;}"
				+ ".container{max-width:600px; margin:0 auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.05), 0 4px 10px rgba(0,0,0,0.03);}"
				+ ".header{background:" + headerGradient + "; padding:40px 20px; text-align:center; color:#ffffff;}"
				+ ".logo{font-size:26px; font-weight:700; letter-spacing:-0.5px; margin-bottom:12px; display:block;}"
				+ ".icon-circle{width:64px; height:64px; background:rgba(255,255,255,0.2); border-radius:50%; display:flex; align-items:center; justify-content:center; margin:0 auto 15px auto; font-size:30px; line-height:64px;}"
				+ ".title{font-size:24px; font-weight:600; margin:0;}"
				+ ".content{padding:40px; color:#374151; font-size:16px; line-height:1.6; text-align:center;}"
				+ ".greeting{font-size:20px; font-weight:600; color:#111827; margin-bottom:20px; margin-top:0;}"
				+ ".inner-body{margin-bottom:30px;}" + ".inner-body p{margin:10px 0;}"
				+ ".highlight-box{background:#f8fafc; border:1px solid #e2e8f0; border-radius:12px; padding:20px; margin:25px 0;}"
				+ ".highlight-text{font-size:32px; font-weight:700; letter-spacing:2px; color:" + btnColor
				+ "; margin:10px 0;}"
				+ ".btn{display:inline-block; padding:14px 32px; color:#ffffff !important; text-decoration:none; border-radius:8px; font-weight:600; font-size:16px; transition:transform 0.2s ease; margin-top:10px;}"
				+ ".footer{background-color:#f8fafc; border-top:1px solid #f1f5f9; padding:30px 40px; text-align:center; color:#64748b; font-size:14px; line-height:1.5;}"
				+ ".social-links{margin-bottom:15px;}"
				+ ".social-links a{color:#94a3b8; text-decoration:none; margin:0 10px; font-weight:500;}"
				+ ".footer p{margin:5px 0;}" + ".footer-links{margin-top:15px; font-size:12px;}"
				+ ".footer-links a{color:#64748b; text-decoration:underline; margin:0 5px;}"
				+ "@media screen and (max-width: 600px) { .content{padding:30px 20px 20px;} .footer{padding:20px;} }"
				+ "</style></head>" + "<body>" + "<div class='wrapper'>" + "<div class='container'>"
				+ "<div class='header'>"
				+ "<div style='text-align:center;'><span class='icon-circle' style='display:inline-block;'>" + iconHtml
				+ "</span></div>" + "<div class='logo'>TechBazaar</div>" + "<h1 class='title'>" + title + "</h1>"
				+ "</div>" + "<div class='content'>" + "<h2 class='greeting'>Hi " + userName + ",</h2>"
				+ "<div class='inner-body'>" + bodyContent + "</div>" + buttonHtml + "</div>" + "<div class='footer'>"
				+ "<div class='social-links'>"
				+ "<a href='#'>Twitter</a> • <a href='#'>Instagram</a> • <a href='#'>Facebook</a>" + "</div>"
				+ "<p>Need help? Contact our <b><a href='mailto:support@techbazaar.com' style='color:#64748b; text-decoration:none;'>Support Team</a></b></p>"
				+ "<p>© 2026 TechBazaar Inc. All rights reserved.</p>" + "<div class='footer-links'>"
				+ "<a href='#'>Privacy Policy</a> | <a href='#'>Terms of Service</a>" + "</div>" + "</div>" + "</div>"
				+ "</div>" + "</body></html>";
	}

	/*
	 * ===================================================== 1️⃣ REGISTRATION OTP
	 * =====================================================
	 */
	public void sendRegistrationOTP(Users user, String otp) {

		String body = "<p>Welcome to <strong>TechBazaar</strong>! We're thrilled to have you onboard.</p>"
				+ "<p>To complete your registration and verify your email address, please use the following One-Time Password (OTP):</p>"
				+ "<div class='highlight-box'>"
				+ "<p style='margin:0; color:#64748b; font-size:14px; text-transform:uppercase; font-weight:600;'>Your Verification Code</p>"
				+ "<div class='highlight-text'>" + otp + "</div>" + "</div>"
				+ "<p style='color:#64748b; font-size:14px; margin-top:15px;'>⏱ This code is valid for the next 5 minutes.</p>"
				+ "<p style='color:#64748b; font-size:14px;'>If you didn't request this, you can safely ignore this email.</p>";

		String content = buildTemplate("Verify Your Account", user.getName(), body,
				"linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)", "#4f46e5", "Go to Verification", "localhost:8282",
				"🔐");

		sendHtmlMail(user.getEmail(), "Verify your TechBazaar account (OTP)", content);
	}

	/*
	 * ===================================================== 2️⃣ ORDER CONFIRMED
	 * =====================================================
	 */
	public void sendOrderConfirmed(Users user, Orders order) {

		String body = "<p>Great news! Your order has been successfully confirmed and is now being processed.</p>"
				+ "<div class='highlight-box'>"
				+ "<p style='margin:0; color:#64748b; font-size:14px; text-transform:uppercase; font-weight:600;'>Order Reference</p>"
				+ "<div style='font-size:24px; font-weight:700; color:#111827; margin:10px 0;'>#" + order.getOrderNumber()
				+ "</div>" + "</div>"
				+ "<p>We'll send you another update as soon as your items have been shipped. Thank you for placing your trust in us!</p>";

		String content = buildTemplate("Order Confirmed!", user.getName(), body,
				"linear-gradient(135deg, #059669 0%, #10b981 100%)", // Emerald
				"#059669", "View Order Details", "" + order.getOrderNumber(), "📦");

		sendHtmlMail(user.getEmail(), "Your TechBazaar Order Confirmation (#" + order.getId() + ")", content);
	}

	/*
	 * ===================================================== 3️⃣ ORDER CANCELLED
	 * =====================================================
	 */
	public void sendOrderCancelled(Users user, Orders order) {

		String body = "<p>We're writing to let you know that your order has been cancelled.</p>"
				+ "<div class='highlight-box' style='background:#fef2f2; border-color:#fecaca;'>"
				+ "<p style='margin:0; color:#991b1b; font-size:14px; text-transform:uppercase; font-weight:600;'>Order Reference</p>"
				+ "<div style='font-size:24px; font-weight:700; color:#7f1d1d; margin:10px 0;'>#" + order.getOrderNumber()
				+ "</div>" + "</div>"
				+ "<p>If a payment was already processed, a full refund has been initiated to your original payment method. It may take 3-5 business days to reflect in your account.</p>";

		String content = buildTemplate("Order Cancelled", user.getName(), body,
				"linear-gradient(135deg, #dc2626 0%, #ef4444 100%)", // Red
				"#dc2626", "Browse Other Products", "", "✖️");

		sendHtmlMail(user.getEmail(), "Update on TechBazaar Order #" + order.getOrderNumber(), content);
	}

	/*
	 * ===================================================== 4️⃣ ORDER DELIVERED
	 * =====================================================
	 */
	public void sendOrderDeliveredWithInvoice(Orders order) {

	    try {

	        Users user = order.getUser();

	        // 🔥 Generate Invoice PDF
	        ByteArrayInputStream pdfStream = invoiceService.generateInvoice(order);
	        byte[] pdfBytes = pdfStream.readAllBytes();

	        MimeMessage mimeMessage = mailSender.createMimeMessage();
	        MimeMessageHelper helper =
	                new MimeMessageHelper(mimeMessage, true, "UTF-8");

	        helper.setTo(user.getEmail());

	        // ✅ Premium Subject
	        helper.setSubject("Your Invoice is Ready! | Order #" 
	                + order.getId() + " | TechBazaar");

	        // ✅ Premium HTML Email Body
	        String body =
	                "<div style='font-family:Segoe UI, sans-serif; background:#f4f6f9; padding:30px;'>" +

	                "<div style='max-width:600px; margin:auto; background:white; border-radius:16px; " +
	                "box-shadow:0 10px 30px rgba(0,0,0,0.08); overflow:hidden;'>" +

	                // Header
	                "<div style='background:linear-gradient(135deg,#0f172a,#1e293b); padding:25px; text-align:center;'>" +
	                "<h2 style='color:white; margin:0;'>TechBazaar</h2>" +
	                "<p style='color:#cbd5e1; margin:5px 0 0;'>Order Delivered Successfully 🎉</p>" +
	                "</div>" +

	                // Body Content
	                "<div style='padding:30px;'>" +

	                "<p style='font-size:16px; color:#374151;'>Hi <strong>" 
	                + user.getName() + "</strong>,</p>" +

	                "<p style='color:#6b7280;'>Your order has been delivered successfully. Here are the details:</p>" +

	                "<div style='background:#f9fafb; border-radius:12px; padding:20px; margin:20px 0; border:1px solid #e5e7eb;'>" +

	                "<p style='margin:10px 0;'><strong>Order Number:</strong> #" 
	                + order.getOrderNumber() + "</p>" +

	                "<p style='margin:10px 0;'><strong>Product:</strong> " 
	                + order.getProductName() + 
	                " (Qty: " + order.getQuantity() + ")</p>" +

	                "<p style='margin:15px 0; font-size:18px; font-weight:600; color:#059669;'>" +
	                "Final Amount: ₹" + order.getFinalAmount() + "</p>" +

	                "</div>" +

	                "<p style='color:#6b7280;'>Please find your official invoice attached with this email.</p>" +

	                "<p style='margin-top:30px; font-size:14px; color:#9ca3af;'>Thank you for shopping with TechBazaar 💙</p>" +

	                "</div>" +

	                // Footer
	                "<div style='background:#f3f4f6; padding:15px; text-align:center; font-size:13px; color:#6b7280;'>" +
	                "© 2026 TechBazaar. All rights reserved." +
	                "</div>" +

	                "</div>" +
	                "</div>";

	        helper.setText(body, true);

	        // ✅ Attach PDF Properly (Download Issue Fixed)
	        helper.addAttachment(
	                "TechBazaar-Invoice-" + order.getOrderNumber() + ".pdf",
	                new ByteArrayResource(pdfBytes)
	        );

	        mailSender.send(mimeMessage);

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException("Invoice email failed: " + e.getMessage());
	    }
	}
	/*
	 * ===================================================== 5️⃣ ORDER RETURN
	 * =====================================================
	 */
	public void sendOrderReturn(Users user, Orders order) {

		String body = "<p>We've received your request to return an item from your recent order.</p>"
				+ "<div class='highlight-box' style='background:#fefce8; border-color:#fef08a;'>"
				+ "<p style='margin:0; color:#854d0e; font-size:14px; text-transform:uppercase; font-weight:600;'>Order Reference</p>"
				+ "<div style='font-size:24px; font-weight:700; color:#713f12; margin:10px 0;'>#" + order.getOrderNumber()
				+ "</div>" + "</div>"
				+ "<p>Our team is currently reviewing your request. We'll follow up with the next steps shortly. Please ensure your item is in its original packaging.</p>";

		String content = buildTemplate("Return Request Received", user.getName(), body,
				"linear-gradient(135deg, #d97706 0%, #f59e0b 100%)", // Amber
				"#d97706", "Track Return Status", "" + order.getOrderNumber(), "🔁");

		sendHtmlMail(user.getEmail(), "TechBazaar Return Request Update (#" + order.getOrderNumber() + ")", content);
	}

}