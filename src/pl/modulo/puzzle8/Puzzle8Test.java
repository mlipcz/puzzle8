package pl.modulo.puzzle8;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class Puzzle8Test extends Puzzle8 {

	@Test
	public void test1() {
		assertEquals(1L, rotate90(1L));
	}

	@Test
	public void test2() {
		assertEquals(0x101, rotate90(3L));
	}

	@Test
	public void test3() {
		assertEquals(0x102, rotate90(0x201L));
	}
	
	@Test
	public void test4() {
		assertEquals(0x102, rotate90(rotate90(0x102L)));
	}

	@Test
	public void test5() {
		assertEquals(0x103, rotate90(0x301L));
	}

	@Test
	public void test6() {
		assertEquals(0x303, rotate90(0x303L));
	}
	
	@Test
	public void test7() {
		assertEquals(0x407, rotate90(0x10103L));
	}
	
	@Test
	public void testRotate1() {
		List<Long> lc = rotate(0x303);
		assertEquals(1, lc.size());
	}
	
	@Test
	public void testFlip1() {
		assertEquals(0x107, flip(0x701L));
	}

	@Test
	public void testMove1() {
		List<Long> m = move(1L);
		assertEquals(64, m.size());
	}
	
	@Test
	public void testMove2() {
		List<Long> m = move(3L);
		assertEquals(56, m.size());
	}
	
	@Test
	public void testMove3() {
		List<Long> m = move(0x103L);
		assertEquals(49, m.size());
	}
}
