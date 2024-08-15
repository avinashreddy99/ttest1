package us.tx.state.dfps.service.security.daoimpl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.common.domain.EmpSecClassLink;
import us.tx.state.dfps.common.domain.EmpTempAssign;
import us.tx.state.dfps.common.domain.Employee;
import us.tx.state.dfps.common.domain.Person;
import us.tx.state.dfps.common.domain.SecurityClass;
import us.tx.state.dfps.common.domain.UserLdapLink;
import us.tx.state.dfps.common.dto.ErrorDto;
import us.tx.state.dfps.service.admin.dto.EmployeeSecurityClassLinkDto;
import us.tx.state.dfps.service.admin.dto.EmployeeTempAssignDto;
import us.tx.state.dfps.service.admin.dto.SecurityClassInfoDto;
import us.tx.state.dfps.service.admin.dto.StaffSecurityRtrvoDto;
import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.common.util.DateUtils;
import us.tx.state.dfps.service.exception.DataLayerException;
import us.tx.state.dfps.service.security.dao.StaffSecurityMaintenanceDao;

/**
 * 
 * Service-business- IMPACT PHASE 2 MODERNIZATION Class Description:This Class
 * retrieves, add and update Staff Security Maintenance Details Sept 25, 2018-
 * 3:12:22 PM © 2018 Texas Department of Family and Protective Services
 */
@Repository
public class StaffSecurityMaintenanceDaoImpl implements StaffSecurityMaintenanceDao {

	/**
	 * 
	 */
	private static final String DFPSAD = "DFPSAD";

	/**
	 * 
	 */
	private static final String ALREADY_EXISTS_PLEASE_USE_ANOTHER_USERID = " already exists, please use another USERID";

	@Autowired
	MessageSource messageSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${StaffSecurityMaintenanceDaoImpl.fetchEmployeeDtl}")
	private String fetchEmployeeDtlSql;

	@Value("${StaffSecurityMaintenanceDaoImpl.fetchEmpTempAssignDtl}")
	private String fetchEmpTempAssignDtlSql;

	@Value("${EmpTempAssignInsUpdDelDaoImpl.updateEmpTmpAssign}")
	private transient String updateEmpTmpAssignSql;

	@Value("${EmpTempAssignInsUpdDelDaoImpl.deleteEmpTmpAssign}")
	private transient String deleteEmpTmpAssignSql;

	@Value("${StaffSecurityMaintenanceDaoImpl.fetchSecurityClassInfoDtl}")
	private transient String fetchSecurityClassInfoDtlSql;

	@Value("${StaffSecurityMaintenanceDaoImpl.fetchEmpSecClassLinkDtl}")
	private transient String fetchEmpSecClassLinkDtlSql;

	@Value("${StaffSecurityMaintenanceDaoImpl.fetchExternalEmployeeDtl}")
	private String fetchExternalEmployeeDtlSql;

	private static final Logger log = Logger.getLogger("StaffSecurityMaintenanceDaoImpl");
	private Long idSecurityPerson;
	private Long idCreatedPerson;;

