package etf.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class OptionalDoubleTest {

	OptionalDouble a;
	OptionalDouble b;
	OptionalDouble c;
	OptionalDouble d;
	
	@Before
	public void setup() {
		a = OptionalDouble.of(new Double("1.23"));
		b = OptionalDouble.ofNullable(new Double("4.56"));
		c = OptionalDouble.empty();
		d = OptionalDouble.ofNullable(null);
	}
	
	@Test
	public void testCompareTo() throws Exception {
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));
		assertEquals(0, a.compareTo(a));
		assertEquals(0, b.compareTo(b));
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testCompareToUnhappy1() throws Exception {
		c.compareTo(d);
	}

	@Test(expected=NoSuchElementException.class)
	public void testCompareToUnhappy2() throws Exception {
		d.compareTo(c);
	}
	@Test(expected=NoSuchElementException.class)
	public void testCompareToUnhappy3() throws Exception {
		a.compareTo(c);
	}
	@Test(expected=NoSuchElementException.class)
	public void testCompareToUnhappy4() throws Exception {
		c.compareTo(a);
	}

	
	@Test
	public void testAdd() {
		assertEquals("Optional[1.23]", a.toString());
		
		
		assertEquals("Optional[4.56]", b.toString());
		assertEquals("Optional.empty", c.toString());
		assertEquals("Optional.empty", d.toString());
		assertEquals("Optional[5.789999999999999]", a.add(b).toString());
		assertEquals("Optional[5.789999999999999]", b.add(a).toString());
		assertEquals("Optional.empty", a.add(c).toString());
		assertEquals("Optional.empty", c.add(a).toString());
		assertEquals("Optional.empty", c.add(d).toString());
		assertEquals("Optional.empty", d.add(c).toString());
		assertEquals("Optional[11.579999999999998]", a.add(b).add(a).add(b).toString());
		assertEquals("Optional.empty", a.add(b).add(c).add(d).toString());
	}
	
	@Test
	public void testDivide() {

		OptionalDouble d1 = OptionalDouble.of(new Double("8"));
		OptionalDouble d2 = OptionalDouble.ofNullable(new Double("2"));

		assertEquals("Optional[4.0]", d1.divide(d2).toString());
		assertEquals("Optional[0.25]", d2.divide(d1).toString());
	}



}
