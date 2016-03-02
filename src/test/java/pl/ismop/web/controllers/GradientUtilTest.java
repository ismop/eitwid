package pl.ismop.web.controllers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pl.ismop.web.client.util.GradientsUtil;
import pl.ismop.web.client.util.GradientsUtil.Color;

public class GradientUtilTest {
	@Test
	public void testGradientUtilBoundaries() {
		GradientsUtil gradientsUtil = new GradientsUtil();
		Color color = gradientsUtil.getColor("testId", 0.0);
		assertEquals(new Color(0, 0, 255), color);
	}
	
	@Test
	public void testIntermediateColor() {
		GradientsUtil gradientsUtil = new GradientsUtil();
		gradientsUtil.updateValues("testId", 1.0);
		
		Color color = gradientsUtil.getColor("testId", 0.6);
		assertEquals(new Color(101, 0, 0), color);
	}
	
	@Test
	public void testConreteExample() {
		GradientsUtil gradientsUtil = new GradientsUtil();
		gradientsUtil.updateValues("testId", 15.24);
		gradientsUtil.updateValues("testId", 17.79);
		
		Color color = gradientsUtil.getColor("testId", 17.1);
		assertEquals(new Color(234, 255, 0), color);
	}
}