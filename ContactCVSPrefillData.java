package us.tx.state.dfps.service.forms.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.service.admin.dto.PersonDoDto;
import us.tx.state.dfps.service.casepackage.dto.CaseStageSummaryDto;
import us.tx.state.dfps.service.common.BookmarkConstants;
import us.tx.state.dfps.service.common.CodesConstant;
import us.tx.state.dfps.service.common.FormConstants;
import us.tx.state.dfps.service.common.FormGroupsConstants;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.util.DateUtils;
import us.tx.state.dfps.service.common.util.TypeConvUtil;
import us.tx.state.dfps.service.contact.dto.ContactCVSDto;
import us.tx.state.dfps.service.contact.dto.ContactEventDto;
import us.tx.state.dfps.service.contact.dto.ContactGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactNarrGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactNarrLogPerDateDto;
import us.tx.state.dfps.service.contact.dto.ContactPersonDto;
import us.tx.state.dfps.service.forms.dto.BlobDataDto;
import us.tx.state.dfps.service.forms.dto.BookmarkDto;
import us.tx.state.dfps.service.forms.dto.FormDataGroupDto;
import us.tx.state.dfps.service.forms.dto.PreFillDataServiceDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutDto;
import us.tx.state.dfps.xmlstructs.outputstructs.ConGuideFetchOutRowDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class Description:
 * ContactCVSPrefillData will implement returnPrefillData operation defined in
 * DocumentServiceUtil Interface to populate the prefill data for Form CVSEVAL
 * Mar 16, 2018- 5:14:11 PM © 2017 Texas Department of Family and Protective
 * Services
 */
@Component
public class ContactCVSPrefillData extends DocumentServiceUtil {

	public ContactCVSPrefillData() {
		super();
	}

	/**
	 * Method Description: This method is used to prefill the data from the
	 * different Dao by passing Dao output Dtos and bookmark and form group
	 * bookmark Dto as objects as input request
	 * 
	 * @param parentDtoobj
	 * @param bookmarkDtoObj
	 * @return PreFillData
	 * 
	 */
	@Override
	public PreFillDataServiceDto returnPrefillData(Object parentDtoobj) {

		ContactCVSDto contactCVSDto = (ContactCVSDto) parentDtoobj;

		if (ObjectUtils.isEmpty(contactCVSDto.getCaseSummaryList())) {
			contactCVSDto.setCaseSummaryList(new ArrayList<CaseStageSummaryDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactChildList())) {
			contactCVSDto.setContactChildList(new ArrayList<ContactPersonDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactParentList())) {
			contactCVSDto.setContactParentList(new ArrayList<ContactPersonDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactGuideList())) {
			contactCVSDto.setContactGuideList(new ArrayList<ContactGuideDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactEventList())) {
			contactCVSDto.setContactEventList(new ArrayList<ContactEventDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactInfoList())) {
			contactCVSDto.setContactInfoList(new ArrayList<ContactNarrLogPerDateDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getNameInfoList())) {
			contactCVSDto.setNameInfoList(new ArrayList<ContactNarrLogPerDateDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactNarrGuideList())) {
			contactCVSDto.setContactNarrGuideList(new ArrayList<ContactNarrGuideDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getPersonDoDtoList())) {
			contactCVSDto.setPersonDoDtoList(new ArrayList<PersonDoDto>());
		}
		if (ObjectUtils.isEmpty(contactCVSDto.getContactDetailList())) {
			contactCVSDto.setContactDetailList(new ArrayList<>());
		}

		List<FormDataGroupDto> formDataGroupList = new ArrayList<FormDataGroupDto>();

		// set the prefill data for group eval07

		for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactCVSDto.getContactInfoList()) {
			if (!ServiceConstants.CCNTCTYP_GKIN.equals(contactNarrLogPerDateDto.getCdContactType())
					&& !ServiceConstants.CCNTCTYP_GKNS.equals(contactNarrLogPerDateDto.getCdContactType())
					&& !ServiceConstants.GCME.equals(contactNarrLogPerDateDto.getCdContactType())
					&& !ServiceConstants.BCME.equals(contactNarrLogPerDateDto.getCdContactType())
					&& !StringUtils.isEmpty(contactNarrLogPerDateDto.getCdContactPurpose())
					&& !(contactNarrLogPerDateDto.getCdContactPurpose().contains(ServiceConstants.FACE_TO_FACE)|| ServiceConstants.CCNTPURP_GCMR.equalsIgnoreCase(contactNarrLogPerDateDto.getCdContactPurpose()))) {
				FormDataGroupDto tempContactFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_CONTACT,
						FormConstants.EMPTY_STRING);
				List<BookmarkDto> bookmarkContactInfoList = new ArrayList<BookmarkDto>();
				List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();

				BookmarkDto bookmarkIndSiblingVisit = createBookmark(BookmarkConstants.IND_SIBLING_VISIT,
						(!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIndSiblingVisit()) && ServiceConstants.Y
								.equalsIgnoreCase(contactNarrLogPerDateDto.getIndSiblingVisit()))
										? ServiceConstants.YES_TEXT : ServiceConstants.NO_TEXT);
				BookmarkDto bookmarkDTContactOccurred = createBookmark(BookmarkConstants.CON_DATE,
						DateUtils.stringDt(contactNarrLogPerDateDto.getDtContactOccurred()));

				 /**ALM Defect 14943- CVS ME narrative displays wrong staff
				 * Wrong Name was displayed for Contacted by field, changed Dto object name
				 * Created a new method to append full Name as per requirement  ***/
				String fullName=getFullName(contactNarrLogPerDateDto.getNmNameLast(),contactNarrLogPerDateDto.getNmNameFirst(),
						 contactNarrLogPerDateDto.getNmNameMiddle());
				 BookmarkDto bookmarkContactedBy =createBookmark(BookmarkConstants.CON_BY,fullName);						
				
				BookmarkDto bookmarkCdContactLocation = createBookmarkWithCodesTable(BookmarkConstants.CON_LOCATION,
						contactNarrLogPerDateDto.getCdContactLocation(), CodesConstant.CCNCTLOC);
				BookmarkDto bookmarkCdContactMethod = createBookmarkWithCodesTable(BookmarkConstants.CON_METHOD,
						contactNarrLogPerDateDto.getCdContactMethod(), CodesConstant.CCNTMETH);
				BookmarkDto bookmarkCdContactPurpose = createBookmarkWithCodesTable(BookmarkConstants.CON_PURPOSE,
						contactNarrLogPerDateDto.getCdContactPurpose(), CodesConstant.CCNTPURP);
				BookmarkDto bookmarkCdContactType = createBookmarkWithCodesTable(BookmarkConstants.CON_TYPE,
						contactNarrLogPerDateDto.getCdContactType(), CodesConstant.CCNTCTYP);

				BookmarkDto bookmarkIndAtt = createBookmark(BookmarkConstants.CON_ATT,
						contactNarrLogPerDateDto.getIndAtt());
				BookmarkDto bookmarkIndNarr = createBookmark(BookmarkConstants.CON_NARR,
						contactNarrLogPerDateDto.getIndNarr());
				BookmarkDto bookmarktmScrTmGeneric2 = createBookmark(BookmarkConstants.CON_TIME,
						DateUtils.getTime(contactNarrLogPerDateDto.getDtContactOccurred()));
				BookmarkDto bookmarkulIdEvent = createBookmark(BookmarkConstants.CON_ID,
						contactNarrLogPerDateDto.getIdEvent());
				BlobDataDto bookmarkBlobIdEvent = createBlobData(BookmarkConstants.CONTACT_NARRATIVE,
						CodesConstant.CONTACT_NARRATIVE, contactNarrLogPerDateDto.getIdEvent().toString());

				bookmarkContactInfoList.add(bookmarkIndSiblingVisit);
				bookmarkContactInfoList.add(bookmarkDTContactOccurred);

				bookmarkContactInfoList.add(bookmarkContactedBy);

				bookmarkContactInfoList.add(bookmarkCdContactLocation);
				bookmarkContactInfoList.add(bookmarkCdContactMethod);
				bookmarkContactInfoList.add(bookmarkCdContactPurpose);
				bookmarkContactInfoList.add(bookmarkCdContactType);

