package us.tx.state.dfps.service.forms.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import us.tx.state.dfps.common.dto.KinshipNotificationDto;
import us.tx.state.dfps.common.dto.PersonAddressDto;
import us.tx.state.dfps.populateletter.dto.CodesTablesDto;
import us.tx.state.dfps.service.common.BookmarkConstants;
import us.tx.state.dfps.service.common.FormConstants;
import us.tx.state.dfps.service.common.FormGroupsConstants;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.util.DateUtils;
import us.tx.state.dfps.service.common.util.TypeConvUtil;
import us.tx.state.dfps.service.forms.dto.BookmarkDto;
import us.tx.state.dfps.service.forms.dto.FormDataGroupDto;
import us.tx.state.dfps.service.forms.dto.PreFillDataServiceDto;
import us.tx.state.dfps.service.lookup.dao.LookupDao;
import us.tx.state.dfps.service.person.dto.PersonPhoneRetDto;
import us.tx.state.dfps.service.workload.dto.PersonDto;

@Component
/**
 * Name:KinshipNotificationPrefillData Description:
 * KinshipNotificationPrefillData will implemented returnPrefillData operation
 * defined in DocumentServiceUtil Interface to populate the prefill data for
 * form KIN15O00.(Kinship Notification) April 24, 2018 - 04:40:29 PM
 */
public class KinshipNotificationPrefillData extends DocumentServiceUtil {

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
	
	@Autowired
	LookupDao lookupDao;
	
