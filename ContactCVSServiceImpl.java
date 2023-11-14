package us.tx.state.dfps.service.contacts.serviceimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.common.dto.GenericCaseInfoDto;
import us.tx.state.dfps.service.admin.dao.EmployeeDao;
import us.tx.state.dfps.service.admin.dao.PersonInfoDao;
import us.tx.state.dfps.service.admin.dto.PersonDiDto;
import us.tx.state.dfps.service.admin.dto.PersonDoDto;
import us.tx.state.dfps.service.casepackage.dao.CaseSummaryDao;
import us.tx.state.dfps.service.casepackage.dto.CaseStageSummaryDto;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.dao.EventDao;
import us.tx.state.dfps.service.common.dao.StageDao;
import us.tx.state.dfps.service.common.request.CpsInvConclValReq;
import us.tx.state.dfps.service.common.request.EventReq;
import us.tx.state.dfps.service.common.request.StageClosureEventReq;
import us.tx.state.dfps.service.common.util.JSONUtil;
import us.tx.state.dfps.service.contact.dto.ContactCVSDto;
import us.tx.state.dfps.service.contact.dto.ContactEventDto;
import us.tx.state.dfps.service.contact.dto.ContactGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactNarrGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactNarrLogPerDateDto;
import us.tx.state.dfps.service.contact.dto.ContactPersonDto;
import us.tx.state.dfps.service.contactnarrlog.dao.ContactNarrLogAppendDao;
import us.tx.state.dfps.service.contacts.dao.ContactCVSDao;
import us.tx.state.dfps.service.contacts.dao.ContactEventPersonDao;
import us.tx.state.dfps.service.contacts.service.ContactCVSService;
import us.tx.state.dfps.service.contacts.service.StageClosureEventService;
import us.tx.state.dfps.service.disasterplan.dao.DisasterPlanDao;
import us.tx.state.dfps.service.forms.dto.PreFillDataServiceDto;
import us.tx.state.dfps.service.forms.util.ContactCVSPrefillData;
import us.tx.state.dfps.service.workload.dto.EmployeePersPhNameDto;
import us.tx.state.dfps.service.workload.dto.StagePersonDto;
import us.tx.state.dfps.web.contact.bean.ContactDetailDto;
import us.tx.state.dfps.xmlstructs.inputstructs.ConGuideFetchInDto;
import us.tx.state.dfps.xmlstructs.inputstructs.ContactDetailsOutDto;
import us.tx.state.dfps.xmlstructs.inputstructs.ServiceInputDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.StageProgramDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class Description :MONTHLY
 * EVALUATION FORM Tuxedo Service :CSUB84S Mar 28, 2018- 11:24:47 AM © 2017
 * Texas Department of Family and Protective Services
 */

@Service
@Transactional
public class ContactCVSServiceImpl implements ContactCVSService {

	@Autowired
	private DisasterPlanDao disasterPlanDao;

	@Autowired
	private EmployeeDao employeeDao;

	@Autowired
	ContactEventPersonDao contactEventPersonDao;

	@Autowired
	private StageDao stageDao;

	@Autowired
	private PersonInfoDao personInfoDao;

	@Autowired
	private ContactCVSDao contactCVSDao;

	@Autowired
	private ContactNarrLogAppendDao contactNarrLogAppendDao;

	@Autowired
	private CaseSummaryDao caseSummaryDao;

	@Autowired
	private ContactCVSPrefillData contactCVSPrefillData;

	@Autowired
	EventDao eventDao;
	
	@Autowired
	StageClosureEventService stageClosureEventService;

	@Autowired
	JSONUtil jsonUtil;

	public ContactCVSServiceImpl() {
		super();
	}

