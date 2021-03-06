package com.backend.commons.serviceImpl;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.backend.commons.service.EmailService;
import com.backend.commons.util.CommonUtil;
import com.backend.commons.util.EmailConstants;
import com.backend.commons.util.EmailUtil;
import com.backend.core.service.BaseService;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author Muhil
 *
 */
@Service
public class EmailServiceImpl implements EmailService {

	private static Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired
	private EmailUtil emailUtil;

	@Autowired
	private BaseService baseService;

	@Autowired
	private Configuration freeMarkerConfig;

	@Override
	public void sendEmail(String recipientEmail, String subject, String body, Map<String, File> inlineImages,
			File attachments) {
		emailUtil.sendEmail(recipientEmail, subject, body, inlineImages, attachments);
	}

	/**
	 * @return - html string with substituted model values.
	 */
	public String constructOnboardEmailBody(String fname, String lname, int empId, String password, String origin, File logo) {
		try {
			Template template = freeMarkerConfig.getTemplate(EmailConstants.employeeOnboardingTemplate);
			Map<String, Object> model = new HashMap<>();
			model.put(EmailConstants.Key_employeeName, fname + " " + lname);
			model.put(EmailConstants.Key_employeeId, empId);
			model.put(EmailConstants.Key_password, password);
			model.put(EmailConstants.Key_origin, origin);
			model.put(EmailConstants.Key_tenantName, baseService.getTenantInfo().getUniqueName());
			model.put(EmailConstants.Key_tenantLogo, "\"cid:" + logo.getName() + "\"");
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (Exception e) {
			logger.error("constructOnboardEmailBody : Failed to generate Email Body - Exception - " + e.getMessage());
		}
		return null;
	}

	@Override
	public void sendOnboardingEmail(String recipientEmail, String fname, String lname, int empId, String password,
			String origin) {
		File tempFile = null;
		try {
			String subject = " Welcome to " + baseService.getTenantInfo().getUniqueName() + " : Onboarding Email";
			tempFile = File.createTempFile("icon", ".png");
			InputStream is = baseService.getTenantInfo().getTenantDetail().fetchTenantLogoBlob().getBinaryStream();
			FileUtils.copyInputStreamToFile(is, tempFile);
			Map<String, File> inlineImages = new HashMap<String, File>();
			inlineImages.put(tempFile.getName(), tempFile);
			String body = constructOnboardEmailBody(fname, lname, empId, password, origin, tempFile);
			sendEmail(recipientEmail, subject, body, inlineImages, null);
		}
		catch (Exception e) {
			logger.error("sendOnboardingEmail : Failed to generate Email Body - Exception - " + e.getMessage());
		}
		finally {
			CommonUtil.deleteDirectoryOrFile(tempFile);
		}
		
	}

	public String constructOtpEmailBody(String otp, File logo) {
		try {
			Template template = freeMarkerConfig.getTemplate(EmailConstants.employeeEmailOtpTemplate);
			Map<String, Object> model = new HashMap<>();
			model.put(EmailConstants.Key_otp, otp);
			model.put(EmailConstants.Key_tenantName, baseService.getTenantInfo().getUniqueName());
			model.put(EmailConstants.Key_tenantLogo, "\"cid:" + logo.getName() + "\"");
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (Exception e) {
			logger.error("constructOtpEmailBody : Failed to generate Email Body - Exception - " + e.getMessage());
		}
		return null;
	}

	@Override
	public void sendOtpEmail(String recipientEmail, String otp) {
		File tempFile = null;
		try {
			String subject = "Email OTP Verification : " + baseService.getTenantInfo().getUniqueName();
			tempFile = File.createTempFile("icon", ".png");
			InputStream is = baseService.getTenantInfo().getTenantDetail().fetchTenantLogoBlob().getBinaryStream();
			FileUtils.copyInputStreamToFile(is, tempFile);
			Map<String, File> inlineImages = new HashMap<String, File>();
			inlineImages.put(tempFile.getName(), tempFile);
			sendEmail(recipientEmail, subject, constructOtpEmailBody(otp, tempFile), inlineImages, null);
		} catch (Exception e) {
			logger.error("sendOtpEmail : Failed to generate Email Body - Exception - " + e.getMessage());
		}
		finally {
			CommonUtil.deleteDirectoryOrFile(tempFile);
		}

	}
}
