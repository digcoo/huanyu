//package com.yh.bigdata.tts.spider.interceptor;
////package com.yh.bigdata.dws.api.interceptor;
////
////import com.meicloud.Common;
////import com.meicloud.model.Account;
////import com.meicloud.model.Perm;
////import com.meicloud.model.role;
////import com.meicloud.model.RolePerm;
////import com.meicloud.services.PermService;
////import com.meicloud.services.RolePermService;
////import com.meicloud.services.RoleService;
////import com.meicloud.utils.Utils;
////import java.util.ArrayList;
////import java.util.Collection;
////import java.util.HashMap;
////import java.util.Iterator;
////import java.util.List;
////import java.util.Map;
////import javax.annotation.Resource;
////import javax.servlet.http.HttpServletRequest;
////import javax.servlet.http.HttpServletResponse;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.web.context.WebApplicationContext;
////import org.springframework.web.context.support.WebApplicationContextUtils;
////import org.springframework.web.servlet.HandlerInterceptor;
////import org.springframework.web.servlet.ModelAndView;
////
////public class UrlInterceptor implements HandlerInterceptor {
////
////	private static Logger log = LoggerFactory.getLogger(UrlInterceptor.class);
////	@Resource
////	private RoleService roleService;
////	@Resource
////	private RolePermService rolePermService;
////	@Resource
////	private PermService permService;
////
////	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)
////			throws Exception {
////		String url = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
////		if (url.startsWith("/") && url.length() > 1) {
////			url = url.substring(1);
////		}
////
////		if (!url.contains("authrity/sync") && !url.contains("account/getUserInfo") && !url.contains("group/groupLeft")
////				&& !url.contains("role/menuInfoList") && !url.contains("executeJob") && !url.contains("killJob")
////				&& !url.contains("error") && !url.contains("login")) {
////			Account account = (Account) httpServletRequest.getSession().getAttribute("user");
////			if (account == null) {
////				httpServletResponse.sendRedirect("/MSS/login/index.html");
////				return false;
////			} else if (httpServletRequest.getSession().getAttribute("ifAdmin") == null) {
////				log.info("当前登录用户没有(  " + url + "  ) 的操作权限，请先登录");
////				return false;
////			} else if (((Boolean) httpServletRequest.getSession().getAttribute("ifAdmin")).booleanValue()) {
////				return true;
////			} else if (httpServletRequest.getSession().getAttribute("perms") == null) {
////				log.info("当前登录用户没有(  " + url + "  ) 的操作权限，请先分配权限");
////				return false;
////			} else {
////				Map permMap = (Map) httpServletRequest.getSession().getAttribute("perms");
////				if (Utils.isEmptyStr((Object) permMap)) {
////					log.info("当前登录用户没有(  " + url + "  ) 的操作权限，请先分配权限");
////					return false;
////				} else {
////					Boolean b = Boolean.valueOf(true);
////					Iterator var10 = Common.allParmMap.keySet().iterator();
////
////					String key;
////					String path;
////					while (var10.hasNext()) {
////						key = (String) var10.next();
////						path = (String) Common.allParmMap.get(key);
////						if (url.contains(path.length() > 1 ? path.substring(1) : "")) {
////							b = Boolean.valueOf(false);
////						}
////					}
////
////					if (!b.booleanValue()) {
////						var10 = permMap.keySet().iterator();
////
////						while (var10.hasNext()) {
////							key = (String) var10.next();
////							path = (String) permMap.get(key);
////							if (url.contains(path.length() > 1 ? path.substring(1) : "")) {
////								b = Boolean.valueOf(true);
////							}
////						}
////					}
////
////					if (b.booleanValue()) {
////						return true;
////					} else {
////						log.info("当前登录用户没有(  " + url + "  ) 的操作权限，请先分配权限");
////						return false;
////					}
////				}
////			}
////		} else {
////			return true;
////		}
////	}
////
////	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
////			ModelAndView modelAndView) throws Exception {
////		String url = httpServletRequest.getRequestURI().substring(httpServletRequest.getContextPath().length());
////		if (url.startsWith("/") && url.length() > 1) {
////			url = url.substring(1);
////		}
////
////		if (url.contains("role/roleSave")) {
////			WebApplicationContext factory = WebApplicationContextUtils
////					.getRequiredWebApplicationContext(httpServletRequest.getServletContext());
////			Account account = (Account) httpServletRequest.getSession().getAttribute("user");
////			this.roleService = (RoleService) factory.getBean("roleService");
////			Object roleList = this.roleService.findListByUser(account.getAccount());
////			if (roleList == null) {
////				roleList = new ArrayList();
////			}
////
////			httpServletRequest.getSession().setAttribute("roles", roleList);
////			if (!Utils.isEmpityCollection((Collection) roleList)) {
////				Iterator role = ((List) roleList).iterator();
////
////				while (role.hasNext()) {
////					role permMap = (role) role.next();
////					if (permMap.getRoleCode().equalsIgnoreCase("ADMIN")) {
////						httpServletRequest.getSession().setAttribute("ifAdmin", Boolean.TRUE);
////						break;
////					}
////				}
////			}
////
////			HashMap permMap1 = new HashMap();
////			if (((Boolean) httpServletRequest.getSession().getAttribute("ifAdmin")).booleanValue()) {
////				this.permService = (PermService) factory.getBean("permService");
////				List role1 = this.permService.getList(new Perm());
////				Iterator rp = role1.iterator();
////
////				while (rp.hasNext()) {
////					Perm perm = (Perm) rp.next();
////					if (!permMap1.containsKey(perm.getPermId())) {
////						permMap1.put(perm.getPermId(), perm.getPermPath());
////					}
////				}
////			} else if (!Utils.isEmpityCollection((Collection) roleList)) {
////				Iterator perm1 = ((List) roleList).iterator();
////
////				while (perm1.hasNext()) {
////					role role2 = (role) perm1.next();
////					RolePerm rp1 = new RolePerm();
////					rp1.setRoleId(role2.getRoleId());
////					this.rolePermService = (RolePermService) factory.getBean("rolePermService");
////					List rolePermList = this.rolePermService.findList(rp1);
////					Iterator var16 = rolePermList.iterator();
////
////					while (var16.hasNext()) {
////						RolePerm rolePerm = (RolePerm) var16.next();
////						if (!permMap1.containsKey(rolePerm.getPermId())) {
////							permMap1.put(rolePerm.getPermId(), rolePerm.getPermPath());
////						}
////					}
////				}
////			}
////
////			httpServletRequest.getSession().setAttribute("perms", permMap1);
////		}
////
////	}
////
////	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
////			Object o, Exception e) throws Exception {
////	}
////}
