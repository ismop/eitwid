package pl.ismop.web.client.util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayUtils;

@Singleton
public class CoordinatesUtil {
	public List<List<Double>> projectCoordinates(List<List<Double>> coordinates) {
		JsArray<JsArrayNumber> sourceCoordinates = (JsArray<JsArrayNumber>) JsArray.createArray();
		
		for(List<Double> cordinatePair : coordinates) {
			sourceCoordinates.push(JsArrayUtils.readOnlyJsArray(new double[] {cordinatePair.get(0), cordinatePair.get(1)}));
		}
		
		JsArray<JsArrayNumber> projected = convertCoordinates(sourceCoordinates);
		List<List<Double>> result = new ArrayList<>();
		
		for(int i = 0; i < projected.length(); i++) {
			List<Double> coordinatePair = new ArrayList<>();
			coordinatePair.add(projected.get(i).get(0));
			coordinatePair.add(projected.get(i).get(1));
			result.add(coordinatePair);
		}
		
		return result;
	}

	private native JsArray<JsArrayNumber> convertCoordinates(JsArray<JsArrayNumber> sourceCoordinates) /*-{
		var output = [];
		
		sourceCoordinates.forEach(function (elem) {
			var pcs2000 = '+proj=tmerc +lat_0=0 +lon_0=21 +k=0.999923 +x_0=7500000 +y_0=0 +ellps=GRS80 +units=m +no_defs ';
			var coords = $wnd.proj4(pcs2000, [elem[0], elem[1]]);
			output.push(coords)
		});
		
		return output;
	}-*/;
}