				bookmarkContactInfoList.add(bookmarkIndAtt);
				bookmarkContactInfoList.add(bookmarkIndNarr);
				bookmarkContactInfoList.add(bookmarktmScrTmGeneric2);
				bookmarkContactInfoList.add(bookmarkulIdEvent);

				bookmarkBlobDataList.add(bookmarkBlobIdEvent);

				tempContactFrmDataGrp.setBookmarkDtoList(bookmarkContactInfoList);
				tempContactFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
				formDataGroupList.add(tempContactFrmDataGrp);

				List<FormDataGroupDto> contactFrmDataGrpList = new ArrayList<FormDataGroupDto>();

				// set the prefill data for group eval26 : subgroup of eval07
				if (ServiceConstants.STRING_IND_Y.equals(contactNarrLogPerDateDto.getIndClientTime())) {
					FormDataGroupDto tempContactclientFrmDataGrp = createFormDataGroup(
							FormGroupsConstants.TMPLAT_CONTACT_CLIENTTIME, FormGroupsConstants.TMPLAT_CONTACT);
					List<BookmarkDto> bookmarkContactInfoList2 = new ArrayList<BookmarkDto>();
					BookmarkDto bookmarkHours = createBookmark(BookmarkConstants.CONTACT_CLIENTTIME_HRS,
							!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getNbrHours())? contactNarrLogPerDateDto.getNbrHours() : ServiceConstants.STR_ZERO_VAL);
					BookmarkDto bookmarkMins = createBookmark(BookmarkConstants.CONTACT_CLIENTTIME_MIN,
							contactNarrLogPerDateDto.getNbrMins());
					bookmarkContactInfoList2.add(bookmarkHours);
					bookmarkContactInfoList2.add(bookmarkMins);
					tempContactclientFrmDataGrp.setBookmarkDtoList(bookmarkContactInfoList2);
					contactFrmDataGrpList.add(tempContactclientFrmDataGrp);
				}

				// set the prefill data for group eval08 : subgroup of eval07

				for (ContactNarrLogPerDateDto contactNarrLogPerDateDtoN : contactCVSDto.getNameInfoList()) {

					if (!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIdEvent())
							&& contactNarrLogPerDateDto.getIdEvent().equals(contactNarrLogPerDateDtoN.getIdEvent())) {
						FormDataGroupDto tempNamesFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_NAMES,
								FormGroupsConstants.TMPLAT_CONTACT);
						List<BookmarkDto> bookmarkNameInfoList = new ArrayList<BookmarkDto>();
						BookmarkDto bookmarkNameFirst = createBookmark(BookmarkConstants.CON_NAMES,
								contactNarrLogPerDateDtoN.getNmNameFirst());
						bookmarkNameInfoList.add(bookmarkNameFirst);
						tempNamesFrmDataGrp.setBookmarkDtoList(bookmarkNameInfoList);
						contactFrmDataGrpList.add(tempNamesFrmDataGrp);
					}

				}

				tempContactFrmDataGrp.setFormDataGroupList(contactFrmDataGrpList);
			}
		}

		// set the prefill data for group eval09
		for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactCVSDto.getContactInfoList()) {
			if (!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getCdContactPurpose())
					&& (contactNarrLogPerDateDto.getCdContactPurpose().contains(ServiceConstants.FACE_TO_FACE)
							|| ServiceConstants.CCNTPURP_GCMR.equalsIgnoreCase(contactNarrLogPerDateDto.getCdContactPurpose()))) {
				FormDataGroupDto tempContactFTFFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_FTF,
						FormConstants.EMPTY_STRING);
				List<BookmarkDto> bookmarkContactInfoList = new ArrayList<BookmarkDto>();

				BookmarkDto bookmarkDTContactOccurred = createBookmark(BookmarkConstants.FTF_CON_DATE,
						DateUtils.stringDt(contactNarrLogPerDateDto.getDtContactOccurred()));
				BookmarkDto bookmarkCdContactLocation = createBookmarkWithCodesTable(BookmarkConstants.FTF_CON_LOCATION,
						contactNarrLogPerDateDto.getCdContactLocation(), CodesConstant.CCNCTLOC);
				BookmarkDto bookmarkCdContactMethod = createBookmarkWithCodesTable(BookmarkConstants.FTF_CON_METHOD,
						contactNarrLogPerDateDto.getCdContactMethod(), CodesConstant.CCNTMETH);
				BookmarkDto bookmarkCdContactPurpose = createBookmarkWithCodesTable(BookmarkConstants.FTF_CON_PURPOSE,
						contactNarrLogPerDateDto.getCdContactPurpose(), CodesConstant.CCNTPURP);
				String contactPurpose = !StringUtils.isEmpty(bookmarkCdContactPurpose.getBookmarkData())
						? ServiceConstants.B_TAG_START + bookmarkCdContactPurpose.getBookmarkData()
								+ ServiceConstants.B_TAG_END
						: FormConstants.EMPTY_STRING;
				bookmarkCdContactPurpose.setBookmarkData(contactPurpose);
				BookmarkDto bookmarkCdContactType = createBookmarkWithCodesTable(BookmarkConstants.FTF_CON_TYPE,
						contactNarrLogPerDateDto.getCdContactType(), CodesConstant.CCNTCTYP);

				BookmarkDto bookmarkIndAtt = createBookmark(BookmarkConstants.FTF_CON_ATT,
						contactNarrLogPerDateDto.getIndAtt());
				BookmarkDto bookmarkIndNarr = createBookmark(BookmarkConstants.FTF_CON_NARR,
						contactNarrLogPerDateDto.getIndNarr());
				BookmarkDto bookmarktmScrTmGeneric2 = createBookmark(BookmarkConstants.FTF_CON_TIME,
						DateUtils.getTime(contactNarrLogPerDateDto.getDtContactOccurred()));
				BookmarkDto bookmarkulIdEvent = createBookmark(BookmarkConstants.FTF_CON_ID,
						contactNarrLogPerDateDto.getIdEvent());

				bookmarkContactInfoList.add(bookmarkDTContactOccurred);
				bookmarkContactInfoList.add(bookmarkCdContactLocation);
				bookmarkContactInfoList.add(bookmarkCdContactMethod);
				bookmarkContactInfoList.add(bookmarkCdContactPurpose);
				bookmarkContactInfoList.add(bookmarkCdContactType);

				bookmarkContactInfoList.add(bookmarkIndAtt);
				bookmarkContactInfoList.add(bookmarkIndNarr);
				bookmarkContactInfoList.add(bookmarktmScrTmGeneric2);
				bookmarkContactInfoList.add(bookmarkulIdEvent);

				tempContactFTFFrmDataGrp.setBookmarkDtoList(bookmarkContactInfoList);
				formDataGroupList.add(tempContactFTFFrmDataGrp);

				List<FormDataGroupDto> contactFTFFrmDataGrpList = new ArrayList<FormDataGroupDto>();

				// set the prefill data for group eval27 : subgroup of eval09
				if (ServiceConstants.STRING_IND_Y.equals(contactNarrLogPerDateDto.getIndClientTime())) {
					FormDataGroupDto tempContactclientFrmDataGrp = createFormDataGroup(
							FormGroupsConstants.TMPLAT_FTF_CLIENTTIME, FormGroupsConstants.TMPLAT_FTF);
					List<BookmarkDto> bookmarkContactInfoList2 = new ArrayList<BookmarkDto>();
					BookmarkDto bookmarkIndSiblingVisit = createBookmark(BookmarkConstants.IND_SIBLING_VISIT,
							(!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIndSiblingVisit()) && ServiceConstants.Y
									.equalsIgnoreCase(contactNarrLogPerDateDto.getIndSiblingVisit()))
											? ServiceConstants.YES_TEXT : ServiceConstants.NO_TEXT);
					BookmarkDto bookmarkHours = createBookmark(BookmarkConstants.CONTACT_FTF_HRS,
							!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getNbrHours())? contactNarrLogPerDateDto.getNbrHours() : ServiceConstants.STR_ZERO_VAL);
					BookmarkDto bookmarkMins = createBookmark(BookmarkConstants.CONTACT_FTF_MIN,
							contactNarrLogPerDateDto.getNbrMins());
					bookmarkContactInfoList2.add(bookmarkHours);
					bookmarkContactInfoList2.add(bookmarkMins);
					bookmarkContactInfoList2.add(bookmarkIndSiblingVisit);

					tempContactclientFrmDataGrp.setBookmarkDtoList(bookmarkContactInfoList2);
					contactFTFFrmDataGrpList.add(tempContactclientFrmDataGrp);
				}

				// set the prefill data for group eval10 : subgroup of eval09

				for (ContactNarrLogPerDateDto contactNarrLogPerDateDtoN : contactCVSDto.getNameInfoList()) {

					if (!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIdEvent())
							&& contactNarrLogPerDateDto.getIdEvent().equals(contactNarrLogPerDateDtoN.getIdEvent())) {
						FormDataGroupDto tempNamesFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_FTF_NAMES,
								FormGroupsConstants.TMPLAT_FTF);
						List<BookmarkDto> bookmarkNameInfoList = new ArrayList<>();
						BookmarkDto bookmarkNameFirst = createBookmark(BookmarkConstants.FTF_CON_NAMES,
								contactNarrLogPerDateDtoN.getNmNameFirst());
						bookmarkNameInfoList.add(bookmarkNameFirst);
						tempNamesFrmDataGrp.setBookmarkDtoList(bookmarkNameInfoList);
						contactFTFFrmDataGrpList.add(tempNamesFrmDataGrp);
					}

				}

				// set the prefill data for group eval19 : subgroup of eval09

				for (ContactNarrGuideDto contactNarrGuideDto : contactCVSDto.getContactNarrGuideList()) {

					if (!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIdEvent())
							&& contactNarrLogPerDateDto.getIdEvent().equals(contactNarrGuideDto.getIdEvent())
							&& ServiceConstants.COLLATERAL.equals(contactNarrGuideDto.getCdType())) {
						FormDataGroupDto tempNamesFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_COLL_NARR,
								FormGroupsConstants.TMPLAT_FTF);
						List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();
						BlobDataDto bookmarkBlobIdContactGuideNarr = createBlobData(BookmarkConstants.COLL_NARR,
								CodesConstant.CONTACT_GUIDE_NARR_VIEW,
								contactNarrGuideDto.getIdContactGuideNarr().toString());
						bookmarkBlobDataList.add(bookmarkBlobIdContactGuideNarr);
						tempNamesFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
						contactFTFFrmDataGrpList.add(tempNamesFrmDataGrp);
					}

				}

				// set the prefill data for group eval17 : subgroup of eval09

				for (ContactNarrGuideDto contactNarrGuideDto : contactCVSDto.getContactNarrGuideList()) {

					if (contactNarrLogPerDateDto.getIdEvent().equals(contactNarrGuideDto.getIdEvent())
							&& ServiceConstants.CGTXTTYP_CGVR.equals(contactNarrGuideDto.getCdType())) {
						FormDataGroupDto tempNamesFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_CAREGIVER_NARR, FormGroupsConstants.TMPLAT_FTF);
						List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();
						BlobDataDto bookmarkBlobIdContactGuideNarr = createBlobData(BookmarkConstants.CAREGIVER_NARR,
								CodesConstant.CONTACT_GUIDE_NARR_VIEW,
								contactNarrGuideDto.getIdContactGuideNarr().toString());
						bookmarkBlobDataList.add(bookmarkBlobIdContactGuideNarr);
						tempNamesFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
						contactFTFFrmDataGrpList.add(tempNamesFrmDataGrp);

						List<FormDataGroupDto> contactGuideTopicFrmDataGrpList = new ArrayList<FormDataGroupDto>();

						// set the prefill data for group eval18 : subgroup of
						// eval17

						for (ContactGuideDto contactGuideDto : contactCVSDto.getContactGuideList()) {
							if (!ObjectUtils.isEmpty(contactGuideDto.getIdContactGuideNarr()) && contactGuideDto
									.getIdContactGuideNarr().equals(contactNarrGuideDto.getIdContactGuideNarr())) {
								FormDataGroupDto tempGuideTopicFrmDataGrp = createFormDataGroup(
										FormGroupsConstants.TMPLAT_CAREGIVER_TOPICS,
										FormGroupsConstants.TMPLAT_CAREGIVER_NARR);
								List<BookmarkDto> bookmarkGuideTopicList = new ArrayList<>();
								BookmarkDto bookmarkGuideTopic = createBookmarkWithCodesTable(
										BookmarkConstants.CAREGIVER_TOPIC, contactGuideDto.getCdGuideTopic(),
										CodesConstant.CGTOPICS);
								bookmarkGuideTopicList.add(bookmarkGuideTopic);
								tempGuideTopicFrmDataGrp.setBookmarkDtoList(bookmarkGuideTopicList);
								contactGuideTopicFrmDataGrpList.add(tempGuideTopicFrmDataGrp);
							}
						}

						tempNamesFrmDataGrp.setFormDataGroupList(contactGuideTopicFrmDataGrpList);
					}

				}

				// set the prefill data for group eval11 : subgroup of eval09

				for (ContactNarrGuideDto contactNarrGuideDto : contactCVSDto.getContactNarrGuideList()) {

					if (contactNarrLogPerDateDto.getIdEvent().equals(contactNarrGuideDto.getIdEvent())
							&& !ObjectUtils.isEmpty(contactNarrGuideDto.getCdGuideRole())
							&& contactNarrGuideDto.getCdGuideRole().contains(ServiceConstants.IN_PROCESS)) {
						FormDataGroupDto tempGuideRoleFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_CHILD_NARR, FormGroupsConstants.TMPLAT_FTF);
						List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();
						List<BookmarkDto> bookmarkGuideTopicList = new ArrayList<BookmarkDto>();

						BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(
								BookmarkConstants.NM_CHILD_SUFFIX, contactNarrGuideDto.getCdNameSuffix(),
								CodesConstant.CSUFFIX);

						BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_CHILD_FIRST,
								contactNarrGuideDto.getNmNameFirst());
						BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_CHILD_LAST,
								contactNarrGuideDto.getNmNameLast());
						BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_CHILD_MIDDLE,
								contactNarrGuideDto.getNmNameMiddle());
						BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_CHILD_FULLNAME,
								TypeConvUtil.formatFullName(contactNarrGuideDto.getNmNameFirst(),contactNarrGuideDto.getNmNameMiddle(),contactNarrGuideDto.getNmNameLast()));

						BlobDataDto bookmarkBlobIdContactGuideNarr = createBlobData(BookmarkConstants.CHILD_NARR,
								CodesConstant.CONTACT_GUIDE_NARR_VIEW,
								contactNarrGuideDto.getIdContactGuideNarr().toString());
						bookmarkGuideTopicList.add(bookmarkNmNameFull);
						bookmarkGuideTopicList.add(bookmarkCdNameSuffix);
						bookmarkGuideTopicList.add(bookmarkNmNameFirst);
						bookmarkGuideTopicList.add(bookmarkNmNameLast);
						bookmarkGuideTopicList.add(bookmarkNmNameMiddle);
						tempGuideRoleFrmDataGrp.setBookmarkDtoList(bookmarkGuideTopicList);

						bookmarkBlobDataList.add(bookmarkBlobIdContactGuideNarr);
						tempGuideRoleFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
						contactFTFFrmDataGrpList.add(tempGuideRoleFrmDataGrp);

						List<FormDataGroupDto> contactChildFrmDataGrpList = new ArrayList<FormDataGroupDto>();

						// set the prefill data for group eval12 : subgroup of
						// eval11

						if (!ObjectUtils.isEmpty(contactNarrGuideDto.getCdNameSuffix())) {
							FormDataGroupDto tempCommaChildFrmDataGrpDtoWorker = createFormDataGroup(
									FormGroupsConstants.TMPLAT_NM_CHILD_COMMA2, FormGroupsConstants.TMPLAT_CHILD_NARR);
							contactChildFrmDataGrpList.add(tempCommaChildFrmDataGrpDtoWorker);
						}

						// set the prefill data for group eval13 : subgroup of
						// eval11

						for (ContactGuideDto contactGuideDto : contactCVSDto.getContactGuideList()) {
							if (contactGuideDto.getIdContactGuideNarr().equals(contactNarrGuideDto
									.getIdContactGuideNarr())) {
								FormDataGroupDto tempGuideTopicFrmDataGrp = createFormDataGroup(
										FormGroupsConstants.TMPLAT_CHILD_TOPICS, FormGroupsConstants.TMPLAT_CHILD_NARR);
								List<BookmarkDto> bookmarkTopicList = new ArrayList<BookmarkDto>();
								BookmarkDto bookmarkGuideTopic = createBookmarkWithCodesTable(
										BookmarkConstants.CHILD_TOPIC, contactGuideDto.getCdGuideTopic(),
										CodesConstant.CGTOPICS);
								bookmarkTopicList.add(bookmarkGuideTopic);
								tempGuideTopicFrmDataGrp.setBookmarkDtoList(bookmarkTopicList);
								contactChildFrmDataGrpList.add(tempGuideTopicFrmDataGrp);
							}
						}

						tempGuideRoleFrmDataGrp.setFormDataGroupList(contactChildFrmDataGrpList);

					}

				}
				// set the prefill data for group eval14 : subgroup of eval09

				for (ContactNarrGuideDto contactNarrGuideDto : contactCVSDto.getContactNarrGuideList()) {

					if (contactNarrLogPerDateDto.getIdEvent().equals(contactNarrGuideDto.getIdEvent())
							&& !ObjectUtils.isEmpty(contactNarrGuideDto.getCdGuideRole())
							&& contactNarrGuideDto.getCdGuideRole().contains(ServiceConstants.CGPROLE_020)) {
						FormDataGroupDto tempGuideRoleFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_PARENT_NARR, FormGroupsConstants.TMPLAT_FTF);
						List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();
						List<BookmarkDto> bookmarkGuideTopicList = new ArrayList<BookmarkDto>();

						BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(
								BookmarkConstants.NM_PARENT_SUFFIX, contactNarrGuideDto.getCdNameSuffix(),
								CodesConstant.CSUFFIX);

						BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_PARENT_FIRST,
								contactNarrGuideDto.getNmNameFirst());
						BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_PARENT_LAST,
								contactNarrGuideDto.getNmNameLast());
						BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_PARENT_MIDDLE,
								contactNarrGuideDto.getNmNameMiddle());
						BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_FULL_PARENT,
								TypeConvUtil.formatFullName(contactNarrGuideDto.getNmNameFirst(),contactNarrGuideDto.getNmNameMiddle(),contactNarrGuideDto.getNmNameLast()));

						BlobDataDto bookmarkBlobIdContactGuideNarr = createBlobData(FormGroupsConstants.PARENT_NARR,
								CodesConstant.CONTACT_GUIDE_NARR_VIEW,
								contactNarrGuideDto.getIdContactGuideNarr().toString());
						bookmarkGuideTopicList.add(bookmarkNmNameFull);
						bookmarkGuideTopicList.add(bookmarkCdNameSuffix);
						bookmarkGuideTopicList.add(bookmarkNmNameFirst);
						bookmarkGuideTopicList.add(bookmarkNmNameLast);
						bookmarkGuideTopicList.add(bookmarkNmNameMiddle);
						tempGuideRoleFrmDataGrp.setBookmarkDtoList(bookmarkGuideTopicList);

						bookmarkBlobDataList.add(bookmarkBlobIdContactGuideNarr);
						tempGuideRoleFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
						contactFTFFrmDataGrpList.add(tempGuideRoleFrmDataGrp);

						List<FormDataGroupDto> contactChildFrmDataGrpList = new ArrayList<FormDataGroupDto>();

						// set the prefill data for group eval12 : subgroup of
						// eval11

						if (!ObjectUtils.isEmpty(contactNarrGuideDto.getCdNameSuffix())) {
							FormDataGroupDto tempCommaChildFrmDataGrpDtoWorker = createFormDataGroup(
									FormGroupsConstants.TMPLAT_NM_PARENT_COMMA2, FormGroupsConstants.TMPLAT_PARENT_NARR);
							contactChildFrmDataGrpList.add(tempCommaChildFrmDataGrpDtoWorker);
						}

						// set the prefill data for group eval13 : subgroup of
						// eval11

						for (ContactGuideDto contactGuideDto : contactCVSDto.getContactGuideList()) {
							if (contactGuideDto.getIdContactGuideNarr().equals(contactNarrGuideDto
									.getIdContactGuideNarr())) {
								FormDataGroupDto tempGuideTopicFrmDataGrp = createFormDataGroup(
										FormGroupsConstants.TMPLAT_PARENT_TOPICS, FormGroupsConstants.TMPLAT_PARENT_NARR);
								List<BookmarkDto> bookmarkTopicList = new ArrayList<BookmarkDto>();
								BookmarkDto bookmarkGuideTopic = createBookmarkWithCodesTable(
										FormGroupsConstants.PARENT_TOPIC, contactGuideDto.getCdGuideTopic(),
										CodesConstant.CGTOPICS);
								bookmarkTopicList.add(bookmarkGuideTopic);
								tempGuideTopicFrmDataGrp.setBookmarkDtoList(bookmarkTopicList);
								contactChildFrmDataGrpList.add(tempGuideTopicFrmDataGrp);
							}
						}

						tempGuideRoleFrmDataGrp.setFormDataGroupList(contactChildFrmDataGrpList);

					}

				}
				tempContactFTFFrmDataGrp.setFormDataGroupList(contactFTFFrmDataGrpList);
			}
		}

		// set the prefill data for group eval22

		for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactCVSDto.getContactInfoList()) {
			if (contactNarrLogPerDateDto.getCdContactType().startsWith(ServiceConstants.GK)) {
				FormDataGroupDto tempContactFrmDataGrp = createFormDataGroup(
						FormGroupsConstants.TMPLAT_KIN_NOTIF_CONTACT, FormConstants.EMPTY_STRING);
				List<BookmarkDto> bookmarkContactInfoList = new ArrayList<BookmarkDto>();
				BookmarkDto bookmarkDTContactOccurred = createBookmark(BookmarkConstants.CON_DATE,
						DateUtils.stringDt(contactNarrLogPerDateDto.getDtContactOccurred()));
				BookmarkDto bookmarkCdContactPurpose = createBookmarkWithCodesTable(BookmarkConstants.CON_PURPOSE,
						contactNarrLogPerDateDto.getCdContactPurpose(), CodesConstant.CCNTPURP);
				BookmarkDto bookmarkCdContactType = createBookmarkWithCodesTable(BookmarkConstants.CON_TYPE,
						contactNarrLogPerDateDto.getCdContactType(), CodesConstant.CCNTCTYP);

				BookmarkDto bookmarkIndAtt = createBookmark(BookmarkConstants.CON_ATT,
						contactNarrLogPerDateDto.getIndAtt());

				BookmarkDto bookmarkulIdEvent = createBookmark(BookmarkConstants.CON_ID,
						contactNarrLogPerDateDto.getIdEvent());

				bookmarkContactInfoList.add(bookmarkDTContactOccurred);
				bookmarkContactInfoList.add(bookmarkCdContactPurpose);
				bookmarkContactInfoList.add(bookmarkCdContactType);
				bookmarkContactInfoList.add(bookmarkIndAtt);
				bookmarkContactInfoList.add(bookmarkulIdEvent);

				tempContactFrmDataGrp.setBookmarkDtoList(bookmarkContactInfoList);
				formDataGroupList.add(tempContactFrmDataGrp);

				List<FormDataGroupDto> contactFrmDataGrpList = new ArrayList<FormDataGroupDto>();

				// set the prefill data for group eval23 : sub group of eval22

				for (ContactEventDto contactEventDto : contactCVSDto.getContactEventList()) {
					if (!ObjectUtils.isEmpty(contactNarrLogPerDateDto.getIdEvent())
							&& contactNarrLogPerDateDto.getIdEvent().equals(contactEventDto.getIdEvent())
							&& ServiceConstants.STRING_IND_Y.equals(contactEventDto.getIndKinNotifChild())) {
						FormDataGroupDto tempNotifChildFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_NOTIF_CHILD, FormGroupsConstants.TMPLAT_KIN_NOTIF_CONTACT);
						List<BookmarkDto> bookmarkNotifChildList = new ArrayList<>();
						BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_CHILD_FIRST,
								contactEventDto.getNmPersonFirst());
						BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_CHILD_LAST,
								contactEventDto.getNmPersonLast());
						bookmarkNotifChildList.add(bookmarkNmNameFirst);
						bookmarkNotifChildList.add(bookmarkNmNameLast);

						tempNotifChildFrmDataGrp.setBookmarkDtoList(bookmarkNotifChildList);
						contactFrmDataGrpList.add(tempNotifChildFrmDataGrp);
					}
				}

				// set the prefill data for group eval25 : sub group of eval22

				for (ContactEventDto contactEventDto : contactCVSDto.getContactEventList()) {
					if (contactNarrLogPerDateDto.getIdEvent() == contactEventDto.getIdEvent()
							&& !ServiceConstants.STRING_IND_Y.equals(contactEventDto.getIndKinNotifChild())
							&& ServiceConstants.STRING_IND_N.equals(contactEventDto.getIndPersRmvlNotified())) {
						FormDataGroupDto tempNotifChildFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_NOTIF_CHILD, FormGroupsConstants.TMPLAT_NOT_NOTIFY);
						List<BookmarkDto> bookmarkNotifChildList = new ArrayList<BookmarkDto>();
						List<BlobDataDto> bookmarkBlobDataList = new ArrayList<BlobDataDto>();

						BookmarkDto bookmarkPersRelInt = createBookmarkWithCodesTable(
								BookmarkConstants.NOT_NOTIFY_REL_INT, contactEventDto.getCdStagePersRelInt(),
								CodesConstant.CRPTRINT);
						BookmarkDto bookmarkRsnNotNotified = createBookmarkWithCodesTable(
								BookmarkConstants.RSN_NOT_NOTIFY, contactEventDto.getCdRsnNotNotified(),
								CodesConstant.CCNTNOTN);

						BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_NOT_NOTIFY_FIRST,
								contactEventDto.getNmPersonFirst());
						BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_NOT_NOTIFY_LAST,
								contactEventDto.getNmPersonLast());

						BlobDataDto bookmarkBlobIdEvent = createBlobData(BookmarkConstants.COMMENTS,
								CodesConstant.CONTACT_PERSON_NARR_VIEW,
								contactEventDto.getIdContactPersonNarr().toString());

						bookmarkNotifChildList.add(bookmarkPersRelInt);
						bookmarkNotifChildList.add(bookmarkRsnNotNotified);
						bookmarkNotifChildList.add(bookmarkNmNameFirst);
						bookmarkNotifChildList.add(bookmarkNmNameLast);

						bookmarkBlobDataList.add(bookmarkBlobIdEvent);

						tempNotifChildFrmDataGrp.setBookmarkDtoList(bookmarkNotifChildList);
						tempNotifChildFrmDataGrp.setBlobDataDtoList(bookmarkBlobDataList);
						contactFrmDataGrpList.add(tempNotifChildFrmDataGrp);
					}
				}

				// set the prefill data for group eval24 : sub group of eval22

				for (ContactEventDto contactEventDto : contactCVSDto.getContactEventList()) {
					if (contactNarrLogPerDateDto.getIdEvent() == contactEventDto.getIdEvent()
							&& !ServiceConstants.STRING_IND_Y.equals(contactEventDto.getIndKinNotifChild())
							&& ServiceConstants.STRING_IND_N.equals(contactEventDto.getIndPersRmvlNotified())) {
						FormDataGroupDto tempNotifChildFrmDataGrp = createFormDataGroup(
								FormGroupsConstants.TMPLAT_NOTIF_CHILD, FormGroupsConstants.TMPLAT_NOTIFY);
						List<BookmarkDto> bookmarkNotifChildList = new ArrayList<BookmarkDto>();
						BookmarkDto bookmarkPersRelInt = createBookmarkWithCodesTable(BookmarkConstants.NOTIFY_REL_INT,
								contactEventDto.getCdStagePersRelInt(), CodesConstant.CRPTRINT);
						BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_NOTIFY_FIRST,
								contactEventDto.getNmPersonFirst());
						BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_NOTIFY_LAST,
								contactEventDto.getNmPersonLast());

						bookmarkNotifChildList.add(bookmarkPersRelInt);
						bookmarkNotifChildList.add(bookmarkNmNameFirst);
						bookmarkNotifChildList.add(bookmarkNmNameLast);

						tempNotifChildFrmDataGrp.setBookmarkDtoList(bookmarkNotifChildList);
						contactFrmDataGrpList.add(tempNotifChildFrmDataGrp);

					}
				}

				tempContactFrmDataGrp.setFormDataGroupList(contactFrmDataGrpList);

			}
		}

		// set the prefill data for group eval01
		Integer cansCounter = ServiceConstants.Zero;
		for (ContactPersonDto contactPersonDto : contactCVSDto.getContactChildList()) {
			cansCounter++;
			FormDataGroupDto tempNotifChildFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_CHILD,
					FormConstants.EMPTY_STRING);

			List<BookmarkDto> bookmarkChildList = new ArrayList<BookmarkDto>();

			if (!ObjectUtils.isEmpty(contactPersonDto.getDtCspPlanCompl())) {
				BookmarkDto bookmarkDTChildPlan = createBookmark(BookmarkConstants.DT_CPOS,
						DateUtils.stringDt(contactPersonDto.getDtCspPlanCompl()));
				bookmarkChildList.add(bookmarkDTChildPlan);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getDtPlocStart())) {
				BookmarkDto bookmarkDTPlocStart = createBookmark(BookmarkConstants.SVC_LOC_DATE,
						DateUtils.stringDt(contactPersonDto.getDtPlocStart()));
				bookmarkChildList.add(bookmarkDTPlocStart);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getDtProfAssmtApptD())) {
				BookmarkDto bookmarkDTProfAssmtApptD = createBookmark(BookmarkConstants.DT_DENTAL,
						DateUtils.stringDt(contactPersonDto.getDtProfAssmtApptD()));
				bookmarkChildList.add(bookmarkDTProfAssmtApptD);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getDtProfAssmtApptM())) {
				BookmarkDto bookmarkDTProfAssmtApptM = createBookmark(BookmarkConstants.DT_ANNUAL,
						DateUtils.stringDt(contactPersonDto.getDtProfAssmtApptM()));
				bookmarkChildList.add(bookmarkDTProfAssmtApptM);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getDtTodoTaskDue())) {
				BookmarkDto bookmarkDTTodoTaskDue = createBookmark(BookmarkConstants.DT_NEXT_HEARING,
						DateUtils.stringDt(contactPersonDto.getDtTodoTaskDue()));
				bookmarkChildList.add(bookmarkDTTodoTaskDue);
			}
			BookmarkDto bookmarkDTCansAssess = createBookmark(BookmarkConstants.UE_GROUPID,
					cansCounter);
			bookmarkChildList.add(bookmarkDTCansAssess);

			BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(BookmarkConstants.NM_CHILD_SUFFIX,
					contactPersonDto.getCdPersonSuffix(), CodesConstant.CSUFFIX);
			if (!ObjectUtils.isEmpty(contactPersonDto.getCdPlocChild())) {
				BookmarkDto bookmarkCdPlocChild = createBookmarkWithCodesTable(BookmarkConstants.SVC_LOC,
						contactPersonDto.getCdPlocChild(), CodesConstant.CATHPLOC);
				bookmarkChildList.add(bookmarkCdPlocChild);
			}

			BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_CHILD_FULLNAME,
					TypeConvUtil.formatFullName(contactPersonDto.getNmPersonFirst(),contactPersonDto.getNmPersonMiddle(),contactPersonDto.getNmPersonLast()));
			BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_CHILD_FIRST,
					contactPersonDto.getNmPersonFirst());
			BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_CHILD_LAST,
					contactPersonDto.getNmPersonLast());
			BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_CHILD_MIDDLE,
					contactPersonDto.getNmPersonMiddle());

			bookmarkChildList.add(bookmarkCdNameSuffix);
			bookmarkChildList.add(bookmarkNmNameFirst);
			bookmarkChildList.add(bookmarkNmNameLast);
			bookmarkChildList.add(bookmarkNmNameMiddle);
			bookmarkChildList.add(bookmarkNmNameFull);

			tempNotifChildFrmDataGrp.setBookmarkDtoList(bookmarkChildList);

			List<FormDataGroupDto> contactChildFrmDataGrpList = new ArrayList<FormDataGroupDto>();

			// set the prefill data for group eval02 : subgroup of
			// eval01

			if (!ObjectUtils.isEmpty(contactPersonDto.getCdPersonSuffix())) {
				FormDataGroupDto tempCommaChildFrmDataGrpDtoWorker = createFormDataGroup(
						FormGroupsConstants.TMPLAT_NM_CHILD_COMMA, FormGroupsConstants.TMPLAT_CHILD);
				contactChildFrmDataGrpList.add(tempCommaChildFrmDataGrpDtoWorker);
			}

			// set the prefill data for group eval03 : subgroup of
			// eval01

			FormDataGroupDto tempFTFChildFrmDataGrpDto = createFormDataGroup(FormGroupsConstants.TMPLAT_CHILD_FTF,
					FormGroupsConstants.TMPLAT_CHILD);
			contactChildFrmDataGrpList.add(tempFTFChildFrmDataGrpDto);

			// set the prefill data for group eval20 : subgroup of
			// eval03 : subsub group of eval01

			List<FormDataGroupDto> contactChildDtFrmDataGrpList = new ArrayList<FormDataGroupDto>();
			for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactCVSDto.getContactInfoList()) {
				if (!StringUtils.isEmpty(contactNarrLogPerDateDto.getCdContactPurpose())
						&& (contactNarrLogPerDateDto.getCdContactPurpose()
								.contains(ServiceConstants.FACE_TO_FACE) || ServiceConstants.CCNTPURP_GCMR.equalsIgnoreCase(contactNarrLogPerDateDto.getCdContactPurpose()))) {
					for (ConGuideFetchOutDto conGuideFetchOutDto : contactCVSDto.getConGuideFetchOutDtos()) {
						for (ConGuideFetchOutRowDto conGuideFetchOutRowDto : conGuideFetchOutDto.
								getConGuideFetchOutArrayDto().getConGuideFetchOutRowDtos()) {
							if (conGuideFetchOutRowDto.getUlIdPerson() == contactPersonDto.getIdPerson()
									&& ServiceConstants.Y.equals(conGuideFetchOutRowDto.getcSysIndContactOccurred())
									&& conGuideFetchOutDto.getEventIdFetchOutDto().getUlIdEvent() == contactNarrLogPerDateDto.getIdEvent()) {
								FormDataGroupDto tempChildDtFrmDataGrpDto = createFormDataGroup(
										FormGroupsConstants.TMPLAT_CHILD_FTF_DT, FormGroupsConstants.TMPLAT_CHILD_FTF);
			
								List<BookmarkDto> bookmarkContactInfoList = new ArrayList<BookmarkDto>();
			
								BookmarkDto bookmarkDTContactOccurred = createBookmark(BookmarkConstants.DT_CONTACT,
										DateUtils.stringDt(contactNarrLogPerDateDto.getDtContactOccurred()));
			
								bookmarkContactInfoList.add(bookmarkDTContactOccurred);
								tempChildDtFrmDataGrpDto.setBookmarkDtoList(bookmarkContactInfoList);
								contactChildDtFrmDataGrpList.add(tempChildDtFrmDataGrpDto);
			
								tempFTFChildFrmDataGrpDto.setFormDataGroupList(contactChildDtFrmDataGrpList);
							}
						}
					}
				}
			}

			tempNotifChildFrmDataGrp.setFormDataGroupList(contactChildFrmDataGrpList);
			formDataGroupList.add(tempNotifChildFrmDataGrp);

		}

		// set the prefill data for child group : ADS change for form cvseval
		Integer counter = ServiceConstants.Zero;
		for (ContactPersonDto contactPersonDto : contactCVSDto.getContactChildList()) {
			FormDataGroupDto tempNotifChildFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_CHILD_EDUCATION,
					FormConstants.EMPTY_STRING);
			counter++;
			List<BookmarkDto> bookmarkChildList = new ArrayList<BookmarkDto>();

			if (!ObjectUtils.isEmpty(contactPersonDto.getNmEdHistSchool())) {
				BookmarkDto bookmarkNmEdHistSchool = createBookmark(BookmarkConstants.SCH_NAME,
						contactPersonDto.getNmEdHistSchool());
				bookmarkChildList.add(bookmarkNmEdHistSchool);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getNmEdHistSchool())) {
				BookmarkDto bookmarkEnrollGrade = createBookmarkWithCodesTable(BookmarkConstants.GRADE,
						contactPersonDto.getCdEdHistEnrollGrade(), CodesConstant.CSCHGRAD);
				bookmarkChildList.add(bookmarkEnrollGrade);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getSchoolPrograms())) {
				BookmarkDto bookmarkSchoolPrograms = createBookmark(BookmarkConstants.SCH_PROGRAMS,
						contactPersonDto.getSchoolPrograms());
				bookmarkChildList.add(bookmarkSchoolPrograms);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getSpecialAccmdtns())) {
				BookmarkDto bookmarkSpecialAccmdtns = createBookmark(BookmarkConstants.ACC_504,
						contactPersonDto.getSpecialAccmdtns());
				bookmarkChildList.add(bookmarkSpecialAccmdtns);
			}
			if (!ObjectUtils.isEmpty(contactPersonDto.getDtLastArdiep())) {
				BookmarkDto bookmarkDtLastArdiep = createBookmark(BookmarkConstants.ARD_MEET,
						DateUtils.stringDt(contactPersonDto.getDtLastArdiep()));
				bookmarkChildList.add(bookmarkDtLastArdiep);
			}

			BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(BookmarkConstants.NM_CHILD_SUFFIX,
					contactPersonDto.getCdPersonSuffix(), CodesConstant.CSUFFIX);
			BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_CHILD_FULLNAME,
					TypeConvUtil.formatFullName(contactPersonDto.getNmPersonFirst(),contactPersonDto.getNmPersonMiddle(),contactPersonDto.getNmPersonLast()));
			BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_CHILD_FIRST,
					contactPersonDto.getNmPersonFirst());
			BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_CHILD_LAST,
					contactPersonDto.getNmPersonLast());
			BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_CHILD_MIDDLE,
					contactPersonDto.getNmPersonMiddle());

			bookmarkChildList.add(bookmarkNmNameFirst);
			bookmarkChildList.add(bookmarkNmNameLast);
			bookmarkChildList.add(bookmarkNmNameMiddle);
			bookmarkChildList.add(bookmarkCdNameSuffix);
			bookmarkChildList.add(bookmarkNmNameFull);

			tempNotifChildFrmDataGrp.setBookmarkDtoList(bookmarkChildList);
			formDataGroupList.add(tempNotifChildFrmDataGrp);

			List<FormDataGroupDto> contactChildFrmDataGrpList = new ArrayList<FormDataGroupDto>();

			// set the prefill data for comma group : subgroup of
			// child group

			if (!ObjectUtils.isEmpty(contactPersonDto.getCdPersonSuffix())) {
				FormDataGroupDto tempCommaChildFrmDataGrpDtoWorker = createFormDataGroup(
						FormGroupsConstants.TMPLAT_NM_CHILD_COMMA, FormGroupsConstants.TMPLAT_CHILD_EDUCATION);
				contactChildFrmDataGrpList.add(tempCommaChildFrmDataGrpDtoWorker);
			}

			// set the prefill data for narrative group : subgroup of
			// child group

			List<String> positionList = ServiceConstants.CHILD_NARRATIVE;
			StringBuilder narrativeList = new StringBuilder();
			for (String position : positionList) {				
				if(ServiceConstants.CHILD_NARRATIVE_SELECTED.stream().anyMatch(pos -> position.equalsIgnoreCase(pos))){
					String textToAppend = String.format(ServiceConstants.CVS_EVAL_CIRCLE_BULLET_SPAN, position);
					narrativeList.append(textToAppend);
				}else{
					String textToAppend = String.format(ServiceConstants.CVS_EVAL_SOLID_BULLET_SPAN, position);
					narrativeList.append(textToAppend);
				}
				//narrativeList.append(ServiceConstants.ITALIC_TAG_START);
				//narrativeList.append(position);
				//narrativeList.append(ServiceConstants.ITALIC_TAG_END);
				//narrativeList.append(ServiceConstants.BREAK);
			}

			
			FormDataGroupDto tempNarrativeChildFrmDataGrpDtoWorker = createFormDataGroup(
					FormGroupsConstants.TMPLAT_CHILD_NARRATIVE, FormConstants.EMPTY_STRING);

			List<BookmarkDto> bookMarkNarrativeListList = new ArrayList<BookmarkDto>();
			BookmarkDto bookMarkNarrativeListContacts = createBookmark(BookmarkConstants.NARRATIVE_CHILDLIST_CONTACTS,
					narrativeList);
			bookMarkNarrativeListList.add(bookMarkNarrativeListContacts);
			BookmarkDto bookmarkChildNarrSpanAddInfo = createBookmark(BookmarkConstants.UE_GROUPID,
					counter);
			bookmarkChildList.add(bookmarkChildNarrSpanAddInfo);
			
			tempNarrativeChildFrmDataGrpDtoWorker.setBookmarkDtoList(bookMarkNarrativeListList);
			contactChildFrmDataGrpList.add(tempNarrativeChildFrmDataGrpDtoWorker);
			tempNotifChildFrmDataGrp.setFormDataGroupList(contactChildFrmDataGrpList);

		}

		// set the prefill data for group eval04

		for (ContactPersonDto contactPersonDto : contactCVSDto.getContactParentList()) {
			for (ConGuideFetchOutRowDto conGuideFetchOutRowDto : contactCVSDto.getConGuideFetchOutDto()
					.getConGuideFetchOutArrayDto().getConGuideFetchOutRowDtos()) {
				if (conGuideFetchOutRowDto.getUlIdPerson() == contactPersonDto.getIdPerson()
						&& ServiceConstants.Y.equals(conGuideFetchOutRowDto.getcSysIndContactOccurred())) {
			FormDataGroupDto tempNotifParentFrmDataGrp = createFormDataGroup(FormGroupsConstants.TMPLAT_PARENT,
					FormConstants.EMPTY_STRING);

			List<BookmarkDto> bookmarkChildList1 = new ArrayList<BookmarkDto>();

			BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(BookmarkConstants.NM_PARENT_SUFFIX,
					contactPersonDto.getCdPersonSuffix(), CodesConstant.CSUFFIX);

			BookmarkDto bookmarkDTChildPlan = createBookmark(BookmarkConstants.DT_FPOS,
					DateUtils.stringDt(contactPersonDto.getDtFpCompl()));
			BookmarkDto bookmarkCdStage = createBookmark(BookmarkConstants.FPOS_STAGE, contactPersonDto.getCdStage());

			BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_PARENT_FIRST,
					contactPersonDto.getNmPersonFirst());
			BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_PARENT_LAST,
					contactPersonDto.getNmPersonLast());
			BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_PARENT_MIDDLE,
					contactPersonDto.getNmPersonMiddle());
			BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_FULL_PARENT,
					TypeConvUtil.formatFullName(contactPersonDto.getNmPersonFirst(),contactPersonDto.getNmPersonMiddle(),contactPersonDto.getNmPersonLast()));

			BookmarkDto bookmarkDTFsna = createBookmark(BookmarkConstants.DT_FSNA,
					DateUtils.stringDt(contactPersonDto.getDtFsnaAssessment()));
			bookmarkChildList1.add(bookmarkNmNameFull);
			bookmarkChildList1.add(bookmarkCdNameSuffix);
			bookmarkChildList1.add(bookmarkNmNameFirst);
			bookmarkChildList1.add(bookmarkNmNameLast);
			bookmarkChildList1.add(bookmarkNmNameMiddle);

			bookmarkChildList1.add(bookmarkDTChildPlan);
			bookmarkChildList1.add(bookmarkCdStage);

			bookmarkChildList1.add(bookmarkDTFsna);

			tempNotifParentFrmDataGrp.setBookmarkDtoList(bookmarkChildList1);
			formDataGroupList.add(tempNotifParentFrmDataGrp);

			List<FormDataGroupDto> contactParentFrmDataGrpList = new ArrayList<FormDataGroupDto>();

			// set the prefill data for group eval05 : subgroup of
			// eval04

			if (!ObjectUtils.isEmpty(contactPersonDto.getCdPersonSuffix())) {
				FormDataGroupDto tempCommaParentFrmDataGrpDtoWorker = createFormDataGroup(
						FormGroupsConstants.TMPLAT_NM_PARENT_COMMA, FormGroupsConstants.TMPLAT_PARENT);
				contactParentFrmDataGrpList.add(tempCommaParentFrmDataGrpDtoWorker);
			}

			// set the prefill data for group eval06 : subgroup of
			// eval04

			FormDataGroupDto tempFTFParentFrmDataGrpDto = createFormDataGroup(FormGroupsConstants.TMPLAT_PARENT_FTF,
					FormGroupsConstants.TMPLAT_PARENT);
			contactParentFrmDataGrpList.add(tempFTFParentFrmDataGrpDto);

			// set the prefill data for group eval21 : subgroup of
			// eval06 : subsub group of eval04

			List<FormDataGroupDto> contactParentDtFrmDataGrpList = new ArrayList<FormDataGroupDto>();
			for (ContactNarrLogPerDateDto contactNarrLogPerDateDto : contactCVSDto.getContactInfoList()) {
						if (!StringUtils.isEmpty(contactNarrLogPerDateDto.getCdContactPurpose())
								&& (contactNarrLogPerDateDto.getCdContactPurpose()
										.contains(ServiceConstants.FACE_TO_FACE)
										|| ServiceConstants.CCNTPURP_GCMR.equalsIgnoreCase(contactNarrLogPerDateDto.getCdContactPurpose()))) {
					for (ConGuideFetchOutDto conGuideFetchOutDto : contactCVSDto.getConGuideFetchOutDtos()) {
						for (ConGuideFetchOutRowDto conGuideFetchDto : conGuideFetchOutDto
								.getConGuideFetchOutArrayDto().getConGuideFetchOutRowDtos()) {
							if (conGuideFetchDto.getUlIdPerson() == contactPersonDto.getIdPerson()
									&& ServiceConstants.Y
											.equals(conGuideFetchDto.getcSysIndContactOccurred())
									&& conGuideFetchOutDto.getEventIdFetchOutDto()
											.getUlIdEvent() == contactNarrLogPerDateDto.getIdEvent()) {
								FormDataGroupDto tempParentDtFrmDataGrpDto = createFormDataGroup(
										FormGroupsConstants.TMPLAT_PARENT_FTF_DT,
										FormGroupsConstants.TMPLAT_PARENT_FTF);
								List<BookmarkDto> bookmarkContactInfoList = new ArrayList<BookmarkDto>();

								BookmarkDto bookmarkDTContactOccurred = createBookmark(
										BookmarkConstants.DT_CONTACT,
										DateUtils.stringDt(contactNarrLogPerDateDto.getDtContactOccurred()));

								bookmarkContactInfoList.add(bookmarkDTContactOccurred);
								tempParentDtFrmDataGrpDto.setBookmarkDtoList(bookmarkContactInfoList);
								contactParentDtFrmDataGrpList.add(tempParentDtFrmDataGrpDto);

								tempFTFParentFrmDataGrpDto.setFormDataGroupList(contactParentDtFrmDataGrpList);
							}
						}
					}
				}
			}
			tempNotifParentFrmDataGrp.setFormDataGroupList(contactParentFrmDataGrpList);
			}
		}
		}

		// ADS change for TMPLAT_PARENT_NARRATIVE
		Integer counter2 = ServiceConstants.Zero;

		for (ContactPersonDto contactPersonDto : contactCVSDto.getContactParentList()) {
			for (ConGuideFetchOutRowDto conGuideFetchOutRowDto : contactCVSDto.getConGuideFetchOutDto()
					.getConGuideFetchOutArrayDto().getConGuideFetchOutRowDtos()) {
				if (conGuideFetchOutRowDto.getUlIdPerson() == contactPersonDto.getIdPerson()
						&& ServiceConstants.Y.equals(conGuideFetchOutRowDto.getcSysIndContactOccurred())) {
			counter2++;
			FormDataGroupDto tempNotifParentFrmDataGrp = createFormDataGroup(
					FormGroupsConstants.TMPLAT_PARENT_NARRATIVE, FormConstants.EMPTY_STRING);

			List<BookmarkDto> bookmarkChildList1 = new ArrayList<BookmarkDto>();

			BookmarkDto bookmarkCdNameSuffix = createBookmarkWithCodesTable(BookmarkConstants.NM_PARENT_SUFFIX,
					contactPersonDto.getCdPersonSuffix(), CodesConstant.CSUFFIX);

			BookmarkDto bookmarkNmNameFirst = createBookmark(BookmarkConstants.NM_PARENT_FIRST,
					contactPersonDto.getNmPersonFirst());
			BookmarkDto bookmarkNmNameLast = createBookmark(BookmarkConstants.NM_PARENT_LAST,
					contactPersonDto.getNmPersonLast());
			BookmarkDto bookmarkNmNameMiddle = createBookmark(BookmarkConstants.NM_PARENT_MIDDLE,
					contactPersonDto.getNmPersonMiddle());

			BookmarkDto bookmarkNmNameFull = createBookmark(BookmarkConstants.NM_FULL_PARENT,
					TypeConvUtil.formatFullName(contactPersonDto.getNmPersonFirst(),contactPersonDto.getNmPersonMiddle(),contactPersonDto.getNmPersonLast()));
			bookmarkChildList1.add(bookmarkNmNameFull);
			bookmarkChildList1.add(bookmarkCdNameSuffix);
			bookmarkChildList1.add(bookmarkNmNameFirst);
			bookmarkChildList1.add(bookmarkNmNameLast);
			bookmarkChildList1.add(bookmarkNmNameMiddle);

			List<FormDataGroupDto> contactParentFrmDataGrpList = new ArrayList<FormDataGroupDto>();

			// set the prefill data for group eval05 : subgroup of
			// eval04

			if (!ObjectUtils.isEmpty(contactPersonDto.getCdPersonSuffix())) {
				FormDataGroupDto tempCommaParentFrmDataGrpDtoWorker = createFormDataGroup(
						FormGroupsConstants.TMPLAT_NM_PARENT_COMMA, FormGroupsConstants.TMPLAT_PARENT_NARRATIVE);
				contactParentFrmDataGrpList.add(tempCommaParentFrmDataGrpDtoWorker);
			}

			List<String> positionList = ServiceConstants.PARENT_NARRATIVE;
			StringBuilder narrativeList = new StringBuilder();
			for (String position : positionList) {	
				String textToAppend = String.format(ServiceConstants.CVS_EVAL_SOLID_BULLET_SPAN, position);
				narrativeList.append(textToAppend);				
			}

			FormDataGroupDto tempNarrativePrFrmDataGrpDtoWorker = createFormDataGroup(
					FormGroupsConstants.TMPLAT_PARENTLIST_NARRATIVE, FormConstants.EMPTY_STRING);
			List<BookmarkDto> bookMarkNarrativeListListPr = new ArrayList<BookmarkDto>();
			BookmarkDto bookMarkNarrativeListContactsPr = createBookmark(
					BookmarkConstants.NARRATIVE_PARENTLIST_CONTACTS, narrativeList);
			bookMarkNarrativeListListPr.add(bookMarkNarrativeListContactsPr);
			BookmarkDto bookmarkNarrPrntSpanAddInfo = createBookmark(BookmarkConstants.UE_GROUPID,
					counter2);
			bookmarkChildList1.add(bookmarkNarrPrntSpanAddInfo);		

			tempNarrativePrFrmDataGrpDtoWorker.setBookmarkDtoList(bookMarkNarrativeListListPr);
			contactParentFrmDataGrpList.add(tempNarrativePrFrmDataGrpDtoWorker);
			tempNotifParentFrmDataGrp.setBookmarkDtoList(bookmarkChildList1);
			tempNotifParentFrmDataGrp.setFormDataGroupList(contactParentFrmDataGrpList);
			formDataGroupList.add(tempNotifParentFrmDataGrp);
				}
			}
		}

		// bookmarks without groups
		List<BookmarkDto> bookmarkNonFormGrpList = new ArrayList<BookmarkDto>();

		// bookmarks for CSEC01D

		BookmarkDto bookmarkAddrMailCodeCounty = createBookmarkWithCodesTable(BookmarkConstants.STAGE_COUNTY,
				contactCVSDto.getEmployeePersPhNameDto().getAddrMailCodeCounty(), CodesConstant.CCOUNT);
		bookmarkNonFormGrpList.add(bookmarkAddrMailCodeCounty);
		BookmarkDto bookmarkNmOfficeName = createBookmark(BookmarkConstants.NM_OFFICE,
				contactCVSDto.getEmployeePersPhNameDto().getNmOfficeName());
		bookmarkNonFormGrpList.add(bookmarkNmOfficeName);

		// bookmarks for CSEC02D

		BookmarkDto bookmarkNmCase = createBookmark(BookmarkConstants.TITLE_CASE_NAME,
				contactCVSDto.getGenCaseInfoDto().getNmCase());
		bookmarkNonFormGrpList.add(bookmarkNmCase);
		BookmarkDto bookmarkIdCase = createBookmark(BookmarkConstants.TITLE_CASE_NUMBER,
				contactCVSDto.getGenCaseInfoDto().getIdCase());
		bookmarkNonFormGrpList.add(bookmarkIdCase);

		// bookmarks for CSYS11D

		BookmarkDto bookmarkDtMonthlySummBegin = createBookmark(BookmarkConstants.DT_FROM,
				DateUtils.stringDt(contactCVSDto.getStageProgramDto().getDtMntlsumBg()));
		bookmarkNonFormGrpList.add(bookmarkDtMonthlySummBegin);
		BookmarkDto bookmarkDtMonthlySummEnd = createBookmark(BookmarkConstants.DT_TO,
				DateUtils.stringDt(contactCVSDto.getStageProgramDto().getDtCntMntSumEnd()));
		bookmarkNonFormGrpList.add(bookmarkDtMonthlySummEnd);

		// bookmark for CCMN19D

		BookmarkDto bookmarkPersonFull = createBookmark(BookmarkConstants.NM_CASEWORKER,
				contactCVSDto.getStagePersonDto().getNmPersonFull());
		bookmarkNonFormGrpList.add(bookmarkPersonFull);

		// bookmark for CCMN60D

		if (!ObjectUtils.isEmpty(contactCVSDto.getPersonDoDtoList())) {
			BookmarkDto bookmarkNmPersonFull = createBookmark(BookmarkConstants.NM_SUPERVISOR,
					contactCVSDto.getPersonDoDtoList().get(0).getNmPersonFull());
			bookmarkNonFormGrpList.add(bookmarkNmPersonFull);
		}

		PreFillDataServiceDto preFillData = new PreFillDataServiceDto();
		preFillData.setFormDataGroupList(formDataGroupList);
		preFillData.setBookmarkDtoList(bookmarkNonFormGrpList);
		return preFillData;
	}

	 /**ALM Defect 14943- CVS ME narrative displays wrong staff*
	  * New method to append name**/	
	private String getFullName(String lastName,String firstName,String middleName){
		
		StringBuffer name = new StringBuffer();
		
		name.append(StringUtils.isNotBlank(lastName)?lastName:ServiceConstants.EMPTY_STR);
		name.append(name.length()>0 && StringUtils.isNotBlank(firstName)?ServiceConstants.COMMA:ServiceConstants.EMPTY_STR);
		name.append(StringUtils.isNotBlank(firstName)?firstName:ServiceConstants.EMPTY_STR);
		name.append(name.length()>0 && StringUtils.isNotBlank(middleName)?ServiceConstants.SPACE:ServiceConstants.EMPTY_STR);
		name.append(StringUtils.isNotBlank(middleName)?middleName:ServiceConstants.EMPTY_STR);
			
		return name.toString();
		
	}
}