	/**
	 * Method : fetchEmployeeDtl Method description : This Method retrieves the
	 * detail FROM EMPLOYEE table, respective Dam is cses00d
	 * 
	 * @param staffSecurityRtrvoDto
	 * @param idPerson
	 * @return staffSecurityRtrvoDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public StaffSecurityRtrvoDto fetchEmployeeDtl(StaffSecurityRtrvoDto staffSecurityRtrvoDto, Long idPerson,
			boolean externalUser) {
		log.debug("Entering method fetchEmployeeDtl in StaffSecurityMaintenanceDaoImpl");
		String sql = null;
		if (!externalUser) {
			sql = fetchEmployeeDtlSql; // Internal User
		} else {
			sql = fetchExternalEmployeeDtlSql; // External User
		}
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(sql)
				.setResultTransformer(Transformers.aliasToBean(StaffSecurityRtrvoDto.class)));
		sqlQuery.addScalar("lastUpdate", StandardBasicTypes.DATE);
		sqlQuery.addScalar("idEmployeeLogon", StandardBasicTypes.STRING);
		sqlQuery.setParameter("idPerson", idPerson);
		List<StaffSecurityRtrvoDto> staffSecurityRtrvoDtos = (List<StaffSecurityRtrvoDto>) sqlQuery.list();

		if (!CollectionUtils.isEmpty(staffSecurityRtrvoDtos) && staffSecurityRtrvoDtos.size() > 0) {
			staffSecurityRtrvoDto.setLastUpdate(staffSecurityRtrvoDtos.get(0).getLastUpdate());
			staffSecurityRtrvoDto.setIdEmployeeLogon(staffSecurityRtrvoDtos.get(0).getIdEmployeeLogon());
		}

		log.debug("Exiting method fetchEmployeeDtl in StaffSecurityMaintenanceDaoImpl");
		return staffSecurityRtrvoDto;
	}

	/**
	 * Method : fetchEmpTempAssignDtl Method Description : This Method retrieves
	 * the details from PERSON ,EMP_TEMP_ASSIGN table. respective dam is clss15d
	 * 
	 * @param PersonEmpTempAssignInDto
	 * @return PersonEmpTempAssignOutDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EmployeeTempAssignDto> fetchEmpTempAssignDtl(Long idPerson) {
		log.debug("Entering method fetchEmpTempAssignDtl in StaffSecurityMaintenanceDaoImpl");
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(fetchEmpTempAssignDtlSql)
				.setResultTransformer(Transformers.aliasToBean(EmployeeTempAssignDto.class)));
		sqlQuery.addScalar("idEmpTempAssign", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtLastUpdate", StandardBasicTypes.DATE);
		sqlQuery.addScalar("idPersonDesignee", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtAssignExpiration", StandardBasicTypes.DATE);
		sqlQuery.addScalar("idPerson", StandardBasicTypes.LONG);
		sqlQuery.addScalar("nmPersonFull", StandardBasicTypes.STRING);
		sqlQuery.setParameter("idPerson", idPerson);
		List<EmployeeTempAssignDto> employeeTempAssignDtos = (List<EmployeeTempAssignDto>) sqlQuery.list();
		log.debug("Exiting method fetchEmpTempAssignDtl in StaffSecurityMaintenanceDaoImpl");
		return employeeTempAssignDtos;
	}

	/**
	 * Method : fetchStaffSecurityDtl Method description : This Method will
	 * fetch staff security details, respective dam is clss12d
	 * 
	 * @return SecurityClassInfoDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<SecurityClassInfoDto> fetchStaffSecurityDtl() {
		log.debug("Entering method clss12dDamQuery in StaffSecurityMaintenanceDaoImpl");
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(fetchSecurityClassInfoDtlSql)
				.setResultTransformer(Transformers.aliasToBean(SecurityClassInfoDto.class)));
		sqlQuery.addScalar("nmSecurityClass", StandardBasicTypes.STRING);
		sqlQuery.addScalar("indRestrict", StandardBasicTypes.STRING);
		List<SecurityClassInfoDto> liSecurityClassInfoDtos = (List<SecurityClassInfoDto>) sqlQuery.list();
		log.debug("Exiting method fetchStaffSecurityDtl in StaffSecurityMaintenanceDaoImpl");
		return liSecurityClassInfoDtos;
	}

	/**
	 * Method : clscb3DamQuery Method description : This Method retrieves
	 * employee sec link details, respective dam is clscb3D
	 * 
	 * @param idPerson
	 * @return EmployeeSecurityClassLinkDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EmployeeSecurityClassLinkDto> fetchEmpSecClassLinkDtl(Long idPerson) {
		log.debug("Entering method fetchEmpSecClassLinkDtl in StaffSecurityMaintenanceDaoImpl");
		SQLQuery sqlQuery = ((SQLQuery) sessionFactory.getCurrentSession().createSQLQuery(fetchEmpSecClassLinkDtlSql)
				.setResultTransformer(Transformers.aliasToBean(EmployeeSecurityClassLinkDto.class)));
		sqlQuery.addScalar("idPerson", StandardBasicTypes.LONG);
		sqlQuery.addScalar("nmSecurityClass", StandardBasicTypes.STRING);
		sqlQuery.addScalar("idEmpSecLink", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtLastUpdate", StandardBasicTypes.DATE);
		sqlQuery.addScalar("indRestrict", StandardBasicTypes.STRING);
		sqlQuery.addScalar("idCreatedPerson", StandardBasicTypes.LONG);
		sqlQuery.addScalar("dtCreated", StandardBasicTypes.DATE);
		sqlQuery.addScalar("idLastUpdatePerson", StandardBasicTypes.LONG);
		sqlQuery.setParameter("idPerson", idPerson);
		List<EmployeeSecurityClassLinkDto> liEmpSecLinkDtos = (List<EmployeeSecurityClassLinkDto>) sqlQuery.list();
		log.debug("Exiting method fetchEmpSecClassLinkDtl in StaffSecurityMaintenanceDaoImpl");
		return liEmpSecLinkDtos;
	}

	/**
	 * Method : updateEmpLogonDtl Method Description : This method is used to
	 * update employeeLogon in employee.
	 * 
	 * @param EmployeeUpdByEmpLogonInDto
	 * @return EmployeeUpdByEmpLogonOutDto
	 */
	@Override
	public ErrorDto updateEmpLogonDtl(String reqFuncCd, String idEmployeeLogon, Long idPerson, Date dtLastUpdate,
			boolean externalUser, String idUser) {
		log.debug("Entering method updateEmpLogonDtl in StaffSecurityMaintenanceDaoImpl");
		ErrorDto errorDto = null;
		errorDto = loadUserLdapLink(idEmployeeLogon, idPerson, dtLastUpdate, idUser);
		if (!externalUser && ServiceConstants.REQ_FUNC_CD_UPDATE.equalsIgnoreCase(reqFuncCd)) {
			updateEmployee(idEmployeeLogon, idPerson);
		}
		log.debug("Exiting method updateEmpLogonDtl in StaffSecurityMaintenanceDaoImpl");
		return errorDto;
	}

