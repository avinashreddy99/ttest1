package us.tx.state.dfps.service.contacts.daoimpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import us.tx.state.dfps.service.common.ServiceConstants;
import us.tx.state.dfps.service.contact.dto.ContactEventDto;
import us.tx.state.dfps.service.contact.dto.ContactGuideDto;
import us.tx.state.dfps.service.contact.dto.ContactPersonDto;
import us.tx.state.dfps.service.contacts.dao.ContactCVSDao;
import us.tx.state.dfps.service.lookup.dao.LookupDao;
import us.tx.state.dfps.web.contact.bean.ContactDetailDto;

/**
 * service-business- IMPACT PHASE 2 MODERNIZATION Class Description:Dao methods
 * for cvs report Mar 27, 2018- 3:56:12 PM © 2017 Texas Department of Family and
 * Protective Services
 */
@Repository
public class ContactCVSDaoImpl implements ContactCVSDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private LookupDao lookupDao;

	@Value("${ContactCVSDaoImpl.getChildInfo}")
	private String getChildInfoSql;

	@Value("${ContactCVSDaoImpl.getParentInfo}")
	private String getParentInfoSql;

	@Value("${ContactCVSDaoImpl.getContactInfo}")
	private String getContactInfoSql;

	@Value("${ContactCVSDaoImpl.getEventInfo}")
	private String getEventInfoSql;

	// CLSCG6D #1
	@Value("${ContactCVSDaoImpl.getLatestFamilyPlanEvalFsu}")
	private String getLatestFamilyPlanEvalFsuSql;

	// CLSCG6D #2
	@Value("${ContactCVSDaoImpl.getLatestFamilyPlanFsu}")
	private String getLatestFamilyPlanFsuSql;

	// CLSCG6D #3
	@Value("${ContactCVSDaoImpl.getLatestFamilyPlanEvalFre}")
	private String getLatestFamilyPlanEvalFreSql;

	// CLSCG6D #4
	@Value("${ContactCVSDaoImpl.getLatestFamilyPlanFre}")
	private String getLatestFamilyPlanFreSql;

	// CLSCG6D #5
	@Value("${ContactCVSDaoImpl.getFsnaFsuFre}")
	private String getFsnaFsuFreSql;

	// CLSCG6D #6
	@Value("${ContactCVSDaoImpl.getFsnaFre}")
	private String getFsnaFreSql;

	// CLSCG5D #1
	@Value("${ContactCVSDaoImpl.getToDoDate}")
	private String getToDoDateSql;

	// CLSCG5D #2
	@Value("${ContactCVSDaoImpl.getDtCSPCompleted}")
	private String getDtCSPCompletedSql;

	// CLSCG5D #3
	@Value("${ContactCVSDaoImpl.getDtMedAppt}")
	private String getDtMedApptSql;

	// CLSCG5D #4
	@Value("${ContactCVSDaoImpl.getDtDentAppt}")
	private String getDtDentApptSql;

	// CLSCG5D #5
	@Value("${ContactCVSDaoImpl.getDtPersLoc}")
	private String getDtPersLocSql;

	// CLSCG5D #6
	@Value("${ContactCVSDaoImpl.getCdPlocChild}")
	private String getCdPlocChildSql;

	// CLSCG5D #7
	@Value("${ContactCVSDaoImpl.getEduHistory}")
	private String getEduHistorySql;

	// CLSCG5D #8
	@Value("${ContactCVSDaoImpl.getCansAssessDate}")
	private String getCansAssessDateSql;
	
	@Value("${ContactCVSDaoImpl.getLatestFPOS}")
	private String getLatestFPOSSql;

	public ContactCVSDaoImpl() {
		super();
	}

	/**
	 * DAM Name: CLSCG5D
	 * 
	 * Method Name: Retrieves child info for monthly eval The requirement of
	 * this DAM cannot be fulfilled in a single query for performance reasons.
	 * Therefore, required data is fetched for the selected children using
	 * multiple subqueries. The other option would be write multiple DAMS, one
	 * for main query and 6 other DAMS for subqueries. In this way form design
	 ** (mappings) would also be very complex. Therefore, it is better to fetch
	 * all the data elements using this single DAM.
	 * 
	 * Added subquery 7 to this query to grab the informations for ADS change.
	 * 
	 * @param Date
	 *            dtMonthlyBeginSumm, Long idCase
	 * @return ContactDateDto
	 */
	@Override
	public List<ContactPersonDto> getChildInfo(Date dtMonthlyBeginSumm, Long idCase) {

		List<ContactPersonDto> contactPersonDtoList = new ArrayList<ContactPersonDto>();

		contactPersonDtoList = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getChildInfoSql).setParameter("idCase", idCase)
				.setParameter("dtMonthlyBeginSumm", dtMonthlyBeginSumm)).addScalar("idPerson", StandardBasicTypes.LONG)
						.addScalar("nmPersonFirst", StandardBasicTypes.STRING)
						.addScalar("nmPersonMiddle", StandardBasicTypes.STRING)
						.addScalar("nmPersonLast", StandardBasicTypes.STRING)
						.addScalar("cdPersonSuffix", StandardBasicTypes.STRING)
						.addScalar("dtPersonBirth", StandardBasicTypes.DATE)
						.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

		for (ContactPersonDto contactPersonDto : contactPersonDtoList) {

			List<ContactPersonDto> contactPersonDtoList1 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList2 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList3 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList4 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList5 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList6 = new ArrayList<ContactPersonDto>();

			List<ContactPersonDto> contactPersonDtoList7 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList8 = new ArrayList<ContactPersonDto>();

			/* subquery will select legal action todo date for each child */

			contactPersonDtoList1 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getToDoDateSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtTodoTaskDue", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList1))
				contactPersonDto.setDtTodoTaskDue(contactPersonDtoList1.get(0).getDtTodoTaskDue());

			/*
			 * This query will select the latest child plan for each child for
			 * the input case
			 */

			contactPersonDtoList2 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getDtCSPCompletedSql).setParameter("idCase", idCase)
					.setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtCspPlanCompl", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList2))
				contactPersonDto.setDtCspPlanCompl(contactPersonDtoList2.get(0).getDtCspPlanCompl());

			/* query to select the latest medical appt for each child */

			contactPersonDtoList3 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getDtMedApptSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtProfAssmtApptM", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList3))
				contactPersonDto.setDtProfAssmtApptM(contactPersonDtoList3.get(0).getDtProfAssmtApptM());

			/* query to select latest dental appt for each child */

			contactPersonDtoList4 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getDtDentApptSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtProfAssmtApptD", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList4))
				contactPersonDto.setDtProfAssmtApptD(contactPersonDtoList4.get(0).getDtProfAssmtApptD());

			/* query to select latest authorized date from person_loc */

			contactPersonDtoList5 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getDtPersLocSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtPlocStart", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList5)) {
				contactPersonDto.setDtPlocStart(contactPersonDtoList5.get(0).getDtPlocStart());

				/*
				 * query to select latest authorized service level for each
				 * child
				 */

				if (!ObjectUtils.isEmpty(contactPersonDtoList5.get(0).getDtPlocStart())) {
					contactPersonDtoList6 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
							.createSQLQuery(getCdPlocChildSql).setParameter("idPerson", contactPersonDto.getIdPerson())
							.setParameter("dtPlocStart", contactPersonDto.getDtPlocStart()))
									.addScalar("cdPlocChild", StandardBasicTypes.STRING)
									.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();
				}
			}
			if (!ObjectUtils.isEmpty(contactPersonDtoList6)) {
				contactPersonDto.setCdPlocChild(contactPersonDtoList6.get(0).getCdPlocChild());
			}

			/* subquery will select educational history for each child */
			//Defect 13013 - Fixed SQL to get latest school name
			contactPersonDtoList7 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getEduHistorySql).setParameter("idPerson", contactPersonDto.getIdPerson())
							.setParameter("dtMonthlyBeginSumm", dtMonthlyBeginSumm))
							.addScalar("idPerson", StandardBasicTypes.LONG)
							.addScalar("cdEdHistEnrollGrade", StandardBasicTypes.STRING)
							.addScalar("specialAccmdtns", StandardBasicTypes.STRING)
							.addScalar("nmEdHistSchool", StandardBasicTypes.STRING)
							.addScalar("dtLastArdiep", StandardBasicTypes.DATE)
							.addScalar("need1", StandardBasicTypes.STRING).addScalar("need2", StandardBasicTypes.STRING)
							.addScalar("need3", StandardBasicTypes.STRING).addScalar("need4", StandardBasicTypes.STRING)
							.addScalar("need5", StandardBasicTypes.STRING).addScalar("need6", StandardBasicTypes.STRING)
							.addScalar("need7", StandardBasicTypes.STRING).addScalar("need8", StandardBasicTypes.STRING)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList7)) {

				contactPersonDto.setNmEdHistSchool(contactPersonDtoList7.get(0).getNmEdHistSchool());
				contactPersonDto.setDtLastArdiep(contactPersonDtoList7.get(0).getDtLastArdiep());
				contactPersonDto.setCdEdHistEnrollGrade(contactPersonDtoList7.get(0).getCdEdHistEnrollGrade());
				contactPersonDto.setSpecialAccmdtns(contactPersonDtoList7.get(0).getSpecialAccmdtns());

				StringBuffer schoolPrograms = new StringBuffer();

				for (ContactPersonDto personDto : contactPersonDtoList7) {
					if (StringUtils.isNotBlank(personDto.getSchoolPrograms())) {
						schoolPrograms.append(lookupDao.simpleDecodeSafe("CEDUCNED", personDto.getSchoolPrograms()) + " ");
					}
				}

				contactPersonDto.setSchoolPrograms(schoolPrograms.toString());
			}

			/* subquery will select CANS Assessment Date for each child */
			contactPersonDtoList8 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getCansAssessDateSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtCansAssessment", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			if (!ObjectUtils.isEmpty(contactPersonDtoList8))
				contactPersonDto.setDtCansAssessment(contactPersonDtoList8.get(0).getDtCansAssessment());

		}

		return contactPersonDtoList;
	}

	/**
	 * DAM Name: CLSCG6D
	 * 
	 * Method Name: getParentInfo Method Description:Parent information for
	 * monthly eval. The requirement of this DAM cannot be fulfilled in a single
	 * query for performance reasons. Therefore, required data is fetched for
	 * the selected parents using multiple subqueries. The other option would be
	 * write multiple DAMS, one for main query and 2 other DAMS for subqueries.
	 * The logic to retrieve the Family Plan data would still be very complex
	 * even with multiple DAMs, and the form mapping would be more complex with
	 * multiple DAMs.
	 * 
	 * @param Date
	 *            dtMonthlyBeginSumm, Long idCase
	 * @return ContactDateDto
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ContactPersonDto> getParentInfo(Date dtMonthlyBeginSumm, Long idCase,
			List<ContactDetailDto> contactListDB) {

		List<ContactPersonDto> contactPersonDtoList1 = new ArrayList<ContactPersonDto>();

			contactPersonDtoList1 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getParentInfoSql).setParameter("idCase", idCase)
					.setParameter("dtMonthlyBeginSumm", dtMonthlyBeginSumm))
							.addScalar("idPerson", StandardBasicTypes.LONG)
							.addScalar("nmPersonFirst", StandardBasicTypes.STRING)
							.addScalar("nmPersonMiddle", StandardBasicTypes.STRING)
							.addScalar("nmPersonLast", StandardBasicTypes.STRING)
							.addScalar("cdPersonSuffix", StandardBasicTypes.STRING)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

		for (ContactPersonDto contactPersonDto : contactPersonDtoList1) {
			List<ContactPersonDto> contactPersonDtoList6 = new ArrayList<ContactPersonDto>();
			List<ContactPersonDto> contactPersonDtoList7 = new ArrayList<ContactPersonDto>();

			List<String> dtFPOSList = ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getLatestFPOSSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtFPOS").list();
			if (!CollectionUtils.isEmpty(dtFPOSList)) {

				String firstElement = dtFPOSList.get(0);
				if (!ObjectUtils.isEmpty(firstElement)) {
					String[] array = firstElement.split(",");
					String date = array[0];
					String cdStage = array[1];
					if (!ObjectUtils.isEmpty(date)) {
						contactPersonDto.setCdStage(cdStage);
						SimpleDateFormat dateFormat=new SimpleDateFormat("dd-MMM-yy");
						try {
							contactPersonDto.setDtFpCompl(dateFormat.parse(date));
						} catch (ParseException e) {
							
						}
					}
				}

			}
			
		

			/*
			 * ADS change for Last FSNA Date for FSU stage
			 */

			contactPersonDtoList6 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getFsnaFsuFreSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtFsnaAssessment", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();

			/*
			 * ADS change for Last FSNA Date for FRE stage
			 

			contactPersonDtoList7 = (List<ContactPersonDto>) ((SQLQuery) sessionFactory.getCurrentSession()
					.createSQLQuery(getFsnaFreSql).setParameter("idPerson", contactPersonDto.getIdPerson()))
							.addScalar("dtFsnaAssessment", StandardBasicTypes.DATE)
							.setResultTransformer(Transformers.aliasToBean(ContactPersonDto.class)).list();*/

			if (!ObjectUtils.isEmpty(contactPersonDtoList6.get(0).getDtFsnaAssessment())) {
				contactPersonDto.setDtFsnaAssessment(contactPersonDtoList6.get(0).getDtFsnaAssessment());
			} /*else if (!ObjectUtils.isEmpty(contactPersonDtoList7.get(0).getDtFsnaAssessment())) {
				contactPersonDto.setDtFsnaAssessment(contactPersonDtoList7.get(0).getDtFsnaAssessment());
			}*/

		}

		return contactPersonDtoList1;
	}

	/**
	 * DAM Name: CLSCG8D
	 * 
	 * Method Name: getContactInfo Method Description:Contact guide topics
	 * 
	 * @param Long
	 *            idCase
	 * @return List<ContactGuideDto>
	 */
	@Override
	public List<ContactGuideDto> getContactInfo(Long idCase, List<Long> idContactGuideNarrList) {

		List<ContactGuideDto> contactGuideDtoList = new ArrayList<ContactGuideDto>();
		contactGuideDtoList = (List<ContactGuideDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getContactInfoSql).setParameter("idCase", idCase)
				.setParameterList("idContactGuideNarrList", idContactGuideNarrList))
						.addScalar("idContactGuideTopic", StandardBasicTypes.LONG)
						.addScalar("idContactGuideNarr", StandardBasicTypes.LONG)
						.addScalar("dtLastUpdate", StandardBasicTypes.DATE)
						.addScalar("cdGuideTopic", StandardBasicTypes.STRING)
						.addScalar("idCase", StandardBasicTypes.LONG).addScalar("idStage", StandardBasicTypes.LONG)
						.addScalar("idEvent", StandardBasicTypes.LONG)
						.setResultTransformer(Transformers.aliasToBean(ContactGuideDto.class)).list();
		return contactGuideDtoList;
	}

	/**
	 * DAM Name: CLSCGGD
	 * 
	 * Method Name: getEventInfo Method Description:Kinship Notification contact
	 * event info
	 * 
	 * @param Long
	 *            idCase
	 * @return List<ContactEventDto>
	 */
	@Override
	public List<ContactEventDto> getEventInfo(Long idCase) {

		List<ContactEventDto> contactEventDtoList = new ArrayList<ContactEventDto>();
		contactEventDtoList = (List<ContactEventDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getEventInfoSql).setParameter("idCase", idCase))
						.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("idEvent", StandardBasicTypes.LONG)
						.addScalar("nmPersonFirst", StandardBasicTypes.STRING)
						.addScalar("nmPersonMiddle", StandardBasicTypes.STRING)
						.addScalar("nmPersonLast", StandardBasicTypes.STRING)
						.addScalar("cdPersonSuffix", StandardBasicTypes.STRING)
						.addScalar("indKinNotifChild", StandardBasicTypes.STRING)
						.addScalar("indPersRmvlNotified", StandardBasicTypes.STRING)
						.addScalar("cdStagePersRelInt", StandardBasicTypes.STRING)
						.addScalar("cdRsnNotNotified", StandardBasicTypes.STRING)
						.addScalar("idContactPersonNarr", StandardBasicTypes.LONG)
						.setResultTransformer(Transformers.aliasToBean(ContactEventDto.class)).list();
		return contactEventDtoList;
	}

}
