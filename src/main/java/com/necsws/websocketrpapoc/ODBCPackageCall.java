package com.necsws.websocketrpapoc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ODBCPackageCall<TenancyDetails, ArrearsResponse> extends NamedParameterJdbcDaoSupport {

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public ODBCPackageCall(JdbcTemplate jdbcTemplate,NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		setJdbcTemplate(jdbcTemplate);
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	@Transactional
	public void updateDWPStatus(int prru_id, String prru_status) {
		String sql = "UPDATE process_runs SET PRRU_STATUS = :1 WHERE PRRU_ID = :2 ";
		getJdbcTemplate().update(sql,prru_status,prru_id);
	}

	public void updateDWPEndDate(int prru_id, String prru_status,Date prru_end_date) {
		String sql = "UPDATE process_runs SET prru_end_date = :1,prru_status = :2 WHERE PRRU_ID = :3 ";
		getJdbcTemplate().update(sql,prru_end_date,prru_status,prru_id);
	}

	public void updateDWPEndStatus(int prru_id, Date prru_end_date) {
		String sql = "UPDATE process_runs SET prru_status = CASE WHEN NVL(PRRU_NB_REC_UNMATCHED,0) <> 0 OR NVL(PRRU_NB_REC_EXTRACT_ERROR,0) <> 0 OR NVL(PRRU_NB_REC_TECH_ERROR,0) <> 0 then 'ERROR' "
				+ " ELSE 'SUCCESS' END , prru_end_date = :1 WHERE PRRU_ID = :2 ";
		getJdbcTemplate().update(sql,prru_end_date,prru_id);
	}

	public void updateDWPTechError(int prru_id) {
		String sql = "UPDATE process_runs SET PRRU_NB_REC_TECH_ERROR = NVL(PRRU_NB_REC_TECH_ERROR,0) + 1 WHERE PRRU_ID = :1 ";
		getJdbcTemplate().update(sql,prru_id);
	}

	public void updateDWPExtError(int prru_id) {
		String sql = "UPDATE process_runs SET PRRU_NB_REC_EXTRACT_ERROR = NVL(PRRU_NB_REC_EXTRACT_ERROR,0) + 1 WHERE PRRU_ID = :1 ";
		getJdbcTemplate().update(sql,prru_id);
	}

	public void updateDWPRecUnMatch(int prru_id) {
		String sql = "UPDATE process_runs SET PRRU_NB_REC_UNMATCHED = NVL( PRRU_NB_REC_UNMATCHED,0) + 1 WHERE PRRU_ID = :1 ";
		getJdbcTemplate().update(sql,prru_id);
	}

	public void updateDWPRecSuccess(int prru_id) {
		String sql = "UPDATE process_runs SET PRRU_NB_REC_SUCCESS = NVL(PRRU_NB_REC_SUCCESS,0) + 1 WHERE PRRU_ID = :1 ";
		getJdbcTemplate().update(sql,prru_id);
	}

	private static List<String> convertStringToList(String valuesAsString) {
		String[] valuesArray = valuesAsString.split(",");
		return new ArrayList<>(Arrays.asList(valuesArray));
	}

	public String getProcessStatus(int prru_id) {
		String sql = "SELECT prru_status FROM process_runs WHERE PRRU_ID = :prru_id ";
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("prru_id", prru_id);
		return namedParameterJdbcTemplate.queryForObject(sql,parameters,String.class) ;
	}

	public List<String> getTenancyDetails(String param1, String param2, String param3, String param4,
			String param5) {

		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate())
				.withSchemaName("HOU")
				.withCatalogName("h_process_runs")
				.withProcedureName("processs_dwp_data");

		{
			MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
			mapSqlParameterSource.addValue("p_claimant_name", param1);
			mapSqlParameterSource.addValue("p_claimant_dob", param2);
			mapSqlParameterSource.addValue("p_claimant_postcode", param3);
			mapSqlParameterSource.addValue("p_claimant_dwp_date", param4);
			mapSqlParameterSource.addValue("p_prru_id", param5);

			Map<String, Object> results = simpleJdbcCall.execute(mapSqlParameterSource);
			String racAccNo = (results.get("P_RAC_ACCNO") != null) ? results.get("P_RAC_ACCNO").toString() : "";
			String ansQ1 = (results.get("P_ANS_TO_Q1") != null) ? results.get("P_ANS_TO_Q1").toString() : "";
			String ansQ2 = (results.get("P_ANS_TO_Q2") != null) ? results.get("P_ANS_TO_Q2").toString() : "";
			String ansQ3 = (results.get("P_ANS_TO_Q3") != null) ? results.get("P_ANS_TO_Q3").toString() : "";
			String ansQ4 = (results.get("P_ANS_TO_Q4") != null) ? results.get("P_ANS_TO_Q4").toString() : "";
			String ansQ5 = (results.get("P_ANS_TO_Q5") != null) ? results.get("P_ANS_TO_Q5").toString() : "";
			String ansQ6 = (results.get("P_ANS_TO_Q6") != null) ? results.get("P_ANS_TO_Q6").toString() : "";
			String ansQ7 = (results.get("P_ANS_TO_Q7") != null) ? results.get("P_ANS_TO_Q7").toString() : "";
			String ansQ8 = (results.get("P_ANS_TO_Q8") != null) ? results.get("P_ANS_TO_Q8").toString() : "";
			String ansQ9 = (results.get("P_ANS_TO_Q9") != null) ? results.get("P_ANS_TO_Q9").toString() : "";
			String ansQ10 = (results.get("P_ANS_TO_Q10") != null) ? results.get("P_ANS_TO_Q10").toString() : "";
			String ansQ11 = (results.get("P_ANS_TO_Q11") != null) ? results.get("P_ANS_TO_Q11").toString() : "";
			String ansQ12 = (results.get("P_ANS_TO_Q12") != null) ? results.get("P_ANS_TO_Q12").toString() : "";
			String ansQ13 = (results.get("P_ANS_TO_Q13") != null) ? results.get("P_ANS_TO_Q13").toString() : "";
			String ansQ14 = (results.get("P_ANS_TO_Q14") != null) ? results.get("P_ANS_TO_Q14").toString() : "";
			String rentChanDt = (results.get("P_RENT_CHNG_DT") != null) ? results.get("P_RENT_CHNG_DT").toString() : "";
			String serviceChrgDt = (results.get("P_SERVICE_CHRG_CHNG_DT") != null) ? results.get("P_SERVICE_CHRG_CHNG_DT").toString() : "";
			String completeInd = "Yes";

			String myList = (String) results.get("P_PRRU_STATUS") + "," 
					+ (String) results.get("P_ERROR_MSG") + ","
					+ racAccNo + "," 
					+ ansQ1 + "," 
					+ ansQ2 + "," 
					+ ansQ3 + "," 
					+ ansQ4 + ","
					+ ansQ5 + "," 
					+ ansQ6 + "," 
					+ ansQ7 + "," 
					+ ansQ8 + "," 
					+ ansQ9 + ","
					+ ansQ10 + "," 
					+ ansQ11 + "," 
					+ ansQ12 + "," 
					+ ansQ13 + "," 
					+ ansQ14 + "," 
					+ rentChanDt + ","
					+ serviceChrgDt + ","
					+ completeInd;
			List<String> tenancyDetails = convertStringToList(myList);
			return tenancyDetails;

		}
	}

	public String isValidSession(String p_session_id,String p_user) {

		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withSchemaName("HOU")
				.withCatalogName("h_process_runs").withFunctionName("is_valid_session");
		{
			MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
			mapSqlParameterSource.addValue("p_session_id", p_session_id);
			mapSqlParameterSource.addValue("p_user", p_user);

			Map<String, Object> result = simpleJdbcCall.execute(mapSqlParameterSource);
			// Print out the parameters
			String l_value_session = null;
			for (Map.Entry<String, Object> entry : result.entrySet()) {
				l_value_session = (String) entry.getValue();
			}
			return l_value_session;
		}

	}

	public String getSystemParam(String p_pdu_pdf_name) {

		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate()).withSchemaName("HOU")
				.withCatalogName("h_process_runs").withFunctionName("get_param_value");
		{
			MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
			mapSqlParameterSource.addValue("p_pdu_pdf_name", p_pdu_pdf_name);

			Map<String, Object> result = simpleJdbcCall.execute(mapSqlParameterSource);
			// Print out the parameters
			String l_value = null;
			for (Map.Entry<String, Object> entry : result.entrySet()) {
				//				System.out.println(
				//						"-------------------" + entry.getKey() + ": " + entry.getValue() + "-----------------------");
				l_value = (String) entry.getValue();
			}
			return l_value;
		}

	}

	public List<ArrearsResponse> createArrearsAction(String param1, String param2, String param3, String param4, String param5, String param6, String param7,
			String param8, String param9, String param10, String param11, String param12, String param13, String param14, String param15, String param16, String param17,
			String param18, String param19, String param20, String param21, String param22, String param23, boolean param24, String param25) {

		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(getJdbcTemplate())
				.withSchemaName("HOU")
				.withCatalogName("h_process_runs")
				.withProcedureName("create_acc_arrear_action_notepad_api")
		        .declareParameters(
				        new SqlParameter("p_rac_accno", Types.VARCHAR),
				        new SqlParameter("p_aca_balance", Types.VARCHAR),
				        new SqlParameter("p_prru_id", Types.VARCHAR),
				        new SqlParameter("p_prop_temp_acc_status", Types.VARCHAR),
				        new SqlParameter("p_dt_rent_charges_change", Types.VARCHAR),
				        new SqlParameter("p_rent_charges", Types.VARCHAR),
				        new SqlParameter("p_dt_serv_charges_change", Types.VARCHAR),
				        new SqlParameter("p_eligible_serv_charges", Types.VARCHAR),
				        new SqlParameter("p_num_tenant", Types.VARCHAR),
				        new SqlParameter("p_freq_rent_charge", Types.VARCHAR),
				        new SqlParameter("p_freq_serv_charges_change", Types.VARCHAR),
				        new SqlParameter("p_num_rent_free_wks", Types.VARCHAR),
				        new SqlParameter("p_num_bedrooms", Types.VARCHAR),				        
				        new SqlParameter("p_rac_epo_code", Types.VARCHAR),
				        new SqlParameter("p_aca_effective_date", Types.VARCHAR),
				        new SqlParameter("p_aca_expiry_date", Types.VARCHAR),
				        new SqlParameter("p_aca_next_action_date", Types.VARCHAR),
				        new SqlParameter("p_rac_review_date", Types.VARCHAR),
				        new SqlParameter("p_nop_highlight_ind", Types.VARCHAR),
				        new SqlParameter("p_nop_ntt_code", Types.VARCHAR),
				        new SqlParameter("p_eac_exists", Types.VARCHAR),
				        new SqlParameter("p_anl_outcome_rsn", Types.VARCHAR),
				        new SqlParameter("p_ahra_refno", Types.VARCHAR),
				        new SqlParameter("p_commit", Types.VARCHAR),
				        new SqlParameter("p_aca_code", Types.VARCHAR),
		     	        new SqlOutParameter("p_error_flag", Types.VARCHAR),
				        new SqlOutParameter("p_error_msg", Types.VARCHAR),
				        new SqlOutParameter("p_var_war_msg", Types.VARCHAR)
				    );

		{   
			MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
			mapSqlParameterSource.addValue("p_rac_accno", (param1.equals("null"))?null:param1);
			mapSqlParameterSource.addValue("p_aca_balance", (param2.equals("null"))?null:param2);
			mapSqlParameterSource.addValue("p_prru_id", (param3.equals("null"))?null:param3);
			mapSqlParameterSource.addValue("p_prop_temp_acc_status", (param4.equals("null"))?null:param4);
//			mapSqlParameterSource.addValue("p_dt_rent_charges_change", (param5.equals("null"))?null:param5);
			if (param5 != null) {
				mapSqlParameterSource.addValue("p_dt_rent_charges_change", param5);
	        } else {
	            // Handling null date
	        	mapSqlParameterSource.addValue("p_dt_rent_charges_change", null, Types.TIMESTAMP);
	        }
			mapSqlParameterSource.addValue("p_rent_charges", (param6.equals("null"))?null:param6);
//			mapSqlParameterSource.addValue("p_dt_serv_charges_change", (param7.equals("null"))?null:param7);
			if (param7 != null) {
				mapSqlParameterSource.addValue("p_dt_serv_charges_change", param7);
	        } else {
	            // Handling null date
	        	mapSqlParameterSource.addValue("p_dt_serv_charges_change", null, Types.TIMESTAMP);
	        }
			mapSqlParameterSource.addValue("p_eligible_serv_charges", (param8.equals("null"))?null:param8);
			mapSqlParameterSource.addValue("p_num_tenant", (param9.equals("null"))?null:param9);
			mapSqlParameterSource.addValue("p_freq_rent_charge", (param10.equals("null"))?null:param10);
			mapSqlParameterSource.addValue("p_freq_serv_charges_change", (param11.equals("null"))?null:param11);
			mapSqlParameterSource.addValue("p_num_rent_free_wks", (param12.equals("null"))?null:param12);
			mapSqlParameterSource.addValue("p_num_bedrooms", (param13.equals("null"))?null:param13);
			mapSqlParameterSource.addValue("p_rac_epo_code", null);
			mapSqlParameterSource.addValue("p_aca_effective_date", null);
			mapSqlParameterSource.addValue("p_aca_expiry_date", null);
			mapSqlParameterSource.addValue("p_aca_next_action_date", null);
			mapSqlParameterSource.addValue("p_rac_review_date", null);
			mapSqlParameterSource.addValue("p_nop_highlight_ind", null);
			mapSqlParameterSource.addValue("p_nop_ntt_code", null);
			mapSqlParameterSource.addValue("p_eac_exists", null);
			mapSqlParameterSource.addValue("p_anl_outcome_rsn", null);
			mapSqlParameterSource.addValue("p_ahra_refno", null);
			mapSqlParameterSource.addValue("p_commit", "true");
			mapSqlParameterSource.addValue("p_aca_code", (param25.equals("null"))?null:param25);

			Map<String, Object> results = simpleJdbcCall.execute(mapSqlParameterSource);
//			String myList =  (String) results.get("P_ERROR_FLAG") + ","	+ (String) results.get("P_ERROR_MSG") + "," + (String) results.get("P_VAR_WAR_MSG");
			String myList =  (String) results.get("p_error_flag") + ","	+ (String) results.get("p_error_msg") + "," + (String) results.get("p_var_war_msg");

			@SuppressWarnings("unchecked")
			List<ArrearsResponse> arrearsResponse = (List<ArrearsResponse>) convertStringToList(myList);
			return arrearsResponse;
		}

	}

}