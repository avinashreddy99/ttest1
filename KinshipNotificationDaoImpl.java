package us.tx.state.dfps.service.contacts.daoimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Repository;

import us.tx.state.dfps.common.dto.PersonAddressDto;
import us.tx.state.dfps.service.contacts.dao.KinshipNotificationDao;
import us.tx.state.dfps.service.medicalconsenter.daoimpl.MedicalConsenterRtrvDaoImpl;
import us.tx.state.dfps.service.person.dto.PersonPhoneRetDto;
import us.tx.state.dfps.service.workload.dto.PersonDto;

@Repository
public class KinshipNotificationDaoImpl implements KinshipNotificationDao {

	@Autowired
	MessageSource messageSource;

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${KinshipNotificationDaoImpl.getPersonNamesDtls}")
	private transient String getPersonNamesDtls;

	@Value("${KinshipNotificationDaoImpl.getPersonAddress}")
	private transient String getPersonAddress;

	@Value("${PhoneDaoImpl.getPhoneList}")
	private String getPhoneList;

	private static final Logger log = Logger.getLogger(MedicalConsenterRtrvDaoImpl.class);

	/**
	 * Method Description: This method retrieves the Names for Kinship
	 * Notification DAM Name: CLSCGFD
	 * 
	 * @param idEvent
	 * @return List<PersonDto>
	 */

	@SuppressWarnings("unchecked")
	public List<PersonDto> getPersonNamesDtls(Long idEvent) {
		log.debug("Entering method getPersonNamesDtls in KinshipNotificationDaoImpl");
		List<PersonDto> personDtoList = new ArrayList<PersonDto>();
		Query queryGetName = sessionFactory.getCurrentSession().createSQLQuery(getPersonNamesDtls)
				.addScalar("idPerson", StandardBasicTypes.LONG).addScalar("nmPersonFirst", StandardBasicTypes.STRING)
				.addScalar("nmPersonMiddle", StandardBasicTypes.STRING)
				.addScalar("nmPersonLast", StandardBasicTypes.STRING)
				.addScalar("cdPersonSuffix", StandardBasicTypes.STRING)
				.addScalar("indKinNotifChild", StandardBasicTypes.STRING)
				.addScalar("indPersRmvlNotified", StandardBasicTypes.STRING)
				.addScalar("cdStagePersType", StandardBasicTypes.STRING)
				.addScalar("cdStagePersRelInt", StandardBasicTypes.STRING)
				.addScalar("dtPersonBirth", StandardBasicTypes.DATE)
				.addScalar("dtEventOccurred", StandardBasicTypes.DATE).setParameter("idEvent", idEvent)
				.setResultTransformer(Transformers.aliasToBean(PersonDto.class));
		personDtoList = queryGetName.list();
		log.debug("Exiting method getPersonNamesDtls in KinshipNotificationDaoImpl");
		return personDtoList;
	}

	/**
	 * Method Description: This method retrieves addresses for kinship
	 * notification DAM Name: CLSCGHD
	 * 
	 * @param idEvent
	 * @return List<PersonDto>
	 */

	@SuppressWarnings("unchecked")
	public List<PersonAddressDto> getPersonAddress(Long idEvent) {
		log.debug("Entering method getPersonAddress in KinshipNotificationDaoImpl");
		List<PersonAddressDto> personAddressDtoList = null;
		Query queryGetAddress = sessionFactory.getCurrentSession().createSQLQuery(getPersonAddress)
				.addScalar("idPersonAddr", StandardBasicTypes.LONG)
				.addScalar("addrPersAddrStLn1", StandardBasicTypes.STRING)
				.addScalar("addrPersAddrStLn2", StandardBasicTypes.STRING)
				.addScalar("addrPersonAddrCity", StandardBasicTypes.STRING)
				.addScalar("cdPersonAddrState", StandardBasicTypes.STRING)
				.addScalar("addrPersonAddrZip", StandardBasicTypes.STRING).setParameter("idEvent", idEvent)
				.setResultTransformer(Transformers.aliasToBean(PersonAddressDto.class));
		personAddressDtoList = queryGetAddress.list();
		log.debug("Exiting method getPersonAddress in KinshipNotificationDaoImpl");
		return personAddressDtoList;

	}

	/**
	 * 
	 * Method Description: This Method will retrieve the full row from the
	 * person table based on the input request. Dam Name: CCMNB0D
	 * 
	 * @param phonereq
	 * @return List<PersonPhoneRetDto>
	 */
	@SuppressWarnings({ "unchecked" })
	public List<PersonPhoneRetDto> getPersonPhoneDetailList(Long idCaseWorker) {
		List<PersonPhoneRetDto> PhoneDtoList = new ArrayList<PersonPhoneRetDto>();
		PhoneDtoList = (List<PersonPhoneRetDto>) ((SQLQuery) sessionFactory.getCurrentSession()
				.createSQLQuery(getPhoneList).setParameter("id_Person", idCaseWorker))
						.addScalar("idPersonPhone", StandardBasicTypes.LONG).addScalar("cdPersonPhoneType")
						.addScalar("dtLastUpdate", StandardBasicTypes.TIMESTAMP)
						.addScalar("dtPersonPhoneEnd", StandardBasicTypes.TIMESTAMP)
						.addScalar("dtPersonPhoneStart", StandardBasicTypes.TIMESTAMP)
						.addScalar("indPersonPhoneInvalid").addScalar("indPersonPhonePrimary").addScalar("personPhone")
						.addScalar("personPhoneExtension").addScalar("personPhoneComments")
						.setResultTransformer(Transformers.aliasToBean(PersonPhoneRetDto.class)).list();
		return PhoneDtoList;
	}
}
