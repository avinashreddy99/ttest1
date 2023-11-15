package us.tx.state.dfps.service.admin.daoimpl;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import oracle.jdbc.OracleTypes;
import us.tx.state.dfps.common.domain.EmpJobHistory;
import us.tx.state.dfps.common.domain.EmpSecClassLink;
import us.tx.state.dfps.common.domain.Employee;
import us.tx.state.dfps.common.domain.EmployeeSkill;
import us.tx.state.dfps.common.domain.ExtrnlUser;
import us.tx.state.dfps.common.domain.Person;
import us.tx.state.dfps.common.dto.ServiceReqHeaderDto;
import us.tx.state.dfps.service.admin.dao.EmployeeDao;
import us.tx.state.dfps.service.admin.dto.EmpJobHisDto;
import us.tx.state.dfps.service.admin.dto.EmpSkillDto;
import us.tx.state.dfps.service.admin.dto.EmployeeDetailDto;
import us.tx.state.dfps.service.admin.dto.EmployeeDto;
import us.tx.state.dfps.service.admin.dto.EmployeeSearchDto;
import us.tx.state.dfps.service.admin.dto.SSCCCatchmentDto;
import us.tx.state.dfps.service.common.CodesConstant;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.request.SearchEmployeeReq;
import us.tx.state.dfps.service.common.util.DateUtils;
import us.tx.state.dfps.service.common.util.TypeConvUtil;
import us.tx.state.dfps.service.exception.DataNotFoundException;
import us.tx.state.dfps.service.externaluser.dto.ExternalUserDto;
import us.tx.state.dfps.service.recordscheck.dto.EmployeePersonDto;
import us.tx.state.dfps.service.workload.dto.AssignmentGroupDto;
import us.tx.state.dfps.service.workload.dto.AvailStaffGroupDto;
import us.tx.state.dfps.service.workload.dto.EmployeePersPhNameDto;
import us.tx.state.dfps.service.workload.dto.RCCPWorkloadDto;
import us.tx.state.dfps.web.workload.bean.AssignAvailableStaffBean;
import us.tx.state.dfps.web.workload.bean.AssignBean;

/**
 * ImpactWS - IMPACT PHASE 2 MODERNIZATION Tuxedo Service Name: Class
 * Description: Do the CRUD operation for Employee related data Apr 12, 2017 -
 * 3:00:33 PM
 */
@Repository
public class EmployeeDaoImpl implements EmployeeDao {

	@Autowired
	MessageSource messageSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${EmployeeDaoImpl.searchEmpbyInputSelect}")
	private transient String searchEmpbyInputSelect;

	@Value("${EmployeeDaoImpl.searchEmpbyInputFrom}")
	private transient String searchEmpbyInputFrom;

	@Value("${EmployeeDaoImpl.searchEmpbyJoin_PersonId}")
	private transient String searchEmpbyJoin_PersonId;

	@Value("${EmployeeDaoImpl.searchEmpbyJoin_Unit}")
	private transient String searchEmpbyJoin_Unit;

	@Value("${EmployeeDaoImpl.searchEmpbyJoin_Office}")
	private transient String searchEmpbyJoin_Office;

	@Value("${EmployeeDaoImpl.searchEmpbyJoin_Mail}")
	private transient String searchEmpbyJoin_Mail;

	@Value("${EmployeeDaoImpl.searchEmpbyJoin_EmpSkill}")
	private transient String searchEmpbyJoin_EmpSkill;

	@Value("${EmployeeDaoImpl.searchEmpbyWhere}")
	private transient String searchEmpbyWhere;

	@Value("${EmployeeDaoImpl.searchEmpbystageListSelect}")
	private transient String searchEmpbystageListSelect;

	@Value("${EmployeeDaoImpl.searchEmpbystageListFrom}")
	private transient String searchEmpbystageListFrom;

	@Value("${EmployeeDaoImpl.searchEmpbyPersonId}")
	private transient String searchEmpbyPersonId;

	@Value("${EmployeeDaoImpl.searchEmpbyFirstName}")
	private transient String searchEmpbyFirstName;

	@Value("${EmployeeDaoImpl.searchEmpbyMiddleName}")
	private transient String searchEmpbyMiddleName;

	@Value("${EmployeeDaoImpl.searchEmpbyLastName}")
	private transient String searchEmpbyLastName;

	@Value("${EmployeeDaoImpl.searchEmpbyPgm}")
	private transient String searchEmpbyPgm;

	@Value("${EmployeeDaoImpl.searchEmpJobAssignCurr}")
	private transient String searchEmpJobAssignCurr;

	@Value("${EmployeeDaoImpl.searchEmpbyOfficeMail}")
	private transient String searchEmpbyOfficeMail;

	@Value("${EmployeeDaoImpl.searchEmpbyExtType}")
	private transient String searchEmpbyExtType;

	@Value("${EmployeeDaoImpl.searchEmpbyExtInd}")
	private transient String searchEmpbyExtInd;

	@Value("${EmployeeDaoImpl.searchEmpoerderBy}")
	private transient String searchEmpoerderBy;

	@Value("${EmployeeDaoImpl.searchEmpCount}")
	private transient String searchEmpCount;

	@Value("${EmployeeDaoImpl.getEmployeeById}")
	private String getEmployeeByIdSql;

	@Value("${EmployeeDaoImpl.searchEmploeeSkill}")
	private String searchEmploeeSkillSql;

	@Value("${EmployeeDaoImpl.searchEmployeeByMailCode}")
	private String searchEmployeeByMailCodeSql;

	@Value("${EmployeeDaoImpl.getEmployeeSkillById}")
	private String getEmployeeSkillByIdSql;

	@Value("${EmployeeDaoImpl.getEmpJobHistoryById}")
	private String getEmpJobHistoryByIdSql;

	@Value("${EmployeeDaoImpl.getEmployeeByIdEmpUnit}")
	private String getEmployeeByIdEmpUnitSql;

	@Value("${EmployeeDaoImpl.getEmployeeByIdOffice}")
	private String getEmployeeByIdOfficeSql;

	@Value("${EmployeeDaoImpl.getEmployeeSkillByPersonId}")
	private String getEmployeeSkillByPersonIdSql;

	@Value("${EmployeeDaoImpl.getEmpJobHistoryByPersonId}")
	private String getEmpJobHistoryByPersonIdSql;

	@Value("${EmployeeDaoImpl.searchCurrentSupervisorByPersonId}")
	private String searchCurrentSupervisorByPersonIdSql;

	@Value("${EmployeeDaoImpl.searchCurrentJobByPersonId}")
	private String searchCurrentJobByPersonIdSql;

	@Value("${EmployeeDaoImpl.getEmpJobHistoryByIdAndLastUpdate}")
	private String getEmpJobHistoryByIdAndLastUpdateSql;

	@Value("${EmployeeDaoImpl.getEmployeeByIdAndLastUpdate}")
	private String getEmployeeByIdAndLastUpdateSql;

	@Value("${EmployeeDaoImpl.getEmployeeSkillByIdSkill}")
	private String getEmployeeSkillByIdSkillSql;

	@Value("${EmployeeDaoImpl.complexDeletePerson}")
	private String complexDeletePersonSql;

	@Value("${EmployeeDaoImpl.complexDeleteEmployee}")
	private String complexDeleteEmployeeSql;

	@Value("${EmployeeDaoImpl.deleteEmployeeJobHistoryByPersonId}")
	private String deleteEmployeeJobHistoryByPersonIdSql;

	@Value("${EmployeeDaoImpl.deleteEmployeeHistoryByPersonId}")
	private String deleteEmployeeHistoryByPersonIdSql;

	@Value("${EmployeeDaoImpl.getEmployeeEntityById}")
	private String getEmployeeEntityByIdSql;

	@Value("${EmployeeDaoImpl.deleteApprovalByPersonId}")
	private String deleteApprovalByPersonIdSql;

	@Value("${EmployeeDaoImpl.getEmployeePersonPhoneDtl}")
	String getEmployeePersonPhoneDtlSql;

	@Value("${EmployeeDaoImpl.getEmployeeProfile}")
	private String getEmployeeProfileSql;

	@Value("${EmployeeDaoImpl.getEmployeeProfileById}")
	private String getEmployeeProfileByIdSql;

	@Value("${EmployeeDaoImpl.searchEmpbyUnitRegion}")
	private String searchEmpbyUnitRegion;

	@Value("${EmployeeDaoImpl.searchEmpbyUnitNBRUnit}")
	private String searchEmpbyUnitNBRUnit;

	@Value("${EmployeeDaoImpl.getEmployeeByClass}")
	private String getEmployeeByClass;

	@Value("${EmployeeDaoImpl.searchEmpByIdentifier}")
	private String searchEmpByIdentifier;

	@Value("${EmployeeDaoImpl.searchExtrnlUserbyInputFrom}")
	private String searchExtrnlUserbyInputFrom;
	@Value("${EmployeeDaoImpl.searchExtrnlUserbyWhere}")
	private String searchExtrnlUserbyWhere;
	@Value("${EmployeeDaoImpl.searchExtrnlActiveUsersbyWhere}")
	private String searchExtrnlActiveUsersbyWhere;

	@Value("${EmployeeDaoImpl.searchExtrnlUserbyFirstName}")
	private String searchExtrnlUserbyFirstName;

	@Value("${EmployeeDaoImpl.searchExtrnlUserbyMiddleName}")
	private String searchExtrnlUserbyMiddleName;

	@Value("${EmployeeDaoImpl.searchExtrnlUserbyLastName}")
	private String searchExtrnlUserbyLastName;
	@Value("${EmployeeDaoImpl.searchExtrnlUserbyInputSelect}")
	private String searchExtrnlUserbyInputSelect;

	@Value("${EmployeeDaoImpl.searchEmpByOrganization}")
	private String searchEmpByOrganization;

	@Value("${EmployeeDaoImpl.searchEmpByOrganizationName}")
	private String searchEmpByOrganizationName;

	@Value("${EmployeeDaoImpl.searchEmpByOrganizationEin}")
	private String searchEmpByOrganizationEin;

	@Value("${EmployeeDaoImpl.getExternalEmployeeById}")
	private String getExternalEmployeeByIdSql;

	@Value("${EmployeeDaoImpl.getExtrnlUserEntityById}")
	private String getExtrnlUserEntityById;

	@Value("${EmployeeDaoImpl.externalUserActiveChildPlacements}")
	private String externalUserActiveChildPlacements;

	@Value("${EmployeeDaoImpl.getExternalUserProfileByLogonID}")
	private String getExternalUserProfileByLogonIDSql;