	@Override
	public PreFillDataServiceDto returnPrefillData(Object parentDtoobj) {

		KinshipNotificationDto kinshipNotificationDto = (KinshipNotificationDto) parentDtoobj;

		// kin1501-->kin1505-->kin1506-->kin1514-->kin1509-->kin1507-->kin1508-->kin1511-->kin1516-->kin1515-->kin1512-->kin1517-->kin1510-->kin1502-->kin1503-->kin1504-->kin1521
		List<FormDataGroupDto> formDataGroupDtoList = new ArrayList<FormDataGroupDto>();
		List<FormDataGroupDto> formDataGroupTmplatAddresseList = new ArrayList<FormDataGroupDto>();
		
		
		/********
		 * kin1501-->kin1505 TMPLAT_NM_WORKER
		 *********************************************************************************************/
		List<FormDataGroupDto> formDataGroupTmplatNmWorkerList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupTmplatNmWorker = createFormDataGroup(FormGroupsConstants.TMPLAT_NM_WORKER,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		// NM_WORKER
		List<BookmarkDto> bookmarkNmWorkerList = new ArrayList<BookmarkDto>();
		BookmarkDto bookMarkDtoNmWorker = createBookmark(BookmarkConstants.NM_WORKER,
				kinshipNotificationDto.getStagePersonDto().getNmPersonFull());
		bookmarkNmWorkerList.add(bookMarkDtoNmWorker);

		formDataGroupTmplatNmWorker.setBookmarkDtoList(bookmarkNmWorkerList);
		formDataGroupTmplatNmWorkerList.add(formDataGroupTmplatNmWorker);
		formDataGroupDtoList.addAll(formDataGroupTmplatNmWorkerList);
		/********
		 * kin1501-->kin1505 TMPLAT_NM_WORKER
		 *********************************************************************************************/
		/********
		 * kin1501-->kin1506 TMPLAT_NM_SUP
		 *********************************************************************************************/
		List<FormDataGroupDto> formDataGroupTmplatNmSupList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupTmplatNmSup = createFormDataGroup(FormGroupsConstants.TMPLAT_NM_SUP,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		// NM_SUP
		List<BookmarkDto> bookmarkNmSupList = new ArrayList<BookmarkDto>();
		BookmarkDto bookMarkDtoNmSup = createBookmark(BookmarkConstants.NM_SUP,
				kinshipNotificationDto.getSupervisorDto().getNmPersonFull());
		bookmarkNmSupList.add(bookMarkDtoNmSup);
		formDataGroupTmplatNmSup.setBookmarkDtoList(bookmarkNmSupList);
		formDataGroupTmplatNmSupList.add(formDataGroupTmplatNmSup);
		formDataGroupDtoList.addAll(formDataGroupTmplatNmSupList);
		/********
		 * kin1501-->kin1506 TMPLAT_NM_SUP
		 *********************************************************************************************/
		/********
		 * kin1501-->kin1514-->kin1522 TMPLAT_WORKER_INFO
		 *********************************************************************************************/
		List<FormDataGroupDto> formDataGroupTmplatWorkerInfoList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupTmplatWorkerInfo = createFormDataGroup(FormGroupsConstants.TMPLAT_WORKER_INFO,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		List<BookmarkDto> bookmarkTmplatWorkerInfoList = new ArrayList<BookmarkDto>();
		// WORKER_PHONE
		BookmarkDto bookMarkDtoWorkerPhn = createBookmark(BookmarkConstants.WORKER_PHONE,
				TypeConvUtil.formatPhone(kinshipNotificationDto.getWorkerDetailDtoCW().getNbrPersonPhone()));
		bookmarkTmplatWorkerInfoList.add(bookMarkDtoWorkerPhn);
		// NM_WORKER_FIRST
		BookmarkDto bookMarkDtoNmWorkerFirst = createBookmark(BookmarkConstants.NM_WORKER_FIRST,
				kinshipNotificationDto.getWorkerDetailDtoCW().getNmNameFirst());
		bookmarkTmplatWorkerInfoList.add(bookMarkDtoNmWorkerFirst);
		// NM_WORKER_LAST
		BookmarkDto bookMarkDtoNmWorkerLast = createBookmark(BookmarkConstants.NM_WORKER_LAST,
				kinshipNotificationDto.getWorkerDetailDtoCW().getNmNameLast());
		bookmarkTmplatWorkerInfoList.add(bookMarkDtoNmWorkerLast);
		// UE_GROUPID
		BookmarkDto bookMarkDtoUEGrpID = createBookmark(BookmarkConstants.UE_GROUPID,
				kinshipNotificationDto.getWorkerDetailDtoCW().getIdPerson());
		bookmarkTmplatWorkerInfoList.add(bookMarkDtoUEGrpID);

		// kin1514-->kin1522 TMPLAT_WORKER_FAX
		for (PersonPhoneRetDto personPhoneDto : kinshipNotificationDto.getPersonPhoneRetDto()) {
			if ((!StringUtils.isEmpty(personPhoneDto.getIndPersonPhoneInvalid())
					&& FormConstants.N.equalsIgnoreCase(personPhoneDto.getIndPersonPhoneInvalid()))
					&& (!StringUtils.isEmpty(personPhoneDto.getCdPersonPhoneType())
							&& FormConstants.BF.equalsIgnoreCase(personPhoneDto.getCdPersonPhoneType()))) {
				List<FormDataGroupDto> formDataGroupTmplatWorkerFaxList = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGroupTmplatWorkerFax = createFormDataGroup(
						FormGroupsConstants.TMPLAT_WORKER_FAX, FormGroupsConstants.TMPLAT_WORKER_INFO);
				// WORKER_FAX
				List<BookmarkDto> bookmarkNbrPhoneList = new ArrayList<BookmarkDto>();
				BookmarkDto bookmarkNbrPhone = createBookmark(BookmarkConstants.WORKER_FAX,
						personPhoneDto.getPersonPhone());
				bookmarkNbrPhoneList.add(bookmarkNbrPhone);
				formDataGroupTmplatWorkerFax.setBookmarkDtoList(bookmarkNbrPhoneList);
				formDataGroupTmplatWorkerFaxList.add(formDataGroupTmplatWorkerFax);
				formDataGroupTmplatWorkerInfo.setFormDataGroupList(formDataGroupTmplatWorkerFaxList);
			}
		}

		formDataGroupTmplatWorkerInfo.setBookmarkDtoList(bookmarkTmplatWorkerInfoList);
		formDataGroupTmplatWorkerInfoList.add(formDataGroupTmplatWorkerInfo);
		formDataGroupDtoList.addAll(formDataGroupTmplatWorkerInfoList);

		/********
		 * kin1501-->kin1514-->kin1522 TMPLAT_WORKER_INFO
		 *********************************************************************************************/
		/********
		 * kin1501-->kin1509-->kin1519 TMPLAT_ADDR2_WORKER
		 *********************************************************************************************/
		List<FormDataGroupDto> formDataGroupTmplatAddr2WorkerList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupDto = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDR2_WORKER,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		List<BookmarkDto> bookmarkDtoWorkrAddrList = new ArrayList<BookmarkDto>();
		// WORKER_ADDR_CITY
		BookmarkDto bookMarkWrkrAddrCity = createBookmark(BookmarkConstants.WORKER_ADDR_CITY,
				kinshipNotificationDto.getWorkerDetailDtoCW().getAddrMailCodeCity());
		bookmarkDtoWorkrAddrList.add(bookMarkWrkrAddrCity);
		// WORKER_ADDR_ZIP
		BookmarkDto bookMarkWrkrAddrZip = createBookmark(BookmarkConstants.WORKER_ADDR_ZIP,
				kinshipNotificationDto.getWorkerDetailDtoCW().getAddrMailCodeZip());
		bookmarkDtoWorkrAddrList.add(bookMarkWrkrAddrZip);
		// kin1509 --> kin1519 TMPLAT_ADDR_LN2
		if (!StringUtils.isEmpty(kinshipNotificationDto.getWorkerDetailDtoCW().getAddrMailCodeStLn2())) {
			List<FormDataGroupDto> formDataGrpTmplatAddrLn2List = new ArrayList<FormDataGroupDto>();
			FormDataGroupDto formDataGrpTmplatAddrLn2 = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDR_LN2,
					FormGroupsConstants.TMPLAT_ADDR2_WORKER);
			// WORKER_ADDR_LN_2
			List<BookmarkDto> bookmarkWorkerAddrLn2List = new ArrayList<BookmarkDto>();
			BookmarkDto bookmarkWorkerAddrLn2 = createBookmark(BookmarkConstants.WORKER_ADDR_LN_2,
					kinshipNotificationDto.getWorkerDetailDtoCW().getAddrMailCodeStLn2());
			bookmarkWorkerAddrLn2List.add(bookmarkWorkerAddrLn2);
			formDataGrpTmplatAddrLn2.setBookmarkDtoList(bookmarkWorkerAddrLn2List);
			formDataGrpTmplatAddrLn2List.add(formDataGrpTmplatAddrLn2);
			formDataGroupDto.setFormDataGroupList(formDataGrpTmplatAddrLn2List);
		}

		formDataGroupDto.setBookmarkDtoList(bookmarkDtoWorkrAddrList);
		formDataGroupTmplatAddr2WorkerList.add(formDataGroupDto);
		formDataGroupDtoList.addAll(formDataGroupTmplatAddr2WorkerList);
		/********
		 * kin1501-->kin1509-->kin1519 TMPLAT_ADDR2_WORKER
		 *********************************************************************************************/
		/********
		 * kin1501-->kin1507****TMPLAT_ADDR_WORKER
		 *****************************************************************************************************/
		List<FormDataGroupDto> formDataGrpTmplatAddrWorkerList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupTmplatAddrWorker = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDR_WORKER,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		List<BookmarkDto> bookmarkWorkerAddrLn1List = new ArrayList<BookmarkDto>();
		BookmarkDto bookmarkWorkerAddrLn1 = createBookmark(BookmarkConstants.WORKER_ADDR_LN_1,
				kinshipNotificationDto.getWorkerDetailDtoCW().getAddrMailCodeStLn1());
		bookmarkWorkerAddrLn1List.add(bookmarkWorkerAddrLn1);
		formDataGroupTmplatAddrWorker.setBookmarkDtoList(bookmarkWorkerAddrLn1List);
		formDataGrpTmplatAddrWorkerList.add(formDataGroupTmplatAddrWorker);
		formDataGroupDtoList.addAll(formDataGrpTmplatAddrWorkerList);
		/********
		 * kin1501-->kin1507****TMPLAT_ADDR_WORKER
		 ****************************************************************************************************/
		/********
		 * kin1501-->kin1508****TMPLAT_PHONE_SUP
		 ******************************************************************************************************/
		List<FormDataGroupDto> formDataGrpTmplatPhoneSubList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGrpTmplatPhoneSub = createFormDataGroup(FormGroupsConstants.TMPLAT_PHONE_SUP,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		List<BookmarkDto> bookmarkSupPhoneList = new ArrayList<BookmarkDto>();
		BookmarkDto bookmarkSupPhone = createBookmark(BookmarkConstants.SUP_PHONE,
				TypeConvUtil.formatPhone(kinshipNotificationDto.getWorkerDetailDtoSV().getNbrPersonPhone()));
		bookmarkSupPhoneList.add(bookmarkSupPhone);
		formDataGrpTmplatPhoneSub.setBookmarkDtoList(bookmarkSupPhoneList);
		formDataGrpTmplatPhoneSubList.add(formDataGrpTmplatPhoneSub);
		formDataGroupDtoList.addAll(formDataGrpTmplatPhoneSubList);
		/********
		 * kin1501-->kin1508****TMPLAT_PHONE_SUP
		 ****************************************************************************************************/
		/********
		 * kin1501-->kin1511****TMPLAT_COMMA1
		 *******************************************************************************************************/
		for (PersonDto personDto : kinshipNotificationDto.getPersonDtoList()) {
			if (!StringUtils.isEmpty(personDto.getCdPersonSuffix())) {
				List<FormDataGroupDto> formDataGrpTmplatComma1List = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGrpTmplatComma1 = createFormDataGroup(FormGroupsConstants.TMPLAT_COMMA1,
						FormGroupsConstants.TMPLAT_ADDRESSEE);
				formDataGrpTmplatComma1List.add(formDataGrpTmplatComma1);
				formDataGroupDtoList.addAll(formDataGrpTmplatComma1List);
			}
		}
		/********
		 * kin1501-->kin1511****TMPLAT_COMMA1
		 *******************************************************************************************************/
		/********
		 * kin1501-->kin1516-->kin1519****TMPLAT_CHILD_FIRST
		 ****************************************************************************************/
		for (PersonDto personDto : kinshipNotificationDto.getPersonDtoList()) {
			if (!StringUtils.isEmpty(personDto.getIndKinNotifChild())
					&& FormConstants.Y.equalsIgnoreCase(personDto.getIndKinNotifChild())) {
				List<FormDataGroupDto> formDataGroupTmplatChildFirstList = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGroupTmplatChildFirst = createFormDataGroup(
						FormGroupsConstants.TMPLAT_CHILD_FIRST, FormGroupsConstants.TMPLAT_ADDRESSEE);
				// NM_FIRST
				List<BookmarkDto> bookmarkNmFirstList = new ArrayList<BookmarkDto>();
				BookmarkDto bookmarkDtoNmFirst = createBookmark(BookmarkConstants.NM_FIRST,
						personDto.getNmPersonFirst());
				bookmarkNmFirstList.add(bookmarkDtoNmFirst);
				// kin1519
				if (!StringUtils.isEmpty(personDto.getIndCaringAdult())
						&& !FormConstants.Y.equalsIgnoreCase(personDto.getIndCaringAdult())) {
					List<FormDataGroupDto> formDataGrpTmplatComma4List = new ArrayList<FormDataGroupDto>();
					FormDataGroupDto formDataGrpTmplatComma4 = createFormDataGroup(FormGroupsConstants.TMPLAT_COMMA4,
							FormGroupsConstants.TMPLAT_CHILD_FIRST);
					formDataGrpTmplatComma4List.add(formDataGrpTmplatComma4);
					formDataGroupTmplatChildFirst.setFormDataGroupList(formDataGrpTmplatComma4List);
				}
				formDataGroupTmplatChildFirst.setBookmarkDtoList(bookmarkNmFirstList);
				formDataGroupTmplatChildFirstList.add(formDataGroupTmplatChildFirst);
				formDataGroupDtoList.addAll(formDataGroupTmplatChildFirstList);
			}
		}
		/********
		 * kin1501-->kin1516-->kin1519****TMPLAT_CHILD_FIRST
		 ****************************************************************************************/
		/********
		 * kin1501-->kin1515-->kin1523--> kin1520****TMPLAT_CHILD
		 ***********************************************************************************/
		if (!CollectionUtils.isEmpty(kinshipNotificationDto.getChildDtoList())) {
			for (PersonDto personDto : kinshipNotificationDto.getChildDtoList()) {
				List<FormDataGroupDto> formDataGroupTmplatChildList = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGroupTmplatChild = createFormDataGroup(FormGroupsConstants.TMPLAT_CHILD,
						FormGroupsConstants.TMPLAT_ADDRESSEE);
				// CHILD_FIRST
				List<BookmarkDto> bookmarNMFirstList = new ArrayList<BookmarkDto>();
				BookmarkDto bookmarkNMFirst = createBookmark(BookmarkConstants.CHILD_FIRST,
						personDto.getNmPersonFirst());
				bookmarNMFirstList.add(bookmarkNMFirst);
				formDataGroupTmplatChild.setBookmarkDtoList(bookmarNMFirstList);

				// kin1523

				// kin1520

				formDataGroupTmplatChildList.add(formDataGroupTmplatChild);
				formDataGroupDtoList.addAll(formDataGroupTmplatChildList);
			}
		}
		/********
		 * kin1501-->kin1515-->kin1523--> kin1520****TMPLAT_CHILD
		 ***********************************************************************************/
		/********
		 * kin1501-->kin1512****TMPLAT_COMMA2
		 *******************************************************************************************************/
		for (PersonDto personDto : kinshipNotificationDto.getPersonDtoList()) {
			if (!StringUtils.isEmpty(personDto.getCdPersonSuffix())) {
				List<FormDataGroupDto> formDataGrpTmplatComma2List = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGrpTmplatComma2 = createFormDataGroup(FormGroupsConstants.TMPLAT_COMMA2,
						FormGroupsConstants.TMPLAT_ADDRESSEE);
				formDataGrpTmplatComma2List.add(formDataGrpTmplatComma2);
				formDataGroupDtoList.addAll(formDataGrpTmplatComma2List);
			}
		}
		/********
		 * kin1501-->kin1512****TMPLAT_COMMA2
		 *******************************************************************************************************/
		/********
		 * kin1501-->kin1517****TMPLAT_PAGEBREAK
		 ****************************************************************************************************/
		for (PersonDto personDto : kinshipNotificationDto.getPersonDtoList()) {
			if ((!StringUtils.isEmpty(personDto.getIndPersRmvlNotified())
					&& FormConstants.Y.equalsIgnoreCase(personDto.getIndPersRmvlNotified()))
					&& (!StringUtils.isEmpty(personDto.getIndKinNotifChild())
							&& !FormConstants.Y.equalsIgnoreCase(personDto.getIndKinNotifChild()))) {
				List<FormDataGroupDto> formDataGrpTmplatPgBreakList = new ArrayList<FormDataGroupDto>();
				FormDataGroupDto formDataGrpTmplatPgBreak = createFormDataGroup(FormGroupsConstants.TMPLAT_PAGEBREAK,
						FormGroupsConstants.TMPLAT_ADDRESSEE);
				formDataGrpTmplatPgBreakList.add(formDataGrpTmplatPgBreak);
				formDataGroupDtoList.addAll(formDataGrpTmplatPgBreakList);
			}
		}
		/********
		 * kin1501-->kin1517****TMPLAT_PAGEBREAK
		 ****************************************************************************************************/
		/********
		 * kin1501-->kin1510-->TMPLAT_COMMA3****TMPLAT_NM_CHILD
		 *************************************************************************************/
		if(!CollectionUtils.isEmpty(kinshipNotificationDto.getChildDtoList())){
			for (PersonDto personDto : kinshipNotificationDto.getChildDtoList()) {
					List<FormDataGroupDto> formDataGroupDtoNmChildList = new ArrayList<FormDataGroupDto>();
					FormDataGroupDto formDataGroupDtoNmChild = createFormDataGroup(FormGroupsConstants.TMPLAT_NM_CHILD,
							FormGroupsConstants.TMPLAT_ADDRESSEE);
					List<BookmarkDto> bookmarkDtoNmChildList = new ArrayList<BookmarkDto>();
					// NM_CHILD_DOB
					BookmarkDto bookmarkDtonmChildDob = createBookmark(BookmarkConstants.NM_CHILD_DOB,
							DateUtils.stringDt(personDto.getDtPersonBirth()));
					bookmarkDtoNmChildList.add(bookmarkDtonmChildDob);
					// NM_CHILD_FIRST
					BookmarkDto bookmarkDtonmChildFirst = createBookmark(BookmarkConstants.NM_CHILD_FIRST,
							personDto.getNmPersonFirst());
					bookmarkDtoNmChildList.add(bookmarkDtonmChildFirst);
					// NM_CHILD_LAST
					BookmarkDto bookmarkDtonmChildLast = createBookmark(BookmarkConstants.NM_CHILD_LAST,
							personDto.getNmPersonLast());
					bookmarkDtoNmChildList.add(bookmarkDtonmChildLast);
					// kin1518
					formDataGroupDtoNmChild.setBookmarkDtoList(bookmarkDtoNmChildList);
					formDataGroupDtoNmChildList.add(formDataGroupDtoNmChild);
					formDataGroupDtoList.addAll(formDataGroupDtoNmChildList);
				
			}
		}
		
		/********
		 * kin1501-->kin1510-->TMPLAT_COMMA3****TMPLAT_NM_CHILD
		 *************************************************************************************/
		/********
		 * kin1501-->kin1502****TMPLAT_HEADER_DIRECTOR
		 **********************************************************************************************/
		for (CodesTablesDto codes : kinshipNotificationDto.getCodestableDtoList()) {
			List<FormDataGroupDto> formDataGroupTmplatHeaderDirectorList = new ArrayList<FormDataGroupDto>();
			FormDataGroupDto formDataGroupTmplatHeaderDirector = createFormDataGroup(
					FormGroupsConstants.TMPLAT_HEADER_DIRECTOR, FormGroupsConstants.TMPLAT_ADDRESSEE);
			List<BookmarkDto> bookmarkDtoList = new ArrayList<BookmarkDto>();

			BookmarkDto bookmarkDtoHeaderDirectorTitle = createBookmark(FormGroupsConstants.HEADER_DIRECTOR_TITLE,
					codes.getaDecode());
			bookmarkDtoList.add(bookmarkDtoHeaderDirectorTitle);

			BookmarkDto bookmarkDtoHeaderDirectorNm = createBookmark(FormGroupsConstants.HEADER_DIRECTOR_NAME,
					codes.getbDecode());
			bookmarkDtoList.add(bookmarkDtoHeaderDirectorNm);
			formDataGroupTmplatHeaderDirector.setBookmarkDtoList(bookmarkDtoList);
			formDataGroupTmplatHeaderDirectorList.add(formDataGroupTmplatHeaderDirector);
			formDataGroupDtoList.addAll(formDataGroupTmplatHeaderDirectorList);
		}
		/********
		 * kin1501-->kin1502****TMPLAT_HEADER_DIRECTOR
		 **********************************************************************************************/
		/********
		 * kin1501-->kin1503****TMPLAT_HEADER_BOARD
		 *************************************************************************************************/
		List<FormDataGroupDto> formDataGroupTmplatHeaderBoardList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupTmplatHeaderBoard = createFormDataGroup(FormGroupsConstants.TMPLAT_HEADER_BOARD,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		formDataGroupTmplatHeaderBoardList.add(formDataGroupTmplatHeaderBoard);
		formDataGroupDtoList.addAll(formDataGroupTmplatHeaderBoardList);
		/********
		 * kin1501-->kin1504****TMPLAT_SYSTEM_DATE
		 *************************************************************************************************/
		List<FormDataGroupDto> formDataGroupSysDtList = new ArrayList<FormDataGroupDto>();
		FormDataGroupDto formDataGroupSysDt = createFormDataGroup(FormGroupsConstants.TMPLAT_SYSTEM_DATE,
				FormGroupsConstants.TMPLAT_ADDRESSEE);
		List<BookmarkDto> bookmarkDtoListgenericDtList = new ArrayList<BookmarkDto>();
		BookmarkDto bookmarkDtoGenericDt = createBookmark(BookmarkConstants.SYSTEM_DATE,
				DateUtils.stringDt(kinshipNotificationDto.getGenericCaseInfoDto().getDtSysDtGenericSysdate()));
		bookmarkDtoListgenericDtList.add(bookmarkDtoGenericDt);
		formDataGroupSysDt.setBookmarkDtoList(bookmarkDtoListgenericDtList);
		formDataGroupSysDtList.add(formDataGroupSysDt);
		formDataGroupDtoList.addAll(formDataGroupSysDtList);
		/********
		 * kin1501-->kin1504****TMPLAT_SYSTEM_DATE
		 *************************************************************************************************/
		/********
		 * kin1501-->kin1521****TMPLAT_ADDRESS
		 *************************************************************************************************/
		/*for (PersonAddressDto personAddressDto : kinshipNotificationDto.getPersonAddressDtoList()) {	
			List<FormDataGroupDto> formDataGroupTmplatAddressList = new ArrayList<FormDataGroupDto>();
			FormDataGroupDto formDataGroupDtoTmplatAddress = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDRESS,
					FormGroupsConstants.TMPLAT_ADDRESSEE);
			List<BookmarkDto> bookmarkDtoTmpAddress = new ArrayList<BookmarkDto>();
			// ADDRESS_CITY
			BookmarkDto bookmarkDtoAddressCity = createBookmark(BookmarkConstants.ADDRESS_CITY,
					personAddressDto.getAddrPersonAddrCity());
			bookmarkDtoTmpAddress.add(bookmarkDtoAddressCity);
			// ADDRESS_LINE_1
			BookmarkDto bookmarkDtoAddressLn1 = createBookmark(BookmarkConstants.ADDRESS_LINE_1,
					personAddressDto.getAddrPersAddrStLn1());
			bookmarkDtoTmpAddress.add(bookmarkDtoAddressLn1);
			// ADDRESS_LINE_2
			BookmarkDto bookmarkDtoAddressLn2 = createBookmark(BookmarkConstants.ADDRESS_LINE_2,
					personAddressDto.getAddrPersAddrStLn2());
			bookmarkDtoTmpAddress.add(bookmarkDtoAddressLn2);
			// ADDRESS_ZIP
			BookmarkDto bookmarkDtoAddressZip = createBookmark(BookmarkConstants.ADDRESS_ZIP,
					personAddressDto.getAddrPersonAddrZip());
			bookmarkDtoTmpAddress.add(bookmarkDtoAddressZip);
			// ADDRESS_STATE
			BookmarkDto bookmarkDtoAddressState = createBookmark(BookmarkConstants.ADDRESS_STATE,
					personAddressDto.getCdPersonAddrState());
			bookmarkDtoTmpAddress.add(bookmarkDtoAddressState);
			formDataGroupDtoTmplatAddress.setBookmarkDtoList(bookmarkDtoTmpAddress);
			formDataGroupTmplatAddressList.add(formDataGroupDtoTmplatAddress);
			formDataGroupDtoList.addAll(formDataGroupTmplatAddressList);
		}	*/
		/********
		 * kin1501-->kin1521****TMPLAT_ADDRESS   person.getIdPersonAddr().equals(kinshipNotificatn.getIdPerson());
		 *************************************************************************************************/
		for (PersonDto kinshipNotificatn : kinshipNotificationDto.getPersonDtoList()) {
			
			if (!StringUtils.isEmpty(kinshipNotificatn.getIndKinNotifChild())
					&& FormConstants.Y.equalsIgnoreCase(kinshipNotificatn.getIndKinNotifChild())) {
				
				// TMPLAT_ADDRESSEE kin1501
				FormDataGroupDto formDataGroupTmplatAddresse = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDRESSEE,
						FormConstants.EMPTY_STRING);
				List<BookmarkDto> bookmarkListTmplatAddresse = new ArrayList<BookmarkDto>();
				
				PersonAddressDto personAddressDto = kinshipNotificationDto.getPersonAddressDtoList().stream()
						.filter(person -> kinshipNotificatn.getIdPerson().equals(person.getIdPersonAddr())).findAny().orElse(null);
				
				if(!ObjectUtils.isEmpty(personAddressDto)){
					/*List<FormDataGroupDto> formDataGroupTmplatAddressList = new ArrayList<FormDataGroupDto>();
					FormDataGroupDto formDataGroupDtoTmplatAddress = createFormDataGroup(FormGroupsConstants.TMPLAT_ADDRESS,
							FormGroupsConstants.TMPLAT_ADDRESSEE);*/
					/*List<BookmarkDto> bookmarkDtoTmpAddress = new ArrayList<BookmarkDto>();*/
					// ADDRESS_CITY
					BookmarkDto bookmarkDtoAddressCity = createBookmark(BookmarkConstants.ADDRESS_CITY,
							personAddressDto.getAddrPersonAddrCity());
					bookmarkListTmplatAddresse.add(bookmarkDtoAddressCity);
					// ADDRESS_LINE_1
					BookmarkDto bookmarkDtoAddressLn1 = createBookmark(BookmarkConstants.ADDRESS_LINE_1,
							personAddressDto.getAddrPersAddrStLn1());
					bookmarkListTmplatAddresse.add(bookmarkDtoAddressLn1);
					// ADDRESS_LINE_2
					BookmarkDto bookmarkDtoAddressLn2 = createBookmark(BookmarkConstants.ADDRESS_LINE_2,
							personAddressDto.getAddrPersAddrStLn2());
					bookmarkListTmplatAddresse.add(bookmarkDtoAddressLn2);
					// ADDRESS_ZIP
					BookmarkDto bookmarkDtoAddressZip = createBookmark(BookmarkConstants.ADDRESS_ZIP,
							personAddressDto.getAddrPersonAddrZip());
					bookmarkListTmplatAddresse.add(bookmarkDtoAddressZip);
					// ADDRESS_STATE
					BookmarkDto bookmarkDtoAddressState = createBookmark(BookmarkConstants.ADDRESS_STATE,
							personAddressDto.getCdPersonAddrState());
					bookmarkListTmplatAddresse.add(bookmarkDtoAddressState);
					
				}
				
				
				// ADDR_NAME_SUFFIX CSUFFIX
				// Warranty Defect Fix - 11749 - Added Null Pointer Check
				if(!ObjectUtils.isEmpty(kinshipNotificatn.getCdPersonSuffix()))
				{
				BookmarkDto bookmarkDtoAddrNameSuffix = createBookmark(BookmarkConstants.ADDR_NAME_SUFFIX,
						lookupDao.decode(ServiceConstants.CSUFFIX2,
								kinshipNotificatn.getCdPersonSuffix()));
				bookmarkListTmplatAddresse.add(bookmarkDtoAddrNameSuffix);
				}
				// NAME_SUFFIX CSUFFIX
				// Warranty Defect Fix - 11749 - Added Null Pointer Check
				if(!ObjectUtils.isEmpty(kinshipNotificatn.getCdPersonSuffix()))
				{
				BookmarkDto bookmarkDtoNameSuffix = createBookmark(BookmarkConstants.NAME_SUFFIX,
						lookupDao.decode(ServiceConstants.CSUFFIX2,
								kinshipNotificatn.getCdPersonSuffix()));
				bookmarkListTmplatAddresse.add(bookmarkDtoNameSuffix);
				}

				// ADDR_NAME_FIRST
				BookmarkDto bookmarkDtoAddrNameFirst = createBookmark(BookmarkConstants.ADDR_NAME_FIRST,
						kinshipNotificatn.getNmPersonFirst());
				bookmarkListTmplatAddresse.add(bookmarkDtoAddrNameFirst);

				// NAME_FIRST
				BookmarkDto bookmarkDtoNameFirst = createBookmark(BookmarkConstants.NAME_FIRST,
						kinshipNotificatn.getNmPersonFirst());
				bookmarkListTmplatAddresse.add(bookmarkDtoNameFirst);

				// NAME_LAST
				BookmarkDto bookmarkDtoNameLast = createBookmark(BookmarkConstants.NAME_LAST,
						kinshipNotificatn.getNmPersonLast());
				bookmarkListTmplatAddresse.add(bookmarkDtoNameLast);

				// ADDR_NAME_LAST
				BookmarkDto bookmarkDtoAddrNameLast = createBookmark(BookmarkConstants.ADDR_NAME_LAST,
						kinshipNotificatn.getNmPersonLast());
				bookmarkListTmplatAddresse.add(bookmarkDtoAddrNameLast);

				// ADDR_NAME_MIDDLE
				BookmarkDto bookmarkDtoAddrNameMiddle = createBookmark(BookmarkConstants.ADDR_NAME_MIDDLE,
						kinshipNotificatn.getNmPersonMiddle());
				bookmarkListTmplatAddresse.add(bookmarkDtoAddrNameMiddle);

				// UE_GROUPID
				BookmarkDto bookmarkDtoUEGroupId = createBookmark(BookmarkConstants.UE_GROUPID,
						kinshipNotificatn.getIdPerson());
				bookmarkListTmplatAddresse.add(bookmarkDtoUEGroupId);
				formDataGroupTmplatAddresse.setBookmarkDtoList(bookmarkListTmplatAddresse);
				formDataGroupTmplatAddresse.setFormDataGroupList(formDataGroupDtoList);
				formDataGroupTmplatAddresseList.add(formDataGroupTmplatAddresse);
			}
		}
		PreFillDataServiceDto preFillData = new PreFillDataServiceDto();
		preFillData.setFormDataGroupList(formDataGroupTmplatAddresseList);
		//preFillData.setBookmarkDtoList(bookmarkListTmplatAddresse);
		return preFillData;
	}

}
