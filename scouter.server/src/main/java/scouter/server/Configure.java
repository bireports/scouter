/*
 *  Copyright 2015 the original author or authors. 
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package scouter.server;

import scouter.lang.conf.ConfigDesc;
import scouter.lang.conf.ConfigValueType;
import scouter.lang.conf.ConfigValueUtil;
import scouter.lang.conf.ValueType;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.net.NetConstants;
import scouter.util.DateUtil;
import scouter.util.FileUtil;
import scouter.util.StringEnumer;
import scouter.util.StringKeyLinkedMap;
import scouter.util.StringLinkedSet;
import scouter.util.StringSet;
import scouter.util.StringUtil;
import scouter.util.SysJMX;
import scouter.util.SystemUtil;
import scouter.util.ThreadUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;

public class Configure extends Thread {

	private static Configure instance = null;
	public final static String CONF_DIR = "./conf/";

	public final static synchronized Configure getInstance() {
		if (instance == null) {
			instance = new Configure();
			instance.setDaemon(true);
			instance.setName(ThreadUtil.getName(instance));
			instance.start();
		}
		return instance;
	}


	//SERVER
	@ConfigDesc("Server ID")
	public String server_id = SysJMX.getHostName();

	//Log
	@ConfigDesc("Logging TCP connection related event")
	public boolean log_tcp_action_enabled = false;
	@ConfigDesc("Logging incoming MultiPacket")
	public boolean log_udp_multipacket = false;
	@ConfigDesc("Logging expired MultiPacket")
	public boolean log_expired_multipacket = true;
	@ConfigDesc("Logging all incoming packs")
	public boolean log_udp_packet = false;
	@ConfigDesc("Logging incoming CounterPack")
	public boolean log_udp_counter = false;
	@ConfigDesc("Logging incoming XLogPack")
	public boolean log_udp_xlog = false;
	@ConfigDesc("Logging incoming ProfilePack")
	public boolean log_udp_profile = false;
	@ConfigDesc("Logging incoming TextPack")
	public boolean log_udp_text = false;
	@ConfigDesc("Logging incoming AlertPack")
	public boolean log_udp_alert = false;
	@ConfigDesc("Logging incoming ObjectPack")
	public boolean log_udp_object = false;
	@ConfigDesc("Logging incoming StatusPack")
	public boolean log_udp_status = false;
	@ConfigDesc("Logging incoming StackPack")
	public boolean log_udp_stack = false;
	@ConfigDesc("Logging incoming SummaryPack")
	public boolean log_udp_summary = false;
	@ConfigDesc("Logging incoming BatchPack")
	public boolean log_udp_batch = false;	
	@ConfigDesc("Logging all request handlers in starting")	
	public boolean log_service_handler_list = false;

	@ConfigDesc("Logging when index traversal is too heavy.")
	public int log_index_traversal_warning_count = 100;

	@ConfigDesc("Retaining log according to date")
	public boolean log_rotation_enabled = true;
	@ConfigDesc("Keeping period of log")
	public int log_keep_days = 31;
	@ConfigDesc("Logging sql failed to parse")
	public boolean log_sql_parsing_fail_enabled = false;
	@ConfigDesc("")
	public boolean _trace = false;

	//Network
	@ConfigDesc("UDP Host")
	public String net_udp_listen_ip = "0.0.0.0";
	@ConfigDesc("UDP Port")
	public int net_udp_listen_port = NetConstants.SERVER_UDP_PORT;
	@ConfigDesc("TCP Host")
	public String net_tcp_listen_ip = "0.0.0.0";
	@ConfigDesc("TCP Port")
	public int net_tcp_listen_port = NetConstants.SERVER_TCP_PORT;
	@ConfigDesc("Client Socket Timeout(ms)")
	public int net_tcp_client_so_timeout_ms = 8000;
	@ConfigDesc("Agent Socket Timeout(ms)")
	public int net_tcp_agent_so_timeout_ms = 60000;
	@ConfigDesc("Transfer period(ms) of KEEP_ALIVE")
	public int net_tcp_agent_keepalive_interval_ms = 5000;
	@ConfigDesc("Waiting time(ms) for agent session")
	public int net_tcp_get_agent_connection_wait_ms = 1000;
	@ConfigDesc("UDP Packet Buffer Size")
	public int net_udp_packet_buffer_size = 65535;
	@ConfigDesc("UDP Receiver Buffer Size")
	public int net_udp_so_rcvbuf_size = 1024 * 1024 * 4;
	@ConfigDesc("")
	public int _net_udp_worker_thread_count = 3;
	@ConfigDesc("TCP Thread Pool Size")
	public int net_tcp_service_pool_size = 100;
	@ConfigDesc("Activating Http Server")
	public boolean net_http_server_enabled = false;
	@ConfigDesc("Http Port")
	public int net_http_port = NetConstants.SERVER_HTTP_PORT;
	@ConfigDesc("Activating Scouter API")
	public boolean net_http_api_enabled = false;
	@ConfigDesc("Enable a swagger for HTTP API.")
	public boolean net_http_api_swagger_enabled = false;
	@ConfigDesc("Swagger option of host's ip or domain to call APIs.")
	public String net_http_api_swagger_host_ip = "";
	@ConfigDesc("API CORS support for Access-Control-Allow-Origin")
	public String net_http_api_cors_allow_origin = "";
	@ConfigDesc("Access-Control-Allow-Credentials")
	public String net_http_api_cors_allow_credentials = "false";

	@ConfigDesc("size of webapp connection pool to collector")
	public int net_webapp_tcp_client_pool_size = 12;
	@ConfigDesc("timeout of web app connection pool to collector(It depends on net_tcp_client_so_timeout_ms)")
	public int net_webapp_tcp_client_pool_timeout = net_tcp_client_so_timeout_ms;

	@ConfigDesc("Enable api access control by client ip")
	public boolean net_http_api_auth_ip_enabled = true;
	@ConfigDesc("If get api caller's ip from http header.")
	public String net_http_api_auth_ip_header_key;

	@ConfigDesc("Enable api access control by JSESSIONID of Cookie")
	public boolean net_http_api_auth_session_enabled = true;
	@ConfigDesc("api http session timeout")
	public int net_http_api_session_timeout = 3600*24;

	@ConfigDesc("api access allow ip addresses")
	@ConfigValueType(ValueType.COMMA_SEPARATED_VALUE)
	public String net_http_api_allow_ips = "localhost,127.0.0.1,0:0:0:0:0:0:0:1,::1";

	//Dir
	@ConfigDesc("Store directory of database")
	public String db_dir = "./database";
	@ConfigDesc("Path to log directory")
	public String log_dir = "./logs";
	@ConfigDesc("Path to plugin directory")
	public String plugin_dir = "./plugin";
	@ConfigDesc("Path to client related directory")
	public String client_dir = "./client";
	@ConfigDesc("temp dir")
	public String temp_dir = "./tempdata";

	//Object
	@ConfigDesc("Waiting time(ms) until stopped heartbeat of object is determined to be inactive")
	public int object_deadtime_ms = 8000;
	@ConfigDesc("inactive object warning level. default 0.(0:info, 1:warn, 2:error, 3:fatal)")
	public int object_inactive_alert_level = 0;

	//Compress
	@ConfigDesc("Activating XLog data in zip file")
	public boolean compress_xlog_enabled = false;
	@ConfigDesc("Activating profile data in zip file")
	public boolean compress_profile_enabled = false;
	@ConfigDesc("")
	public int _compress_write_buffer_block_count = 3;
	@ConfigDesc("")
	public int _compress_read_cache_block_count = 3;
	@ConfigDesc("")
	public long _compress_read_cache_expired_ms = DateUtil.MILLIS_PER_MINUTE;
	@ConfigDesc("")
	public int _compress_dailycount_header_cache_size = 3;
	@ConfigDesc("")
	public int _compress_write_thread = 2;

	//Auto
	@ConfigDesc("")
	public boolean _auto_5m_sampling = true;

	//Manager
	@ConfigDesc("Activating automatic deletion function in the database")
	public boolean mgr_purge_enabled = true;
	@ConfigDesc("Condition of disk usage for automatic deletion. if lack, delete profile data first exclude today data.")
	public int mgr_purge_disk_usage_pct = 80;

	@ConfigDesc("Retaining date for automatic deletion. delete profile data first.")
	public int mgr_purge_profile_keep_days = 10;
	@Deprecated
	@ConfigDesc("Deprecated : use mgr_purge_profile_keep_days")
	public int mgr_purge_keep_days = mgr_purge_profile_keep_days;

	@ConfigDesc("Retaining date for automatic deletion.")
	public int mgr_purge_xlog_keep_days = 30;
	@Deprecated
	@ConfigDesc("Deprecated : use mgr_purge_xlog_keep_days")
	public int mgr_purge_xlog_without_profile_keep_days = mgr_purge_xlog_keep_days;

	@ConfigDesc("Retaining date for automatic deletion")
	public int mgr_purge_counter_keep_days = 70;

	@ConfigDesc("Ignored log ID set")
	@ConfigValueType(ValueType.COMMA_SEPARATED_VALUE)
	public StringSet mgr_log_ignore_ids = new StringSet();

	//db
	@ConfigDesc("true for daily dictionary mode about service name. default value is false that means it's permanent.")
	public boolean mgr_text_db_daily_service_enabled = false;
	@ConfigDesc("true for daily dictionary mode about api name. default value is false that means it's permanent.")
	public boolean mgr_text_db_daily_api_enabled = false;
	@ConfigDesc("true for daily dictionary mode about user agent. default value is false that means it's permanent.")
	public boolean mgr_text_db_daily_ua_enabled = false;

	@ConfigDesc("change default memory size of hash index.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_default_mb = 1;
	@ConfigDesc("change memory size of hash index for service text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_service_mb = 1;
	@ConfigDesc("change memory size of hash index for apicall text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_api_mb = 1;
	@ConfigDesc("change memory size of hash index for user agent text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_ua_mb = 1;
	@ConfigDesc("change memory size of hash index for login text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_login_mb = 1;
	@ConfigDesc("change memory size of hash index for desc text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_desc_mb = 1;
	@ConfigDesc("change memory size of hash index for hashed message text.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_index_hmsg_mb = 1;
	@ConfigDesc("change memory size of hash index for daily text db.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_text_db_daily_index_mb = 1;

	@ConfigDesc("change default memory size of key value store index.(MB)" +
			"[warn] modified this will break the database files.\nbackup old database files before change values.(restart required)")
	public int _mgr_kv_store_index_default_mb = 4;

	//XLog
	@ConfigDesc("XLog Writer Queue Size")
	public int xlog_queue_size = 10000;
	@ConfigDesc("Ignored time(ms) in retrieving XLog in real time")
	public int xlog_realtime_lower_bound_ms = 0;
	@ConfigDesc("Ignored time(ms) in retrieving previous XLog")
	public int xlog_pasttime_lower_bound_ms = 0;
//	@ConfigDesc("Ignored profile time(ms) without saving")
//	public int xlog_profile_save_lower_bound_ms = 0;

	//Profile
	@ConfigDesc("Profile Writer Queue Size")
	public int profile_queue_size = 1000;

	//GeoIP
	@ConfigDesc("Activating IP-based city/country extraction")
	public boolean geoip_enabled = true;
	@ConfigDesc("Path to GeoIP data file")
	public String geoip_data_city_file = CONF_DIR + "GeoLiteCity.dat";

	//SQL
	@ConfigDesc("Activating table-based SQL compression")
	public boolean sql_table_parsing_enabled = true;

	//TagCount
	@ConfigDesc("Activating TagCount function")
	public boolean tagcnt_enabled = false;

	//Service request options from client
	@ConfigDesc("search xlog service option - max xlog count to search per request")
	public int req_search_xlog_max_count = 500;

	//Visitor Hourly
	public boolean visitor_hourly_count_enabled = true;

	private Configure() {
		reload(false);
	}

	/**
	 * @deprecated
	 */
	private Configure(boolean b) {
	}

	private long last_load_time = -1;
	public Properties property = new Properties();

	private boolean running = true;

	public void run() {
		while (running) {
			reload(false);
			ThreadUtil.sleep(3000);
		}
	}

	private File propertyFile;

	public File getPropertyFile() {
		if (propertyFile != null) {
			return propertyFile;
		}
		String s = System.getProperty("scouter.config", CONF_DIR + "scouter.conf");
		propertyFile = new File(s.trim());
		return propertyFile;
	}

	long last_check = 0;

	public synchronized boolean reload(boolean force) {
		long now = System.currentTimeMillis();
		if (force == false && now < last_check + 3000)
			return false;
		last_check = now;

		File file = getPropertyFile();

		if (file.lastModified() == last_load_time) {
			return false;
		}

		last_load_time = file.lastModified();

		Properties temp = new Properties();
		if (file.canRead()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				temp.load(in);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				FileUtil.close(in);
			}
		}
		property = ConfigValueUtil.replaceSysProp(temp);

		apply();

		return true;
	}

	public static boolean WORKABLE = true;

	private void apply() {
		this.xlog_queue_size = getInt("xlog_queue_size", 10000);
		this.profile_queue_size = getInt("profile_queue_size", 1000);
		this.log_tcp_action_enabled = getBoolean("log_tcp_action_enabled", false);

		this.net_udp_listen_ip = getValue("net_udp_listen_ip", "0.0.0.0");
		this.net_udp_listen_port = getInt("net_udp_listen_port", NetConstants.SERVER_UDP_PORT);
		this.net_tcp_listen_ip = getValue("net_tcp_listen_ip", "0.0.0.0");
		this.net_tcp_listen_port = getInt("net_tcp_listen_port", NetConstants.SERVER_TCP_PORT);
		this.net_tcp_client_so_timeout_ms = getInt("net_tcp_client_so_timeout_ms", 8000);
		this.net_tcp_agent_so_timeout_ms = getInt("net_tcp_agent_so_timeout_ms", 60000);
		this.net_tcp_agent_keepalive_interval_ms = getInt("net_tcp_agent_keepalive_interval_ms", 5000);
		this.net_tcp_get_agent_connection_wait_ms = getInt("net_tcp_get_agent_connection_wait_ms", 1000);
		this.net_http_server_enabled = getBoolean("net_http_server_enabled", false);
		this.net_http_port = getInt("net_http_port", NetConstants.SERVER_HTTP_PORT);
		this.net_http_api_enabled = getBoolean("net_http_api_enabled", false);
		this.net_http_api_swagger_enabled = getBoolean("net_http_api_swagger_enabled", false);
		this.net_http_api_swagger_host_ip = getValue("net_http_api_swagger_host_ip", "");
		this.net_http_api_cors_allow_origin = getValue("net_http_api_cors_allow_origin", "");
		this.net_http_api_cors_allow_credentials = getValue("net_http_api_cors_allow_credentials", "false");

		this.net_webapp_tcp_client_pool_size = getInt("net_webapp_tcp_client_pool_size", 12);
		this.net_webapp_tcp_client_pool_timeout = getInt("net_webapp_tcp_client_pool_timeout", net_tcp_client_so_timeout_ms);

		this.net_http_api_auth_ip_enabled = getBoolean("net_http_api_auth_ip_enabled", true);
		this.net_http_api_auth_ip_header_key = getValue("net_http_api_auth_ip_header_key", "");
		this.net_http_api_auth_session_enabled = getBoolean("net_http_api_auth_session_enabled", true);
		this.net_http_api_session_timeout = getInt("net_http_api_session_timeout", 3600*24);

		this.net_http_api_allow_ips = getValue("net_http_api_allow_ips", "localhost,127.0.0.1,0:0:0:0:0:0:0:1,::1");

		this.server_id = getValue("server_id", SysJMX.getHostName());
		this.db_dir = getValue("db_dir", "./database");
		this.log_dir = getValue("log_dir", "./logs");
		this.plugin_dir = getValue("plugin_dir", "./plugin");
		this.client_dir = getValue("client_dir", "./client");
		this.temp_dir = getValue("temp_dir", "./tempdata");

		this.object_deadtime_ms = getInt("object_deadtime_ms", 8000);
		this.object_inactive_alert_level = getInt("object_inactive_alert_level", 0);

		this.compress_xlog_enabled = getBoolean("compress_xlog_enabled", false);
		this.compress_profile_enabled = getBoolean("compress_profile_enabled", false);
		this._compress_write_buffer_block_count = getInt("_compress_write_buffer_block_count", 3);
		this._compress_dailycount_header_cache_size = getInt("_compress_dailycount_header_cache_size", 3);
		this._compress_read_cache_block_count = getInt("_compress_read_cache_block_count", 3);
		this._compress_read_cache_expired_ms = getLong("_compress_read_cache_expired_ms", DateUtil.MILLIS_PER_MINUTE);
		this._compress_write_thread = getInt("_compress_write_thread", 2);
		
		this.net_udp_packet_buffer_size = getInt("net_udp_packet_buffer_size", 65535);

		int default_so_rcvbuf = 1024 * 1024 * 4;
		if (SystemUtil.IS_AIX || SystemUtil.IS_HP_UX) {
			default_so_rcvbuf = 0;
		}
		this.net_udp_so_rcvbuf_size = getInt("net_udp_so_rcvbuf_size", default_so_rcvbuf);
		this.log_expired_multipacket = getBoolean("log_expired_multipacket", true);
		this.log_udp_multipacket = getBoolean("log_udp_multipacket", false);
		this.log_udp_packet = getBoolean("log_udp_packet", false);
		this.log_udp_counter = getBoolean("log_udp_counter", false);
		this.log_udp_xlog = getBoolean("log_udp_xlog", false);
		this.log_udp_profile = getBoolean("log_udp_profile", false);
		this.log_udp_text = getBoolean("log_udp_text", false);
		this.log_udp_alert = getBoolean("log_udp_alert", false);
		this.log_udp_object = getBoolean("log_udp_object", false);
		this.log_udp_status = getBoolean("log_udp_status", false);
		this.log_udp_stack = getBoolean("log_udp_stack", false);
		this.log_udp_summary = getBoolean("log_udp_summary", false);
		this.log_udp_batch = getBoolean("log_udp_batch", false);
		this.log_service_handler_list = getBoolean("log_service_handler_list", false);

		this.log_index_traversal_warning_count = getInt("log_index_traversal_warning_count", 100);

		this.log_rotation_enabled = getBoolean("log_rotation_enabled", true);
		this.log_keep_days = getInt("log_keep_days", 31);
		this.log_sql_parsing_fail_enabled = getBoolean("log_sql_parsing_fail_enabled", false);
		this._trace = getBoolean("_trace", false);

		this._auto_5m_sampling = getBoolean("_auto_5m_sampling", true);

		this.xlog_realtime_lower_bound_ms = getInt("xlog_realtime_lower_bound_ms", 0);
		this.xlog_pasttime_lower_bound_ms = getInt("xlog_pasttime_lower_bound_ms", 0);
		this.mgr_purge_enabled = getBoolean("mgr_purge_enabled", true);
		this.mgr_purge_disk_usage_pct = getInt("mgr_purge_disk_usage_pct", 80);

		this.mgr_purge_keep_days = getInt("mgr_purge_keep_days", 10);
		this.mgr_purge_profile_keep_days = getInt("mgr_purge_keep_days", 10);
		if(mgr_purge_profile_keep_days == 0) mgr_purge_profile_keep_days = this.mgr_purge_keep_days;

		this.mgr_purge_xlog_without_profile_keep_days = getInt("mgr_purge_xlog_without_profile_keep_days", mgr_purge_profile_keep_days*3);
		this.mgr_purge_xlog_keep_days = getInt("mgr_purge_xlog_keep_days", mgr_purge_profile_keep_days*3);
		if(mgr_purge_xlog_keep_days == 0) mgr_purge_xlog_keep_days = this.mgr_purge_xlog_without_profile_keep_days;

		this.mgr_purge_counter_keep_days = getInt("mgr_purge_counter_keep_days", mgr_purge_keep_days*7);

		this.mgr_text_db_daily_service_enabled = getBoolean("mgr_text_db_daily_service_enabled", false);
		this.mgr_text_db_daily_api_enabled = getBoolean("mgr_text_db_daily_api_enabled", false);
		this.mgr_text_db_daily_ua_enabled = getBoolean("mgr_text_db_daily_ua_enabled", false);

		this._mgr_text_db_index_default_mb = getInt("_mgr_text_db_index_default_mb", 1);
		this._mgr_text_db_index_service_mb = getInt("_mgr_text_db_index_service_mb", 1);
		this._mgr_text_db_index_api_mb = getInt("_mgr_text_db_index_api_mb", 1);
		this._mgr_text_db_index_ua_mb = getInt("_mgr_text_db_index_ua_mb", 1);
		this._mgr_text_db_index_login_mb = getInt("_mgr_text_db_index_login_mb", 1);
		this._mgr_text_db_index_desc_mb = getInt("_mgr_text_db_index_desc_mb", 1);
		this._mgr_text_db_index_hmsg_mb = getInt("_mgr_text_db_index_hmsg_mb", 1);
		this._mgr_text_db_daily_index_mb = getInt("_mgr_text_db_daily_index_mb", 1);

		this._mgr_kv_store_index_default_mb = getInt("_mgr_kv_store_index_default_mb", 4);

		this._net_udp_worker_thread_count = getInt("_net_udp_worker_thread_count", 3);
		this.geoip_data_city_file = getValue("geoip_data_city_file", CONF_DIR + "GeoLiteCity.dat");
		this.geoip_enabled = getBoolean("geoip_enabled", true);

		//this.xlog_profile_save_lower_bound_ms = getInt("xlog_profile_save_lower_bound_ms", 0);
		this.sql_table_parsing_enabled = getBoolean("sql_table_parsing_enabled", true);

		this.mgr_log_ignore_ids = getStringSet("mgr_log_ignore_ids", ",");

		this.tagcnt_enabled = getBoolean("tagcnt_enabled", false);
		
		this.visitor_hourly_count_enabled = getBoolean("visitor_hourly_count_enabled", true);
		
		this.net_tcp_service_pool_size = getInt("net_tcp_service_pool_size", 100);

		this.req_search_xlog_max_count = getInt("req_search_xlog_max_count", 500);
		
		ConfObserver.exec();
	}

	private StringSet getStringSet(String key, String deli) {
		StringSet set = new StringSet();
		String v = getValue(key);
		if (v != null) {
			String[] vv = StringUtil.split(v, deli);
			for (String x : vv) {
				x = StringUtil.trimToEmpty(x);
				if (x.length() > 0)
					set.put(x);
			}
		}
		return set;
	}

	public String getValue(String key) {
		return StringUtil.trim(property.getProperty(key));
	}

	public String getValue(String key, String def) {
		return StringUtil.trim(property.getProperty(key, def));
	}

	public int getInt(String key, int def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Integer.parseInt(v);
		} catch (Exception e) {
		}
		return def;
	}

	public long getLong(String key, long def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Long.parseLong(v);
		} catch (Exception e) {
		}
		return def;
	}

	public boolean getBoolean(String key, boolean def) {
		try {
			String v = getValue(key);
			if (v != null)
				return Boolean.parseBoolean(v);
		} catch (Exception e) {
		}
		return def;
	}

	public String loadText() {
		File file = getPropertyFile();
		InputStream fin = null;
		try {
			fin = new FileInputStream(file);
			byte[] buff = FileUtil.readAll(fin);
			return new String(buff);
		} catch (Exception e) {
		} finally {
			FileUtil.close(fin);
		}
		return null;
	}

	public boolean saveText(String text) {
		File file = getPropertyFile();
		OutputStream out = null;
		try {
			if (file.getParentFile().exists() == false) {
				file.getParentFile().mkdirs();
			}
			out = new FileOutputStream(file);
			out.write(text.getBytes());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(out);
		}
		return false;
	}

	private static HashSet<String> ignoreSet = new HashSet<String>();

	static {
		ignoreSet.add("property");
	}

	public MapValue getKeyValueInfo() {
		StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(new Configure(true));
		StringKeyLinkedMap<Object> curMap = ConfigValueUtil.getConfigDefault(this);
		MapValue m = new MapValue();
		ListValue nameList = m.newList("key");
		ListValue valueList = m.newList("value");
		ListValue defList = m.newList("default");

		StringEnumer enu = defMap.keys();
		while (enu.hasMoreElements()) {
			String key = enu.nextString();
			if (ignoreSet.contains(key))
				continue;
			nameList.add(key);
			valueList.add(ConfigValueUtil.toValue(curMap.get(key)));
			defList.add(ConfigValueUtil.toValue(defMap.get(key)));
		}

		return m;
	}
	
	public StringKeyLinkedMap<String> getConfigureDesc() {
		return ConfigValueUtil.getConfigDescMap(this);
	}

	public StringKeyLinkedMap<ValueType> getConfigureValueType() {
		return ConfigValueUtil.getConfigValueTypeMap(this);
	}

	public static StringLinkedSet toOrderSet(String values, String deli) {
		StringLinkedSet set = new StringLinkedSet();
		StringTokenizer nizer = new StringTokenizer(values, deli);
		while (nizer.hasMoreTokens()) {
			String s = StringUtil.trimToEmpty(nizer.nextToken());
			if (s.length() > 0) {
				set.put(s);
			}
		}
		return set;
	}

	public static void main(String[] args) {
		StringKeyLinkedMap<ValueType> map = new Configure().getConfigureValueType();
		System.out.println(map);

//		Configure o = new Configure(true);
//		StringKeyLinkedMap<Object> defMap = ConfigValueUtil.getConfigDefault(o);
//		StringKeyLinkedMap<String> descMap = ConfigValueUtil.getConfigDescMap(o);
//		StringEnumer enu = defMap.keys();
//		while (enu.hasMoreElements()) {
//			String key = enu.nextString();
//			if (ignoreSet.contains(key))
//				continue;
//			System.out.println(key + " : " + ConfigValueUtil.toValue(defMap.get(key) + (descMap.containsKey(key) ? " (" + descMap.get(key) + ")" : "")));
//		}
	}


}
