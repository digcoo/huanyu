package com.yh.bigdata.silkworm.api.bangdan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yh.bigdata.tts.common.utils.FileUtil;

public class SinaCorpCralwerTest {

		/**
		 * 上市公司
		 * @throws ClientProtocolException
		 * @throws IOException
		 * @throws InterruptedException
		 */
		@Test
		public void SinaCorpsTest() throws ClientProtocolException, IOException, InterruptedException {
			
			Set<String> corps = Sets.newHashSet();

			File file = new File("D:\\output_sina_corps.csv");
			int cnt = 1;
			
			/**
			 * 上证
			 */
			for (int i = 1; i <= 20; i++) {
				String content = Request.Get("http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?num=80&sort=changepercent&asc=0&node=sh_a&symbol=&_s_r_a=page&page=" + i).execute().returnContent().asString();
				System.out.println(content);
				
				JSONArray parseArray = JSON.parseArray(content);
				
				for (int j = 0; j < parseArray.size(); j++) {
					JSONObject jsonObject = parseArray.getJSONObject(j);
					try {
						String name_simple = jsonObject.getString("name");
						String code = jsonObject.getString("symbol");
						
						String line = parseA(cnt++, code, name_simple, "上证");
						
						FileUtil.writeLines(file, Arrays.asList(line), true);
						
						corps.add(name_simple + "\t" + "上证");
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(1000);
			}
			
			/**
			 * 深证
			 */
			for (int i = 1; i <= 28; i++) {
				String content = Request.Get("http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?num=80&sort=changepercent&asc=0&node=sz_a&symbol=&_s_r_a=page&page=" + i).execute().returnContent().asString();
				System.out.println(content);
				
				JSONArray parseArray = JSON.parseArray(content);
				
				for (int j = 0; j < parseArray.size(); j++) {
					JSONObject jsonObject = parseArray.getJSONObject(j);
					try {
						
						String name_simple = jsonObject.getString("name");
						String code = jsonObject.getString("symbol");

						String line = parseA(cnt++, code, name_simple, "深证");
						
						FileUtil.writeLines(file, Arrays.asList(line), true);

						corps.add(name_simple + "\t" + "深证");
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(1000);
			}
			
			/**
			 * 港股
			 */
			for (int i = 1; i <= 31; i++) {
				String content = Request.Get("http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHKStockData?num=80&sort=symbol&asc=1&node=qbgg_hk&_s_r_a=page&page=" + i).execute().returnContent().asString();
				System.out.println(content);
				
				JSONArray parseArray = JSON.parseArray(content);
				
				for (int j = 0; j < parseArray.size(); j++) {
					JSONObject jsonObject = parseArray.getJSONObject(j);
					try {
						String name_simple = jsonObject.getString("name");
						
						String code = jsonObject.getString("symbol");

						String line = parseHK(cnt++, code, name_simple, "港股");
						
						FileUtil.writeLines(file, Arrays.asList(line), true);
						
						corps.add(name_simple + "\t" + "港股");
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(1000);
			}
			
			/**
			 * 美股
			 */
			for (int i = 1; i <= 163; i++) {
				String content = Request.Get("http://stock.finance.sina.com.cn/usstock/api/jsonp.php/jsonp_result/US_CategoryService.getList?&num=60&sort=mktcap&asc=0&market=&id=&page=" + i).execute().returnContent().asString(Charset.forName("utf-8"));
				content = content.substring(content.indexOf("jsonp_result") + "jsonp_result(".length(), content.length() - 2);
				System.out.println(content);

				JSONArray parseArray = JSON.parseObject(content).getJSONArray("data");
				
				for (int j = 0; j < parseArray.size(); j++) {
					JSONObject jsonObject = parseArray.getJSONObject(j);
					try {
						String name_simple = jsonObject.getString("cname");
						long mktcap = jsonObject.getLong("mktcap");
						if (mktcap > 1) {

							String code = jsonObject.getString("symbol");

							String line = parseUSA(cnt++, code, name_simple, "美股");
							
							FileUtil.writeLines(file, Arrays.asList(line), true);

							corps.add(name_simple + "\t" + "美股");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				Thread.sleep(1000);
			}
			
			/**
			 * 英股(知名 )
			 */
			String url1 = "http://hq.sinajs.cn/rn=wzocg&list=lse_htsc,lse_htsc_i,lse_aly,lse_aly_i,lse_dpp,lse_dpp_i,lse_ukog,lse_ukog_i,lse_hhpd,lse_hhpd_i,lse_knm,lse_knm_i,lse_aep,lse_aep_i,lse_cnel,lse_cnel_i,lse_bmy,lse_bmy_i,lse_sxx,lse_sxx_i,lse_hsv,lse_hsv_i,lse_rio,lse_rio_i,lse_mgam,lse_mgam_i,lse_dlar,lse_dlar_i,lse_bnc,lse_bnc_i,lse_elm,lse_elm_i,lse_idh,lse_idh_i,lse_glen,lse_glen_i,lse_evr,lse_evr_i,lse_jmat,lse_jmat_i,lse_rcdo,lse_rcdo_i,lse_aal,lse_aal_i,lse_fevr,lse_fevr_i,lse_weir,lse_weir_i,lse_rsw,lse_rsw_i,lse_rya,lse_rya_i,lse_sxs,lse_sxs_i,lse_vcp,lse_vcp_i,lse_anto,lse_anto_i,lse_aml,lse_aml_i,lse_imi,lse_imi_i,lse_mnod,lse_mnod_i,lse_skg,lse_skg_i,lse_boy,lse_boy_i,lse_bhp,lse_bhp_i,lse_mnzs,lse_mnzs_i,lse_htg,lse_htg_i,lse_rmg,lse_rmg_i,lse_mro,lse_mro_i,lse_rkh,lse_rkh_i,lse_mndi,lse_mndi_i,lse_rr$2e,lse_rr$2e_i,lse_iag,lse_iag_i,lse_wmh,lse_wmh_i,lse_ipf,lse_ipf_i,lse_tui,lse_tui_i,lse_paf,lse_paf_i,lse_ezj,lse_ezj_i,lse_hyud,lse_hyud_i,lse_ashm,lse_ashm_i,lse_has,lse_has_i,lse_phnx,lse_phnx_i,lse_tlw,lse_tlw_i,lse_pdl,lse_pdl_i,lse_vct,lse_vct_i,lse_smin,lse_smin_i,lse_bkg,lse_bkg_i,lse_rdsb,lse_rdsb_i,lse_bp$2e,lse_bp$2e_i,lse_apf,lse_apf_i,lse_wtb,lse_wtb_i,lse_tpk,lse_tpk_i,lse_tw$2e,lse_tw$2e_i,lse_smds,lse_smds_i,lse_bats,lse_bats_i,lse_fres,lse_fres_i,lse_invp,lse_invp_i,lse_emg,lse_emg_i,lse_hlma,lse_hlma_i,lse_glb,lse_glb_i,lse_rdsa,lse_rdsa_i,lse_lse,lse_lse_i,lse_bee,lse_bee_i,lse_ccl,lse_ccl_i,lse_tsco,lse_tsco_i,lse_ihg,lse_ihg_i,lse_av$2e,lse_av$2e_i,lse_abf,lse_abf_i,lse_trig,lse_trig_i,lse_hsba,lse_hsba_i,lse_rigd,lse_rigd_i,lse_caml,lse_caml_i,lse_rbs,lse_rbs_i,lse_rle,lse_rle_i,lse_kgf,lse_kgf_i,lse_lloy,lse_lloy_i,lse_wizz,lse_wizz_i,lse_lgen,lse_lgen_i,lse_rnk,lse_rnk_i,lse_hdy,lse_hdy_i,lse_xpp,lse_xpp_i,lse_nxt,lse_nxt_i,lse_stan,lse_stan_i,lse_clig,lse_clig_i,lse_vod,lse_vod_i,lse_snr,lse_snr_i,lse_sn$2e,lse_sn$2e_i,lse_pru,lse_pru_i,lse_inf,lse_inf_i,lse_spt,lse_spt_i";
			String url2 = "http://hq.sinajs.cn/rn=zy2ac&list=lse_sla,lse_sla_i,lse_gfm,lse_gfm_i,lse_avon,lse_avon_i,lse_sse,lse_sse_i,lse_sbry,lse_sbry_i,lse_psn,lse_psn_i,lse_bur,lse_bur_i,lse_agk,lse_agk_i,lse_crda,lse_crda_i,lse_sge,lse_sge_i,lse_rtn,lse_rtn_i,lse_cob,lse_cob_i,lse_bdev,lse_bdev_i,lse_pson,lse_pson_i,lse_tate,lse_tate_i,lse_chh,lse_chh_i,lse_sdr,lse_sdr_i,lse_je$2e,lse_je$2e_i,lse_imb,lse_imb_i,lse_jup,lse_jup_i,lse_tta,lse_tta_i,lse_cch,lse_cch_i,lse_hfg,lse_hfg_i,lse_bnzl,lse_bnzl_i,lse_wine,lse_wine_i,lse_svs,lse_svs_i,lse_hcm,lse_hcm_i,lse_dlg,lse_dlg_i,lse_itrk,lse_itrk_i,lse_boe,lse_boe_i,lse_merl,lse_merl_i,lse_som,lse_som_i,lse_smt,lse_smt_i,lse_pvr,lse_pvr_i,lse_oxig,lse_oxig_i,lse_opp,lse_opp_i,lse_kmr,lse_kmr_i,lse_kdr,lse_kdr_i,lse_icp,lse_icp_i,lse_cgh,lse_cgh_i,lse_bor,lse_bor_i,lse_agm,lse_agm_i,lse_isat,lse_isat_i,lse_bva,lse_bva_i,lse_ng$2e,lse_ng$2e_i,lse_mrw,lse_mrw_i,lse_uu$2e,lse_uu$2e_i,lse_svt,lse_svt_i,lse_smp,lse_smp_i,lse_ulvr,lse_ulvr_i,lse_fgp,lse_fgp_i,lse_wg$2e,lse_wg$2e_i,lse_stj,lse_stj_i,lse_ferg,lse_ferg_i,lse_rto,lse_rto_i,lse_expn,lse_expn_i,lse_dge,lse_dge_i,lse_sgc,lse_sgc_i,lse_oxb,lse_oxb_i,lse_barc,lse_barc_i,lse_gfs,lse_gfs_i,lse_vlx,lse_vlx_i,lse_gsk,lse_gsk_i,lse_brby,lse_brby_i,lse_azn,lse_azn_i,lse_sgro,lse_sgro_i,lse_cne,lse_cne_i,lse_itv,lse_itv_i,lse_bt$2ea,lse_bt$2ea_i,lse_utg,lse_utg_i,lse_rb$2e,lse_rb$2e_i,lse_you,lse_you_i,lse_vtc,lse_vtc_i,lse_land,lse_land_i,lse_cpg,lse_cpg_i,lse_mks,lse_mks_i,lse_sms,lse_sms_i,lse_tcm,lse_tcm_i,lse_xar,lse_xar_i,lse_bms,lse_bms_i,lse_cay,lse_cay_i,lse_stvg,lse_stvg_i,lse_dci,lse_dci_i,lse_stob,lse_stob_i,lse_blnd,lse_blnd_i,lse_cmcx,lse_cmcx_i,lse_ckn,lse_ckn_i,lse_lre,lse_lre_i,lse_cna,lse_cna_i,lse_phtm,lse_phtm_i,lse_hl$2e,lse_hl$2e_i,lse_bab,lse_bab_i,lse_dom,lse_dom_i,lse_inch,lse_inch_i,lse_scpa,lse_scpa_i,lse_brk,lse_brk_i,lse_mcro,lse_mcro_i,lse_erm,lse_erm_i,lse_rno,lse_rno_i,lse_algw,lse_algw_i";
			String url3 = "http://hq.sinajs.cn/rn=eavvt&list=lse_htsc,lse_htsc_i,lse_aly,lse_aly_i,lse_dpp,lse_dpp_i,lse_ukog,lse_ukog_i,lse_hhpd,lse_hhpd_i,lse_knm,lse_knm_i,lse_aep,lse_aep_i,lse_cnel,lse_cnel_i,lse_bmy,lse_bmy_i,lse_sxx,lse_sxx_i,lse_hsv,lse_hsv_i,lse_rio,lse_rio_i,lse_mgam,lse_mgam_i,lse_dlar,lse_dlar_i,lse_bnc,lse_bnc_i,lse_elm,lse_elm_i,lse_idh,lse_idh_i,lse_glen,lse_glen_i,lse_evr,lse_evr_i,lse_jmat,lse_jmat_i,lse_rcdo,lse_rcdo_i,lse_aal,lse_aal_i,lse_fevr,lse_fevr_i,lse_weir,lse_weir_i,lse_rsw,lse_rsw_i,lse_rya,lse_rya_i,lse_sxs,lse_sxs_i,lse_vcp,lse_vcp_i,lse_anto,lse_anto_i,lse_aml,lse_aml_i,lse_imi,lse_imi_i,lse_mnod,lse_mnod_i,lse_skg,lse_skg_i,lse_boy,lse_boy_i,lse_bhp,lse_bhp_i,lse_mnzs,lse_mnzs_i,lse_htg,lse_htg_i,lse_rmg,lse_rmg_i,lse_mro,lse_mro_i,lse_rkh,lse_rkh_i,lse_mndi,lse_mndi_i,lse_rr$2e,lse_rr$2e_i,lse_iag,lse_iag_i,lse_wmh,lse_wmh_i,lse_ipf,lse_ipf_i,lse_tui,lse_tui_i,lse_paf,lse_paf_i,lse_ezj,lse_ezj_i,lse_hyud,lse_hyud_i,lse_ashm,lse_ashm_i,lse_has,lse_has_i,lse_phnx,lse_phnx_i,lse_tlw,lse_tlw_i,lse_pdl,lse_pdl_i,lse_vct,lse_vct_i,lse_smin,lse_smin_i,lse_bkg,lse_bkg_i,lse_rdsb,lse_rdsb_i,lse_bp$2e,lse_bp$2e_i,lse_apf,lse_apf_i,lse_wtb,lse_wtb_i,lse_tpk,lse_tpk_i,lse_tw$2e,lse_tw$2e_i,lse_smds,lse_smds_i,lse_bats,lse_bats_i,lse_fres,lse_fres_i,lse_invp,lse_invp_i,lse_emg,lse_emg_i,lse_hlma,lse_hlma_i,lse_glb,lse_glb_i,lse_rdsa,lse_rdsa_i,lse_lse,lse_lse_i,lse_bee,lse_bee_i,lse_ccl,lse_ccl_i,lse_tsco,lse_tsco_i,lse_ihg,lse_ihg_i,lse_av$2e,lse_av$2e_i,lse_abf,lse_abf_i,lse_trig,lse_trig_i,lse_hsba,lse_hsba_i,lse_rigd,lse_rigd_i,lse_caml,lse_caml_i,lse_rbs,lse_rbs_i,lse_rle,lse_rle_i,lse_kgf,lse_kgf_i,lse_lloy,lse_lloy_i,lse_wizz,lse_wizz_i,lse_lgen,lse_lgen_i,lse_rnk,lse_rnk_i,lse_hdy,lse_hdy_i,lse_xpp,lse_xpp_i,lse_nxt,lse_nxt_i,lse_stan,lse_stan_i,lse_clig,lse_clig_i,lse_vod,lse_vod_i,lse_snr,lse_snr_i,lse_sn$2e,lse_sn$2e_i,lse_pru,lse_pru_i,lse_inf,lse_inf_i,lse_spt,lse_spt_i";
			String url4 = "http://hq.sinajs.cn/rn=f8bwi&list=lse_sla,lse_sla_i,lse_gfm,lse_gfm_i,lse_avon,lse_avon_i,lse_sse,lse_sse_i,lse_sbry,lse_sbry_i,lse_psn,lse_psn_i,lse_bur,lse_bur_i,lse_agk,lse_agk_i,lse_crda,lse_crda_i,lse_sge,lse_sge_i,lse_rtn,lse_rtn_i,lse_cob,lse_cob_i,lse_bdev,lse_bdev_i,lse_pson,lse_pson_i,lse_tate,lse_tate_i,lse_chh,lse_chh_i,lse_sdr,lse_sdr_i,lse_je$2e,lse_je$2e_i,lse_imb,lse_imb_i,lse_jup,lse_jup_i,lse_tta,lse_tta_i,lse_cch,lse_cch_i,lse_hfg,lse_hfg_i,lse_bnzl,lse_bnzl_i,lse_wine,lse_wine_i,lse_svs,lse_svs_i,lse_hcm,lse_hcm_i,lse_dlg,lse_dlg_i,lse_itrk,lse_itrk_i,lse_boe,lse_boe_i,lse_merl,lse_merl_i,lse_som,lse_som_i,lse_smt,lse_smt_i,lse_pvr,lse_pvr_i,lse_oxig,lse_oxig_i,lse_opp,lse_opp_i,lse_kmr,lse_kmr_i,lse_kdr,lse_kdr_i,lse_icp,lse_icp_i,lse_cgh,lse_cgh_i,lse_bor,lse_bor_i,lse_agm,lse_agm_i,lse_isat,lse_isat_i,lse_bva,lse_bva_i,lse_ng$2e,lse_ng$2e_i,lse_mrw,lse_mrw_i,lse_uu$2e,lse_uu$2e_i,lse_svt,lse_svt_i,lse_smp,lse_smp_i,lse_ulvr,lse_ulvr_i,lse_fgp,lse_fgp_i,lse_wg$2e,lse_wg$2e_i,lse_stj,lse_stj_i,lse_ferg,lse_ferg_i,lse_rto,lse_rto_i,lse_expn,lse_expn_i,lse_dge,lse_dge_i,lse_sgc,lse_sgc_i,lse_oxb,lse_oxb_i,lse_barc,lse_barc_i,lse_gfs,lse_gfs_i,lse_vlx,lse_vlx_i,lse_gsk,lse_gsk_i,lse_brby,lse_brby_i,lse_azn,lse_azn_i,lse_sgro,lse_sgro_i,lse_cne,lse_cne_i,lse_itv,lse_itv_i,lse_bt$2ea,lse_bt$2ea_i,lse_utg,lse_utg_i,lse_rb$2e,lse_rb$2e_i,lse_you,lse_you_i,lse_vtc,lse_vtc_i,lse_land,lse_land_i,lse_cpg,lse_cpg_i,lse_mks,lse_mks_i,lse_sms,lse_sms_i,lse_tcm,lse_tcm_i,lse_xar,lse_xar_i,lse_bms,lse_bms_i,lse_cay,lse_cay_i,lse_stvg,lse_stvg_i,lse_dci,lse_dci_i,lse_stob,lse_stob_i,lse_blnd,lse_blnd_i,lse_cmcx,lse_cmcx_i,lse_ckn,lse_ckn_i,lse_lre,lse_lre_i,lse_cna,lse_cna_i,lse_phtm,lse_phtm_i,lse_hl$2e,lse_hl$2e_i,lse_bab,lse_bab_i,lse_dom,lse_dom_i,lse_inch,lse_inch_i,lse_scpa,lse_scpa_i,lse_brk,lse_brk_i,lse_mcro,lse_mcro_i,lse_erm,lse_erm_i,lse_rno,lse_rno_i,lse_algw,lse_algw_i";
			String url5 = "http://hq.sinajs.cn/rn=9qpux&list=lse_htsc,lse_htsc_i,lse_aly,lse_aly_i,lse_dpp,lse_dpp_i,lse_ukog,lse_ukog_i,lse_hhpd,lse_hhpd_i,lse_knm,lse_knm_i,lse_aep,lse_aep_i,lse_cnel,lse_cnel_i,lse_bmy,lse_bmy_i,lse_sxx,lse_sxx_i,lse_hsv,lse_hsv_i,lse_rio,lse_rio_i,lse_mgam,lse_mgam_i,lse_dlar,lse_dlar_i,lse_bnc,lse_bnc_i,lse_elm,lse_elm_i,lse_idh,lse_idh_i,lse_glen,lse_glen_i,lse_evr,lse_evr_i,lse_jmat,lse_jmat_i,lse_rcdo,lse_rcdo_i,lse_aal,lse_aal_i,lse_fevr,lse_fevr_i,lse_weir,lse_weir_i,lse_rsw,lse_rsw_i,lse_rya,lse_rya_i,lse_sxs,lse_sxs_i,lse_vcp,lse_vcp_i,lse_anto,lse_anto_i,lse_aml,lse_aml_i,lse_imi,lse_imi_i,lse_mnod,lse_mnod_i,lse_skg,lse_skg_i,lse_boy,lse_boy_i,lse_bhp,lse_bhp_i,lse_mnzs,lse_mnzs_i,lse_htg,lse_htg_i,lse_rmg,lse_rmg_i,lse_mro,lse_mro_i,lse_rkh,lse_rkh_i,lse_mndi,lse_mndi_i,lse_rr$2e,lse_rr$2e_i,lse_iag,lse_iag_i,lse_wmh,lse_wmh_i,lse_ipf,lse_ipf_i,lse_tui,lse_tui_i,lse_paf,lse_paf_i,lse_ezj,lse_ezj_i,lse_hyud,lse_hyud_i,lse_ashm,lse_ashm_i,lse_has,lse_has_i,lse_phnx,lse_phnx_i,lse_tlw,lse_tlw_i,lse_pdl,lse_pdl_i,lse_vct,lse_vct_i,lse_smin,lse_smin_i,lse_bkg,lse_bkg_i,lse_rdsb,lse_rdsb_i,lse_bp$2e,lse_bp$2e_i,lse_apf,lse_apf_i,lse_wtb,lse_wtb_i,lse_tpk,lse_tpk_i,lse_tw$2e,lse_tw$2e_i,lse_smds,lse_smds_i,lse_bats,lse_bats_i,lse_fres,lse_fres_i,lse_invp,lse_invp_i,lse_emg,lse_emg_i,lse_hlma,lse_hlma_i,lse_glb,lse_glb_i,lse_rdsa,lse_rdsa_i,lse_lse,lse_lse_i,lse_bee,lse_bee_i,lse_ccl,lse_ccl_i,lse_tsco,lse_tsco_i,lse_ihg,lse_ihg_i,lse_av$2e,lse_av$2e_i,lse_abf,lse_abf_i,lse_trig,lse_trig_i,lse_hsba,lse_hsba_i,lse_rigd,lse_rigd_i,lse_caml,lse_caml_i,lse_rbs,lse_rbs_i,lse_rle,lse_rle_i,lse_kgf,lse_kgf_i,lse_lloy,lse_lloy_i,lse_wizz,lse_wizz_i,lse_lgen,lse_lgen_i,lse_rnk,lse_rnk_i,lse_hdy,lse_hdy_i,lse_xpp,lse_xpp_i,lse_nxt,lse_nxt_i,lse_stan,lse_stan_i,lse_clig,lse_clig_i,lse_vod,lse_vod_i,lse_snr,lse_snr_i,lse_sn$2e,lse_sn$2e_i,lse_pru,lse_pru_i,lse_inf,lse_inf_i,lse_spt,lse_spt_i";
			String url6 = "http://hq.sinajs.cn/rn=ifwcl&list=lse_sla,lse_sla_i,lse_gfm,lse_gfm_i,lse_avon,lse_avon_i,lse_sse,lse_sse_i,lse_sbry,lse_sbry_i,lse_psn,lse_psn_i,lse_bur,lse_bur_i,lse_agk,lse_agk_i,lse_crda,lse_crda_i,lse_sge,lse_sge_i,lse_rtn,lse_rtn_i,lse_cob,lse_cob_i,lse_bdev,lse_bdev_i,lse_pson,lse_pson_i,lse_tate,lse_tate_i,lse_chh,lse_chh_i,lse_sdr,lse_sdr_i,lse_je$2e,lse_je$2e_i,lse_imb,lse_imb_i,lse_jup,lse_jup_i,lse_tta,lse_tta_i,lse_cch,lse_cch_i,lse_hfg,lse_hfg_i,lse_bnzl,lse_bnzl_i,lse_wine,lse_wine_i,lse_svs,lse_svs_i,lse_hcm,lse_hcm_i,lse_dlg,lse_dlg_i,lse_itrk,lse_itrk_i,lse_boe,lse_boe_i,lse_merl,lse_merl_i,lse_som,lse_som_i,lse_smt,lse_smt_i,lse_pvr,lse_pvr_i,lse_oxig,lse_oxig_i,lse_opp,lse_opp_i,lse_kmr,lse_kmr_i,lse_kdr,lse_kdr_i,lse_icp,lse_icp_i,lse_cgh,lse_cgh_i,lse_bor,lse_bor_i,lse_agm,lse_agm_i,lse_isat,lse_isat_i,lse_bva,lse_bva_i,lse_ng$2e,lse_ng$2e_i,lse_mrw,lse_mrw_i,lse_uu$2e,lse_uu$2e_i,lse_svt,lse_svt_i,lse_smp,lse_smp_i,lse_ulvr,lse_ulvr_i,lse_fgp,lse_fgp_i,lse_wg$2e,lse_wg$2e_i,lse_stj,lse_stj_i,lse_ferg,lse_ferg_i,lse_rto,lse_rto_i,lse_expn,lse_expn_i,lse_dge,lse_dge_i,lse_sgc,lse_sgc_i,lse_oxb,lse_oxb_i,lse_barc,lse_barc_i,lse_gfs,lse_gfs_i,lse_vlx,lse_vlx_i,lse_gsk,lse_gsk_i,lse_brby,lse_brby_i,lse_azn,lse_azn_i,lse_sgro,lse_sgro_i,lse_cne,lse_cne_i,lse_itv,lse_itv_i,lse_bt$2ea,lse_bt$2ea_i,lse_utg,lse_utg_i,lse_rb$2e,lse_rb$2e_i,lse_you,lse_you_i,lse_vtc,lse_vtc_i,lse_land,lse_land_i,lse_cpg,lse_cpg_i,lse_mks,lse_mks_i,lse_sms,lse_sms_i,lse_tcm,lse_tcm_i,lse_xar,lse_xar_i,lse_bms,lse_bms_i,lse_cay,lse_cay_i,lse_stvg,lse_stvg_i,lse_dci,lse_dci_i,lse_stob,lse_stob_i,lse_blnd,lse_blnd_i,lse_cmcx,lse_cmcx_i,lse_ckn,lse_ckn_i,lse_lre,lse_lre_i,lse_cna,lse_cna_i,lse_phtm,lse_phtm_i,lse_hl$2e,lse_hl$2e_i,lse_bab,lse_bab_i,lse_dom,lse_dom_i,lse_inch,lse_inch_i,lse_scpa,lse_scpa_i,lse_brk,lse_brk_i,lse_mcro,lse_mcro_i,lse_erm,lse_erm_i,lse_rno,lse_rno_i,lse_algw,lse_algw_i";
			List<String> urls = Lists.newArrayList(url1, url2, url3, url4, url5, url6);
			
			for (String url : urls) {
				String content = Request.Get(url).execute().returnContent().asString(Charset.forName("gbk"));
				String[] lines = content.split(";");
				for (String stock_line : lines) {
					try {
						if (!stock_line.contains("_i=")) {
							continue;
						}
						
						String name_simple = stock_line.substring(stock_line.indexOf("_i=") + "_i=".length() + 1, stock_line.indexOf(","));
						

						String code = "";

						String line = parseUK(cnt++, code, name_simple, "英股");
						
						FileUtil.writeLines(file, Arrays.asList(line), true);
						
						corps.add(name_simple + "\t" + "英股");	
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Thread.sleep(1000);
				
			}
			
 			System.out.println(corps.size());
// 			int i = 1;
//			for (String corp : corps) {
//				FileUtil.writeLines(file, Arrays.asList(i + "\t" + corp), true);
//			}
		}
		
		private String parseA(int seq, String code, String simpleName, String ipoAddr) throws ClientProtocolException, IOException {
			
			StringBuffer stringBuffer = new StringBuffer();
			
			try {
				String url_format = "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";
				String content = Request.Get(String.format(url_format, code)).execute().returnContent().asString(Charset.forName("gbk"));
				
				Document doc = Jsoup.parse(content);
				Elements ps = doc.select("div.com_overview p");
				String corp_quancheng = ps.get(1).text();
				String corp_main_business = ps.get(3).attr("title") != ""?ps.get(3).attr("title") : ps.get(3).text();
				String corp_faren = ps.get(8).text().replaceAll("法人代表：", "");
				String corp_ceo = ps.get(9).text().replaceAll("总 经 理：", "");
				
				stringBuffer.append(seq).append("\t")
				.append(ipoAddr).append("\t")
				.append(simpleName).append("\t")
				.append(corp_quancheng).append("\t")
				.append(code).append("\t")
				.append(corp_faren).append("\t")
				.append(corp_ceo).append("\t")
				.append("未知行业").append("\t")
				.append(corp_main_business);
				
				return stringBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		
		private String parseHK(int seq, String code, String simpleName, String ipoAddr) throws ClientProtocolException, IOException {
			
			StringBuffer stringBuffer = new StringBuffer();
			
			try {
				String url_format = "http://stock.finance.sina.com.cn/hkstock/info/%s.html";
				String content = Request.Get(String.format(url_format, code)).execute().returnContent().asString(Charset.forName("gbk"));
				
				Document doc = Jsoup.parse(content);
				Elements ps = doc.select("#sub01 div.sub01_cc table tbody tr");
				String corp_quancheng = ps.get(1).select("td").get(1).text();
				String corp_main_business = ps.get(3).select("td").get(1).text();
				String corp_hangye = ps.get(4).select("td").get(1).text();
				String corp_ceo = ps.get(6).select("td").get(1).text();
				String corp_owner = ps.get(7).select("td").get(1).text();
				
				stringBuffer.append(seq).append("\t")
				.append(ipoAddr).append("\t")
				.append(simpleName).append("\t")
				.append(corp_quancheng).append("\t")
				.append(code).append("\t")
				.append(corp_owner).append("\t")
				.append(corp_ceo).append("\t")
				.append(corp_hangye).append("\t")
				.append(corp_main_business);
				
				return stringBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		private String parseUSA(int seq, String code, String simpleName, String ipoAddr) throws ClientProtocolException, IOException {
			
			StringBuffer stringBuffer = new StringBuffer();
			
			try {
				
				String url_format = "http://quotes.sina.com.cn/usstock/hq/officers.php?s=%s";
				String content = Request.Get(String.format(url_format, code)).execute().returnContent().asString(Charset.forName("gbk"));
				
				Document doc = Jsoup.parse(content);
				Elements officerPs = doc.select("div.news div.tbl_wrap table tbody tr th");
				String corp_ceo = officerPs.get(0).text();
				String corp_owner = officerPs.get(0).text();
				
				String url_format2 = "http://quotes.sina.com.cn/usstock/hq/summary.php?s=%s";
				String content2 = Request.Get(String.format(url_format2, code)).execute().returnContent().asString(Charset.forName("gbk"));
				
				Document doc2 = Jsoup.parse(content2);
				Elements summaryPs = doc2.select("div.news div.tbl_wrap table tbody tr td");
				String corp_main_business = summaryPs.get(0).text();
				
				String corp_quancheng = "";
				String corp_hangye = "";
				
				stringBuffer.append(seq).append("\t")
				.append(ipoAddr).append("\t")
				.append(simpleName).append("\t")
				.append(corp_quancheng).append("\t")
				.append(code).append("\t")
				.append(corp_owner).append("\t")
				.append(corp_ceo).append("\t")
				.append(corp_hangye).append("\t")
				.append(corp_main_business);
				
				return stringBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		
		private String parseUK(int seq, String code, String simpleName, String ipoAddr) throws ClientProtocolException, IOException {
			
			StringBuffer stringBuffer = new StringBuffer();
			
			try {
				String corp_quancheng = "";
				String corp_main_business = "";
				String corp_hangye = "";
				String corp_ceo = "";
				String corp_owner = "";
				
				stringBuffer.append(seq).append("\t")
				.append(ipoAddr).append("\t")
				.append(simpleName).append("\t")
				.append(corp_quancheng).append("\t")
				.append(code).append("\t")
				.append(corp_owner).append("\t")
				.append(corp_ceo).append("\t")
				.append(corp_hangye).append("\t")
				.append(corp_main_business);
				
				return stringBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		
}
