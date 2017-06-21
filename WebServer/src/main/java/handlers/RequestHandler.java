package handlers;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by giggiux on 07/05/2017.
 */
public class RequestHandler implements HttpHandler {
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		Map<String, Object> parameters = new HashMap<String, Object>();
		InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
		BufferedReader br = new BufferedReader(isr);
		String query = br.readLine();
		parseQuery(query, parameters);

		double tot;
		double file_tot;

		double devnumberMax = upperBound(paramToDouble("devnumberMax", parameters));

		double devnumberMin = paramToDouble("devnumberMin", parameters)-1; // On the database 1 dev is 0

		double classnumberMax = upperBound(paramToDouble("classnumberMax", parameters));

		double classnumberMin = paramToDouble("classnumberMin", parameters);

		double locMax = upperBound(paramToDouble("locMax", parameters));

		double locMin = paramToDouble("locMin", parameters);

		double locFileMax = upperBound(paramToDouble("locFileMax", parameters));
		double locFileMin = paramToDouble("locFileMin", parameters);

		double c3 = paramToDouble("c3", parameters);
		double cr = paramToDouble("cr", parameters);
		double cd = paramToDouble("cd", parameters);

		double file_c3 = paramToDouble("file_c3", parameters);
		double file_cr = paramToDouble("file_cr", parameters);
		double file_cd = paramToDouble("file_cd", parameters);

		double cbo = upperBound(paramToDouble("cbo", parameters));
		double lcom = upperBound(paramToDouble("lcom", parameters));
		double ccbc = upperBound(paramToDouble("ccbc", parameters));
		double wmc = upperBound(paramToDouble("wmc", parameters));

		double file_cbo = upperBound(paramToDouble("file_cbo", parameters));
		double file_lcom = upperBound(paramToDouble("file_lcom", parameters));
		double file_ccbc = upperBound(paramToDouble("file_ccbc", parameters));
		double file_wmc = upperBound(paramToDouble("file_wmc", parameters));

		double[] args = {devnumberMax, devnumberMin, classnumberMax, classnumberMin, locMax, locMin, locFileMax, locFileMin, c3, cr, cd, cbo, lcom, ccbc, wmc, file_c3, file_cr, file_cd, file_cbo, file_lcom, file_ccbc, file_wmc};

		String response = runSQLQueryAndCreateResponse(args);

		String values = String.format("%f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f, %f%n",
				devnumberMax, devnumberMin, classnumberMax, classnumberMin, locMax, locMin, locFileMax, locFileMin,
				c3, cr, cd, cbo, lcom, ccbc, wmc,
				file_c3, file_cr, file_cd, file_cbo, file_lcom, file_ccbc, file_wmc);

		System.out.printf(values);
		System.out.println(response);