	@Value("${EmployeeDaoImpl.extUserBgCheck}")
	private String extUserBgCheck;

	@Value("${UnitDaoImpl.searchLPSupervisor}")
	private String searchLPSupervisorSql;

	@Value("${EmployeeDaoImpl.getEmployeeEmailAddressForStage}")
	private String getEmployeeEmailAddressForStageSql;

	@Value("${EmployeeDaoImpl.getSSCCCatchmentForVendor}")
	private String getSSCCCatchmentForVendor;

	@Value("${EmployeeDaoImpl.getVendorUrl}")
	private String getVendorUrl;

	private static final Logger log = Logger.getLogger(EmployeeDaoImpl.class);

	public EmployeeDaoImpl() {
		super();
	}

	// query changes made to this method needs to be changed for the same query
	// to get the count in the method EmpbyInputRecCount
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<Long, EmployeeSearchDto> searchEmpbyInput(SearchEmployeeReq searchEmployeeReq) {
		HashMap<Long, EmployeeSearchDto> searchResults;

		boolean isInternalUserSearch = false;
		// isInternalUserSearch is set to true then search is for legacy staff
		// employee
		// otherwise
		// search is for external users
		if (!ObjectUtils.isEmpty(searchEmployeeReq.getSearchForInternalUser())
				&& (searchEmployeeReq.getSearchForInternalUser().booleanValue())) {
			isInternalUserSearch = true;
		}

		StringBuilder qryString = null;
		if (isInternalUserSearch) {
			qryString = new StringBuilder(searchEmpbyInputSelect);
		} else {
			qryString = new StringBuilder(searchExtrnlUserbyInputSelect);
		}

		// search by office is applicable to only legacy staff
		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchCdOfficeCounty()))
			qryString.append(searchEmpbyJoin_Office);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchNbrPersonIdNumber()))
			qryString.append(searchEmpbyJoin_PersonId);

		// search by mail code is applicable to only legacy staff
		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMailCodeCity()))
			qryString.append(searchEmpbyJoin_Mail);

		// search by specialization is applicable to only legacy staff
		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchSpecialization()))
			qryString.append(searchEmpbyJoin_Unit);

		// search by Emp Skill is applicable to only legacy staff
		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchSkill()))
			qryString.append(searchEmpbyJoin_EmpSkill);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchIdentifierNumber()))
			qryString.append(searchEmpByIdentifier);

		// search by specialization is applicable to only legacy staff
		if (!isInternalUserSearch && (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgName())
				|| !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgEIN()))) {
			qryString.append(searchEmpByOrganization);
		}

		// search by organization name is applicable to only external user
		if (!isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgName())) {
			qryString.append(searchEmpByOrganizationName);
		}

		// search by organization EIN is applicable to only external User
		if (!isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgEIN())) {
			qryString.append(searchEmpByOrganizationEin);
		}

		if (isInternalUserSearch) {
			qryString = qryString.append(searchEmpbyWhere);
		} else {
			if (ObjectUtils.isEmpty(searchEmployeeReq.getSearchActiveStatus())) {
				qryString = qryString.append(searchExtrnlUserbyWhere);
			} else {
				qryString = qryString.append(searchExtrnlActiveUsersbyWhere);
			}
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchUnitRegion()))
			qryString = qryString.append(searchEmpbyUnitRegion);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchNBRUnit()))
			qryString = qryString.append(searchEmpbyUnitNBRUnit);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchId()))
			qryString = qryString.append(searchEmpbyPersonId);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchIndExternalType()))
			qryString.append(searchEmpbyExtInd);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchExternalType()))
			qryString.append(searchEmpbyExtType);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchProgram()))
			qryString.append(searchEmpbyPgm);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchindEmpJobAssignCurr()))
			qryString.append(searchEmpJobAssignCurr);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOfficeMail()))
			qryString.append(searchEmpbyOfficeMail);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchFirst())) {
			qryString.append(searchEmpbyFirstName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchFirst())) {
			qryString.append(searchExtrnlUserbyFirstName);
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMiddle())) {
			qryString.append(searchEmpbyMiddleName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMiddle())) {
			qryString.append(searchExtrnlUserbyMiddleName);
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchLast())) {
			qryString.append(searchEmpbyLastName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchLast())) {
			qryString.append(searchExtrnlUserbyLastName);
		}

		qryString = qryString.append(searchEmpoerderBy);
		SQLQuery sQLQuery = null;
		if (isInternalUserSearch) {
			sQLQuery = (SQLQuery) ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(qryString.toString())
					.setString("hI_cScrIndActive", searchEmployeeReq.getSearchActiveStatus()))
							.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFull")
							.addScalar("bjnJob").addScalar("cdEmployeeClass").addScalar("dtEmpLastAssigned")
							.addScalar("unit").addScalar("cdUnitRegion").addScalar("idUnit", StandardBasicTypes.LONG)
							.addScalar("nmOfficeName").addScalar("cdAddrMail").addScalar("cdExternalUserType")
							.addScalar("dtEmpTerm").addScalar("employeeEmailAddress").addScalar("cdStatus")
							.setResultTransformer(Transformers.aliasToBean(EmployeeSearchDto.class));
		} else {
			sQLQuery = (SQLQuery) ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(qryString.toString()))
					.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFull").addScalar("cdStatus")
					.setResultTransformer(Transformers.aliasToBean(EmployeeSearchDto.class));
		}
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitRegion"))
			sQLQuery.setParameter("hI_szCdUnitRegion", searchEmployeeReq.getSearchUnitRegion());

		if (StringUtils.contains(qryString.toString(), "hI_szNbrUnit"))
			sQLQuery.setParameter("hI_szNbrUnit", searchEmployeeReq.getSearchNBRUnit());

		if (StringUtils.contains(qryString.toString(), "hI_szCdOfficeCounty"))
			sQLQuery.setParameter("hI_szCdOfficeCounty", searchEmployeeReq.getSearchCdOfficeCounty());
		if (StringUtils.contains(qryString.toString(), "hI_szNbrPersonIdNumber"))
			sQLQuery.setParameter("hI_szNbrPersonIdNumber", searchEmployeeReq.getSearchNbrPersonIdNumber());
		if (StringUtils.contains(qryString.toString(), "hI_szAddrCity"))
			sQLQuery.setString("hI_szAddrCity",
					TypeConvUtil.stringHelperForLikeCheckAtEnd(searchEmployeeReq.getSearchMailCodeCity()));
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitSpecialization"))
			sQLQuery.setParameter("hI_szCdUnitSpecialization", searchEmployeeReq.getSearchSpecialization());
		if (StringUtils.contains(qryString.toString(), "hI_szCdEmpSkill"))
			sQLQuery.setParameter("hI_szCdEmpSkill", searchEmployeeReq.getSearchSkill());
		if (StringUtils.contains(qryString.toString(), "hI_ulIdPerson"))
			sQLQuery.setParameter("hI_ulIdPerson", searchEmployeeReq.getSearchId());
		if (StringUtils.contains(qryString.toString(), "hI_cIndStaffExtUser"))
			sQLQuery.setParameter("hI_cIndStaffExtUser", searchEmployeeReq.getSearchExternalType());
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitProgram"))
			sQLQuery.setParameter("hI_szCdUnitProgram", searchEmployeeReq.getSearchProgram());
		if (StringUtils.contains(qryString.toString(), "hI_indEmpJobAssignCurr"))
			sQLQuery.setParameter("hI_indEmpJobAssignCurr", searchEmployeeReq.getSearchindEmpJobAssignCurr());
		if (StringUtils.contains(qryString.toString(), "hI_szAddrMailCode"))
			sQLQuery.setParameter("hI_szAddrMailCode", searchEmployeeReq.getSearchOfficeMail());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameFirst"))
			sQLQuery.setString("hI_szNmNameFirst", searchEmployeeReq.getSearchFirst());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameMiddle"))
			sQLQuery.setString("hI_szNmNameMiddle", searchEmployeeReq.getSearchMiddle());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameLast"))
			sQLQuery.setString("hI_szNmNameLast", searchEmployeeReq.getSearchLast());
		if (StringUtils.contains(qryString.toString(), "szIdentifierTypeId")) {
			sQLQuery.setParameter("szIdentifierTypeId", searchEmployeeReq.getSearchIdentifierType());
			sQLQuery.setParameter("szValueId", searchEmployeeReq.getSearchIdentifierNumber());
		}
		if (StringUtils.contains(qryString.toString(), "hI_szOrgName")) {
			sQLQuery.setParameter("hI_szOrgName", searchEmployeeReq.getSearchOrgName());
		}
		if (StringUtils.contains(qryString.toString(), "hI_szOrgEin")) {
			sQLQuery.setParameter("hI_szOrgEin", searchEmployeeReq.getSearchOrgEIN());
		}

		if (searchEmployeeReq.getTotalRecCount() > searchEmployeeReq.getPageSizeNbr()) {
			int firstResult = ((searchEmployeeReq.getPageNbr() - 1) * searchEmployeeReq.getPageSizeNbr());
			sQLQuery.setFirstResult(firstResult);
			sQLQuery.setMaxResults(searchEmployeeReq.getPageSizeNbr());
		}
		List<EmployeeSearchDto> employeeSearchList = new ArrayList<>();

		employeeSearchList = (List<EmployeeSearchDto>) sQLQuery.list();
		searchResults = new HashMap<>();
		searchResults = (HashMap<Long, EmployeeSearchDto>) employeeSearchList.stream()
				.filter(employeeSearch -> null != employeeSearch.getIdPerson()).collect(Collectors.toMap(
						rowEmployeeSearch -> rowEmployeeSearch.getIdPerson(), rowEmployeeSearch -> rowEmployeeSearch));

		log.info("TransactionId :" + searchEmployeeReq.getTransactionId());
		return searchResults;
	}

	// query changes made to this method needs to be changed for the same query
	// to get the count in the method EmpbyStageListRecCount
	@SuppressWarnings("unchecked")
	@Override
	public HashMap<Long, EmployeeSearchDto> searchEmpbystageList(SearchEmployeeReq searchEmployeeReq) {

		List<EmployeeSearchDto> employeeSearchList = new ArrayList<>();
		HashMap<Long, EmployeeSearchDto> searchResults = new HashMap<>();

		SQLQuery sQLQuery = (SQLQuery) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(searchEmpbystageListSelect + searchEmpbystageListFrom)
				.setParameterList("hI_uidstageList", searchEmployeeReq.getSearchstageIds())).addScalar("nmPersonFull")
						.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("bjnJob").addScalar("cdEmployeeClass")
						.addScalar("dtEmpLastAssigned").addScalar("unit").addScalar("cdUnitRegion")
						.addScalar("idUnit", StandardBasicTypes.LONG).addScalar("nmOfficeName").addScalar("cdAddrMail")
						.addScalar("employeeEmailAddress")
						.setResultTransformer(Transformers.aliasToBean(EmployeeSearchDto.class));

		if (searchEmployeeReq.getTotalRecCount() > searchEmployeeReq.getPageSizeNbr()) {
			int firstResult = ((searchEmployeeReq.getPageNbr() - 1) * searchEmployeeReq.getPageSizeNbr());
			sQLQuery.setFirstResult(firstResult);
			sQLQuery.setMaxResults(searchEmployeeReq.getPageSizeNbr());
		}
		employeeSearchList = (List<EmployeeSearchDto>) sQLQuery.list();

		searchResults = (HashMap<Long, EmployeeSearchDto>) employeeSearchList.stream().collect(Collectors
				.toMap(rowEmployeeSearch -> rowEmployeeSearch.getIdPerson(), rowEmployeeSearch -> rowEmployeeSearch));
		log.info("TransactionId :" + searchEmployeeReq.getTransactionId());
		return searchResults;

	}

	@Override
	public Long EmpbyInputRecCount(SearchEmployeeReq searchEmployeeReq) {
		// isInternalUserSearch is set to true then search is for legacy staff
		// employee
		// otherwise
		// search is for external users
		boolean isInternalUserSearch = false;
		if (!ObjectUtils.isEmpty(searchEmployeeReq.getSearchForInternalUser())
				&& (searchEmployeeReq.getSearchForInternalUser().booleanValue())) {
			isInternalUserSearch = true;
		}

		StringBuilder qryString = new StringBuilder(searchEmpCount);
		if (isInternalUserSearch) {
			qryString.append(searchEmpbyInputFrom);
		} else {
			qryString.append(searchExtrnlUserbyInputFrom);
		}

		Long resultCount;
		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchCdOfficeCounty()))
			qryString.append(searchEmpbyJoin_Office);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchNbrPersonIdNumber()))
			qryString.append(searchEmpbyJoin_PersonId);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMailCodeCity()))
			qryString.append(searchEmpbyJoin_Mail);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchSpecialization()))
			qryString.append(searchEmpbyJoin_Unit);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchSkill()))
			qryString.append(searchEmpbyJoin_EmpSkill);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchIdentifierNumber()))
			qryString.append(searchEmpByIdentifier);

		if (!isInternalUserSearch && (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgName())
				|| !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgEIN()))) {
			qryString.append(searchEmpByOrganization);
		}

		if (!isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgName())) {
			qryString.append(searchEmpByOrganizationName);
		}

		if (!isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOrgEIN())) {
			qryString.append(searchEmpByOrganizationEin);
		}

		if (isInternalUserSearch) {
			qryString = qryString.append(searchEmpbyWhere);
		} else {
			if (ObjectUtils.isEmpty(searchEmployeeReq.getSearchActiveStatus())) {
				qryString = qryString.append(searchExtrnlUserbyWhere);
			} else {
				qryString = qryString.append(searchExtrnlActiveUsersbyWhere);
			}
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchUnitRegion()))
			qryString = qryString.append(searchEmpbyUnitRegion);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchNBRUnit()))
			qryString = qryString.append(searchEmpbyUnitNBRUnit);

		if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchId()))
			qryString = qryString.append(searchEmpbyPersonId);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchIndExternalType()))
			qryString.append(searchEmpbyExtInd);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchExternalType()))
			qryString.append(searchEmpbyExtType);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchProgram()))
			qryString.append(searchEmpbyPgm);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchindEmpJobAssignCurr()))
			qryString.append(searchEmpJobAssignCurr);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchOfficeMail()))
			qryString.append(searchEmpbyOfficeMail);

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchFirst())) {
			qryString.append(searchEmpbyFirstName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchFirst())) {
			qryString.append(searchExtrnlUserbyFirstName);
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMiddle())) {
			qryString.append(searchEmpbyMiddleName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchMiddle())) {
			qryString.append(searchExtrnlUserbyMiddleName);
		}

		if (isInternalUserSearch && !TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchLast())) {
			qryString.append(searchEmpbyLastName);
		} else if (!TypeConvUtil.isNullOrEmpty(searchEmployeeReq.getSearchLast())) {
			qryString.append(searchExtrnlUserbyLastName);
		}

		qryString.append(')');
		SQLQuery sQLQuery = (SQLQuery) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(qryString.toString()));
		if (isInternalUserSearch) {
			sQLQuery.setString("hI_cScrIndActive", searchEmployeeReq.getSearchActiveStatus());
		}
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitRegion"))
			sQLQuery.setParameter("hI_szCdUnitRegion", searchEmployeeReq.getSearchUnitRegion());
		if (StringUtils.contains(qryString.toString(), "hI_szNbrUnit"))
			sQLQuery.setParameter("hI_szNbrUnit", searchEmployeeReq.getSearchNBRUnit());

		if (StringUtils.contains(qryString.toString(), "hI_szCdOfficeCounty"))
			sQLQuery.setParameter("hI_szCdOfficeCounty", searchEmployeeReq.getSearchCdOfficeCounty());
		if (StringUtils.contains(qryString.toString(), "hI_szNbrPersonIdNumber"))
			sQLQuery.setParameter("hI_szNbrPersonIdNumber", searchEmployeeReq.getSearchNbrPersonIdNumber());
		if (StringUtils.contains(qryString.toString(), "hI_szAddrCity"))
			sQLQuery.setString("hI_szAddrCity",
					TypeConvUtil.stringHelperForLikeCheckAtEnd(searchEmployeeReq.getSearchMailCodeCity()));
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitSpecialization"))
			sQLQuery.setParameter("hI_szCdUnitSpecialization", searchEmployeeReq.getSearchSpecialization());
		if (StringUtils.contains(qryString.toString(), "hI_szCdEmpSkill"))
			sQLQuery.setParameter("hI_szCdEmpSkill", searchEmployeeReq.getSearchSkill());
		if (StringUtils.contains(qryString.toString(), "hI_ulIdPerson"))
			sQLQuery.setParameter("hI_ulIdPerson", searchEmployeeReq.getSearchId());
		if (StringUtils.contains(qryString.toString(), "hI_cIndStaffExtUser"))
			sQLQuery.setParameter("hI_cIndStaffExtUser", searchEmployeeReq.getSearchExternalType());
		if (StringUtils.contains(qryString.toString(), "hI_szCdUnitProgram"))
			sQLQuery.setParameter("hI_szCdUnitProgram", searchEmployeeReq.getSearchProgram());
		if (StringUtils.contains(qryString.toString(), "hI_indEmpJobAssignCurr"))
			sQLQuery.setParameter("hI_indEmpJobAssignCurr", searchEmployeeReq.getSearchindEmpJobAssignCurr());
		if (StringUtils.contains(qryString.toString(), "hI_szAddrMailCode"))
			sQLQuery.setParameter("hI_szAddrMailCode", searchEmployeeReq.getSearchOfficeMail());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameFirst"))
			sQLQuery.setString("hI_szNmNameFirst", searchEmployeeReq.getSearchFirst());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameMiddle"))
			sQLQuery.setString("hI_szNmNameMiddle", searchEmployeeReq.getSearchMiddle());
		if (StringUtils.contains(qryString.toString(), "hI_szNmNameLast"))
			sQLQuery.setString("hI_szNmNameLast", searchEmployeeReq.getSearchLast());
		if (StringUtils.contains(qryString.toString(), "szIdentifierTypeId")) {
			sQLQuery.setString("szIdentifierTypeId", searchEmployeeReq.getSearchIdentifierType());
			sQLQuery.setString("szValueId", searchEmployeeReq.getSearchIdentifierNumber());
		}
		if (StringUtils.contains(qryString.toString(), "hI_szOrgName")) {
			sQLQuery.setParameter("hI_szOrgName", searchEmployeeReq.getSearchOrgName());
		}
		if (StringUtils.contains(qryString.toString(), "hI_szOrgEin")) {
			sQLQuery.setParameter("hI_szOrgEin", searchEmployeeReq.getSearchOrgEIN());
		}
		resultCount = ((BigDecimal) sQLQuery.uniqueResult()).longValue();

		log.info("TransactionId :" + searchEmployeeReq.getTransactionId());
		return resultCount;
	}

	@Override
	public Long EmpbyStageListRecCount(SearchEmployeeReq searchEmployeeReq) {

		Long resultCount;
		resultCount = ((BigDecimal) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(searchEmpCount + searchEmpbystageListFrom + ")")
				.setParameterList("hI_uidstageList", searchEmployeeReq.getSearchstageIds())).uniqueResult())
						.longValue();
		log.info("TransactionId :" + searchEmployeeReq.getTransactionId());
		return resultCount;
	}

	/**
	 * 
	 * Method Description:getEmployeeById
	 * 
	 * @param employeeId
	 * @return @ @
	 */
	// CCMN04S
	@Override
	public EmployeeDetailDto getEmployeeById(Long employeeId) {

		EmployeeDetailDto employeeDetailDto = new EmployeeDetailDto();

		Query queryEmployee = (sessionFactory.getCurrentSession().createSQLQuery(getEmployeeByIdSql))
				.addScalar("cdEmpProgram", StandardBasicTypes.STRING)
				.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
				.addScalar("employeeClass", StandardBasicTypes.STRING)
				.addScalar("dtEmpHire", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtEmpLastAssigned", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtEmpTermination", StandardBasicTypes.TIMESTAMP)
				.addScalar("idOffice", StandardBasicTypes.LONG).addScalar("idEmpJobHistory", StandardBasicTypes.LONG)
				.addScalar("idEmployeeLogon", StandardBasicTypes.STRING).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("indActiveStatus", StandardBasicTypes.STRING)
				.addScalar("indEmpConfirmedHrmis", StandardBasicTypes.STRING)
				.addScalar("indEmpPendingHrmis", StandardBasicTypes.STRING)
				.addScalar("nbrEmpActivePct", StandardBasicTypes.SHORT)
				.addScalar("tsLastUpdate", StandardBasicTypes.TIMESTAMP)
				.addScalar("cdExternalUserType", StandardBasicTypes.STRING)
				.addScalar("emailAddress", StandardBasicTypes.STRING)
				.addScalar("employeeEmailAddress", StandardBasicTypes.STRING)
				.addScalar("dtHrEmpTerm", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtHrHire", StandardBasicTypes.TIMESTAMP)
				.addScalar("indExtAccess", StandardBasicTypes.STRING).addScalar("birthCity", StandardBasicTypes.STRING)
				.addScalar("cdBirthState", StandardBasicTypes.STRING).addScalar("idReqsitn", StandardBasicTypes.STRING)
				.addScalar("hhscPurchsOrder", StandardBasicTypes.STRING).addScalar("idDept", StandardBasicTypes.STRING)
				.addScalar("roleJobDuty", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(EmployeeDetailDto.class));

		queryEmployee.setParameter("idSearch", employeeId);

		employeeDetailDto = (EmployeeDetailDto) queryEmployee.uniqueResult();

		return employeeDetailDto;
	}

	/**
	 * Method Name:getExternalUserById Method Description:Fetch the external
	 * user information from extrnl_employee table
	 * 
	 * @param userId
	 * @return employeeDetailDto
	 */

	@Override
	public EmployeeDetailDto getExternalUserById(Long userId) {

		EmployeeDetailDto employeeDetailDto = new EmployeeDetailDto();

		Query queryEmployee = (sessionFactory.getCurrentSession().createSQLQuery(getExternalEmployeeByIdSql))
				.addScalar("dtEmpHire", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtEmpTermination", StandardBasicTypes.TIMESTAMP)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("birthCity", StandardBasicTypes.STRING)
				.addScalar("cdBirthState", StandardBasicTypes.STRING)
				.addScalar("indActiveStatus", StandardBasicTypes.STRING)

				.addScalar("tsLastUpdate", StandardBasicTypes.TIMESTAMP)
				.setResultTransformer(Transformers.aliasToBean(EmployeeDetailDto.class));

		queryEmployee.setParameter("idSearch", userId);

		employeeDetailDto = (EmployeeDetailDto) queryEmployee.uniqueResult();

		return employeeDetailDto;
	}

	/**
	 * 
	 * Method Description:searchEmploeeSkill
	 * 
	 * @param searchSkill
	 * @return @ @
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Long> searchEmploeeSkill(String searchSkill) {

		List<Long> searchEmployeeSkills = null;

		Query queryEmployeeSkill = sessionFactory.getCurrentSession().createQuery(searchEmploeeSkillSql);
		queryEmployeeSkill.setParameter("searchSkill", searchSkill);
		queryEmployeeSkill.setMaxResults(100);
		searchEmployeeSkills = queryEmployeeSkill.list();

		if (TypeConvUtil.isNullOrEmpty(searchEmployeeSkills)) {
			throw new DataNotFoundException(
					messageSource.getMessage("EmployeeSkill.not.found.searchSkill", null, Locale.US));
		}

		return searchEmployeeSkills;

	}

	/**
	 * 
	 * Method Description:searchEmployeeByMailCode
	 * 
	 * @param mailCode
	 * @return @ @
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Employee> searchEmployeeByMailCode(String mailCode) {
		List<Employee> employees = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(searchEmployeeByMailCodeSql);
		queryEmployee.setParameter("searchCode", mailCode);
		queryEmployee.setMaxResults(100);
		employees = queryEmployee.list();

		if (TypeConvUtil.isNullOrEmpty(employees)) {
			throw new DataNotFoundException(messageSource.getMessage("Employee.not.found.mailCode", null, Locale.US));
		}

		return employees;
	}

	/**
	 * 
	 * Method Description:updateEmployee
	 * 
	 * @param employee
	 * @ @
	 */
	@Override
	public void updateEmployee(Employee employee) {

		if (employee.getDtEmpHire() != null) {
			Calendar updatedDate = Calendar.getInstance();
			updatedDate.setTime(employee.getDtEmpHire());
			// updatedDate.add(Calendar.DATE, 1);
			employee.setDtEmpHire(updatedDate.getTime());
		}
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(employee));

	}

	/**
	 * Method Name: updateExternalEmployee Method Description:To update external
	 * employee
	 * 
	 * @param ExtrnlUserloyee
	 */
	@Override
	public void updateExternalEmployee(ExtrnlUser ExtrnlUserloyee) {

		if (ExtrnlUserloyee.getDtExtrnlUserStart() != null) {
			Calendar updatedDate = Calendar.getInstance();
			updatedDate.setTime(ExtrnlUserloyee.getDtExtrnlUserStart());
			ExtrnlUserloyee.setDtExtrnlUserStart(updatedDate.getTime());
		}
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(ExtrnlUserloyee));

	}

	/**
	 * 
	 * Method Description:updateEmployeeSkill
	 * 
	 * @param employeeSkill
	 * @ @
	 */
	@Override
	public void updateEmployeeSkill(EmployeeSkill employeeSkill) {
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(employeeSkill));

	}

	/**
	 * 
	 * Method Description:getEmployeeSkillById
	 * 
	 * @param employeeSkillId
	 * @return @ @
	 */
	@Override
	public EmployeeSkill getEmployeeSkillById(Long employeeSkillId) {
		EmployeeSkill employeeSkill = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeSkillByIdSql);
		queryEmployee.setParameter("idSearch", employeeSkillId);

		employeeSkill = (EmployeeSkill) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(employeeSkill)) {
			throw new DataNotFoundException(
					messageSource.getMessage("employeeSkill.not.found.employeeSkillId", null, Locale.US));
		}

		return employeeSkill;
	}

	/**
	 * 
	 * Method Description:getEmpJobHistoryById
	 * 
	 * @param empJobHistoryId
	 * @return @ @
	 */
	@Override
	public EmpJobHistory getEmpJobHistoryById(Long empJobHistoryId) {
		EmpJobHistory empJobHistory = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmpJobHistoryByIdSql);
		queryEmployee.setParameter("idSearch", empJobHistoryId);

		empJobHistory = (EmpJobHistory) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(empJobHistory)) {
			throw new DataNotFoundException(
					messageSource.getMessage("empJobHistory.not.found.empJobHistoryId", null, Locale.US));
		}

		return empJobHistory;
	}

	/**
	 * 
	 * Method Description:updateEmpJobHistory
	 * 
	 * @param empJobHistory
	 * @ @
	 */
	@Override
	public void updateEmpJobHistory(EmpJobHistory empJobHistory) {
		if (empJobHistory.getDtJobStart() != null) {
			Calendar updatedDate = Calendar.getInstance();
			updatedDate.setTime(empJobHistory.getDtJobStart());
			// updatedDate.add(Calendar.DATE, 1);
			empJobHistory.setDtJobStart(updatedDate.getTime());
		}
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(empJobHistory));

	}

	/**
	 * 
	 * Method Description:saveEmpJobHistory
	 * 
	 * @param empJobHistory
	 * @ @
	 */
	@Override
	public void saveEmpJobHistory(EmpJobHistory empJobHistory) {

		if (empJobHistory.getDtJobStart() != null) {
			Calendar updatedDate = Calendar.getInstance();
			updatedDate.setTime(empJobHistory.getDtJobStart());
			// updatedDate.add(Calendar.DATE, 1);
			empJobHistory.setDtJobStart(updatedDate.getTime());
		}
		sessionFactory.getCurrentSession().persist(empJobHistory);

	}

	/**
	 * 
	 * Method Description:saveEmployeeSkill
	 * 
	 * @param employeeSkill
	 * @ @
	 */
	@Override
	public void saveEmployeeSkill(EmployeeSkill employeeSkill) {
		sessionFactory.getCurrentSession().persist(employeeSkill);

	}

	/**
	 * 
	 * Method Description:saveEmployee
	 * 
	 * @param employee
	 * @ @
	 */
	@Override
	public void saveEmployee(Employee employee) {
		if (employee.getDtEmpHire() != null) {
			Calendar updatedDate = Calendar.getInstance();
			updatedDate.setTime(employee.getDtEmpHire());
			// updatedDate.add(Calendar.DATE, 1);
			employee.setDtEmpHire(updatedDate.getTime());
		}
		sessionFactory.getCurrentSession().saveOrUpdate(sessionFactory.getCurrentSession().merge(employee));

	}

	/**
	 * 
	 * Method Description:deleteEmpJobHistory
	 * 
	 * @param empJobHistory
	 * @ @
	 */
	@Override
	public void deleteEmpJobHistory(EmpJobHistory empJobHistory) {

		sessionFactory.getCurrentSession().delete(empJobHistory);

	}

	/**
	 * 
	 * Method Description:deleteEmployeeSkill
	 * 
	 * @param employeeSkill
	 * @ @
	 */
	@Override
	public void deleteEmployeeSkill(EmployeeSkill employeeSkill) {

		sessionFactory.getCurrentSession().delete(employeeSkill);

	}

	/**
	 * 
	 * Method Description:deleteEmployee
	 * 
	 * @param employee
	 * @ @
	 */
	@Override
	public void deleteEmployee(Employee employee) {

		sessionFactory.getCurrentSession().delete(employee);
	}

	/**
	 * 
	 * Method Description:getEmployeeByIdEmpUnit
	 * 
	 * @param idEmpUnit
	 * @return @ @
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<Employee> getEmployeeByIdEmpUnit(Long idEmpUnit) {
		List<Employee> employees = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeByIdEmpUnitSql);
		queryEmployee.setParameter("idSearch", idEmpUnit);
		queryEmployee.setMaxResults(100);
		employees = queryEmployee.list();

		if (TypeConvUtil.isNullOrEmpty(employees)) {
			throw new DataNotFoundException(messageSource.getMessage("employee.not.found.idEmpUnit", null, Locale.US));
		}

		return employees;
	}

	/**
	 * 
	 * Method Description:getEmployeeByIdOffice
	 * 
	 * @param idOffice
	 * @return @ @
	 */

	@SuppressWarnings("unchecked")
	@Override
	public List<Employee> getEmployeeByIdOffice(Long idOffice) {
		List<Employee> employees = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeByIdOfficeSql);
		queryEmployee.setParameter("idSearch", idOffice);
		queryEmployee.setMaxResults(100);
		employees = queryEmployee.list();

		if (TypeConvUtil.isNullOrEmpty(employees)) {
			throw new DataNotFoundException(messageSource.getMessage("employee.not.found.idOffice", null, Locale.US));
		}

		return employees;
	}

	/**
	 * 
	 * Method Description:getEmployeeSkillByPersonId
	 * 
	 * @param personId
	 * @return @ @
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EmpSkillDto> getEmployeeSkillByPersonId(Long personId) {

		List<EmpSkillDto> employeeSkills = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createSQLQuery(getEmployeeSkillByPersonIdSql)
				.addScalar("idEmpSkill", StandardBasicTypes.LONG).addScalar("cdEmpSkill", StandardBasicTypes.STRING)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("dtLastUpdate", StandardBasicTypes.TIMESTAMP)
				.setResultTransformer(Transformers.aliasToBean(EmpSkillDto.class));
		queryEmployee.setParameter("idSearch", personId);
		queryEmployee.setMaxResults(100);
		employeeSkills = new ArrayList<>();
		employeeSkills = queryEmployee.list();

		if (TypeConvUtil.isNullOrEmpty(employeeSkills)) {
			throw new DataNotFoundException(
					messageSource.getMessage("employeeSkill.not.found.personId", null, Locale.US));
		}

		return employeeSkills;

	}

	/**
	 * 
	 * Method Description:getEmployeeSkillByPersonId
	 * 
	 * @param personId
	 * @return @ @
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EmpJobHisDto> getEmpJobHistoryByPersonId(Long personId) {
		List<EmpJobHisDto> empJobHistorys;
		Query queryEmployee = sessionFactory.getCurrentSession().createSQLQuery(getEmpJobHistoryByPersonIdSql)
				.addScalar("bjnJob", StandardBasicTypes.STRING).addScalar("cdJobClass", StandardBasicTypes.STRING)
				.addScalar("cdJobFunction", StandardBasicTypes.STRING)
				.addScalar("dtJobEnd", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtJobStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("idEmpJobHistory", StandardBasicTypes.LONG).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("idJobPersSupv", StandardBasicTypes.LONG)
				.addScalar("nmPersonFull", StandardBasicTypes.STRING)
				.addScalar("indJobAssignable", StandardBasicTypes.STRING)
				.addScalar("tsLastUpdate", StandardBasicTypes.TIMESTAMP)
				.setResultTransformer(Transformers.aliasToBean(EmpJobHisDto.class));
		queryEmployee.setParameter("idSearch", personId);
		queryEmployee.setMaxResults(100);
		empJobHistorys = new ArrayList<>();
		empJobHistorys = queryEmployee.list();

		if (TypeConvUtil.isNullOrEmpty(empJobHistorys)) {
			throw new DataNotFoundException(
					messageSource.getMessage("empJobHistory.not.found.personId", null, Locale.US));
		}

		return empJobHistorys;
	}

	/**
	 * 
	 * Method Description:searchCurrentSupervisorByPersonId
	 * 
	 * @param personId
	 * @return @ @
	 */
	@Override
	public Person searchCurrentSupervisorByPersonId(Long personId) {
		Person person = null;
		Query queryPerson = sessionFactory.getCurrentSession().createQuery(searchCurrentSupervisorByPersonIdSql);
		queryPerson.setParameter("idPerson", personId);
		person = (Person) queryPerson.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(person)) {
			throw new DataNotFoundException(
					messageSource.getMessage("currentSupervisor.not.found.personId", null, Locale.US));
		}

		return person;
	}

	/**
	 * 
	 * Method Description:searchCurrentJobByPersonId
	 * 
	 * @param personId
	 * @return @ @
	 */
	@Override
	public EmpJobHistory searchCurrentJobByPersonId(Long personId) {
		EmpJobHistory empJobHistory = null;

		Query queryPerson = sessionFactory.getCurrentSession().createQuery(searchCurrentJobByPersonIdSql);
		queryPerson.setParameter("idPerson", personId);
		empJobHistory = (EmpJobHistory) queryPerson.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(empJobHistory)) {
			throw new DataNotFoundException(messageSource.getMessage("currentJob.not.found.personId", null, Locale.US));
		}

		return empJobHistory;
	}

	/**
	 * 
	 * Method Description:getEmpJobHistoryByIdAndLastUpdate
	 * 
	 * @param empJobHistoryId
	 * @param date
	 * @return @ @
	 */
	@Override
	public EmpJobHistory getEmpJobHistoryByIdAndLastUpdate(Long empJobHistoryId, Date date) {
		EmpJobHistory empJobHistory = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmpJobHistoryByIdAndLastUpdateSql);
		queryEmployee.setParameter("idSearch", empJobHistoryId);
		queryEmployee.setParameter("date", date);

		empJobHistory = (EmpJobHistory) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(empJobHistory)) {
			throw new DataNotFoundException(
					messageSource.getMessage("empJobHistory.not.found.empJobHistoryId.date", null, Locale.US));
		}

		return empJobHistory;
	}

	/**
	 * 
	 * Method Description:getEmployeeByIdAndLastUpdate
	 * 
	 * @param employeeId
	 * @param date
	 * @return @ @
	 */
	@Override
	public Employee getEmployeeByIdAndLastUpdate(Long employeeId, Date date) {
		Employee employee = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeByIdAndLastUpdateSql);
		queryEmployee.setParameter("idSearch", employeeId);
		queryEmployee.setParameter("date", date);

		employee = (Employee) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(employee)) {
			throw new DataNotFoundException(
					messageSource.getMessage("employee.not.found.employeeId.date", null, Locale.US));
		}

		return employee;
	}

	/**
	 * 
	 * Method Description:getEmployeeSkillByIdSkill
	 * 
	 * @param personId
	 * @param skill
	 * @return @ @
	 */
	@Override
	public EmployeeSkill getEmployeeSkillByIdSkill(Long personId, String skill) {
		EmployeeSkill employeeSkill = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeSkillByIdSkillSql);
		queryEmployee.setParameter("idPerson", personId);
		queryEmployee.setParameter("skill", skill);

		employeeSkill = (EmployeeSkill) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(employeeSkill)) {
			throw new DataNotFoundException(
					messageSource.getMessage("employeeSkill.not.found.personId.skill", null, Locale.US));
		}

		return employeeSkill;
	}

	/**
	 * 
	 * @param personId
	 * @ @
	 */
	// CCMNF7D
	@Override
	public void complexDeletePerson(Long personId) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(complexDeletePersonSql).setParameter("idPerson",
				personId);
		query.executeUpdate();
	}

	@Override
	public void complexDeleteEmployee(Long personId) {

		Query query = sessionFactory.getCurrentSession().createSQLQuery(complexDeleteEmployeeSql)
				.setParameter("idPerson", personId);
		query.executeUpdate();

	}

	@Override
	public void deleteEmployeeHistoryByPersonId(Long personId) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(deleteEmployeeHistoryByPersonIdSql)
				.setParameter("idPerson", personId);
		query.executeUpdate();

	}

	@Override
	public void deleteEmployeeJobHistoryByPersonId(Long personId) {
		Query query = sessionFactory.getCurrentSession().createSQLQuery(deleteEmployeeJobHistoryByPersonIdSql)
				.setParameter("idPerson", personId);
		query.executeUpdate();

	}

	/**
	 * Method Name: getExternalUserEntityById Method Description:To get external
	 * user by user id
	 * 
	 * @param employeeId
	 * @return ExtrnlUserloyee
	 */
	@Override
	public ExtrnlUser getExternalUserEntityById(Long extUserId) {
		ExtrnlUser ExtrnlUserloyee = null;
		Query queryExtrnlUser = sessionFactory.getCurrentSession().createQuery(getExtrnlUserEntityById);
		queryExtrnlUser.setParameter("idExtUser", extUserId);
		ExtrnlUserloyee = (ExtrnlUser) queryExtrnlUser.uniqueResult();
		return ExtrnlUserloyee;
	}

	@Override
	public Employee getEmployeeEntityById(Long employeeId) {
		Employee employee = null;

		Query queryEmployee = sessionFactory.getCurrentSession().createQuery(getEmployeeEntityByIdSql);

		queryEmployee.setParameter("idSearch", employeeId);

		employee = (Employee) queryEmployee.uniqueResult();

		if (TypeConvUtil.isNullOrEmpty(employee)) {
			throw new DataNotFoundException(messageSource.getMessage("employee.not.found.employeeId", null, Locale.US));
		}

		return employee;
	}

	@Override
	public void deleteApprovalByPersonId(Long personId) {

		Query query = sessionFactory.getCurrentSession().createSQLQuery(deleteApprovalByPersonIdSql)
				.setParameter("idPerson", personId);
		query.executeUpdate();

	}

	/**
	 * 
	 * Method Description: This Method is used to perform update operation in
	 * employee table based on idPerson Service Name: CCMN25S Dam Name : CCMN81D
	 * 
	 * @param idPerson
	 * @
	 */
	@Override
	public String getEmployeeUpdate(Long idPerson, ServiceReqHeaderDto serviceReqHeaderDto) {

		Employee employeeEntity = new Employee();
		String returnMsg = "";
		Date currentDate = new Date();
		Criteria cr = sessionFactory.getCurrentSession().createCriteria(Employee.class)
				.add(Restrictions.eq("idPerson", idPerson));
		employeeEntity = (Employee) cr.uniqueResult();
		employeeEntity.setIdPerson(idPerson);
		employeeEntity.setDtLastUpdate(currentDate);
		employeeEntity.setDtEmpLastAssigned(currentDate);

		sessionFactory.getCurrentSession().saveOrUpdate(employeeEntity);
		returnMsg = ServiceConstants.SUCCESS;
		return returnMsg;
	}

	/**
	 * 
	 * Method Description: This Method will retrieve the active primary address,
	 * phone number, and name for an employee Dam Name : CSEC01D
	 * 
	 * @param idPerson
	 * @return PersonPhoneNameDto @
	 */
	public EmployeePersPhNameDto searchPersonPhoneName(Long idPerson) {

		EmployeePersPhNameDto employeePersPhNameDto = null;

		employeePersPhNameDto = (EmployeePersPhNameDto) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getEmployeePersonPhoneDtlSql).setParameter("uIdPerson", idPerson))
						.addScalar("idPerson", StandardBasicTypes.LONG)
						.addScalar("nbrEmpActivePct", StandardBasicTypes.LONG)
						.addScalar("dtEmpHire", StandardBasicTypes.TIMESTAMP)
						.addScalar("idEmpJobHistory", StandardBasicTypes.LONG)
						.addScalar("idEmployeeLogon", StandardBasicTypes.STRING)
						.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
						.addScalar("txtEmployeeClass", StandardBasicTypes.STRING)
						.addScalar("cdEmpSecurityClassNm", StandardBasicTypes.STRING)
						.addScalar("idOffice", StandardBasicTypes.LONG)
						.addScalar("dtEmpLastAssigned", StandardBasicTypes.TIMESTAMP)
						.addScalar("cdEmpProgram", StandardBasicTypes.STRING)
						.addScalar("indEmpConfirmedHrmis", StandardBasicTypes.STRING)
						.addScalar("indEmpPendingHrmis", StandardBasicTypes.STRING)
						.addScalar("indActiveStatus", StandardBasicTypes.STRING)
						.addScalar("dtEmpTermination", StandardBasicTypes.TIMESTAMP)
						.addScalar("idUnit", StandardBasicTypes.LONG)
						.addScalar("idJobPersSupv", StandardBasicTypes.LONG)
						.addScalar("cdJobClass", StandardBasicTypes.STRING)
						.addScalar("txtJobDesc", StandardBasicTypes.STRING)
						.addScalar("indJobAssignable", StandardBasicTypes.STRING)
						.addScalar("cdJobFunction", StandardBasicTypes.STRING)
						.addScalar("bjnJob", StandardBasicTypes.STRING)
						.addScalar("dtJobEnd", StandardBasicTypes.TIMESTAMP)
						.addScalar("dtJobStart", StandardBasicTypes.TIMESTAMP)
						.addScalar("cdOfficeMail", StandardBasicTypes.STRING)
						.addScalar("cdOfficeProgram", StandardBasicTypes.STRING)
						.addScalar("cdOfficeRegion", StandardBasicTypes.STRING)
						.addScalar("nmOfficeName", StandardBasicTypes.STRING)
						.addScalar("mailCodePhone", StandardBasicTypes.STRING)
						.addScalar("mailCodePhoneExt", StandardBasicTypes.STRING)
						.addScalar("addrMailCodeStLn1", StandardBasicTypes.STRING)
						.addScalar("addrMailCodeStLn2", StandardBasicTypes.STRING)
						.addScalar("addrMailCodeCity", StandardBasicTypes.STRING)
						.addScalar("addrMailCodeZip", StandardBasicTypes.STRING)
						.addScalar("addrMailCodeCounty", StandardBasicTypes.STRING)
						.addScalar("indMailCodeInvalid", StandardBasicTypes.STRING)
						.addScalar("idPhone", StandardBasicTypes.LONG)
						.addScalar("phoneComments", StandardBasicTypes.STRING)
						.addScalar("nbrPhoneExtension", StandardBasicTypes.STRING)
						.addScalar("nbrPhone", StandardBasicTypes.STRING)
						.addScalar("dtPersonPhoneStart", StandardBasicTypes.TIMESTAMP)
						.addScalar("dtPersonPhoneEnd", StandardBasicTypes.TIMESTAMP)
						.addScalar("indPersonPhoneInvalid", StandardBasicTypes.STRING)
						.addScalar("indPersonPhonePrimary", StandardBasicTypes.STRING)
						.addScalar("cdPhoneType", StandardBasicTypes.STRING)
						.addScalar("idName", StandardBasicTypes.LONG)
						.addScalar("indNameInvalid", StandardBasicTypes.STRING)
						.addScalar("nmNameFirst", StandardBasicTypes.STRING)
						.addScalar("nmNameMiddle", StandardBasicTypes.STRING)
						.addScalar("nmNameLast", StandardBasicTypes.STRING)
						.addScalar("indNamePrimary", StandardBasicTypes.STRING)
						.addScalar("cdNameSuffix", StandardBasicTypes.STRING)
						.addScalar("dtNameStart", StandardBasicTypes.TIMESTAMP)
						.addScalar("dtNameEnd", StandardBasicTypes.TIMESTAMP)
						.setResultTransformer(Transformers.aliasToBean(EmployeePersPhNameDto.class)).uniqueResult();

		return employeePersPhNameDto;
	}

	/**
	 * 
	 * Method Description: This method gets Employee Details using Employee
	 * LogonId - Service Name: CARC01S Dam Name : CARC01D
	 * 
	 * @param employeeLogonId
	 * @return employee @ @
	 * 
	 */
	// CARC01D
	@Override
	public EmployeeDto getEmployeeByLogonId(String employeeLogonId) {
		EmployeeDto employee = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createSQLQuery(getEmployeeProfileSql)
				.addScalar("idHrEmp", StandardBasicTypes.STRING).addScalar("idEmployeeLogon", StandardBasicTypes.STRING)
				.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
				.addScalar("cdExternalType", StandardBasicTypes.STRING)
				.addScalar("emailAddress", StandardBasicTypes.STRING)
				.addScalar("employeeEmailAddress", StandardBasicTypes.STRING)
				.addScalar("personFullName", StandardBasicTypes.STRING)
				.addScalar("cdUnitRegion", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeCity", StandardBasicTypes.STRING).addScalar("idOffice", StandardBasicTypes.LONG)
				.addScalar("nbrUnit", StandardBasicTypes.STRING).addScalar("cdUnitProgram", StandardBasicTypes.STRING)
				.addScalar("indExternal", StandardBasicTypes.STRING)
				.addScalar("employeeClass", StandardBasicTypes.STRING).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("cdNmUserSuffix").setResultTransformer(Transformers.aliasToBean(EmployeeDto.class));

		queryEmployee.setParameter("employeeLogon", employeeLogonId);

		/*
		 * String queryString =
		 * "from Employee where ID_EMPLOYEE_LOGON = :employeeLogonId"; Query
		 * query = sessionFactory.getCurrentSession().createQuery(queryString);
		 * 
		 * query.setString("employeeLogonId", employeeLogonId);
		 */
		List<EmployeeDto> employeeList = (List<EmployeeDto>) queryEmployee.list();
		if (!ObjectUtils.isEmpty(employeeList)) {
			employee = employeeList.get(0);
		}

		return employee;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * us.tx.state.dfps.service.admin.dao.EmployeeDao#getEmployeeByIdUser(java.
	 * lang. Long)
	 */
	@Override
	public EmployeeDto getEmployeeByIdUser(Long idUser) {
		EmployeeDto employee = null;
		Query queryEmployee = sessionFactory.getCurrentSession().createSQLQuery(getEmployeeProfileByIdSql)
				.addScalar("idHrEmp", StandardBasicTypes.STRING).addScalar("idEmployeeLogon", StandardBasicTypes.STRING)
				.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
				.addScalar("cdExternalType", StandardBasicTypes.STRING)
				.addScalar("emailAddress", StandardBasicTypes.STRING)
				.addScalar("employeeEmailAddress", StandardBasicTypes.STRING)
				.addScalar("personFullName", StandardBasicTypes.STRING)
				.addScalar("cdUnitRegion", StandardBasicTypes.STRING)
				.addScalar("addrMailCodeCity", StandardBasicTypes.STRING).addScalar("idOffice", StandardBasicTypes.LONG)
				.addScalar("nbrUnit", StandardBasicTypes.STRING).addScalar("cdUnitProgram", StandardBasicTypes.STRING)
				.addScalar("indExternal", StandardBasicTypes.STRING)
				.addScalar("employeeClass", StandardBasicTypes.STRING).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("cdNmUserSuffix").setResultTransformer(Transformers.aliasToBean(EmployeeDto.class));

		queryEmployee.setParameter("idSearch", idUser);

		List<EmployeeDto> employeeList = (List<EmployeeDto>) queryEmployee.list();
		if (!ObjectUtils.isEmpty(employeeList)) {
			employee = employeeList.get(0);
		}

		return employee;
	}

	/**
	 * Method Name: updateEmployeeLogon Method Description: This method updates
	 * the ID_EMPLOYEE_LOGON column
	 * 
	 * @param employeeLogonId
	 * @return @ @
	 * 
	 */
	// New DAO
	@Override
	public void updateEmployeeLogon(String employeeLogonId, Long personId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class);
		// First find if the logged in user is associated with any employee-id.
		// If yes,
		// then set the logon user to null.
		Employee employee = (Employee) criteria.add(Restrictions.eq("idEmployeeLogon", employeeLogonId)).uniqueResult();
		if (employee != null) {
			employee.setIdEmployeeLogon("");
			sessionFactory.getCurrentSession().saveOrUpdate(employee);
		}
		// Associate the logged-on user with the employee-id
		employee = (Employee) sessionFactory.getCurrentSession().get(Employee.class, personId);
		if (TypeConvUtil.isNullOrEmpty(employee)) {
			throw new DataNotFoundException(messageSource.getMessage("employee.not.found.employeeId", null, Locale.US));
		}
		employee.setIdEmployeeLogon(employeeLogonId);
		sessionFactory.getCurrentSession().saveOrUpdate(employee);

	}

	/**
	 * Method Description: This method get the This method get the
	 * PlusCF1050BSecAttr Enabled
	 * 
	 * @param personId
	 */
	public boolean getSecAttrEnabled(Long ulIdPerson) {
		boolean isEnabled = false;
		// Employee Log on from idPerson.
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class);
		criteria.add(Restrictions.eq("idPerson", ulIdPerson));
		Employee employee = (Employee) criteria.uniqueResult();
		if (!ObjectUtils.isEmpty(employee) && ObjectUtils.isEmpty(employee.getIdEmployeeLogon())) {
			isEnabled = false;
		} else {
			isEnabled = true;
		}
		// Check the Security Flag If the Log on ID is available
		if (isEnabled) {
			Criteria cr = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class);
			cr.add(Restrictions.eq("idPerson", ulIdPerson));
			cr.add(Restrictions.eq(ServiceConstants.SECURITY_CLASS, ServiceConstants.PLUS_CF_1050B));
			cr.setProjection(Projections.rowCount());
			List rowCount = cr.list();
			if (!TypeConvUtil.isNullOrEmpty(rowCount) && (Long) rowCount.get(0) > 0) {
				isEnabled = true;
			}
			else {
				isEnabled = false;
			}
		}
		return isEnabled;

	}

	/**
	 * Method Name: getEmployeeOfficeIdentifier Method Description:returns the
	 * employee office id based on a staff id
	 * 
	 * @param idEmployee
	 * @return Long
	 */
	public Long getEmployeeOfficeIdentifier(Long idEmployee) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class);
		criteria.add(Restrictions.eq("idPerson", idEmployee));
		Employee employee = (Employee) criteria.uniqueResult();
		if (!ObjectUtils.isEmpty(employee)) {
			return employee.getOffice().getIdOffice();
		} else {
			return 0l;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * us.tx.state.dfps.service.admin.dao.EmployeeDao#getEmployeeByClass(java.
	 * lang. String, java.lang.String)
	 */
	// UIDS 2.3.3.5 - Remove a child from home - To-Do Detail
	@SuppressWarnings("unchecked")
	@Override
	public List<EmployeePersonDto> getEmployeeByClass(String employeeClass, String securityClass) {
		List<EmployeePersonDto> employeePersonDtoList = (List<EmployeePersonDto>) ((SQLQuery) sessionFactory
				.getCurrentSession().createSQLQuery(getEmployeeByClass).setParameter("securityClass", securityClass))
						.addScalar("nmEmployeeFirst", StandardBasicTypes.STRING)
						.addScalar("nmEmployeeMiddle", StandardBasicTypes.STRING)
						.addScalar("nmEmployeeLast", StandardBasicTypes.STRING)
						.addScalar("idPerson", StandardBasicTypes.LONG)
						.addScalar("cdEmpBjnEmp", StandardBasicTypes.STRING)
						.addScalar("cdEmployeeClass", StandardBasicTypes.STRING)
						.addScalar("dtEmpLastAssigned", StandardBasicTypes.DATE)
						.addScalar("nbrEmpUnitEmpIn", StandardBasicTypes.STRING)
						.addScalar("cdEmpUnitRegion", StandardBasicTypes.STRING)
						.addScalar("idEmpUnit", StandardBasicTypes.LONG)
						.addScalar("nmEmpOfficeName", StandardBasicTypes.STRING)
						.addScalar("cdEmpOfficeMail", StandardBasicTypes.STRING)
						.addScalar("cdExternalType", StandardBasicTypes.STRING)
						.addScalar("dtPersonBirth", StandardBasicTypes.DATE)
						.addScalar("txtEmailAddress", StandardBasicTypes.STRING)
						.addScalar("txtEmployeeEmailAddress", StandardBasicTypes.STRING)
						.addScalar("cdEmpProgram", StandardBasicTypes.STRING)
						.addScalar("nmPersonFull", StandardBasicTypes.STRING)
						.addScalar("nbrPersonPhone", StandardBasicTypes.STRING)
						.addScalar("nbrPersonPhoneExtension", StandardBasicTypes.STRING)

						.setResultTransformer(Transformers.aliasToBean(EmployeePersonDto.class)).list();
		return employeePersonDtoList;
	}

	/**
	 * 
	 * Method Name: isExternalStaff Method Description: Check is the person is
	 * external staff
	 * 
	 * @param idPerson
	 * @return boolean
	 */
	@Override
	public boolean isExternalStaff(Long idPerson) {
		Employee employee = (Employee) sessionFactory.getCurrentSession().createCriteria(Employee.class)
				.add(Restrictions.eq("idPerson", idPerson)).uniqueResult();
		if (!ObjectUtils.isEmpty(employee) && CodesConstant.CEMPJBCL_EXST.equals(employee.getCdEmployeeClass())) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * Method Name: getLocalPlacementSupervisor Method Description:Get LPS
	 * Supervisor for the region
	 * 
	 * @param cdRegion
	 * @return Employee
	 */
	@Override
	public Employee getLocalPlacementSupervisor(String cdRegion) {
		// Get LIP Supervisor List based on Region
		Criteria empSecClassLinkCriteria = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class)
				.add(Restrictions.eq("securityClass.cdSecurityClassName", ServiceConstants.LP_SUPVR_ASSIGN));
		List<EmpSecClassLink> empSecClassLinkList = empSecClassLinkCriteria.list();
		List<Long> personIdList = new ArrayList<>();
		empSecClassLinkList.stream().forEach(empSecClassLink -> personIdList.add(empSecClassLink.getIdPerson()));
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class)
				.add(Restrictions.eq("cdEmpUnitRegion", cdRegion)).add(Restrictions.isNull("dtEmpTermination"))
				.add(Restrictions.in("idPerson", personIdList));
		List<Employee> employeeList = criteria.list();
		if (CollectionUtils.isNotEmpty(employeeList)) {
			if (employeeList.size() == 1)
				// If only one record, then return
				return employeeList.get(0);
			else {
				// If there are more than one record, Get the one who has more
				// work experience
				// Find the employee job history
				Comparator<Employee> comparator = (Employee e1, Employee e2) -> {
					Criteria criteria1 = sessionFactory.getCurrentSession().createCriteria(EmpJobHistory.class)
							.add(Restrictions.eq("person.idPerson", e1.getIdPerson()))
							.add(Restrictions.eq("dtJobEnd", DateUtils.getMaxJavaDate()));
					EmpJobHistory empJobHistory1 = (EmpJobHistory) criteria1.uniqueResult();
					Criteria criteria2 = sessionFactory.getCurrentSession().createCriteria(EmpJobHistory.class)
							.add(Restrictions.eq("person.idPerson", e2.getIdPerson()))
							.add(Restrictions.eq("dtJobEnd", DateUtils.getMaxJavaDate()));
					EmpJobHistory empJobHistory2 = (EmpJobHistory) criteria2.uniqueResult();
					return empJobHistory1.getDtJobStart().compareTo(empJobHistory2.getDtJobStart());
				};
				employeeList.stream().sorted(comparator);
				return employeeList.get(0);
			}
		}
		return null;
	}

	/**
	 * 
	 * Method Name: getLocalPlacementSupervisorList Method Description:Get LPS
	 * Supervisor List for the region and populate the Assign page bean which
	 * has available staff details and assignment details.
	 *
	 * @param cdRegion
	 * @return AssignBean
	 */
	@Override
	public AssignBean getLocalPlacementSupervisorList(String cdRegion, AssignBean assignBean) {

		// Get the Employee Security Class for LPS
		Criteria empSecClassLinkCriteria = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class)
				.add(Restrictions.eq("securityClass.cdSecurityClassName", ServiceConstants.LP_SUPVR_ASSIGN));
		List<EmpSecClassLink> empSecClassLinkList = empSecClassLinkCriteria.list();

		// Get the person ID List
		List<Long> personIdList = new ArrayList<>();
		empSecClassLinkList.stream().forEach(empSecClassLink -> personIdList.add(empSecClassLink.getIdPerson()));

		// Get the Employee List based on security class
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class)
				.add(Restrictions.eq("cdEmpUnitRegion", cdRegion)).add(Restrictions.isNull("dtEmpTermination"))
				.add(Restrictions.in("idPerson", personIdList));
		List<Employee> employeeList = criteria.list();

		// Longest Tenured Supervisor
		Long idMostTnrdSuperVisor = 0L;

		// Get the First Employee Person ID
		idMostTnrdSuperVisor = !CollectionUtils.isEmpty(employeeList) ? employeeList.get(0).getIdPerson() : 0L;

		assignBean.setIdLongestTenuredEmp(idMostTnrdSuperVisor);

		// If there are more than one record, Get the one who has more work
		// experience
		// Find the employee job history
		if (CollectionUtils.isNotEmpty(employeeList) && employeeList.size() > 1) {
			Comparator<Employee> comparator = (Employee e1, Employee e2) -> {
				Criteria criteria1 = sessionFactory.getCurrentSession().createCriteria(EmpJobHistory.class)
						.add(Restrictions.eq("person.idPerson", e1.getIdPerson()))
						.add(Restrictions.eq("dtJobEnd", DateUtils.getMaxJavaDate()));
				EmpJobHistory empJobHistory1 = (EmpJobHistory) criteria1.uniqueResult();
				Criteria criteria2 = sessionFactory.getCurrentSession().createCriteria(EmpJobHistory.class)
						.add(Restrictions.eq("person.idPerson", e2.getIdPerson()))
						.add(Restrictions.eq("dtJobEnd", DateUtils.getMaxJavaDate()));
				EmpJobHistory empJobHistory2 = (EmpJobHistory) criteria2.uniqueResult();
				return empJobHistory1.getDtJobStart().compareTo(empJobHistory2.getDtJobStart());
			};
			employeeList.stream().sorted(comparator);

		}
		List<AvailStaffGroupDto> availStaff = new ArrayList<AvailStaffGroupDto>();
		// Get the Unit Mapping for the LPS Employees
		if (CollectionUtils.isNotEmpty(personIdList)) {
			for (Employee empDto : employeeList) {
				AvailStaffGroupDto availStaffGroupDto = getAvailableStaffGroupDto(empDto.getIdPerson(),
						empDto.getIdEmpUnit(), empDto.getNbrEmpUnitEmpIn());
				if (!ObjectUtils.isEmpty(availStaffGroupDto)) {
					availStaff.add(availStaffGroupDto);
				}
			}
		}
		populateStaffBeans(assignBean, availStaff);
		return assignBean;
	}

	/**
	 * Gets the available staff group .
	 *
	 * @param idPerson
	 *            the id person
	 * @param idUnit
	 *            the id unit
	 * @param nbrUnit
	 *            the unit no
	 * @return the available staff group
	 */
	public AvailStaffGroupDto getAvailableStaffGroupDto(Long idPerson, Long idUnit, String nbrUnit) {
		AvailStaffGroupDto availStaffGroupDto = new AvailStaffGroupDto();
		Query query = (Query) sessionFactory.getCurrentSession().createSQLQuery(searchLPSupervisorSql)
				.addScalar("unit", StandardBasicTypes.STRING).addScalar("nmPersonFull", StandardBasicTypes.STRING)
				.addScalar("bjnJob", StandardBasicTypes.STRING)
				.addScalar("dtEmpLastAssigned", StandardBasicTypes.TIMESTAMP)
				.addScalar("phone", StandardBasicTypes.STRING).addScalar("phoneExtension", StandardBasicTypes.STRING)
				.addScalar("nmOfficeName", StandardBasicTypes.STRING).addScalar("idUnit", StandardBasicTypes.LONG)
				.addScalar("idPerson", StandardBasicTypes.LONG).setParameter("idUnit", idUnit)
				.setParameter("idPerson", idPerson).setParameter("nbrUnit", nbrUnit)
				.setResultTransformer(Transformers.aliasToBean(AvailStaffGroupDto.class));
		availStaffGroupDto = (AvailStaffGroupDto) query.uniqueResult();
		return availStaffGroupDto;
	}

	/**
	 * Populate staff beans.
	 *
	 * @param assignBean
	 *            the assign bean
	 * @param availStaff
	 *            the avail staff List
	 * @param staffAssignmentsList
	 *            the staff assignments list
	 */
	public void populateStaffBeans(AssignBean assignBean, List<AvailStaffGroupDto> availStaff) {
		List<AssignAvailableStaffBean> assignAvailableStaffList = new ArrayList<AssignAvailableStaffBean>();
		for (AvailStaffGroupDto availStaffGroupDto : availStaff) {
			AssignAvailableStaffBean assignAvailableStaffBean = new AssignAvailableStaffBean();
			if (null != availStaffGroupDto) {
				assignAvailableStaffBean.setIdUnit(availStaffGroupDto.getIdUnit());
				assignAvailableStaffBean.setStaffName(availStaffGroupDto.getNmPersonFull());
				assignAvailableStaffBean.setIdStaff(availStaffGroupDto.getIdPerson());
				assignAvailableStaffBean.setLastAssigned(availStaffGroupDto.getDtEmpLastAssigned());
				assignAvailableStaffBean.setPhone(availStaffGroupDto.getPhone());
				assignAvailableStaffBean.setExtn(availStaffGroupDto.getPhoneExtension());
				assignAvailableStaffBean.setOfficeName(availStaffGroupDto.getNmOfficeName());
				assignAvailableStaffBean.setBjnJob(availStaffGroupDto.getBjnJob());
				assignAvailableStaffBean.setUnit(availStaffGroupDto.getUnit());
				assignAvailableStaffBean.setAssignStaffSelected(availStaffGroupDto.getIdPerson().toString());
				//Added to include External Organization in Assign page PPM#45615
				assignAvailableStaffBean.setExternalOrganization(availStaffGroupDto.getExternalOrg());
				assignAvailableStaffList.add(assignAvailableStaffBean);
			}
		}
		assignBean.setAssignAvailableStaffList(assignAvailableStaffList);
	}

	/**
	 * 
	 * Method Name: getEmployeeByEmployeeId Method Description: Get Employee by
	 * employee person Id
	 * 
	 * @param idEmp
	 * @return Employee
	 */
	@Override
	public Employee getEmployeeByEmployeeId(Long idEmp) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class)
				.add(Restrictions.eq("idPerson", idEmp));
		return (Employee) criteria.uniqueResult();
	}

	/**
	 * Method Name: impactAccessForExternalUser Method Description: impact
	 * access check for external user
	 * 
	 * @param idPerson
	 * @return boolean
	 */
	@Override
	public boolean impactAccessForExternalUser(Long idperson) {
		boolean impactAccess = false;
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class);
		criteria.add(Restrictions.eq("idPerson", idperson));
		criteria.setProjection(Projections.rowCount());
		List rowCount = criteria.list();
		if (!TypeConvUtil.isNullOrEmpty(rowCount) && (Long) rowCount.get(0) > 0) {
			impactAccess = true;
		}
		return impactAccess;
	}

	/**
	 * Method Name: externalUserAccessToChildActivePlacement Method Description:
	 * access check for external user to child placement resource
	 * 
	 * @param idExternalUser
	 ** @param idChild
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean externalUserAccessToChildActivePlacement(Long idExternalUser, Long idChild) {
		boolean externalUserAccess = false;

		List<RCCPWorkloadDto> rccpWorkloadList = (List<RCCPWorkloadDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(externalUserActiveChildPlacements).setParameter("idPerson", idExternalUser))
						.addScalar("idPerson", StandardBasicTypes.LONG)
						.setResultTransformer(Transformers.aliasToBean(RCCPWorkloadDto.class)).list();
		if (!ObjectUtils.isEmpty(rccpWorkloadList)) {
			RCCPWorkloadDto rccpWorkloadDto = rccpWorkloadList.stream()
					.filter(rccpWorkload -> idChild.equals(rccpWorkload.getIdPerson())).findAny().orElse(null);
			if (!ObjectUtils.isEmpty(rccpWorkloadDto)) {
				externalUserAccess = true;
			}
		}
		return externalUserAccess;

	}

	/**
	 * 
	 * Method Description: This method gets External User Details using
	 * extUserId
	 * 
	 * @param extUserId
	 * @return ExternalUserDto
	 * 
	 */
	@Override
	public ExternalUserDto getExtUserByLogonId(String extUserId) {
		ExternalUserDto externalUserDto = null;
		Query queryExternalUser = sessionFactory.getCurrentSession().createSQLQuery(getExternalUserProfileByLogonIDSql)
				.addScalar("idExtrnlUser", StandardBasicTypes.LONG).addScalar("idPerson", StandardBasicTypes.LONG)
				.addScalar("dtLastUpdate", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtExtrnlUserStart", StandardBasicTypes.TIMESTAMP)
				.addScalar("dtExtrnlUserEnd", StandardBasicTypes.TIMESTAMP)
				.addScalar("nmExtrnlUserFirst", StandardBasicTypes.STRING)
				.addScalar("nmExtrnlUserMiddle", StandardBasicTypes.STRING)
				.addScalar("nmExtrnlUserLast", StandardBasicTypes.STRING)
				.addScalar("indExtrnlUserConsent", StandardBasicTypes.STRING)
				.addScalar("dtLastLogin", StandardBasicTypes.TIMESTAMP)
				.addScalar("idLastUpdatePerson", StandardBasicTypes.LONG)
				.addScalar("dtCreated", StandardBasicTypes.TIMESTAMP)
				.addScalar("idCreatedPerson", StandardBasicTypes.LONG)
				.setResultTransformer(Transformers.aliasToBean(ExternalUserDto.class));

		queryExternalUser.setParameter("idLdapUser", extUserId);
		List<ExternalUserDto> externalUserList = (List<ExternalUserDto>) queryExternalUser.list();
		if (!ObjectUtils.isEmpty(externalUserList)) {
			externalUserDto = externalUserList.get(0);
			if (ObjectUtils.isEmpty(externalUserDto.getDtExtrnlUserEnd())) {
				externalUserDto.setExtUserActiveStatus(true);
			}
		}
		return externalUserDto;
	}

	/**
	 * Method Name: externalUserBackGroundCheck Method Description: return true
	 * for external user back ground check passes
	 * 
	 * @param extUserId
	 * @return boolean
	 */
	@Override
	public boolean externalUserBackGroundCheck(long extUserId) {

		boolean bckgCheck = false;
		SessionImplementor sessionImplementor = (SessionImplementor) sessionFactory.getCurrentSession();
		try {
			Connection connection = sessionImplementor.getJdbcConnectionAccess().obtainConnection();
			CallableStatement callableStatement = connection.prepareCall(extUserBgCheck);
			try {
				callableStatement.setLong(1, extUserId);
				callableStatement.registerOutParameter(2, OracleTypes.VARCHAR);
				callableStatement.registerOutParameter(3, OracleTypes.VARCHAR);

				callableStatement.execute();
				String bckgCheckStr = callableStatement.getString(2);
				if (ServiceConstants.Y.equals(bckgCheckStr)) {
					bckgCheck = true;
				}

				if (bckgCheck) {
					setExtrnlUserLoginUpdateLastDateTime(extUserId);
				}

			} catch (Exception e) {
				log.debug("Exception occured while accessing the stored procedure " + e.getMessage());
			} finally {
				if (!ObjectUtils.isEmpty(callableStatement))
					callableStatement.close();
				if (!ObjectUtils.isEmpty(connection))
					connection.close();
			}
		} catch (SQLException e1) {
			log.debug("Exception occured while accessing the stored procedure " + e1.getMessage());
		}
		return bckgCheck;
	}

	/**
	 * Method Name: setExtrnlUserLoginUpdateLastDateTime Method Description:This
	 * method will update last login for external user
	 * 
	 * @param idExtrnlUser
	 */
	@Override
	public void setExtrnlUserLoginUpdateLastDateTime(Long idExtrnlUser) {
		// Fetch entity for external login user
		ExtrnlUser extrnlUser = (ExtrnlUser) sessionFactory.getCurrentSession().createCriteria(ExtrnlUser.class)
				.add(Restrictions.eq("idPerson", idExtrnlUser)).uniqueResult();
		if (!ObjectUtils.isEmpty(extrnlUser)) {
			extrnlUser.setDtLastLogin(new Date());
			sessionFactory.getCurrentSession().update(extrnlUser);
		}

	}

	/**
	 * Return assignment group DTO.
	 *
	 * @param assignmentGroupDtos
	 *            the assignment group DTO's
	 * @param indCSSReviewContact
	 *            the indicator CSS review contact
	 * @return the stream
	 */
	Stream<AssignmentGroupDto> returnAssignmentGroupDto(List<AssignmentGroupDto> assignmentGroupDtos,
			final boolean indCSSReviewContact) {
		return (Stream<AssignmentGroupDto>) assignmentGroupDtos.stream().map(assignmentGroupDto -> {
			assignmentGroupDto.setIndCSSReviewContact(String.valueOf(indCSSReviewContact));
			return assignmentGroupDto;
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getEmployeeEmailAddressList(Long idStage) {
		/*
	     Defect#11034 - added the scalar to fix the "Could not find setter" issue
	     Which then resolves the 8888 error to the user. Which in turn fixes the Duplicate SUB stage issue
	     when user reloads the 8888 error page
	     */
		SQLQuery queryEmployee = (SQLQuery) (sessionFactory.getCurrentSession()
				.createSQLQuery(getEmployeeEmailAddressForStageSql)).addScalar("employeeEmailAddress", StandardBasicTypes.STRING)
				.setResultTransformer(Transformers.aliasToBean(EmployeeDetailDto.class));
		queryEmployee.setParameter("idStage", idStage);
		List<String> emailAddressList = queryEmployee.list();

		return emailAddressList;
	}

	@Override
	public List<SSCCCatchmentDto> fetchSSCCCatchment(Long personId) {
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getSSCCCatchmentForVendor)
				.setParameter("personId", personId)
				.setResultTransformer(Transformers.aliasToBean(SSCCCatchmentDto.class)));
		sqlQuery.addScalar("personId", StandardBasicTypes.LONG);
		sqlQuery.addScalar("region", StandardBasicTypes.STRING);
		sqlQuery.addScalar("ssccCatchment", StandardBasicTypes.STRING);
		List<SSCCCatchmentDto> ssccCatchmentDtos = (List<SSCCCatchmentDto>) sqlQuery.list();
		return ssccCatchmentDtos;
	}

	@Override
	public String fetchVendorUrl(String region, String catchment) {
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(getVendorUrl)
				.setResultTransformer(Transformers.aliasToBean(SSCCCatchmentDto.class)));
		sqlQuery.setParameter("region", region);
		sqlQuery.setParameter("catchment", catchment);
		sqlQuery.addScalar("vendorUrl", StandardBasicTypes.STRING);
		SSCCCatchmentDto ssccCatchmentDto = (SSCCCatchmentDto) sqlQuery.uniqueResult();
		return !ObjectUtils.isEmpty(ssccCatchmentDto) ? ssccCatchmentDto.getVendorUrl() : "";
	}
}
