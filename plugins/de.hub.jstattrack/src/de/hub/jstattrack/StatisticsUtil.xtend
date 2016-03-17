package de.hub.jstattrack

import com.google.common.collect.AbstractIterator
import com.google.common.collect.FluentIterable
import de.hub.jstattrack.services.Summary
import de.vandermeer.asciitable.v2.V2_AsciiTable
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer
import de.vandermeer.asciitable.v2.render.WidthLongestLine
import org.json.JSONArray
import org.json.JSONObject
import java.util.List

class StatisticsUtil {
	
	public static def toCSVHeader(JSONArray json) {
		val keys = json.getJSONObject(0).keySet.toList.sort
		return '''«FOR key:keys SEPARATOR ", "»«key»«ENDFOR»'''
	}
	
	public static def toCSV(JSONArray json) {
		return json.toCSV(true)
	}
	
	public static def toCSV(JSONArray json, boolean withHeader) {
		if (json.length > 0) {
			val keys = json.getJSONObject(0).keySet.toList.sort
			return '''
				«IF withHeader»«FOR key:keys SEPARATOR ", "»«key»«ENDFOR»«ENDIF»
				«FOR entry:json.toIterable»
					«FOR key:keys SEPARATOR ", "»«entry.get(key).toString»«ENDFOR»
				«ENDFOR»
			'''
		} else {
			return ''''''
		}
	}
	
	public static def toHumanReadable(JSONObject json) { 
		val array = new JSONArray
		array.put(json)
		return array.toHumanReadable
	}
	
	public static def toHumanReadable(JSONArray json) {
		if (json.length > 0) {
			val keys = json.getJSONObject(0).keySet.toList.sort
			val header = keys.toArray
			val data = json.toIterable.map[entry|keys.map[entry.get(it).toString].toArray]		
			
			val table = new V2_AsciiTable()
			table.addRow(header.toArray)
			table.addRule
			data.forEach[table.addRow(it.toArray)]
			val renderer = new V2_AsciiTableRenderer()
			renderer.width =  new WidthLongestLine
			return (renderer).render(table).toString			
		} else {
			return ""
		}	
	}
	
	public static def toIterable(JSONArray jsonArray) {
		return new FluentIterable<JSONObject> {			
			override iterator() {
				new AbstractIterator<JSONObject>() {
					var index = 0				
					override protected computeNext() {
						if (jsonArray.length > index) {
							return jsonArray.getJSONObject(index++)
						} else {
							endOfData
							return null
						}
					}					
				}
			}			
		}
	}
	
	public static def toArray(JSONObject jsonObject) {
		val array = new JSONArray
		array.put(jsonObject)
		return array
	}
	
	public static def toSummaryData(JSONArray statisticsData, String name, List<Pair<AbstractStatistic, String>> stats, boolean sumOnly) {
		return new JSONObject('''{
			«summaryDataJSONStr(statisticsData, name, stats, sumOnly)»
		}'''.toString)
	}
	
	public static def summaryDataJSONStr(JSONArray statisticsData, String name, List<Pair<AbstractStatistic, String>> stats, boolean sumOnly) {
		return '''
			name : «name»,
			«FOR stat:stats SEPARATOR ","»
				«summaryDatumJSONStr(statisticsData, stat.key, stat.value, sumOnly)»
			«ENDFOR»
		'''.toString
	}
	public static def summaryDatumJSONStr(JSONArray jsonData, AbstractStatistic statDef, String key) {
		return summaryDatumJSONStr(jsonData, statDef, key, false)
	}
	
	public static def summaryDatumJSONStr(JSONArray jsonData, AbstractStatistic statDef, String key, boolean sumOnly) {
		val serviceData = Statistics.getStatServiceDataFromJSONReport(statDef.id, Summary.serviceName, jsonData)
		if (serviceData == null) {
			throw new IllegalArgumentException("Could not find a statistic called " + statDef.id + ".")
		} else {
			return '''
				«FOR dataTuple:serviceData.toIterable.filter[!sumOnly || it.getString("key") == "sum"] SEPARATOR ", "»
					«key»«dataTuple.getString("key").toFirstUpper» : «dataTuple.get("value").toString»
				«ENDFOR»
			'''.toString
		}
	}
}