		httpExchange.sendResponseHeaders(200, response.length());
		OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();

	}

	private double upperBound(double value) {
		return (value >= 0) ? value : Double.MAX_VALUE;
	}

	/**
	 *
	 * @param key String key to be searched in the map
	 * @param parameters Map<String, Object> that contains keys and parameters (returned from parseQuery)
	 * @return return the actual double value contained in the map, or -1
	 */
	private double paramToDouble(String key, Map<String, Object> parameters) {
		double value = -1;

		try {
			if (parameters.containsKey(key)) {
				Object obj = parameters.get(key);
				String objString = (String) obj;
				value = Double.parseDouble(objString);
			}
		} catch (Exception e) {}

		return value;

	}

	private void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String[] pairs = query.split("[&]");
			for (String pair : pairs) {
				String[] param = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0],
							System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1],
							System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);

					} else if (obj instanceof String) {
						List<String> values = new ArrayList<>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}

	private String runSQLQueryAndCreateResponse(double[] args) {
		String values = "Connection to the database failed";
		final String valueNameTOT = "counttot";

		final String valueNameC3 = "countc3";
		final String valueNameCBO = "countcbo";
		final String valueNameLCOM = "countlcom";
		final String valueNameCCBC = "countccbc";
		final String valueNameCR = "countcr";
		final String valueNameWMC = "countwmc";
		final String valueNameCD = "countcd";

		final String file_valueNameTOT = "counttotfile";

		final String file_valueNameC3 = "countc3file";
		final String file_valueNameCBO = "countcbofile";
		final String file_valueNameLCOM = "countlcomfile";
		final String file_valueNameCCBC = "countccbcfile";
		final String file_valueNameCR = "countcrfile";
		final String file_valueNameWMC = "countwmcfile";
		final String file_valueNameCD = "countcdfile";

		Connection c = null;
		try {
			String dbServer = System.getenv("DB_SERVER");
			String dbName = System.getenv("DB_NAME");
			String dbUser = System.getenv("DB_USER");
			String dbPass = System.getenv("DB_PASS");

			Class.forName("org.postgresql.Driver");

			c = DriverManager
					.getConnection(dbServer+dbName,
							dbUser, dbPass);

			double tot;
			double file_tot;
			double devnumberMax = args[0];
			double devnumberMin = args[1];
			double classnumberMax = args[2];
			double classnumberMin = args[3];

			double locMax = args[4];
			double locMin = args[5];

			double locFileMax = args[6];
			double locFileMin = args[7];

			double c3 = args[8];
			double cr = args[9];
			double cd = args[10];
			double cbo = args[11];
			double lcom = args[12];
			double ccbc = args[13];
			double wmc = args[14];

			double file_c3 = args[15];
			double file_cr = args[16];
			double file_cd = args[17];
			double file_cbo = args[18];
			double file_lcom = args[19];
			double file_ccbc = args[20];
			double file_wmc = args[21];

			Statement stmt = c.createStatement();
			String query = "CREATE TEMPORARY TABLE first_filter AS SELECT id from repos where (devnumber > "+ Double.toString(devnumberMin) +" and devnumber < "+ Double.toString(devnumberMax) +
					" and classnumber > "+ Double.toString(classnumberMin) +" and classnumber < "+ Double.toString(classnumberMax) +");" +

					"CREATE TEMPORARY TABLE projectsAvarageValues as " +
					"SELECT distinct id, c3, cbo, lcom, ccbc, cr, loc, wmc, cd from repo_avg_metrics as a " +
					"where loc > "+ Double.toString(locMin) +" and loc < "+ Double.toString(locMax) +" and " +
					"exists (select id from first_filter " +
					"where a.id = first_filter.id);" +

					"CREATE TEMPORARY TABLE classesValues as " +
					"(SELECT C3 , CBO, LCOM, CCBC, CR, LOC, WMC, CD from classes as c " +
					"where exists (select id from projectsAvarageValues as repos where c.repoid = repos.id) and (loc > "+ Double.toString(locFileMin) +" and loc < "+ Double.toString(locFileMax) +")); " +

					"Select a." + valueNameTOT + ", b." + valueNameC3 + ", c." + valueNameCBO + ", d." + valueNameLCOM + ", e." + valueNameCCBC + ", f." + valueNameCR + ", g." + valueNameWMC + ", h." + valueNameCD + "," +
					"aFILE." + file_valueNameTOT + ", bFILE." + file_valueNameC3 + ", cFILE." + file_valueNameCBO + ", dFILE." + file_valueNameLCOM + ", eFILE." + file_valueNameCCBC + ", fFILE." + file_valueNameCR + ", gFILE." + file_valueNameWMC + ", hFILE." + file_valueNameCD + " from " +
					"(select count(c3)      as " + valueNameTOT + "   from projectsAvarageValues) as a," +
					"(select count(c3)      as " + valueNameC3 + "   from projectsAvarageValues where c3 < " + Double.toString(c3) + ") as b, " +
					"(select count(cbo)     as " + valueNameCBO + "   from projectsAvarageValues where cbo > " + Double.toString(cbo) + ") as c," +
					"(select count(lcom)    as " + valueNameLCOM + "   from projectsAvarageValues where lcom > " + Double.toString(lcom) + ") as d," +
					"(select count(ccbc)    as " + valueNameCCBC + "   from projectsAvarageValues where ccbc > " + Double.toString(ccbc) + ") as e," +
					"(select count(cr)      as " + valueNameCR + "   from projectsAvarageValues where cr < " + Double.toString(cr) + ") as f," +
					"(select count(wmc)     as " + valueNameWMC + "   from projectsAvarageValues where wmc > " + Double.toString(wmc) + ") as g," +
					"(select count(cd)      as " + valueNameCD + "   from projectsAvarageValues where cd < " + Double.toString(cd) + ") as h," +
					"(select count(c3)      as " + file_valueNameTOT + "   from classesValues) as aFILE," +
					"(select count(c3)      as " + file_valueNameC3 + "   from classesValues where c3 < " + Double.toString(file_c3) + ") as bFILE, " +
					"(select count(cbo)     as " + file_valueNameCBO + "   from classesValues where cbo > " + Double.toString(file_cbo) + ") as cFILE," +
					"(select count(lcom)    as " + file_valueNameLCOM + "   from classesValues where lcom > " + Double.toString(file_lcom) + ") as dFILE," +
					"(select count(ccbc)    as " + file_valueNameCCBC + "   from classesValues where ccbc > " + Double.toString(file_ccbc) + ") as eFILE," +
					"(select count(cr)      as " + file_valueNameCR + "   from classesValues where cr < " + Double.toString(file_cr) + ") as fFILE," +
					"(select count(wmc)     as " + file_valueNameWMC + "   from classesValues where wmc > " + Double.toString(file_wmc) + ") as gFILE," +
					"(select count(cd)      as " + file_valueNameCD + "   from classesValues where cd < " + Double.toString(file_cd) + ") as hFILE;";


			System.out.println(query);
			stmt.execute(query);
			ResultSet rs = stmt.getResultSet();
			while (rs == null) {
				stmt.getMoreResults();
				rs = stmt.getResultSet();
			}
			
			rs.next();
			tot = rs.getDouble(valueNameTOT);
			file_tot = rs.getDouble(file_valueNameTOT);

			c3 = rs.getDouble(valueNameC3);
			cbo = rs.getDouble(valueNameCBO);
			lcom = rs.getDouble(valueNameLCOM);
			ccbc = rs.getDouble(valueNameCCBC);
			cr = rs.getDouble(valueNameCR);
			wmc = rs.getDouble(valueNameWMC);
			cd = rs.getDouble(valueNameCD);

			file_c3 = rs.getDouble(file_valueNameC3);
			file_cbo = rs.getDouble(file_valueNameCBO);
			file_lcom = rs.getDouble(file_valueNameLCOM);
			file_ccbc = rs.getDouble(file_valueNameCCBC);
			file_cr = rs.getDouble(file_valueNameCR);
			file_wmc = rs.getDouble(file_valueNameWMC);
			file_cd = rs.getDouble(file_valueNameCD);


			values = String.format("%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f&%s=%f",
					valueNameTOT, tot, file_valueNameTOT, file_tot,
					valueNameC3, c3, valueNameCR, cr, valueNameCD, cd, valueNameCBO, cbo, valueNameLCOM, lcom, valueNameCCBC, ccbc, valueNameWMC, wmc,
					file_valueNameC3, file_c3, file_valueNameCR, file_cr, file_valueNameCD, file_cd, file_valueNameCBO, file_cbo, file_valueNameLCOM, file_lcom, file_valueNameCCBC, file_ccbc, file_valueNameWMC, file_wmc);

			c.close();
			stmt.close();
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;
	}
}