	/**
	 * Method Name: loadUserLdapLink Method Description: This method is used to
	 * update TXT_ID_LDAP_USER in UserLdapLink table.
	 * 
	 * @param idEmployeeLogon
	 * @param idPerson
	 * @param dtLastUpdate
	 */
	@SuppressWarnings("unchecked")
	private ErrorDto loadUserLdapLink(String idEmployeeLogon, Long idPerson, Date dtLastUpdate, String idUser) {
		List<UserLdapLink> userLdapLinkExternal = null;
		UserLdapLink externalEmployee = null;
		ErrorDto errorDto = null;
		List<UserLdapLink> userLdapLinkList=null;
		//Artifact artf242007: adding start and end date user_ldap_link table
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date openEndDate = DateUtils.getDefaultFutureDate();
		if(StringUtils.isNotBlank(idEmployeeLogon)) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(UserLdapLink.class);
			criteria.add(Restrictions.eq("txtIdLdapUser", idEmployeeLogon));
			criteria.add(Restrictions.eq("cdLdap", DFPSAD));
			userLdapLinkList = (List<UserLdapLink>) criteria.list();
		}

		if (CollectionUtils.isEmpty(userLdapLinkList)) {
			// means user id is available for use
			// load the entity using the person id and dfpsad
			Criteria query = sessionFactory.getCurrentSession().createCriteria(UserLdapLink.class);
			query.add(Restrictions.eq("person.idPerson", idPerson));
			query.add(Restrictions.eq("cdLdap", DFPSAD));
			userLdapLinkExternal = (List<UserLdapLink>) query.list();
			// if loaded entity is null
			if (CollectionUtils.isEmpty(userLdapLinkExternal)) {
				// add new row with the details
				externalEmployee = new UserLdapLink();
				Person person = new Person();
				person.setIdPerson(idPerson);
				externalEmployee.setPerson(person);
				externalEmployee.setTxtIdLdapUser(idEmployeeLogon);
				externalEmployee.setDtCreated(new Date());
				externalEmployee.setDtLastUpdate(new Date());
				externalEmployee.setCdLdap(DFPSAD);
				externalEmployee.setIdCreatedPerson(new Long(idUser));
				externalEmployee.setIdLastPersonUpdate(new Long(idUser));
				/*Adding END date and start date */
				if (!ObjectUtils.isEmpty(idEmployeeLogon)) {
					externalEmployee.setTxtIdLdapUser(idEmployeeLogon);
					externalEmployee.setDtEnd(openEndDate);
				} else{
					externalEmployee.setDtEnd(new Date());
				}
				externalEmployee.setDtStart(new Date());

				sessionFactory.getCurrentSession().saveOrUpdate(externalEmployee);
			} else {
				// update the idemployeelogon, dtLastUpdate from front end to
				// this row
				userLdapLinkExternal.forEach(ldapLink -> {
					if (!ObjectUtils.isEmpty(idEmployeeLogon)) {
						ldapLink.setTxtIdLdapUser(idEmployeeLogon);
						if(ldapLink.getDtEnd()!=null &&
								!dateFormat.format(ldapLink.getDtEnd()).equals(dateFormat.format(openEndDate))){
							ldapLink.setDtEnd(openEndDate);
							ldapLink.setDtStart(new Date());
						}
					} else{
						/* end date all */
						ldapLink.setDtEnd(new Date());
					}
					if (!ObjectUtils.isEmpty(dtLastUpdate)) {
						ldapLink.setDtLastUpdate(new Date());
					}
					if (!ObjectUtils.isEmpty(idUser)) {
						ldapLink.setIdLastPersonUpdate(new Long(idUser));
					}
					sessionFactory.getCurrentSession().saveOrUpdate(ldapLink);
				});

			}

		} else if (!CollectionUtils.isEmpty(userLdapLinkList) && userLdapLinkList.size() == 1
				&& userLdapLinkList.get(0).getPerson().getIdPerson() == idPerson.longValue()) {
			// check if person id is same as that came from front end and entity
			// value id person
			externalEmployee = userLdapLinkList.get(0);
			if(externalEmployee.getDtEnd()!=null &&
					!dateFormat.format(externalEmployee.getDtEnd()).equals(dateFormat.format(openEndDate))){
				externalEmployee.setDtEnd(openEndDate);
			}
			// update logon id as dt lasst update needs to be updated
			if (!ObjectUtils.isEmpty(dtLastUpdate)) {
				externalEmployee.setDtLastUpdate(new Date());
			}
		} else {
			// if person id is different from front end then that means
			// duplicate error
			errorDto = new ErrorDto();
			String errorMessage = idEmployeeLogon + ALREADY_EXISTS_PLEASE_USE_ANOTHER_USERID;
			errorDto.setErrorMsg(errorMessage); // duplciate error message
		}
		return errorDto;
	}

	/**
	 * Method : updateEmployee Method Description - This method is used to
	 * update employee Logon in employee table.
	 * 
	 * @param EmployeeUpdByEmpLogonInDto
	 * @return long
	 */
	private void updateEmployee(String idEmployeeLogon, Long idPerson) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Employee.class);
		criteria.add(Restrictions.eq("idPerson", idPerson));
		Employee employee = (Employee) criteria.uniqueResult();

		if (!ObjectUtils.isEmpty(idEmployeeLogon)) {
			Criteria criteriaExisting = sessionFactory.getCurrentSession().createCriteria(Employee.class);
			criteriaExisting.add(Restrictions.eq("idEmployeeLogon", idEmployeeLogon));
			Employee employeeWithLogonId = (Employee) criteriaExisting.uniqueResult();

			if (!ObjectUtils.isEmpty(employeeWithLogonId) && !employeeWithLogonId.getIdPerson().equals(idPerson)) {
				throw new DataLayerException(ServiceConstants.MSG_DUPLICATE_RECORD_ERROR,
						Long.valueOf(ServiceConstants.MSG_DUPLICATE_RECORD), null);
			}
		}
		employee.setIdEmployeeLogon(idEmployeeLogon);
		sessionFactory.getCurrentSession().saveOrUpdate(employee);
	}

	/**
	 * Method : insertDeleteEmpClassLinkDtl Method Description - This method
	 * will add or delete rows on the EMP_SEC_CLASS_LINK table for a selected
	 * user.
	 * 
	 * @param EmpSecClassLinkInsDelInDto
	 * @return EmpSecClassLinkInsDelOutDto
	 */
	@Override
	public void insertDeleteEmpClassLinkDtl(String cdDataActionOutcome, Long idEmpSecLink, Long idSecurityPerson,
			String nmSecurityClass, Date dtLastUpdateSecurity, Long idCreatedPerson, Date dtCreated, Long idLastUpdatePerson) {
		this.idSecurityPerson = idSecurityPerson;
		this.idCreatedPerson = idCreatedPerson;
		log.debug("Entering method insertDeleteEmpClassLinkDtl in StaffSecurityMaintenanceDaoImpl");
		if (ServiceConstants.REQ_FUNC_CD_ADD.equalsIgnoreCase(cdDataActionOutcome)) {
			saveEmpSecClassLink(idEmpSecLink, idSecurityPerson, nmSecurityClass, dtLastUpdateSecurity,
					idCreatedPerson, dtCreated ,idLastUpdatePerson);
					}
		if (ServiceConstants.REQ_FUNC_CD_DELETE.equalsIgnoreCase(cdDataActionOutcome)) {
			//artf255802: Update for last update field and delete the record
			updateEmpSecClassLink(idEmpSecLink,idCreatedPerson, dtCreated ,idLastUpdatePerson);
			deleteEmpSecClassLink(idEmpSecLink);

		}
		if (ServiceConstants.REQ_FUNC_CD_UPDATE.equalsIgnoreCase(cdDataActionOutcome)) {
			updateEmpSecClassLink(idEmpSecLink,idCreatedPerson, dtCreated ,idLastUpdatePerson);
		}
		log.debug("Exiting method insertDeleteEmpClassLinkDtl in StaffSecurityMaintenanceDaoImpl");
	}

	/**
	 * Method : updateEmpSecClassLink Method Description: Method to update
	 * emp_sec_class_link table details
	 *
	 * @param idEmpSecLink, idCreatedPerson, dtCreated, idLastUpdatePerson
	 * @return EmpTempAssignInsUpdDelOutDto
	 */
	//artf252942 : Update method to update updateEmpSecClassLink
	private void updateEmpSecClassLink(Long idEmpSecLink, Long idCreatedPerson, Date dtCreated, Long idLastUpdatePerson)  {
		EmpSecClassLink empsecclasslink = new EmpSecClassLink();
		if (!ObjectUtils.isEmpty(idEmpSecLink)) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class);
			criteria.add(Restrictions.eq("idEmpSecLink", idEmpSecLink));
			empsecclasslink = (EmpSecClassLink) criteria.uniqueResult();
			if (!ObjectUtils.isEmpty(idLastUpdatePerson)) {
				empsecclasslink.setIdLastUpdatePerson(idLastUpdatePerson);
			}
			if (!ObjectUtils.isEmpty(dtCreated)) {
				empsecclasslink.setDtCreated(dtCreated);
			}
			if (!ObjectUtils.isEmpty(idCreatedPerson)) {
				empsecclasslink.setIdCreatedPerson(idCreatedPerson);
			}
			sessionFactory.getCurrentSession().update(empsecclasslink);
		}
	}
	/**
	 * Method : saveEmpSecClassLink Method Description : This method is used to
	 * insert new record in EmpSecClassLink entity.
	 * 
	 * @param EmpSecClassLinkInsDelInDto
	 * @return idEmpSecLink
	 */
	private void saveEmpSecClassLink(Long idEmpSecLink, Long idSecurityPerson, String nmSecurityClass,
			Date dtLastUpdateSecurity, Long idCreatedPerson, Date dtCreated, Long idLastUpdatePerson) {
		EmpSecClassLink empsecclasslink = new EmpSecClassLink();
		if (!ObjectUtils.isEmpty(idSecurityPerson)) {
			empsecclasslink.setIdPerson(idSecurityPerson);
		}
		if (!ObjectUtils.isEmpty(nmSecurityClass)) {
			SecurityClass securityClass = (SecurityClass) sessionFactory.getCurrentSession().load(SecurityClass.class,
					nmSecurityClass);
			empsecclasslink.setSecurityClass(securityClass);
		}
		if (!ObjectUtils.isEmpty(idEmpSecLink)) {
			empsecclasslink.setIdEmpSecLink(0L);
		}
		if (!ObjectUtils.isEmpty(dtLastUpdateSecurity)) {
			empsecclasslink.setDtLastUpdate(dtLastUpdateSecurity);
		}
		if (!ObjectUtils.isEmpty(idCreatedPerson)) {
			empsecclasslink.setIdCreatedPerson(idCreatedPerson);
		}
		if (!ObjectUtils.isEmpty(dtCreated)) {
			empsecclasslink.setDtCreated(dtCreated);
		}
		if (!ObjectUtils.isEmpty(idLastUpdatePerson)) {
			empsecclasslink.setIdLastUpdatePerson(idLastUpdatePerson);
		}
		sessionFactory.getCurrentSession().save(empsecclasslink);
	}

	/**
	 * Method : deleteEmpSecClassLink Method Description: This method is used to
	 * delete a record from EmpSecClassLink entity.
	 * 
	 * @param EmpSecClassLinkInsDelInDto
	 * @return long
	 */
	private void deleteEmpSecClassLink(Long idEmpSecLink) {
		EmpSecClassLink empsecclasslink = new EmpSecClassLink();
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EmpSecClassLink.class);
		criteria.add(Restrictions.eq("idEmpSecLink", idEmpSecLink));
		empsecclasslink = (EmpSecClassLink) criteria.uniqueResult();
		sessionFactory.getCurrentSession().delete(empsecclasslink);
	}

	/**
	 * Method : saveUpdateEmpTempAssignDtl Method Description - This method is
	 * used to Add/Update/Delete from table EMP_TEMP_ASSIGN.
	 * 
	 * @param EmpTempAssignInsUpdDelInDto
	 * @return EmpTempAssignInsUpdDelOutDto
	 */
	@Override
	public void saveUpdateEmpTempAssignDtl(String cdDataActionOutcome, Long idEmpTempAssign, Long idEmpPerson,
			Long idPersonDesignee, Date dtAssignExpiration, Date dtLastUpdateEmp) {
		log.debug("Entering method saveUpdateEmpTempAssignDtl in StaffSecurityMaintenanceDaoImpl");
		if (ServiceConstants.REQ_FUNC_CD_ADD.equalsIgnoreCase(cdDataActionOutcome)) {
			saveEmpTempAssign(idEmpPerson, idPersonDesignee, dtAssignExpiration, dtLastUpdateEmp);
		}
		if (ServiceConstants.REQ_FUNC_CD_UPDATE.equalsIgnoreCase(cdDataActionOutcome)) {
			updateEmpTmpAssign(idEmpTempAssign, idPersonDesignee, dtAssignExpiration, dtLastUpdateEmp, idEmpPerson);
		}
		if (ServiceConstants.REQ_FUNC_CD_DELETE.equalsIgnoreCase(cdDataActionOutcome)) {
			deleteEmpTmpAssign(idEmpTempAssign);
		}
		log.debug("Exiting method saveUpdateEmpTempAssignDtl in StaffSecurityMaintenanceDaoImpl");
	}

	/**
	 * Method : deleteEmpTmpAssign Method Description: Method to delete employee
	 * details
	 * 
	 * @param EmpTempAssignInsUpdDelInDto
	 * @return EmpTempAssignInsUpdDelOutDto
	 */
	private void deleteEmpTmpAssign(Long idEmpTempAssign) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EmpTempAssign.class);
		criteria.add(Restrictions.eq("idEmpTempAssign", idEmpTempAssign));
		EmpTempAssign empTempAssign = (EmpTempAssign) criteria.uniqueResult();
		sessionFactory.getCurrentSession().delete(empTempAssign);
	}

	/**
	 * Method : saveEmpTempAssign Method Description: Method to save data
	 * EMP_TEMP_ASSIGN table.
	 * 
	 * @param EmpTempAssignInsUpdDelInDto
	 * @return EmpTempAssignInsUpdDelOutDto
	 */
	private void saveEmpTempAssign(Long idEmpPerson, Long idPersonDesignee,
			Date dtAssignExpiration, Date dtLastUpdateEmp) {
		log.debug("Entering method saveEmpTempAssign in StaffSecurityMaintenanceDaoImpl");
		EmpTempAssign empTempAssign = new EmpTempAssign();
		if (!ObjectUtils.isEmpty(dtAssignExpiration)) {
			empTempAssign.setDtAssignExpiration(dtAssignExpiration);
		}
		if (!ObjectUtils.isEmpty(idPersonDesignee)) {
			empTempAssign.setIdPersonDesignee(idPersonDesignee);
		}
		if (!ObjectUtils.isEmpty(idEmpPerson)) {
			empTempAssign.setIdPersonEmp(idEmpPerson);
		}
		if (!ObjectUtils.isEmpty(dtLastUpdateEmp)) {
			empTempAssign.setDtLastUpdate(dtLastUpdateEmp);
		}
		sessionFactory.getCurrentSession().save(empTempAssign);

		log.debug("Exiting method saveEmpTempAssign in StaffSecurityMaintenanceDaoImpl");
	}

	/**
	 * Method : updateEmpTmpAssign Method Description: Method to update
	 * EMP_TEMP_ASSIGN table details
	 * 
	 * @param EmpTempAssignInsUpdDelInDto
	 * @return EmpTempAssignInsUpdDelOutDto
	 */
	private void updateEmpTmpAssign(Long idEmpTempAssign, Long idPersonDesignee, Date dtAssignExpiration,
			Date dtLastUpdateEmp, Long idEmpPerson) {

		if (!ObjectUtils.isEmpty(dtLastUpdateEmp) && !ObjectUtils.isEmpty(idEmpTempAssign)) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EmpTempAssign.class);
			criteria.add(Restrictions.eq("idEmpTempAssign", idEmpTempAssign));
			EmpTempAssign empTempAssign = (EmpTempAssign) criteria.uniqueResult();
			if (!ObjectUtils.isEmpty(dtAssignExpiration)) {
				empTempAssign.setDtAssignExpiration(dtAssignExpiration);
			}
			if (!ObjectUtils.isEmpty(idPersonDesignee)) {
				empTempAssign.setIdPersonDesignee(idPersonDesignee);
			}
			if (!ObjectUtils.isEmpty(idEmpPerson)) {
				empTempAssign.setIdPersonEmp(idEmpPerson);
			}

			sessionFactory.getCurrentSession().update(empTempAssign);
			// }
		}
	}
	
}