	/**
	 * Method Name: getContactCVS Method Description: Populate MONTHLY
	 * EVALUATION FORM
	 * 
	 * @param cpsInvConclValReq
	 * @return PreFillDataServiceDto @
	 */
	@Override
	@Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED, rollbackFor = {
			Exception.class })
	public PreFillDataServiceDto getContactCVS(CpsInvConclValReq cpsInvConclValReq) {

		ContactCVSDto contactCVSDto = new ContactCVSDto();
		Long ulIdCaseworker = null;
		EmployeePersPhNameDto employeePersPhNameDtoWorker = null;
		List<ContactNarrLogPerDateDto> contactInfoList = new ArrayList<ContactNarrLogPerDateDto>();
		List<ContactNarrLogPerDateDto> nameInfoList = new ArrayList<ContactNarrLogPerDateDto>();
		List<PersonDoDto> personDoDtoList = new ArrayList<PersonDoDto>();
		List<ContactPersonDto> contactChildList = new ArrayList<ContactPersonDto>();
		List<ContactPersonDto> contactParentList = new ArrayList<ContactPersonDto>();
		List<ContactNarrGuideDto> contactNarrGuideList = new ArrayList<ContactNarrGuideDto>();
		List<ContactNarrLogPerDateDto> contactInfoListTemp = new ArrayList<ContactNarrLogPerDateDto>();
		List<ContactNarrLogPerDateDto> nameInfoListTemp = new ArrayList<ContactNarrLogPerDateDto>();
		StageClosureEventReq stageClosureEventReq = populateStageClosureEventReq(cpsInvConclValReq.getIdEvent(), cpsInvConclValReq.getIdCase(), cpsInvConclValReq.getIdStage(),cpsInvConclValReq.getUserId());
		// calling the service to get the contact detail
		ConGuideFetchOutDto conGuideFetchOutDto =	stageClosureEventService.getContactDetailCFRes(stageClosureEventReq.getConGuideFetchInDto());
		contactCVSDto.setConGuideFetchOutDto(conGuideFetchOutDto);
		// List<ContactDetailDto> contactedListDB =
		// (ArrayList<ContactDetailDto>) JSONUtil
		// .jsonStringToObjectList(cpsInvConclValReq.getHiddenField(),
		// ContactDetailDto.class);

		@SuppressWarnings({ "unchecked", "static-access" })
		List<ContactDetailDto> contactedListDB = (ArrayList<ContactDetailDto>) JSONUtil.jsonStringToObjectList(
				JSONUtil.jsonToDecodedString(cpsInvConclValReq.getHiddenField()),
				ContactDetailDto.class);

		/* CALL DAM: CSEC02D AND RETURN CASE NAME AND CASE NUMBER */

		GenericCaseInfoDto genCaseInfoDto = disasterPlanDao.getGenericCaseInfo(cpsInvConclValReq.getIdStage());

		/* CALL DAM: CSYS11D FOR SUMMARY BEGIN/END DATES */
		ContactDetailsOutDto contactDetailsOutDto = new ContactDetailsOutDto();
		contactDetailsOutDto.setUlIdEvent(cpsInvConclValReq.getIdEvent().intValue());
		contactDetailsOutDto.setUlIdStage(cpsInvConclValReq.getIdStage().intValue());
		StageProgramDto stageProgramDto = contactEventPersonDao.getContactDetails(contactDetailsOutDto);

		/* retrieve stage_person_link CallCCMN19D */
		StagePersonDto stagePersonDto = stageDao.getStagePersonLinkDetails(cpsInvConclValReq.getIdStage(),
				ServiceConstants.PRIMARY_ROLE_STAGE_OPEN);

		/* Caseworker will be used as input to CCMN60D */
		ulIdCaseworker = stagePersonDto.getIdTodoPersWorker();

		/* retrieves worker info (callCSEC01D) for CallCCMN19D */
		if (!ObjectUtils.isEmpty(ulIdCaseworker)) {
			employeePersPhNameDtoWorker = employeeDao.searchPersonPhoneName(ulIdCaseworker);
		}

		/* Call DAM CCMN60D - supervisor name */
		if (!ObjectUtils.isEmpty(ulIdCaseworker)) {
			PersonDiDto personDiDto = new PersonDiDto();
			personDiDto.setIdPerson(ulIdCaseworker);
			personDoDtoList = personInfoDao.getPersonName(personDiDto);
		}

		if (!ObjectUtils.isEmpty(stageProgramDto.getDtMntlsumBg())) {
			/* Call person DAM for principal children CallCLSCG5D */
			contactChildList = contactCVSDao.getChildInfo(stageProgramDto.getDtMntlsumBg(),
					cpsInvConclValReq.getIdCase());

			/*
			 * Call person DAM for principal parent CallCLSCG6D : main structure
			 * of this dao has been changed according to ADS change
			 */
			contactParentList = contactCVSDao.getParentInfo(stageProgramDto.getDtMntlsumBg(),
					cpsInvConclValReq.getIdCase(), contactedListDB);
		}

		List<Long> contactNarrGuideIdsList;
		List<ContactGuideDto> contactGuideList = new ArrayList<ContactGuideDto>() ;
		if (!ObjectUtils.isEmpty(stageProgramDto.getDtMntlsumBg())
				&& !ObjectUtils.isEmpty(stageProgramDto.getDtCntMntSumEnd())) {

			/* Call contact_guide_narr DAM CLSCG7D */
			contactNarrGuideList = contactNarrLogAppendDao.getContactGuide(cpsInvConclValReq.getIdCase(),
					stageProgramDto.getDtMntlsumBg(), stageProgramDto.getDtCntMntSumEnd());
			if (!ObjectUtils.isEmpty(contactNarrGuideList)){
				contactNarrGuideIdsList = contactNarrGuideList.stream().map(dto -> {
					return dto.getIdContactGuideNarr();
				}).collect(Collectors.toList());
				/* Call contact_guide_topic DAM CallCLSCG8D */
				contactGuideList = contactCVSDao.getContactInfo(cpsInvConclValReq.getIdCase(),contactNarrGuideIdsList);
			}
		}
		
		// CallCLSCGGD
		List<ContactEventDto> contactEventList = contactCVSDao.getEventInfo(cpsInvConclValReq.getIdCase());

		/* Call stage DAM for contacts CallCCMN15D */
		List<CaseStageSummaryDto> caseSummaryList = caseSummaryDao.getCaseStageCPSInfo(cpsInvConclValReq.getIdCase());

		/* allocate temp output records for contacts */
		if (!ObjectUtils.isEmpty(caseSummaryList)) {
			for (CaseStageSummaryDto caseStageSummary : caseSummaryList) {
				if (ServiceConstants.CSTAGES_FRE.equals(caseStageSummary.getCdStage())
						|| ServiceConstants.CSTAGES_FSU.equals(caseStageSummary.getCdStage())
						|| ServiceConstants.CSTAGES_SUB.equals(caseStageSummary.getCdStage())) {

					if (!ObjectUtils.isEmpty(stageProgramDto.getDtMntlsumBg())
							&& !ObjectUtils.isEmpty(stageProgramDto.getDtCntMntSumEnd())) {

						/* Call DAM for contacts CallCLSCD5D */

						contactInfoListTemp = contactNarrLogAppendDao.getContactInfo(caseStageSummary.getIdStage(),
								stageProgramDto.getDtMntlsumBg(), stageProgramDto.getDtCntMntSumEnd());
						if (!ObjectUtils.isEmpty(contactInfoListTemp)) {
							for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactInfoListTemp) {
								contactInfoList.add(contactNarrLogPerDateDto);
							}
						}

						/* Call DAM for contacts CallCLSCD6D */

						nameInfoListTemp = contactNarrLogAppendDao.getNameContact(caseStageSummary.getIdStage(),
								stageProgramDto.getDtMntlsumBg(), stageProgramDto.getDtCntMntSumEnd());
						if (!ObjectUtils.isEmpty(nameInfoListTemp)) {
							for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : nameInfoListTemp) {
								nameInfoList.add(contactNarrLogPerDateDto);
							}
						}
					}
				}
			}
		}
		//get all contact details for each principals/collaterals selected for this contact
		if (!CollectionUtils.isEmpty(contactInfoList)) {
			List<ConGuideFetchOutDto> conGuideFetchOutDtos = new ArrayList<ConGuideFetchOutDto>();
			for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactInfoList) {
				StageClosureEventReq stageClrEventReq = populateStageClosureEventReq(contactNarrLogPerDateDto.getIdEvent(), cpsInvConclValReq.getIdCase(), cpsInvConclValReq.getIdStage(),cpsInvConclValReq.getUserId());
				// calling the service to get the contact detail
				ConGuideFetchOutDto conGuideFetchDto =	stageClosureEventService.getContactDetailCFRes(stageClrEventReq.getConGuideFetchInDto());
				conGuideFetchOutDtos.add(conGuideFetchDto);
			}
			contactCVSDto.setConGuideFetchOutDtos(conGuideFetchOutDtos);
		}
		EventReq eventReq = new EventReq();
		eventReq.setUlIdStage(cpsInvConclValReq.getIdStage());
		eventReq.setUlIdCase(cpsInvConclValReq.getIdCase());

		contactCVSDto.setGenCaseInfoDto(genCaseInfoDto);
		contactCVSDto.setCaseSummaryList(caseSummaryList);
		contactCVSDto.setContactChildList(contactChildList);
		contactCVSDto.setContactParentList(contactParentList);
		contactCVSDto.setContactEventList(contactEventList);
		contactCVSDto.setContactGuideList(contactGuideList);
		contactCVSDto.setContactInfoList(contactInfoList);
		contactCVSDto.setContactNarrGuideList(contactNarrGuideList);
		contactCVSDto.setEmployeePersPhNameDto(employeePersPhNameDtoWorker);
		contactCVSDto.setPersonDoDtoList(personDoDtoList);
		contactCVSDto.setStageProgramDto(stageProgramDto);
		contactCVSDto.setNameInfoList(nameInfoList);
		contactCVSDto.setStagePersonDto(stagePersonDto);
		contactCVSDto.setContactDetailList(contactedListDB);
		contactCVSDto.setTxtNmPersonFull(cpsInvConclValReq.getTxtNmPersonFull());

		return contactCVSPrefillData.returnPrefillData(contactCVSDto);
	}

	/**
	 * 
	 *Method Name:	populateStageClosureEventReq
	 *Method Description:
	 *@param idEvent
	 *@param contactFormDto
	 *@return
	 */
	private StageClosureEventReq populateStageClosureEventReq(Long idEvent, Long idCase, Long idStage,String idUser) {
		StageClosureEventReq stageClosureEventReq = new StageClosureEventReq();
		ConGuideFetchInDto conGuideFetchInDto = new ConGuideFetchInDto();

		stageClosureEventReq.setUserId(idUser);
		conGuideFetchInDto.setIdCase(ObjectUtils.isEmpty(idCase) ? ServiceConstants.ZERO_VAL
				: (long) idCase);
		conGuideFetchInDto.setIdEvent(idEvent);
		conGuideFetchInDto.setIdStage(idStage);
		conGuideFetchInDto.setNmTable("CONTACT_NARRATIVE");
		ServiceInputDto serviceInputDto = new ServiceInputDto();
		serviceInputDto.setUlPageSizeNbr(50);
		serviceInputDto.setUsPageNbr(1);
		stageClosureEventReq.setConGuideFetchInDto(conGuideFetchInDto);
		stageClosureEventReq.getConGuideFetchInDto().setServiceInputDto(serviceInputDto);

		return stageClosureEventReq;
	}
}
