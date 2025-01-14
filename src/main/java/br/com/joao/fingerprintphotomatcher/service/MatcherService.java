package br.com.joao.fingerprintphotomatcher.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.joao.fingerprintphotomatcher.rest.vo.ExternalMatchRequestVO;
import br.com.joao.fingerprintphotomatcher.rest.vo.ExternalMatchResponseVO;
import br.com.joao.fingerprintphotomatcher.rest.vo.ExtractResponseVO;
import br.com.joao.fingerprintphotomatcher.rest.vo.MatchResponseVO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MatcherService {

	@Value("${neurotec.api.verify:http://neurotec-services.hom.bry.com.br/api/verify}")
	private String apiVerify;

	private static RestTemplate restTemplate = new RestTemplate();
	private static ObjectMapper objectMapper = new ObjectMapper();

	public MatchResponseVO verifyPhotos(ExtractResponseVO template1Extraction,
			ExtractResponseVO template2Extraction)
			throws Exception {
		log.info("Preparing data to send to matcher");
		ExternalMatchRequestVO externalMatchRequestVO = new ExternalMatchRequestVO(template1Extraction.getTemplate(),
				template2Extraction.getTemplate());
		return verifyTemplates(externalMatchRequestVO);
	}

	public MatchResponseVO verifyTemplates(ExternalMatchRequestVO externalMatchRequestVO) throws Exception {
		log.info("Request to perform a verify in neurotec service");
		try {
			HttpHeaders headers = new HttpHeaders();
			String ctxId = UUID.randomUUID().toString().substring(0, 7);
			headers.set("ctxId", ctxId);
			HttpEntity<ExternalMatchRequestVO> request = new HttpEntity<>(externalMatchRequestVO, headers);
			ResponseEntity<byte[]> response = restTemplate.exchange(apiVerify,
					HttpMethod.POST, request, byte[].class);
			ExternalMatchResponseVO matchResponse = objectMapper.readValue(response.getBody(),
					ExternalMatchResponseVO.class);
			log.info("Data matched successfully");
			return convertExternalMatchResponseToMatchResponse(matchResponse);
		} catch (Exception e) {
			log.error("Error consuming template matcher service > url: {} | message: {}",
					apiVerify, e.getMessage(), e);
			throw e;
		}
	}

	private MatchResponseVO convertExternalMatchResponseToMatchResponse(
			ExternalMatchResponseVO externalMatchResponseVO) {
		log.info("Converting match result to more readable result");
		return new MatchResponseVO(externalMatchResponseVO.getOperation(),
				externalMatchResponseVO.getResult(), externalMatchResponseVO.getScore(), null,
				externalMatchResponseVO.getDescription());
	}

}
